package service

import (
	"log"
	"net/http"
	"sort"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/enums"
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
		log.Printf("获取权限列表失败: %v\n", err)
		return dto.Error[dto.PageResult[dto.PermissionDto]]("查询失败", http.StatusInternalServerError)
	}

	// 收集所有需要查询的权限ID（包括当前权限和它们的祖先权限）
	allPermissionIDSet := make(map[uint64]bool)

	// 先添加当前列表中的所有权限ID
	for _, p := range permissions {
		allPermissionIDSet[p.ID] = true
	}

	// 递归收集所有祖先权限ID（迭代查询直到没有新的父权限）
	parentIDsToQuery := make([]uint64, 0)
	for _, p := range permissions {
		if p.ParentID != nil && !allPermissionIDSet[*p.ParentID] {
			parentIDsToQuery = append(parentIDsToQuery, *p.ParentID)
			allPermissionIDSet[*p.ParentID] = true
		}
	}

	// 迭代查询所有祖先权限
	allQueriedPermissions := make([]entity.Permission, 0)
	for len(parentIDsToQuery) > 0 {
		parentPermissions, _ := s.permissionRepo.FindByIDs(parentIDsToQuery)
		allQueriedPermissions = append(allQueriedPermissions, parentPermissions...)

		// 收集下一层父权限ID
		parentIDsToQuery = make([]uint64, 0)
		for _, p := range parentPermissions {
			if p.ParentID != nil && !allPermissionIDSet[*p.ParentID] {
				parentIDsToQuery = append(parentIDsToQuery, *p.ParentID)
				allPermissionIDSet[*p.ParentID] = true
			}
		}
	}

	// 构建完整的权限Map（包括当前列表中的权限和查询到的所有祖先权限）
	allPermissionMap := make(map[uint64]*entity.Permission)
	// 先添加当前列表中的权限
	for i := range permissions {
		allPermissionMap[permissions[i].ID] = &permissions[i]
	}
	// 再添加查询到的所有祖先权限
	for i := range allQueriedPermissions {
		allPermissionMap[allQueriedPermissions[i].ID] = &allQueriedPermissions[i]
	}

	// 构建父权限Map（仅包含直接父权限）
	parentPermissionMap := make(map[uint64]*entity.Permission)
	for _, p := range permissions {
		if p.ParentID != nil {
			if parent, exists := allPermissionMap[*p.ParentID]; exists {
				parentPermissionMap[*p.ParentID] = parent
			}
		}
	}

	// 转换权限列表，使用缓存的父权限数据
	permissionDtos := make([]dto.PermissionDto, len(permissions))
	for i, p := range permissions {
		permissionDtos[i] = s.convertPermissionToDtoWithParentCached(&p, parentPermissionMap, allPermissionMap)
	}

	result := dto.PageResult[dto.PermissionDto]{
		Records: permissionDtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

// GetPermissionTree 获取权限树
func (s *PermissionService) GetPermissionTree() dto.ApiData[[]dto.PermissionDto] {
	permissions, _ := s.permissionRepo.FindAll()

	// 只返回激活状态的权限
	activePermissions := make([]entity.Permission, 0)
	for _, p := range permissions {
		if p.Status == enums.StatusActive.Code() {
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
		log.Printf("获取权限详情失败，权限ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.PermissionDto]("权限不存在", http.StatusNotFound)
	}

	permissionDto := s.convertPermissionToDtoWithParent(permission)
	return dto.Success(permissionDto)
}

// CreatePermission 创建权限
func (s *PermissionService) CreatePermission(form dto.PermissionFormDto) dto.ApiData[dto.PermissionDto] {
	// 检查权限编码是否已存在
	if _, err := s.permissionRepo.FindByCode(form.Code); err == nil {
		return dto.Error[dto.PermissionDto]("权限编码已存在", http.StatusBadRequest)
	}

	permission := &entity.Permission{
		Name:     form.Name,
		Code:     form.Code,
		Type:     form.Type,
		ParentID: form.ParentID,
		Path:     form.Path,
		Icon:     form.Icon,
		Sort:     form.Sort,
		Status:   enums.StatusActive.Code(),
	}

	if err := s.permissionRepo.Create(permission); err != nil {
		log.Printf("创建权限失败，权限编码: %s, 错误: %v\n", form.Code, err)
		return dto.Error[dto.PermissionDto]("创建权限失败", http.StatusInternalServerError)
	}

	permissionDto := s.convertPermissionToDto(permission, nil)
	return dto.Success(permissionDto)
}

// UpdatePermission 更新权限
func (s *PermissionService) UpdatePermission(id uint64, form dto.PermissionFormDto) dto.ApiData[dto.PermissionDto] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		log.Printf("更新权限失败，权限ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.PermissionDto]("权限不存在", http.StatusNotFound)
	}

	// 检查权限编码是否已被其他权限使用
	if existingPermission, err := s.permissionRepo.FindByCode(form.Code); err == nil && existingPermission.ID != permission.ID {
		return dto.Error[dto.PermissionDto]("权限编码已存在", http.StatusBadRequest)
	}

	permission.Name = form.Name
	permission.Type = form.Type
	permission.ParentID = form.ParentID
	permission.Path = form.Path
	permission.Icon = form.Icon
	permission.Sort = form.Sort

	if err := s.permissionRepo.Update(permission); err != nil {
		log.Printf("更新权限失败，权限ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.PermissionDto]("更新权限失败", http.StatusInternalServerError)
	}

	permissionDto := s.convertPermissionToDto(permission, nil)
	return dto.Success(permissionDto)
}

// DeletePermission 删除权限
func (s *PermissionService) DeletePermission(id uint64) dto.ApiData[any] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		log.Printf("删除权限失败，权限ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("权限不存在", http.StatusNotFound)
	}

	if permission.Code == enums.SystemPermissionCode {
		return dto.Error[any]("不能删除系统权限", http.StatusBadRequest)
	}

	// 检查是否有子权限
	childPermissions, _ := s.permissionRepo.FindByParentID(id)
	if len(childPermissions) > 0 {
		return dto.Error[any]("该权限下还有子权限，无法删除", http.StatusBadRequest)
	}

	permission.Status = enums.StatusDeleted.Code()
	if err := s.permissionRepo.Update(permission); err != nil {
		log.Printf("删除权限失败，权限ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除权限失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// UpdatePermissionStatus 更新权限状态
func (s *PermissionService) UpdatePermissionStatus(id uint64, status string) dto.ApiData[any] {
	permission, err := s.permissionRepo.FindByID(id)
	if err != nil {
		log.Printf("更新权限状态失败，权限ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("权限不存在", http.StatusNotFound)
	}

	if permission.Code == enums.SystemPermissionCode && status == enums.StatusDeleted.Code() {
		return dto.Error[any]("不能删除系统权限", http.StatusBadRequest)
	}

	if permission.Status == enums.StatusDeleted.Code() && status != enums.StatusDeleted.Code() {
		return dto.Error[any]("已删除的权限不能重新启用", http.StatusBadRequest)
	}

	if status == enums.StatusDeleted.Code() {
		return s.DeletePermission(id)
	}

	permission.Status = status
	if err := s.permissionRepo.Update(permission); err != nil {
		log.Printf("更新权限状态失败，权限ID: %d, 状态: %s, 错误: %v\n", id, status, err)
		return dto.Error[any]("更新状态失败", http.StatusInternalServerError)
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

	return buildPermissionTreeFromMap(permissions, permissionMap)
}

// buildPermissionTreeFromMap 从权限Map构建权限树（公共函数）
func buildPermissionTreeFromMap(permissions []entity.Permission, permissionMap map[uint64]dto.PermissionDto) []dto.PermissionDto {
	// 构建树形结构
	var roots []dto.PermissionDto
	for _, p := range permissions {
		if p.ParentID == nil {
			// 根节点，递归构建其子节点树
			rootDto := buildPermissionWithChildren(permissionMap[p.ID], permissionMap)
			roots = append(roots, rootDto)
		}
	}

	// 按sort字段排序根节点
	sort.Slice(roots, func(i, j int) bool {
		return roots[i].Sort < roots[j].Sort
	})

	return roots
}

// buildPermissionWithChildren 递归构建带子权限的权限对象（公共函数）
func buildPermissionWithChildren(permission dto.PermissionDto, permissionMap map[uint64]dto.PermissionDto) dto.PermissionDto {
	var children []dto.PermissionDto

	// 查找所有以当前权限为父级的权限
	for _, childPermission := range permissionMap {
		if childPermission.ParentID != nil && *childPermission.ParentID == permission.ID {
			// 递归构建子权限
			childWithChildren := buildPermissionWithChildren(childPermission, permissionMap)
			children = append(children, childWithChildren)
		}
	}

	// 按sort字段排序子权限
	sort.Slice(children, func(i, j int) bool {
		return children[i].Sort < children[j].Sort
	})

	// 返回带有子权限的新PermissionDto
	permission.Children = children
	return permission
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

// convertPermissionToDtoWithParent 转换权限实体为DTO，包含父级权限信息和访问路径
func (s *PermissionService) convertPermissionToDtoWithParent(permission *entity.Permission) dto.PermissionDto {
	permissionDto := s.convertPermissionToDto(permission, nil)

	// 获取父级权限信息
	if permission.ParentID != nil {
		parentPermission, err := s.permissionRepo.FindByID(*permission.ParentID)
		if err == nil {
			permissionDto.ParentPermission = &dto.ParentPermissionDto{
				ID:   parentPermission.ID,
				Name: parentPermission.Name,
				Code: parentPermission.Code,
			}
		}
	}

	// 构建访问路径（从根到当前权限的路径链）
	permissionDto.AccessPath = s.buildAccessPath(permission)

	return permissionDto
}

// buildAccessPath 构建访问路径（从根权限到当前权限的名称链）
func (s *PermissionService) buildAccessPath(permission *entity.Permission) []string {
	var path []string
	current := permission

	// 向上遍历到根节点，收集权限名称
	for current != nil {
		path = append([]string{current.Name}, path...)
		if current.ParentID != nil {
			parent, err := s.permissionRepo.FindByID(*current.ParentID)
			if err != nil {
				break
			}
			current = parent
		} else {
			break
		}
	}

	return path
}

// buildAccessPathCached 构建访问路径（使用缓存的权限Map）
func (s *PermissionService) buildAccessPathCached(permission *entity.Permission, allPermissionMap map[uint64]*entity.Permission) []string {
	var path []string
	current := permission

	// 向上遍历到根节点，收集权限名称
	for current != nil {
		path = append([]string{current.Name}, path...)
		if current.ParentID != nil {
			if parent, exists := allPermissionMap[*current.ParentID]; exists {
				current = parent
			} else {
				break
			}
		} else {
			break
		}
	}

	return path
}

// convertPermissionToDtoWithParentCached 转换权限实体为DTO，使用缓存的父权限数据
func (s *PermissionService) convertPermissionToDtoWithParentCached(
	permission *entity.Permission,
	parentPermissionMap map[uint64]*entity.Permission,
	allPermissionMap map[uint64]*entity.Permission,
) dto.PermissionDto {
	permissionDto := s.convertPermissionToDto(permission, nil)

	// 获取父级权限信息（从缓存中获取）
	if permission.ParentID != nil {
		if parentPermission, exists := parentPermissionMap[*permission.ParentID]; exists {
			permissionDto.ParentPermission = &dto.ParentPermissionDto{
				ID:   parentPermission.ID,
				Name: parentPermission.Name,
				Code: parentPermission.Code,
			}
		}
	}

	// 构建访问路径（使用缓存的权限Map）
	permissionDto.AccessPath = s.buildAccessPathCached(permission, allPermissionMap)

	return permissionDto
}
