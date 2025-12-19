package service

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
)

type RoleService struct {
	roleRepo           *repository.RoleRepository
	rolePermissionRepo *repository.RolePermissionRepository
	userRoleRepo       *repository.UserRoleRepository
}

func NewRoleService(
	roleRepo *repository.RoleRepository,
	rolePermissionRepo *repository.RolePermissionRepository,
	userRoleRepo *repository.UserRoleRepository,
) *RoleService {
	return &RoleService{
		roleRepo:           roleRepo,
		rolePermissionRepo: rolePermissionRepo,
		userRoleRepo:       userRoleRepo,
	}
}

// GetRoles 获取角色列表
func (s *RoleService) GetRoles(query dto.RoleQueryDto) dto.ApiData[dto.PageResult[dto.RoleDto]] {
	roles, total, err := s.roleRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.RoleDto]]("查询失败", 500)
	}

	// 批量获取所有角色的权限ID
	roleIDs := make([]uint64, len(roles))
	for i, r := range roles {
		roleIDs[i] = r.ID
	}

	rolePermissions, _ := s.rolePermissionRepo.FindByRoleIDs(roleIDs)
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
	roles, err := s.roleRepo.FindByStatus("active")
	if err != nil {
		return dto.Error[[]dto.RoleDto]("查询失败", 500)
	}

	// 批量获取所有角色的权限ID
	roleIDs := make([]uint64, len(roles))
	for i, r := range roles {
		roleIDs[i] = r.ID
	}

	rolePermissions, _ := s.rolePermissionRepo.FindByRoleIDs(roleIDs)
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

	return dto.Success(roleDtos)
}

// GetRole 获取角色详情
func (s *RoleService) GetRole(id uint64) dto.ApiData[dto.RoleDto] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.RoleDto]("角色不存在", 404)
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
		return dto.Error[dto.RoleDto]("角色编码已存在", 400)
	}

	role := &entity.Role{
		Name:        form.Name,
		Code:        form.Code,
		Description: form.Description,
		Status:      "active",
	}

	if err := s.roleRepo.Create(role); err != nil {
		return dto.Error[dto.RoleDto]("创建角色失败", 500)
	}

	// 保存角色权限关联
	for _, permissionID := range form.PermissionIds {
		rolePermission := &entity.RolePermission{
			RoleID:       role.ID,
			PermissionID: permissionID,
		}
		s.rolePermissionRepo.Create(rolePermission)
	}

	roleDto := s.convertRoleToDto(role, form.PermissionIds)
	return dto.Success(roleDto)
}

// UpdateRole 更新角色
func (s *RoleService) UpdateRole(id uint64, form dto.RoleFormDto) dto.ApiData[dto.RoleDto] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.RoleDto]("角色不存在", 404)
	}

	// 检查角色编码是否已被其他角色使用
	if existingRole, err := s.roleRepo.FindByCode(form.Code); err == nil && existingRole.ID != role.ID {
		return dto.Error[dto.RoleDto]("角色编码已存在", 400)
	}

	role.Name = form.Name
	role.Description = form.Description

	if err := s.roleRepo.Update(role); err != nil {
		return dto.Error[dto.RoleDto]("更新角色失败", 500)
	}

	// 更新角色权限关联
	s.rolePermissionRepo.DeleteByRoleID(id)
	for _, permissionID := range form.PermissionIds {
		rolePermission := &entity.RolePermission{
			RoleID:       id,
			PermissionID: permissionID,
		}
		s.rolePermissionRepo.Create(rolePermission)
	}

	roleDto := s.convertRoleToDto(role, form.PermissionIds)
	return dto.Success(roleDto)
}

// DeleteRole 删除角色
func (s *RoleService) DeleteRole(id uint64) dto.ApiData[interface{}] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		return dto.Error[interface{}]("角色不存在", 404)
	}

	if role.Code == "admin" {
		return dto.Error[interface{}]("不能删除系统角色", 400)
	}

	// 检查是否有用户正在使用此角色
	userCount, _ := s.userRoleRepo.CountByRoleID(id)
	if userCount > 0 {
		return dto.Error[interface{}]("该角色下还有用户，无法删除", 400)
	}

	role.Status = "deleted"
	if err := s.roleRepo.Update(role); err != nil {
		return dto.Error[interface{}]("删除角色失败", 500)
	}

	return dto.Success[interface{}](nil)
}

// UpdateRoleStatus 更新角色状态
func (s *RoleService) UpdateRoleStatus(id uint64, status string) dto.ApiData[interface{}] {
	role, err := s.roleRepo.FindByID(id)
	if err != nil {
		return dto.Error[interface{}]("角色不存在", 404)
	}

	if role.Code == "admin" && status == "deleted" {
		return dto.Error[interface{}]("不能删除系统角色", 400)
	}

	if role.Status == "deleted" && status != "deleted" {
		return dto.Error[interface{}]("已删除的角色不能重新启用", 400)
	}

	if status == "deleted" {
		return s.DeleteRole(id)
	}

	role.Status = status
	if err := s.roleRepo.Update(role); err != nil {
		return dto.Error[interface{}]("更新状态失败", 500)
	}

	return dto.Success[interface{}](nil)
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
