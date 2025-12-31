package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type PermissionRepository struct {
	db *gorm.DB
}

func NewPermissionRepository(db *gorm.DB) *PermissionRepository {
	return &PermissionRepository{db: db}
}

// FindByCode 根据编码查询
func (r *PermissionRepository) FindByCode(code string) (*entity.Permission, error) {
	var permission entity.Permission
	err := r.db.Where("code = ? AND status != ?", code, "deleted").First(&permission).Error
	if err != nil {
		return nil, err
	}
	return &permission, nil
}

// FindByID 根据ID查询
func (r *PermissionRepository) FindByID(id uint64) (*entity.Permission, error) {
	var permission entity.Permission
	err := r.db.Where("id = ? AND status != ?", id, "deleted").First(&permission).Error
	if err != nil {
		return nil, err
	}
	return &permission, nil
}

// FindByIDs 批量查询
func (r *PermissionRepository) FindByIDs(ids []uint64) ([]entity.Permission, error) {
	if len(ids) == 0 {
		return []entity.Permission{}, nil
	}
	var permissions []entity.Permission
	err := r.db.Where("id IN ? AND status != ?", ids, "deleted").Find(&permissions).Error
	return permissions, err
}

// FindByParentID 根据父ID查询
func (r *PermissionRepository) FindByParentID(parentID uint64) ([]entity.Permission, error) {
	var permissions []entity.Permission
	err := r.db.Where("parent_id = ? AND status != ?", parentID, "deleted").Find(&permissions).Error
	return permissions, err
}

// FindAll 查询所有权限
func (r *PermissionRepository) FindAll() ([]entity.Permission, error) {
	var permissions []entity.Permission
	err := r.db.Where("status != ?", "deleted").Order("sort ASC, id ASC").Find(&permissions).Error
	return permissions, err
}

// Page 分页查询
func (r *PermissionRepository) Page(query dto.PermissionQueryDto) ([]entity.Permission, int64, error) {
	var permissions []entity.Permission

	db := r.db.Model(&entity.Permission{}).Where("status != ?", "deleted")
	db = ApplyCommonFilters(db, query.Name, query.Code, query.Status)
	
	if util.IsNotBlank(query.Type) {
		db = db.Where("type = ?", *query.Type)
	}

	return PaginateQuery(db, &query, "id ASC", &permissions)
}

// Create 创建权限
func (r *PermissionRepository) Create(permission *entity.Permission) error {
	return r.db.Create(permission).Error
}

// Update 更新权限
func (r *PermissionRepository) Update(permission *entity.Permission) error {
	return r.db.Save(permission).Error
}

// Delete 删除权限（软删除）
func (r *PermissionRepository) Delete(id uint64) error {
	return r.db.Model(&entity.Permission{}).Where("id = ?", id).Update("status", "deleted").Error
}

// Count 获取权限总数
func (r *PermissionRepository) Count() (int64, error) {
	var count int64
	err := r.db.Model(&entity.Permission{}).Where("status != ?", "deleted").Count(&count).Error
	return count, err
}

// CountBeforeDate 获取指定时间之前的权限总数
func (r *PermissionRepository) CountBeforeDate(timestamp int64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.Permission{}).
		Where("status != ? AND create_time <= ?", "deleted", timestamp).
		Count(&count).Error
	return count, err
}

// FindByStatus 根据状态查询
func (r *PermissionRepository) FindByStatus(status string) ([]entity.Permission, error) {
	var permissions []entity.Permission
	err := r.db.Where("status = ?", status).Order("sort ASC, id ASC").Find(&permissions).Error
	return permissions, err
}
