package service

import (
	"log"
	"net/http"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/enums"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type RoleService struct {
	db                 *gorm.DB
	roleRepo           *repository.RoleRepository
	rolePermissionRepo *repository.RolePermissionRepository
	userRoleRepo       *repository.UserRoleRepository
}

func NewRoleService(
	db *gorm.DB,
	roleRepo *repository.RoleRepository,
	rolePermissionRepo *repository.RolePermissionRepository,
	userRoleRepo *repository.UserRoleRepository,
) *RoleService {
	return &RoleService{
		db:                 db,
		roleRepo:           roleRepo,
		rolePermissionRepo: rolePermissionRepo,
		userRoleRepo:       userRoleRepo,
	}
}

// convertRolesToDtos 将角色列表转换为DTO列表，包含权限信息
func (s *RoleService) convertRolesToDtos(roles []entity.Role) []dto.RoleDto {
	if len(roles) == 0 {
		return []dto.RoleDto{}
	}

	// 提取角色ID列表
	roleIDs := make([]uint64, len(roles))
	for i, r := range roles {
		roleIDs[i] = r.ID
	}

	// 批量获取角色权限关联
	rolePermissions, _ := s.rolePermissionRepo.FindByRoleIDs(roleIDs)

	// 构建角色权限映射
	rolePermissionMap := make(map[uint64][]uint64)
	for _, rp := range rolePermissions {
		rolePermissionMap[rp.RoleID] = append(rolePermissionMap[rp.RoleID], rp.PermissionID)
	}

	// 转换为DTO
	roleDtos := make([]dto.RoleDto, len(roles))
	for i, role := range roles {
		permissionIds := rolePermissionMap[role.ID]
		roleDtos[i] = s.convertRoleToDto(&role, permissionIds)
	}

	return roleDtos
}

// GetRoles 获取角色列表
func (s *RoleService) GetRoles(query dto.RoleQueryDto) dto.ApiData[dto.PageResult[dto.RoleDto]] {
	roles, total, err := s.roleRepo.Page(query)
	if err != nil {
		log.Printf("获取角色列表失败: %v\n", err)
		return dto.Error[dto.PageResult[dto.RoleDto]]("查询失败", http.StatusInternalServerError)
	}

	roleDtos := s.convertRolesToDtos(roles)

	result := dto.PageResult[dto.RoleDto]{
		Records: roleDtos,
		Total:   total,
		Current: query.Current,
		Size:    query.Size,
	}

	return dto.Success(result)
}

// GetAllRoles 获取所有激活的角色
func (s *RoleService) GetAllRoles() dto.ApiData[[]dto.RoleDto] {
	roles, err := s.roleRepo.FindByStatus(enums.StatusActive.Code())
	if err != nil {
		log.Printf("获取所有激活角色失败: %v\n", err)
		return dto.Error[[]dto.RoleDto]("查询失败", http.StatusInternalServerError)
	}

	roleDtos := s.convertRolesToDtos(roles)
	return dto.Success(roleDtos)
}

// GetRole 获取角色详情
func (s *RoleService) GetRole(id uint64) dto.ApiData[dto.RoleDto] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		log.Printf("获取角色详情失败，角色ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.RoleDto]("角色不存在", http.StatusNotFound)
	}

	rolePermissions, _ := s.rolePermissionRepo.FindByRoleID(id)
	permissionIds := make([]uint64, 0, len(rolePermissions))
	for _, rp := range rolePermissions {
		permissionIds = append(permissionIds, rp.PermissionID)
	}

	roleDto := s.convertRoleToDto(role, permissionIds)
	return dto.Success(roleDto)
}

