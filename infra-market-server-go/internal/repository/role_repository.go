package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"gorm.io/gorm"
)

type RoleRepository struct {
	db *gorm.DB
}

func NewRoleRepository(db *gorm.DB) *RoleRepository {
	return &RoleRepository{db: db}
}

// FindByCode 根据编码查询
func (r *RoleRepository) FindByCode(code string) (*entity.Role, error) {
	var role entity.Role
	err := r.db.Where("code = ? AND status != ?", code, "deleted").First(&role).Error
	if err != nil {
		return nil, err
	}
	return &role, nil
}

// FindByID 根据ID查询
func (r *RoleRepository) FindByID(id uint64) (*entity.Role, error) {
	var role entity.Role
	err := r.db.Where("id = ? AND status != ?", id, "deleted").First(&role).Error
	if err != nil {
		return nil, err
	}
	return &role, nil
}

// FindByIDs 批量查询
func (r *RoleRepository) FindByIDs(ids []uint64) ([]entity.Role, error) {
	if len(ids) == 0 {
		return []entity.Role{}, nil
	}
	var roles []entity.Role
	err := r.db.Where("id IN ? AND status != ?", ids, "deleted").Find(&roles).Error
	return roles, err
}

// Page 分页查询
func (r *RoleRepository) Page(query dto.RoleQueryDto) ([]entity.Role, int64, error) {
	var roles []entity.Role
	var total int64

	db := r.db.Model(&entity.Role{}).Where("status != ?", "deleted")

	if query.Name != nil && *query.Name != "" {
		db = db.Where("name LIKE ?", "%"+*query.Name+"%")
	}
	if query.Code != nil && *query.Code != "" {
		db = db.Where("code LIKE ?", "%"+*query.Code+"%")
	}
	if query.Status != nil && *query.Status != "" {
		db = db.Where("status = ?", *query.Status)
	}

	if err := db.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	current := query.Current
	if current < 1 {
		current = 1
	}
	size := query.Size
	if size < 1 {
		size = 10
	}
	offset := (current - 1) * size

	err := db.Order("id ASC").Offset(offset).Limit(size).Find(&roles).Error
	return roles, total, err
}

// Create 创建角色
func (r *RoleRepository) Create(role *entity.Role) error {
	return r.db.Create(role).Error
}

// Update 更新角色
func (r *RoleRepository) Update(role *entity.Role) error {
	return r.db.Save(role).Error
}

// Delete 删除角色（软删除）
func (r *RoleRepository) Delete(id uint64) error {
	return r.db.Model(&entity.Role{}).Where("id = ?", id).Update("status", "deleted").Error
}

// Count 获取角色总数
func (r *RoleRepository) Count() (int64, error) {
	var count int64
	err := r.db.Model(&entity.Role{}).Where("status != ?", "deleted").Count(&count).Error
	return count, err
}

// CountBeforeDate 获取指定时间之前的角色总数
func (r *RoleRepository) CountBeforeDate(timestamp int64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.Role{}).
		Where("status != ? AND create_time <= ?", "deleted", timestamp).
		Count(&count).Error
	return count, err
}

// FindByStatus 根据状态查询
func (r *RoleRepository) FindByStatus(status string) ([]entity.Role, error) {
	var roles []entity.Role
	err := r.db.Where("status = ?", status).Find(&roles).Error
	return roles, err
}
