package repository

import (
	"github.com/bucketheadv/infra-market/internal/entity"
	"gorm.io/gorm"
)

type RolePermissionRepository struct {
	db *gorm.DB
}

func NewRolePermissionRepository(db *gorm.DB) *RolePermissionRepository {
	return &RolePermissionRepository{db: db}
}

// FindByRoleID 根据角色ID查询
func (r *RolePermissionRepository) FindByRoleID(roleID uint64) ([]entity.RolePermission, error) {
	var rolePermissions []entity.RolePermission
	err := r.db.Where("role_id = ?", roleID).Find(&rolePermissions).Error
	return rolePermissions, err
}

// FindByRoleIDs 批量查询
func (r *RolePermissionRepository) FindByRoleIDs(roleIDs []uint64) ([]entity.RolePermission, error) {
	if len(roleIDs) == 0 {
		return []entity.RolePermission{}, nil
	}
	var rolePermissions []entity.RolePermission
	err := r.db.Where("role_id IN ?", roleIDs).Find(&rolePermissions).Error
	return rolePermissions, err
}

// DeleteByRoleID 删除角色的所有权限关联
func (r *RolePermissionRepository) DeleteByRoleID(roleID uint64) error {
	return r.db.Where("role_id = ?", roleID).Delete(&entity.RolePermission{}).Error
}

// Create 创建角色权限关联
func (r *RolePermissionRepository) Create(rolePermission *entity.RolePermission) error {
	return r.db.Create(rolePermission).Error
}

// CountByPermissionID 统计权限的角色数量
func (r *RolePermissionRepository) CountByPermissionID(permissionID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.RolePermission{}).Where("permission_id = ?", permissionID).Count(&count).Error
	return count, err
}
