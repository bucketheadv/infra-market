package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"gorm.io/gorm"
)

type ActivityRepository struct {
	db *gorm.DB
}

func NewActivityRepository(db *gorm.DB) *ActivityRepository {
	return &ActivityRepository{db: db}
}

// FindByID 根据ID查询活动
func (r *ActivityRepository) FindByID(id uint64) (*entity.Activity, error) {
	var activity entity.Activity
	err := r.db.Where("id = ?", id).First(&activity).Error
	if err != nil {
		return nil, err
	}
	return &activity, nil
}

// Page 分页查询活动
func (r *ActivityRepository) Page(query dto.ActivityQueryDto) ([]entity.Activity, int64, error) {
	var activities []entity.Activity

	db := ApplyNameStatusFilters(r.db.Model(&entity.Activity{}), query.Name, query.Status)

	if query.TemplateID != nil {
		db = db.Where("template_id = ?", *query.TemplateID)
	}

	return PaginateQuery(db, &query, "create_time DESC", &activities)
}

// Create 创建活动
func (r *ActivityRepository) Create(activity *entity.Activity) error {
	return r.db.Create(activity).Error
}

// Update 更新活动
func (r *ActivityRepository) Update(activity *entity.Activity) error {
	return r.db.Save(activity).Error
}

// Delete 删除活动
func (r *ActivityRepository) Delete(id uint64) error {
	return r.db.Delete(&entity.Activity{}, id).Error
}
