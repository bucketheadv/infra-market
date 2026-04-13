package repository

import (
	"time"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type ApiInterfaceExecutionRecordRepository struct {
	db *gorm.DB
}

func NewApiInterfaceExecutionRecordRepository(db *gorm.DB) *ApiInterfaceExecutionRecordRepository {
	return &ApiInterfaceExecutionRecordRepository{db: db}
}

// FindByID 根据ID查询
func (r *ApiInterfaceExecutionRecordRepository) FindByID(id uint64) (*entity.ApiInterfaceExecutionRecord, error) {
	var record entity.ApiInterfaceExecutionRecord
	err := r.db.First(&record, id).Error
	if err != nil {
		return nil, err
	}
	return &record, nil
}

// Page 分页查询
func (r *ApiInterfaceExecutionRecordRepository) Page(query dto.ApiInterfaceExecutionRecordQueryDto) ([]entity.ApiInterfaceExecutionRecord, int64, error) {
	var records []entity.ApiInterfaceExecutionRecord

	db := r.db.Model(&entity.ApiInterfaceExecutionRecord{})

	if query.InterfaceID != nil {
		db = db.Where("interface_id = ?", *query.InterfaceID)
	}

	// 关键字查询：在执行人姓名、错误信息、备注等字段中搜索
	if util.IsNotBlank(query.Keyword) {
		keyword := "%" + *query.Keyword + "%"
		db = db.Where("executor_name LIKE ? OR error_message LIKE ? OR remark LIKE ?", keyword, keyword, keyword)
	}

	if query.ExecutorID != nil {
		db = db.Where("executor_id = ?", *query.ExecutorID)
	}
	if util.IsNotBlank(query.ExecutorName) {
		db = db.Where("executor_name LIKE ?", "%"+*query.ExecutorName+"%")
	}
	if query.Success != nil {
		db = db.Where("success = ?", *query.Success)
	}
	if query.MinExecutionTime != nil {
		db = db.Where("execution_time >= ?", *query.MinExecutionTime)
	}
	if query.MaxExecutionTime != nil {
		db = db.Where("execution_time <= ?", *query.MaxExecutionTime)
	}
	if query.StartTime != nil {
		db = db.Where("create_time >= ?", *query.StartTime)
	}
	if query.EndTime != nil {
		db = db.Where("create_time <= ?", *query.EndTime)
	}

	return PaginateQuery(db, &query, "id DESC", &records)
}

// FindByExecutorID 根据执行人ID查询
func (r *ApiInterfaceExecutionRecordRepository) FindByExecutorID(executorID uint64, limit int) ([]entity.ApiInterfaceExecutionRecord, error) {
	var records []entity.ApiInterfaceExecutionRecord
	err := r.db.Where("executor_id = ?", executorID).
		Order("create_time DESC").
		Limit(limit).
		Find(&records).Error
	return records, err
}

// Create 创建执行记录
func (r *ApiInterfaceExecutionRecordRepository) Create(record *entity.ApiInterfaceExecutionRecord) error {
	return r.db.Create(record).Error
}

// CountByTimeRange 根据时间范围统计
func (r *ApiInterfaceExecutionRecordRepository) CountByTimeRange(startTime, endTime int64) (int64, error) {
	var count int64
	err := r.db.Model(&entity.ApiInterfaceExecutionRecord{}).
		Where("create_time >= ? AND create_time <= ?", startTime, endTime).
		Count(&count).Error
	return count, err
}

// DeleteByTimeBefore 删除指定时间之前的记录
func (r *ApiInterfaceExecutionRecordRepository) DeleteByTimeBefore(beforeTime int64) (int64, error) {
	result := r.db.Where("create_time < ?", beforeTime).
		Delete(&entity.ApiInterfaceExecutionRecord{})
	return result.RowsAffected, result.Error
}

// FindMostUsedInterfaceIDs 查询最近最热门的接口ID列表
func (r *ApiInterfaceExecutionRecordRepository) FindMostUsedInterfaceIDs(days, limit int) ([]uint64, error) {
	// 计算开始时间（毫秒时间戳）
	startTime := time.Now().AddDate(0, 0, -days).UnixMilli()

	var results []struct {
		InterfaceID uint64
		Count       int64
	}

	err := r.db.Model(&entity.ApiInterfaceExecutionRecord{}).
		Select("interface_id, COUNT(*) as count").
		Where("create_time >= ?", startTime).
		Group("interface_id").
		Order("count DESC").
		Limit(limit).
		Scan(&results).Error

	if err != nil {
		return nil, err
	}

	ids := make([]uint64, len(results))
	for i, result := range results {
		ids[i] = result.InterfaceID
	}

	return ids, nil
}
