package service

import (
	"log"
	"time"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
)

type AuthService struct {
	userRepo           *repository.UserRepository
	userRoleRepo       *repository.UserRoleRepository
	rolePermissionRepo *repository.RolePermissionRepository
	permissionRepo     *repository.PermissionRepository
	tokenService       *TokenService
}

func NewAuthService(
	userRepo *repository.UserRepository,
	userRoleRepo *repository.UserRoleRepository,
	rolePermissionRepo *repository.RolePermissionRepository,
	permissionRepo *repository.PermissionRepository,
	tokenService *TokenService,
) *AuthService {
	return &AuthService{
		userRepo:           userRepo,
		userRoleRepo:       userRoleRepo,
		rolePermissionRepo: rolePermissionRepo,
		permissionRepo:     permissionRepo,
		tokenService:       tokenService,
	}
}

// Login 用户登录
func (s *AuthService) Login(req dto.LoginRequest) dto.ApiData[dto.LoginResponse] {
	user, err := s.userRepo.FindByUsername(req.Username)
	if err != nil {
		return dto.Error[dto.LoginResponse]("用户名或密码错误", 400)
	}

	// 验证密码
	if !util.Matches(req.Password, user.Password) {
		return dto.Error[dto.LoginResponse]("用户名或密码错误", 400)
	}

	// 检查用户状态
	if user.Status != "active" {
		return dto.Error[dto.LoginResponse]("用户已被禁用", 400)
	}

	// 更新登录时间
	now := time.Now().UnixMilli()
	user.LastLoginTime = &now
	_ = s.userRepo.Update(user)

	// 获取用户权限
	permissions := s.getUserPermissions(user.ID)

	// 生成JWT token
	token, err := util.GenerateToken(user.ID, user.Username)
	if err != nil {
		log.Printf("用户登录失败，生成token失败，用户名: %s, 错误: %v\n", req.Username, err)
		return dto.Error[dto.LoginResponse]("生成token失败", 500)
	}

	// 保存token到Redis
	s.tokenService.SaveToken(user.ID, token)

	userDto := s.convertUserToDto(user, nil)
	response := dto.LoginResponse{
		Token:       token,
		User:        userDto,
		Permissions: permissions,
	}

	return dto.Success(response)
}

// Logout 用户登出
func (s *AuthService) Logout(uid uint64) dto.ApiData[any] {
	s.tokenService.DeleteToken(uid)
	return dto.Success[any](nil)
}

// GetCurrentUser 获取当前用户信息
func (s *AuthService) GetCurrentUser(uid uint64) dto.ApiData[dto.LoginResponse] {
	user, err := s.userRepo.FindByUID(uid)
	if err != nil {
		log.Printf("获取当前用户信息失败，用户ID: %d, 错误: %v\n", uid, err)
		return dto.Error[dto.LoginResponse]("用户不存在", 404)
	}

	if user.Status != "active" {
		return dto.Error[dto.LoginResponse]("用户已被禁用", 400)
	}

	permissions := s.getUserPermissions(user.ID)
	userDto := s.convertUserToDto(user, nil)

	response := dto.LoginResponse{
		Token:       "", // 不再返回token
		User:        userDto,
		Permissions: permissions,
	}

	return dto.Success(response)
}

