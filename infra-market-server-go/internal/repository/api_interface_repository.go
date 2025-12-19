package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"gorm.io/gorm"
)

type ApiInterfaceRepository struct {
	db *gorm.DB
}

func NewApiInterfaceRepository(db *gorm.DB) *ApiInterfaceRepository {
	return &ApiInterfaceRepository{db: db}
}

// FindByID 根据ID查询
func (r *ApiInterfaceRepository) FindByID(id uint64) (*entity.ApiInterface, error) {
	var apiInterface entity.ApiInterface
	err := r.db.First(&apiInterface, id).Error
	if err != nil {
		return nil, err
	}
	return &apiInterface, nil
}

// FindByIDs 批量查询
func (r *ApiInterfaceRepository) FindByIDs(ids []uint64) ([]entity.ApiInterface, error) {
	if len(ids) == 0 {
		return []entity.ApiInterface{}, nil
	}
	var interfaces []entity.ApiInterface
	err := r.db.Where("id IN ? AND status = ?", ids, 1).Find(&interfaces).Error
	return interfaces, err
}

// Page 分页查询
func (r *ApiInterfaceRepository) Page(query dto.ApiInterfaceQueryDto) ([]entity.ApiInterface, int64, error) {
	var interfaces []entity.ApiInterface
	var total int64

	db := r.db.Model(&entity.ApiInterface{})

	if query.Name != nil && *query.Name != "" {
		db = db.Where("name LIKE ?", "%"+*query.Name+"%")
	}
	if query.Method != nil && *query.Method != "" {
		db = db.Where("method = ?", *query.Method)
	}
	if query.Status != nil {
		db = db.Where("status = ?", *query.Status)
	}
	if query.Environment != nil && *query.Environment != "" {
		db = db.Where("environment = ?", *query.Environment)
	}

	if err := db.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	page := 1
	if query.Page != nil {
		page = *query.Page
	}
	size := 10
	if query.Size != nil {
		size = *query.Size
	}
	offset := (page - 1) * size

	err := db.Order("create_time DESC").Offset(offset).Limit(size).Find(&interfaces).Error
	return interfaces, total, err
}

// Create 创建接口
func (r *ApiInterfaceRepository) Create(apiInterface *entity.ApiInterface) error {
	return r.db.Create(apiInterface).Error
}

// Update 更新接口
func (r *ApiInterfaceRepository) Update(apiInterface *entity.ApiInterface) error {
	return r.db.Save(apiInterface).Error
}

// Delete 删除接口
func (r *ApiInterfaceRepository) Delete(id uint64) error {
	return r.db.Delete(&entity.ApiInterface{}, id).Error
}

// Count 获取接口总数
func (r *ApiInterfaceRepository) Count() (int64, error) {
	var count int64
	err := r.db.Model(&entity.ApiInterface{}).Count(&count).Error
	return count, err
}

// CountBeforeDate 获取指定时间之前的接口总数
func (r *ApiInterfaceRepository) CountBeforeDate(timestamp int64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.ApiInterface{}).
		Where("create_time <= ?", timestamp).
		Count(&count).Error
	return count, err
}
