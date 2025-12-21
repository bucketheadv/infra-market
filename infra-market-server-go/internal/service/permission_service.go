package service

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
)

type PermissionService struct {
	permissionRepo     *repository.PermissionRepository
	rolePermissionRepo *repository.RolePermissionRepository
}

func NewPermissionService(
	permissionRepo *repository.PermissionRepository,
	rolePermissionRepo *repository.RolePermissionRepository,
) *PermissionService {
	return &PermissionService{
		permissionRepo:     permissionRepo,
		rolePermissionRepo: rolePermissionRepo,
	}
}

// GetPermissions 获取权限列表
func (s *PermissionService) GetPermissions(query dto.PermissionQueryDto) dto.ApiData[dto.PageResult[dto.PermissionDto]] {
	permissions, total, err := s.permissionRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.PermissionDto]]("查询失败", 500)
	}

	permissionDtos := make([]dto.PermissionDto, len(permissions))
	for i, p := range permissions {
		permissionDtos[i] = s.convertPermissionToDto(&p, nil)
	}

	result := dto.PageResult[dto.PermissionDto]{
		Records: permissionDtos,
		Total:   total,
		Current: query.Current,
		Size:    query.Size,
	}

	return dto.Success(result)
}

// GetPermissionTree 获取权限树
func (s *PermissionService) GetPermissionTree() dto.ApiData[[]dto.PermissionDto] {
	permissions, _ := s.permissionRepo.FindAll()

	// 只返回激活状态的权限
	activePermissions := make([]entity.Permission, 0)
	for _, p := range permissions {
		if p.Status == "active" {
			activePermissions = append(activePermissions, p)
		}
	}

	tree := s.buildPermissionTree(activePermissions)
	return dto.Success(tree)
}

// GetPermission 获取权限详情
func (s *PermissionService) GetPermission(id uint64) dto.ApiData[dto.PermissionDto] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.PermissionDto]("权限不存在", 404)
	}

	permissionDto := s.convertPermissionToDto(permission, nil)
	return dto.Success(permissionDto)
}

// CreatePermission 创建权限
func (s *PermissionService) CreatePermission(form dto.PermissionFormDto) dto.ApiData[dto.PermissionDto] {
	// 检查权限编码是否已存在
	if _, err := s.permissionRepo.FindByCode(form.Code); err == nil {
		return dto.Error[dto.PermissionDto]("权限编码已存在", 400)
	}

	permission := &entity.Permission{
		Name:     form.Name,
		Code:     form.Code,
		Type:     form.Type,
		ParentID: form.ParentID,
		Path:     form.Path,
		Icon:     form.Icon,
		Sort:     form.Sort,
		Status:   "active",
	}

	if err := s.permissionRepo.Create(permission); err != nil {
		return dto.Error[dto.PermissionDto]("创建权限失败", 500)
	}

	permissionDto := s.convertPermissionToDto(permission, nil)
	return dto.Success(permissionDto)
}

// UpdatePermission 更新权限
func (s *PermissionService) UpdatePermission(id uint64, form dto.PermissionFormDto) dto.ApiData[dto.PermissionDto] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.PermissionDto]("权限不存在", 404)
	}

	// 检查权限编码是否已被其他权限使用
	if existingPermission, err := s.permissionRepo.FindByCode(form.Code); err == nil && existingPermission.ID != permission.ID {
		return dto.Error[dto.PermissionDto]("权限编码已存在", 400)
	}

	permission.Name = form.Name
	permission.Type = form.Type
	permission.ParentID = form.ParentID
	permission.Path = form.Path
	permission.Icon = form.Icon
	permission.Sort = form.Sort

	if err := s.permissionRepo.Update(permission); err != nil {
		return dto.Error[dto.PermissionDto]("更新权限失败", 500)
	}

	permissionDto := s.convertPermissionToDto(permission, nil)
	return dto.Success(permissionDto)
}

// DeletePermission 删除权限
func (s *PermissionService) DeletePermission(id uint64) dto.ApiData[any] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		return dto.Error[any]("权限不存在", 404)
	}

	if permission.Code == "system" {
		return dto.Error[any]("不能删除系统权限", 400)
	}

	// 检查是否有子权限
	childPermissions, _ := s.permissionRepo.FindByParentID(id)
	if len(childPermissions) > 0 {
		return dto.Error[any]("该权限下还有子权限，无法删除", 400)
	}

	permission.Status = "deleted"
	if err := s.permissionRepo.Update(permission); err != nil {
		return dto.Error[any]("删除权限失败", 500)
	}

	return dto.Success[any](nil)
}

// UpdatePermissionStatus 更新权限状态
func (s *PermissionService) UpdatePermissionStatus(id uint64, status string) dto.ApiData[any] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		return dto.Error[any]("权限不存在", 404)
	}

	if permission.Code == "system" && status == "deleted" {
		return dto.Error[any]("不能删除系统权限", 400)
	}

	if permission.Status == "deleted" && status != "deleted" {
		return dto.Error[any]("已删除的权限不能重新启用", 400)
	}

	if status == "deleted" {
		return s.DeletePermission(id)
	}

	permission.Status = status
	if err := s.permissionRepo.Update(permission); err != nil {
		return dto.Error[any]("更新状态失败", 500)
	}

	return dto.Success[any](nil)
}

// buildPermissionTree 构建权限树
func (s *PermissionService) buildPermissionTree(permissions []entity.Permission) []dto.PermissionDto {
	permissionMap := make(map[uint64]dto.PermissionDto)

	// 先转换为DTO
	for _, p := range permissions {
		permissionMap[p.ID] = s.convertPermissionToDto(&p, nil)
	}

	// 构建树形结构
	var roots []dto.PermissionDto
	for _, p := range permissions {
		permissionDto := permissionMap[p.ID]
		if p.ParentID == nil {
			roots = append(roots, permissionDto)
		} else {
			if parent, ok := permissionMap[*p.ParentID]; ok {
				parent.Children = append(parent.Children, permissionDto)
				permissionMap[*p.ParentID] = parent
			}
		}
	}

	return roots
}

// convertPermissionToDto 转换权限实体为DTO
func (s *PermissionService) convertPermissionToDto(permission *entity.Permission, children []dto.PermissionDto) dto.PermissionDto {
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