// GetUserMenus 获取用户菜单
func (s *AuthService) GetUserMenus(uid uint64) dto.ApiData[[]dto.PermissionDto] {
	user, err := s.userRepo.FindByUID(uid)
	if err != nil {
		log.Printf("获取用户菜单失败，用户ID: %d, 错误: %v\n", uid, err)
		return dto.Error[[]dto.PermissionDto]("用户不存在", 404)
	}

	if user.Status != "active" {
		return dto.Error[[]dto.PermissionDto]("用户已被禁用", 400)
	}

	// 获取用户的所有权限（包括按钮权限）
	allUserPermissions := s.getUserPermissionList(uid)

	if len(allUserPermissions) == 0 {
		return dto.Success([]dto.PermissionDto{})
	}

	// 收集所有需要查询的父级权限ID
	parentIdsToQuery := make(map[uint64]bool)

	// 收集所有按钮权限和菜单权限的父级权限ID
	for _, permission := range allUserPermissions {
		if permission.ParentID != nil {
			parentIdsToQuery[*permission.ParentID] = true
		}
	}

	// 递归收集所有父级权限ID
	allParentIds := s.collectAllParentIds(parentIdsToQuery)

	// 批量获取所有父级权限，避免N+1查询
	var allParentPermissions []entity.Permission
	if len(allParentIds) > 0 {
		parentIdsSlice := make([]uint64, 0, len(allParentIds))
		for id := range allParentIds {
			parentIdsSlice = append(parentIdsSlice, id)
		}
		allParentPermissions, _ = s.permissionRepo.FindByIDs(parentIdsSlice)
	}

	// 构建权限ID到权限对象的映射
	permissionMap := make(map[uint64]entity.Permission)
	for _, p := range allParentPermissions {
		permissionMap[p.ID] = p
	}

	// 收集所有需要的菜单权限ID（包括父级链）
	allMenuPermissionIds := make(map[uint64]bool)

	// 从按钮权限向上收集父级菜单
	for _, permission := range allUserPermissions {
		if permission.Type == "button" && permission.ParentID != nil {
			currentParentID := permission.ParentID
			for currentParentID != nil {
				allMenuPermissionIds[*currentParentID] = true
				// 从 permissionMap 中查找父级权限
				if parent, ok := permissionMap[*currentParentID]; ok && parent.ParentID != nil {
					currentParentID = parent.ParentID
				} else {
					// 如果不在 permissionMap 中，说明已经到根节点或权限不存在
					break
				}
			}
		}
	}

	// 从菜单权限向上收集父级菜单
	for _, permission := range allUserPermissions {
		if permission.Type == "menu" {
			// 向上收集父级菜单
			if permission.ParentID != nil {
				currentParentID := permission.ParentID
				for currentParentID != nil {
					allMenuPermissionIds[*currentParentID] = true
					// 从 permissionMap 中查找父级权限
					if parent, ok := permissionMap[*currentParentID]; ok && parent.ParentID != nil {
						currentParentID = parent.ParentID
					} else {
						// 如果不在 permissionMap 中，说明已经到根节点或权限不存在
						break
					}
				}
			}
		}
	}

	// 合并所有需要的权限ID（包括所有父级权限和用户直接拥有的权限）
	for id := range allParentIds {
		allMenuPermissionIds[id] = true
	}
	for _, permission := range allUserPermissions {
		allMenuPermissionIds[permission.ID] = true
	}

	// 批量获取所有需要的权限详情
	allNeededPermissionIds := make([]uint64, 0, len(allMenuPermissionIds))
	for id := range allMenuPermissionIds {
		allNeededPermissionIds = append(allNeededPermissionIds, id)
	}
	allPermissions, _ := s.permissionRepo.FindByIDs(allNeededPermissionIds)

	// 只保留菜单类型的权限
	menuPermissions := make([]entity.Permission, 0)
	for _, p := range allPermissions {
		if p.Type == "menu" {
			menuPermissions = append(menuPermissions, p)
		}
	}

	// 构建权限树
	tree := s.buildPermissionTree(menuPermissions)

	return dto.Success(tree)
}

// RefreshToken 刷新token
func (s *AuthService) RefreshToken(uid uint64) dto.ApiData[map[string]string] {
	user, err := s.userRepo.FindByUID(uid)
	if err != nil {
		return dto.Error[map[string]string]("用户不存在", 404)
	}

	token, err := util.GenerateToken(user.ID, user.Username)
	if err != nil {
		log.Printf("刷新token失败，生成token失败，用户ID: %d, 错误: %v\n", uid, err)
		return dto.Error[map[string]string]("生成token失败", 500)
	}

	s.tokenService.SaveToken(uid, token)

	return dto.Success(map[string]string{"token": token})
}

// ChangePassword 修改密码
func (s *AuthService) ChangePassword(uid uint64, req dto.ChangePasswordRequest) dto.ApiData[any] {
	user, err := s.userRepo.FindByUID(uid)
	if err != nil {
		return dto.Error[any]("用户不存在", 404)
	}

	// 验证原密码
	if !util.Matches(req.OldPassword, user.Password) {
		return dto.Error[any]("原密码错误", 400)
	}

	// 加密新密码
	encryptedPassword, err := util.Encrypt(req.NewPassword)
	if err != nil {
		log.Printf("修改密码失败，密码加密失败，用户ID: %d, 错误: %v\n", uid, err)
		return dto.Error[any]("密码加密失败", 500)
	}

	user.Password = encryptedPassword
	if err := s.userRepo.Update(user); err != nil {
		log.Printf("修改密码失败，更新密码失败，用户ID: %d, 错误: %v\n", uid, err)
		return dto.Error[any]("更新密码失败", 500)
	}

	return dto.Success[any](nil)
}

