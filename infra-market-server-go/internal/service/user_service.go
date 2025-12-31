package service

import (
	"crypto/rand"
	"log"
	"math/big"
	"net/http"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/enums"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type UserService struct {
	db           *gorm.DB
	userRepo     *repository.UserRepository
	userRoleRepo *repository.UserRoleRepository
}

func NewUserService(db *gorm.DB, userRepo *repository.UserRepository, userRoleRepo *repository.UserRoleRepository) *UserService {
	return &UserService{
		db:           db,
		userRepo:     userRepo,
		userRoleRepo: userRoleRepo,
	}
}

// GetUsers 获取用户列表
func (s *UserService) GetUsers(query dto.UserQueryDto) dto.ApiData[dto.PageResult[dto.UserDto]] {
	users, total, err := s.userRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.UserDto]]("查询失败", http.StatusInternalServerError)
	}

	// 批量获取所有用户的角色ID
	uids := make([]uint64, len(users))
	for i, u := range users {
		uids[i] = u.ID
	}

	userRoles, _ := s.userRoleRepo.FindByUIDs(uids)
	userRoleMap := make(map[uint64][]uint64)
	for _, ur := range userRoles {
		if ur.UID != nil && ur.RoleID != nil {
			userRoleMap[*ur.UID] = append(userRoleMap[*ur.UID], *ur.RoleID)
		}
	}

	// 转换为DTO
	userDtos := make([]dto.UserDto, len(users))
	for i, user := range users {
		roleIds := userRoleMap[user.ID]
		userDtos[i] = s.convertUserToDto(&user, roleIds)
	}

	result := dto.PageResult[dto.UserDto]{
		Records: userDtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

// GetUser 获取用户详情
func (s *UserService) GetUser(id uint64) dto.ApiData[dto.UserDto] {
	user, err := s.userRepo.FindByUID(id)
	if err != nil {
		log.Printf("获取用户详情失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.UserDto]("用户不存在", http.StatusNotFound)
	}

	userRoles, _ := s.userRoleRepo.FindByUID(id)
	roleIds := make([]uint64, 0, len(userRoles))
	for _, ur := range userRoles {
		if ur.RoleID != nil {
			roleIds = append(roleIds, *ur.RoleID)
		}
	}

	userDto := s.convertUserToDto(user, roleIds)
	return dto.Success(userDto)
}

// CreateUser 创建用户
func (s *UserService) CreateUser(form dto.UserFormDto) dto.ApiData[dto.UserDto] {
	// 检查用户名是否已存在
	if _, err := s.userRepo.FindByUsername(form.Username); err == nil {
		return dto.Error[dto.UserDto]("用户名已存在", http.StatusBadRequest)
	}

	// 检查邮箱是否已存在
	if form.Email != nil && *form.Email != "" {
		if _, err := s.userRepo.FindByEmail(*form.Email); err == nil {
			return dto.Error[dto.UserDto]("邮箱已存在", http.StatusBadRequest)
		}
	}

	// 检查手机号是否已存在
	if form.Phone != nil && *form.Phone != "" {
		if _, err := s.userRepo.FindByPhone(*form.Phone); err == nil {
			return dto.Error[dto.UserDto]("手机号已存在", http.StatusBadRequest)
		}
	}

	// 加密密码
	password := enums.DefaultPassword
	if form.Password != nil && *form.Password != "" {
		password = *form.Password
	}
	encryptedPassword, err := util.Encrypt(password)
	if err != nil {
		log.Printf("创建用户失败，密码加密失败，用户名: %s, 错误: %v\n", form.Username, err)
		return dto.Error[dto.UserDto]("密码加密失败", http.StatusInternalServerError)
	}

	user := &entity.User{
		Username: form.Username,
		Password: encryptedPassword,
		Email:    form.Email,
		Phone:    form.Phone,
		Status:   enums.StatusActive.Code(),
	}

	// 在事务中执行：创建用户 + 创建用户角色关联
	err = WithTransaction(s.db, func(tx *gorm.DB) error {
		// 使用事务 db 创建新的 repository 实例
		txUserRepo := repository.NewUserRepository(tx)
		txUserRoleRepo := repository.NewUserRoleRepository(tx)

		// 创建用户
		if err := txUserRepo.Create(user); err != nil {
			return err
		}

		// 保存用户角色关联
		for _, roleID := range form.RoleIds {
			userRole := &entity.UserRole{
				UID:    &user.ID,
				RoleID: &roleID,
			}
			if err := txUserRoleRepo.Create(userRole); err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		log.Printf("创建用户失败，用户名: %s, 错误: %v\n", form.Username, err)
		return dto.Error[dto.UserDto]("创建用户失败", http.StatusInternalServerError)
	}

	userDto := s.convertUserToDto(user, form.RoleIds)
	return dto.Success(userDto)
}

// UpdateUser 更新用户
func (s *UserService) UpdateUser(id uint64, form dto.UserUpdateDto) dto.ApiData[dto.UserDto] {
	user, err := s.userRepo.FindByUID(id)
	if err != nil {
		log.Printf("更新用户失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.UserDto]("用户不存在", http.StatusNotFound)
	}

	// 检查用户名是否已被其他用户使用
	if existingUser, err := s.userRepo.FindByUsername(form.Username); err == nil && existingUser.ID != user.ID {
		return dto.Error[dto.UserDto]("用户名已存在", http.StatusBadRequest)
	}

	// 检查邮箱
	if form.Email != nil && *form.Email != "" {
		if existingUser, err := s.userRepo.FindByEmail(*form.Email); err == nil && existingUser.ID != user.ID {
			return dto.Error[dto.UserDto]("邮箱已存在", http.StatusBadRequest)
		}
	}

	// 检查手机号
	if form.Phone != nil && *form.Phone != "" {
		if existingUser, err := s.userRepo.FindByPhone(*form.Phone); err == nil && existingUser.ID != user.ID {
			return dto.Error[dto.UserDto]("手机号已存在", http.StatusBadRequest)
		}
	}

	user.Username = form.Username
	user.Email = form.Email
	user.Phone = form.Phone

	// 如果提供了新密码，则更新密码
	if form.Password != nil && *form.Password != "" {
		encryptedPassword, err := util.Encrypt(*form.Password)
		if err != nil {
			log.Printf("更新用户失败，密码加密失败，用户ID: %d, 错误: %v\n", id, err)
			return dto.Error[dto.UserDto]("密码加密失败", http.StatusInternalServerError)
		}
		user.Password = encryptedPassword
	}

	// 在事务中执行：更新用户 + 删除旧角色关联 + 创建新角色关联
	err = WithTransaction(s.db, func(tx *gorm.DB) error {
		// 使用事务 db 创建新的 repository 实例
		txUserRepo := repository.NewUserRepository(tx)
		txUserRoleRepo := repository.NewUserRoleRepository(tx)

		// 更新用户
		if err := txUserRepo.Update(user); err != nil {
			return err
		}

		// 删除旧角色关联
		if err := txUserRoleRepo.DeleteByUID(id); err != nil {
			return err
		}

		// 创建新角色关联
		for _, roleID := range form.RoleIds {
			userRole := &entity.UserRole{
				UID:    &id,
				RoleID: &roleID,
			}
			if err := txUserRoleRepo.Create(userRole); err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		log.Printf("更新用户失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.UserDto]("更新用户失败", http.StatusInternalServerError)
	}

	userDto := s.convertUserToDto(user, form.RoleIds)
	return dto.Success(userDto)
}

// DeleteUser 删除用户
func (s *UserService) DeleteUser(id uint64) dto.ApiData[any] {
	user, err := s.userRepo.FindByUID(id)
	if err != nil {
		log.Printf("删除用户失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("用户不存在", http.StatusNotFound)
	}

	if user.Username == enums.AdminUsername {
		return dto.Error[any]("不能删除超级管理员用户", http.StatusBadRequest)
	}

	if user.Status == enums.StatusDeleted.Code() {
		return dto.Error[any]("用户已被删除", http.StatusBadRequest)
	}

	user.Status = enums.StatusDeleted.Code()
	if err := s.userRepo.Update(user); err != nil {
		log.Printf("删除用户失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除用户失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// UpdateUserStatus 更新用户状态
func (s *UserService) UpdateUserStatus(id uint64, status string) dto.ApiData[any] {
	user, err := s.userRepo.FindByUID(id)
	if err != nil {
		log.Printf("更新用户状态失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("用户不存在", http.StatusNotFound)
	}

	if user.Username == enums.AdminUsername && status == enums.StatusDeleted.Code() {
		return dto.Error[any]("不能删除超级管理员用户", http.StatusBadRequest)
	}

	if user.Status == enums.StatusDeleted.Code() && status != enums.StatusDeleted.Code() {
		return dto.Error[any]("已删除的用户不能重新启用", http.StatusBadRequest)
	}

	if status == enums.StatusDeleted.Code() {
		return s.DeleteUser(id)
	}

	user.Status = status
	if err := s.userRepo.Update(user); err != nil {
		log.Printf("更新用户状态失败，用户ID: %d, 状态: %s, 错误: %v\n", id, status, err)
		return dto.Error[any]("更新状态失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// ResetPassword 重置密码
func (s *UserService) ResetPassword(id uint64) dto.ApiData[map[string]string] {
	user, err := s.userRepo.FindByUID(id)
	if err != nil {
		log.Printf("重置密码失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[map[string]string]("用户不存在", http.StatusNotFound)
	}

	// 生成随机密码
	newPassword := generateRandomPassword()
	encryptedPassword, err := util.Encrypt(newPassword)
	if err != nil {
		log.Printf("重置密码失败，密码加密失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[map[string]string]("密码加密失败", http.StatusInternalServerError)
	}

	user.Password = encryptedPassword
	if err := s.userRepo.Update(user); err != nil {
		log.Printf("重置密码失败，用户ID: %d, 错误: %v\n", id, err)
		return dto.Error[map[string]string]("重置密码失败", http.StatusInternalServerError)
	}

	return dto.Success(map[string]string{"password": newPassword})
}

// BatchDeleteUsers 批量删除用户
func (s *UserService) BatchDeleteUsers(ids []uint64) dto.ApiData[any] {
	if len(ids) == 0 {
		return dto.Error[any]("请选择要删除的用户", http.StatusBadRequest)
	}

	users, err := s.userRepo.FindByUIDs(ids)
	if err != nil || len(users) != len(ids) {
		log.Printf("批量删除用户失败，查询用户失败，错误: %v\n", err)
		return dto.Error[any]("部分用户不存在", http.StatusBadRequest)
	}

	// 检查是否包含超级管理员
	for _, user := range users {
		if user.Username == enums.AdminUsername {
			return dto.Error[any]("不能删除超级管理员用户", http.StatusBadRequest)
		}
	}

	// 在事务中批量删除
	err = WithTransaction(s.db, func(tx *gorm.DB) error {
		txUserRepo := repository.NewUserRepository(tx)
		for _, user := range users {
			user.Status = enums.StatusDeleted.Code()
			if err := txUserRepo.Update(&user); err != nil {
				return err
			}
		}
		return nil
	})

	if err != nil {
		log.Printf("批量删除用户失败，错误: %v\n", err)
		return dto.Error[any]("批量删除用户失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// convertUserToDto 转换用户实体为DTO
func (s *UserService) convertUserToDto(user *entity.User, roleIds []uint64) dto.UserDto {
	var lastLoginTime *string
	if user.LastLoginTime != nil {
		formatted := util.Format(user.LastLoginTime)
		lastLoginTime = &formatted
	}

	return dto.UserDto{
		ID:            user.ID,
		Username:      user.Username,
		Email:         user.Email,
		Phone:         user.Phone,
		Status:        user.Status,
		LastLoginTime: lastLoginTime,
		RoleIds:       roleIds,
		CreateTime:    util.Format(&user.CreateTime),
		UpdateTime:    util.Format(&user.UpdateTime),
	}
}

// generateRandomPassword 生成随机密码
func generateRandomPassword() string {
	chars := "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
	password := make([]byte, 8)
	for i := range password {
		num, _ := rand.Int(rand.Reader, big.NewInt(int64(len(chars))))
		password[i] = chars[num.Int64()]
	}
	return string(password)
}
