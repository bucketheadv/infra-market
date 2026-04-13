package repository

import (
	"github.com/bucketheadv/infra-market/internal/entity"
	"gorm.io/gorm"
)

type UserRoleRepository struct {
	db *gorm.DB
}

func NewUserRoleRepository(db *gorm.DB) *UserRoleRepository {
	return &UserRoleRepository{db: db}
}

// FindByUID 根据用户ID查询
func (r *UserRoleRepository) FindByUID(uid uint64) ([]entity.UserRole, error) {
	var userRoles []entity.UserRole
	err := r.db.Where("uid = ?", uid).Find(&userRoles).Error
	return userRoles, err
}

// FindByRoleID 根据角色ID查询
func (r *UserRoleRepository) FindByRoleID(roleID uint64) ([]entity.UserRole, error) {
	var userRoles []entity.UserRole
	err := r.db.Where("role_id = ?", roleID).Find(&userRoles).Error
	return userRoles, err
}

// FindByUIDs 批量查询
func (r *UserRoleRepository) FindByUIDs(uids []uint64) ([]entity.UserRole, error) {
	if len(uids) == 0 {
		return []entity.UserRole{}, nil
	}
	var userRoles []entity.UserRole
	err := r.db.Where("uid IN ?", uids).Find(&userRoles).Error
	return userRoles, err
}

// DeleteByUID 删除用户的所有角色关联
func (r *UserRoleRepository) DeleteByUID(uid uint64) error {
	return r.db.Where("uid = ?", uid).Delete(&entity.UserRole{}).Error
}

// Create 创建用户角色关联
func (r *UserRoleRepository) Create(userRole *entity.UserRole) error {
	return r.db.Create(userRole).Error
}

// CountByRoleID 统计角色的用户数量
func (r *UserRoleRepository) CountByRoleID(roleID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.UserRole{}).Where("role_id = ?", roleID).Count(&count).Error
	return count, err
}