// getUserPermissions 获取用户权限编码列表
func (s *AuthService) getUserPermissions(uid uint64) []string {
	userRoles, err := s.userRoleRepo.FindByUID(uid)
	if err != nil {
		return []string{}
	}
	if len(userRoles) == 0 {
		return []string{}
	}

	roleIDs := make([]uint64, 0)
	for _, ur := range userRoles {
		if ur.RoleID != nil {
			roleIDs = append(roleIDs, *ur.RoleID)
		}
	}

	if len(roleIDs) == 0 {
		return []string{}
	}

	rolePermissions, err := s.rolePermissionRepo.FindByRoleIDs(roleIDs)
	if err != nil {
		return []string{}
	}
	if len(rolePermissions) == 0 {
		return []string{}
	}

	// 收集权限ID并去重
	permissionIDSet := make(map[uint64]bool)
	for _, rp := range rolePermissions {
		permissionIDSet[rp.PermissionID] = true
	}

	if len(permissionIDSet) == 0 {
		return []string{}
	}

	// 转换为切片
	permissionIDs := make([]uint64, 0, len(permissionIDSet))
	for id := range permissionIDSet {
		permissionIDs = append(permissionIDs, id)
	}

	permissions, err := s.permissionRepo.FindByIDs(permissionIDs)
	if err != nil {
		return []string{}
	}

	// 过滤激活状态的权限并去重
	codeSet := make(map[string]bool)
	codes := make([]string, 0)
	for _, p := range permissions {
		// 只返回激活状态的权限
		if p.Status == "active" && p.Code != "" {
			// 去重
			if !codeSet[p.Code] {
				codeSet[p.Code] = true
				codes = append(codes, p.Code)
			}
		}
	}

	return codes
}

// getUserPermissionList 获取用户权限实体列表
func (s *AuthService) getUserPermissionList(uid uint64) []entity.Permission {
	userRoles, _ := s.userRoleRepo.FindByUID(uid)
	if len(userRoles) == 0 {
		return []entity.Permission{}
	}

	roleIDs := make([]uint64, 0)
	for _, ur := range userRoles {
		if ur.RoleID != nil {
			roleIDs = append(roleIDs, *ur.RoleID)
		}
	}

	rolePermissions, _ := s.rolePermissionRepo.FindByRoleIDs(roleIDs)
	if len(rolePermissions) == 0 {
		return []entity.Permission{}
	}

	permissionIDs := make([]uint64, 0)
	for _, rp := range rolePermissions {
		permissionIDs = append(permissionIDs, rp.PermissionID)
	}

	permissions, _ := s.permissionRepo.FindByIDs(permissionIDs)
	return permissions
}

// collectAllParentIds 递归收集所有父级权限ID
func (s *AuthService) collectAllParentIds(parentIds map[uint64]bool) map[uint64]bool {
	allParentIds := make(map[uint64]bool)
	for id := range parentIds {
		allParentIds[id] = true
	}

	// 查询这些父级权限
	if len(parentIds) == 0 {
		return allParentIds
	}

	parentIdsSlice := make([]uint64, 0, len(parentIds))
	for id := range parentIds {
		parentIdsSlice = append(parentIdsSlice, id)
	}

	parentPermissions, _ := s.permissionRepo.FindByIDs(parentIdsSlice)

	// 收集这些父级权限的父级ID
	nextLevelParentIds := make(map[uint64]bool)
	for _, p := range parentPermissions {
		if p.ParentID != nil && !allParentIds[*p.ParentID] {
			nextLevelParentIds[*p.ParentID] = true
		}
	}

	// 如果有新的父级ID，递归收集
	if len(nextLevelParentIds) > 0 {
		recursiveParents := s.collectAllParentIds(nextLevelParentIds)
		for id := range recursiveParents {
			allParentIds[id] = true
		}
	}

	return allParentIds
}

// buildPermissionTree 构建权限树
func (s *AuthService) buildPermissionTree(permissions []entity.Permission) []dto.PermissionDto {
	// 先转换为DTO
	permissionMap := make(map[uint64]dto.PermissionDto)
	for _, p := range permissions {
		permissionMap[p.ID] = s.convertPermissionToDto(p, nil)
	}

	return buildPermissionTreeFromMap(permissions, permissionMap)
}

// convertUserToDto 转换用户实体为DTO
func (s *AuthService) convertUserToDto(user *entity.User, roleIds []uint64) dto.UserDto {
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

// convertPermissionToDto 转换权限实体为DTO
func (s *AuthService) convertPermissionToDto(permission entity.Permission, children []dto.PermissionDto) dto.PermissionDto {
	return dto.PermissionDto{
		ID:         permission.ID,
		Name:       permission.Name,
		Code:       permission.Code,
		Type:       permission.Type,
		ParentID:   permission.ParentID,
		Path:       permission.Path,
		Icon:       permission.Icon,
		Sort:       permission.Sort,
		Status:     permission.Status,
		Children:   children,
		CreateTime: util.Format(&permission.CreateTime),
		UpdateTime: util.Format(&permission.UpdateTime),
	}
}