// CreateRole 创建角色
func (s *RoleService) CreateRole(form dto.RoleFormDto) dto.ApiData[dto.RoleDto] {
	// 检查角色编码是否已存在
	if _, err := s.roleRepo.FindByCode(form.Code); err == nil {
		return dto.Error[dto.RoleDto]("角色编码已存在", http.StatusBadRequest)
	}

	role := &entity.Role{
		Name:        form.Name,
		Code:        form.Code,
		Description: form.Description,
		Status:      enums.StatusActive.Code(),
	}

	// 在事务中执行：创建角色 + 创建角色权限关联
	err := WithTransaction(s.db, func(tx *gorm.DB) error {
		// 使用事务 db 创建新的 repository 实例
		txRoleRepo := repository.NewRoleRepository(tx)
		txRolePermissionRepo := repository.NewRolePermissionRepository(tx)

		// 创建角色
		if err := txRoleRepo.Create(role); err != nil {
			return err
		}

		// 保存角色权限关联
		for _, permissionID := range form.PermissionIds {
			rolePermission := &entity.RolePermission{
				RoleID:       role.ID,
				PermissionID: permissionID,
			}
			if err := txRolePermissionRepo.Create(rolePermission); err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		log.Printf("创建角色失败，角色编码: %s, 错误: %v\n", form.Code, err)
		return dto.Error[dto.RoleDto]("创建角色失败", http.StatusInternalServerError)
	}

	roleDto := s.convertRoleToDto(role, form.PermissionIds)
	return dto.Success(roleDto)
}

// UpdateRole 更新角色
func (s *RoleService) UpdateRole(id uint64, form dto.RoleFormDto) dto.ApiData[dto.RoleDto] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		log.Printf("更新角色失败，角色ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.RoleDto]("角色不存在", http.StatusNotFound)
	}

	// 检查角色编码是否已被其他角色使用
	if existingRole, err := s.roleRepo.FindByCode(form.Code); err == nil && existingRole.ID != role.ID {
		return dto.Error[dto.RoleDto]("角色编码已存在", http.StatusBadRequest)
	}

	role.Name = form.Name
	role.Description = form.Description

	// 在事务中执行：更新角色 + 删除旧权限关联 + 创建新权限关联
	err = WithTransaction(s.db, func(tx *gorm.DB) error {
		// 使用事务 db 创建新的 repository 实例
		txRoleRepo := repository.NewRoleRepository(tx)
		txRolePermissionRepo := repository.NewRolePermissionRepository(tx)

		// 更新角色
		if err := txRoleRepo.Update(role); err != nil {
			return err
		}

		// 删除旧权限关联
		if err := txRolePermissionRepo.DeleteByRoleID(id); err != nil {
			return err
		}

		// 创建新权限关联
		for _, permissionID := range form.PermissionIds {
			rolePermission := &entity.RolePermission{
				RoleID:       id,
				PermissionID: permissionID,
			}
			if err := txRolePermissionRepo.Create(rolePermission); err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		log.Printf("更新角色失败，角色ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.RoleDto]("更新角色失败", http.StatusInternalServerError)
	}

	roleDto := s.convertRoleToDto(role, form.PermissionIds)
	return dto.Success(roleDto)
}

// DeleteRole 删除角色
func (s *RoleService) DeleteRole(id uint64) dto.ApiData[any] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		log.Printf("删除角色失败，角色ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("角色不存在", http.StatusNotFound)
	}

	if role.Code == enums.AdminRoleCode {
		return dto.Error[any]("不能删除系统角色", http.StatusBadRequest)
	}

	// 检查是否有用户正在使用此角色
	userCount, _ := s.userRoleRepo.CountByRoleID(id)
	if userCount > 0 {
		return dto.Error[any]("该角色下还有用户，无法删除", http.StatusBadRequest)
	}

	role.Status = enums.StatusDeleted.Code()
	if err := s.roleRepo.Update(role); err != nil {
		log.Printf("删除角色失败，角色ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除角色失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// UpdateRoleStatus 更新角色状态
func (s *RoleService) UpdateRoleStatus(id uint64, status string) dto.ApiData[any] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		log.Printf("更新角色状态失败，角色ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("角色不存在", http.StatusNotFound)
	}

	if role.Code == enums.AdminRoleCode && status == enums.StatusDeleted.Code() {
		return dto.Error[any]("不能删除系统角色", http.StatusBadRequest)
	}

	if role.Status == enums.StatusDeleted.Code() && status != enums.StatusDeleted.Code() {
		return dto.Error[any]("已删除的角色不能重新启用", http.StatusBadRequest)
	}

	if status == enums.StatusDeleted.Code() {
		return s.DeleteRole(id)
	}

	role.Status = status
	if err := s.roleRepo.Update(role); err != nil {
		log.Printf("更新角色状态失败，角色ID: %d, 状态: %s, 错误: %v\n", id, status, err)
		return dto.Error[any]("更新状态失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// convertRoleToDto 转换角色实体为DTO
func (s *RoleService) convertRoleToDto(role *entity.Role, permissionIds []uint64) dto.RoleDto {
	return dto.RoleDto{
		ID:            role.ID,
		Name:          role.Name,
		Code:          role.Code,
		Description:   role.Description,
		Status:        role.Status,
		PermissionIds: permissionIds,
		CreateTime:    util.Format(&role.CreateTime),
		UpdateTime:    util.Format(&role.UpdateTime),
	}
}
