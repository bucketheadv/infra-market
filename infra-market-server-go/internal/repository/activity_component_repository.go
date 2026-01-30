package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type ActivityComponentRepository struct {
	db *gorm.DB
}

func NewActivityComponentRepository(db *gorm.DB) *ActivityComponentRepository {
	return &ActivityComponentRepository{db: db}
}

// FindByID 根据ID查询活动组件
func (r *ActivityComponentRepository) FindByID(id uint64) (*entity.ActivityComponent, error) {
	var component entity.ActivityComponent
	err := r.db.Where("id = ?", id).First(&component).Error
	if err != nil {
		return nil, err
	}
	return &component, nil
}

// Page 分页查询活动组件
func (r *ActivityComponentRepository) Page(query dto.ActivityComponentQueryDto) ([]entity.ActivityComponent, int64, error) {
	var components []entity.ActivityComponent

	db := r.db.Model(&entity.ActivityComponent{})

	if util.IsNotBlank(query.Name) {
		db = db.Where("name LIKE ?", "%"+*query.Name+"%")
	}
	if query.Status != nil {
		db = db.Where("status = ?", *query.Status)
	}

	return PaginateQuery(db, &query, "create_time DESC", &components)
}

// List 查询所有活动组件
func (r *ActivityComponentRepository) List() ([]entity.ActivityComponent, error) {
	var components []entity.ActivityComponent
	err := r.db.Order("create_time DESC").Find(&components).Error
	return components, err
}

// Create 创建活动组件
func (r *ActivityComponentRepository) Create(component *entity.ActivityComponent) error {
	return r.db.Create(component).Error
}

// Update 更新活动组件
func (r *ActivityComponentRepository) Update(component *entity.ActivityComponent) error {
	return r.db.Save(component).Error
}

// Delete 删除活动组件
func (r *ActivityComponentRepository) Delete(id uint64) error {
	return r.db.Delete(&entity.ActivityComponent{}, id).Error
}
