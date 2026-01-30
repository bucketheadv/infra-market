package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type ActivityTemplateRepository struct {
	db *gorm.DB
}

func NewActivityTemplateRepository(db *gorm.DB) *ActivityTemplateRepository {
	return &ActivityTemplateRepository{db: db}
}

// FindByID 根据ID查询活动模板
func (r *ActivityTemplateRepository) FindByID(id uint64) (*entity.ActivityTemplate, error) {
	var template entity.ActivityTemplate
	err := r.db.Where("id = ?", id).First(&template).Error
	if err != nil {
		return nil, err
	}
	return &template, nil
}

// Page 分页查询活动模板
func (r *ActivityTemplateRepository) Page(query dto.ActivityTemplateQueryDto) ([]entity.ActivityTemplate, int64, error) {
	var templates []entity.ActivityTemplate

	db := r.db.Model(&entity.ActivityTemplate{})

	if util.IsNotBlank(query.Name) {
		db = db.Where("name LIKE ?", "%"+*query.Name+"%")
	}
	if query.Status != nil {
		db = db.Where("status = ?", *query.Status)
	}

	return PaginateQuery(db, &query, "create_time DESC", &templates)
}

// List 查询所有活动模板
func (r *ActivityTemplateRepository) List() ([]entity.ActivityTemplate, error) {
	var templates []entity.ActivityTemplate
	err := r.db.Order("create_time DESC").Find(&templates).Error
	return templates, err
}

// Create 创建活动模板
func (r *ActivityTemplateRepository) Create(template *entity.ActivityTemplate) error {
	return r.db.Create(template).Error
}

// Update 更新活动模板
func (r *ActivityTemplateRepository) Update(template *entity.ActivityTemplate) error {
	return r.db.Save(template).Error
}

// Delete 删除活动模板
func (r *ActivityTemplateRepository) Delete(id uint64) error {
	return r.db.Delete(&entity.ActivityTemplate{}, id).Error
}
