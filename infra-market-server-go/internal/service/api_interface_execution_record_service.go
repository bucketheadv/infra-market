package service

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
)

type ApiInterfaceExecutionRecordService struct {
	repo *repository.ApiInterfaceExecutionRecordRepository
}

func NewApiInterfaceExecutionRecordService(repo *repository.ApiInterfaceExecutionRecordRepository) *ApiInterfaceExecutionRecordService {
	return &ApiInterfaceExecutionRecordService{repo: repo}
}

// FindPage 分页查询
func (s *ApiInterfaceExecutionRecordService) FindPage(query dto.ApiInterfaceExecutionRecordQueryDto) dto.ApiData[dto.PageResult[dto.ApiInterfaceExecutionRecordDto]] {
	records, total, err := s.repo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.ApiInterfaceExecutionRecordDto]]("查询失败", 500)
	}

	recordDtos := make([]dto.ApiInterfaceExecutionRecordDto, len(records))
	for i, record := range records {
		recordDtos[i] = s.convertToDto(&record)
	}

	page := 1
	if query.Page != nil {
		page = *query.Page
	}
	size := 10
	if query.Size != nil {
		size = *query.Size
	}

	result := dto.PageResult[dto.ApiInterfaceExecutionRecordDto]{
		Records: recordDtos,
		Total:   total,
		Current: page,
		Size:    size,
	}

	return dto.Success(result)
}

// GetByID 根据ID查询
func (s *ApiInterfaceExecutionRecordService) GetByID(id uint64) dto.ApiData[dto.ApiInterfaceExecutionRecordDto] {
	record, err := s.repo.FindByID(id)
	if err != nil {
		return dto.Error[dto.ApiInterfaceExecutionRecordDto]("执行记录不存在", 404)
	}

	recordDto := s.convertToDto(record)
	return dto.Success(recordDto)
}

// FindByExecutorID 根据执行人ID查询
func (s *ApiInterfaceExecutionRecordService) FindByExecutorID(executorID uint64, limit int) dto.ApiData[[]dto.ApiInterfaceExecutionRecordDto] {
	records, err := s.repo.FindByExecutorID(executorID, limit)
	if err != nil {
		return dto.Error[[]dto.ApiInterfaceExecutionRecordDto]("查询失败", 500)
	}

	recordDtos := make([]dto.ApiInterfaceExecutionRecordDto, len(records))
	for i, record := range records {
		recordDtos[i] = s.convertToDto(&record)
	}

	return dto.Success(recordDtos)
}

// GetExecutionStats 获取执行统计信息
func (s *ApiInterfaceExecutionRecordService) GetExecutionStats(interfaceID uint64) dto.ApiData[dto.ApiInterfaceExecutionRecordStatsDto] {
	// 查询所有执行记录
	query := dto.ApiInterfaceExecutionRecordQueryDto{
		InterfaceID: &interfaceID,
	}
	records, _, err := s.repo.Page(query)
	if err != nil {
		return dto.Error[dto.ApiInterfaceExecutionRecordStatsDto]("查询失败", 500)
	}

	if len(records) == 0 {
		return dto.Success(dto.ApiInterfaceExecutionRecordStatsDto{
			InterfaceID: &interfaceID,
		})
	}

	// 计算统计信息
	totalExecutions := int64(len(records))
	successCount := int64(0)
	var executionTimes []int64

	for _, record := range records {
		if record.Success != nil && *record.Success {
			successCount++
		}
		if record.ExecutionTime != nil {
			executionTimes = append(executionTimes, *record.ExecutionTime)
		}
	}

	failedExecutions := totalExecutions - successCount
	successRate := 0.0
	if totalExecutions > 0 {
		successRate = float64(successCount) / float64(totalExecutions) * 100
	}

	avgExecutionTime := 0.0
	minExecutionTime := int64(0)
	maxExecutionTime := int64(0)
	if len(executionTimes) > 0 {
		sum := int64(0)
		for _, t := range executionTimes {
			sum += t
			if minExecutionTime == 0 || t < minExecutionTime {
				minExecutionTime = t
			}
			if t > maxExecutionTime {
				maxExecutionTime = t
			}
		}
		avgExecutionTime = float64(sum) / float64(len(executionTimes))
	}

	// 获取最后执行时间
	var lastExecutionTime *string
	if len(records) > 0 {
		lastRecord := records[0] // 已按时间倒序
		if lastRecord.CreateTime > 0 {
			formatted := util.Format(&lastRecord.CreateTime)
			lastExecutionTime = &formatted
		}
	}

	stats := dto.ApiInterfaceExecutionRecordStatsDto{
		InterfaceID:       &interfaceID,
		TotalExecutions:   totalExecutions,
		SuccessExecutions: successCount,
		FailedExecutions:  failedExecutions,
		SuccessRate:       successRate,
		AvgExecutionTime:  avgExecutionTime,
		MinExecutionTime:  minExecutionTime,
		MaxExecutionTime:  maxExecutionTime,
		LastExecutionTime: lastExecutionTime,
	}

	return dto.Success(stats)
}

// CountByTimeRange 根据时间范围统计
func (s *ApiInterfaceExecutionRecordService) CountByTimeRange(startTime, endTime int64) dto.ApiData[int64] {
	count, err := s.repo.CountByTimeRange(startTime, endTime)
	if err != nil {
		return dto.Error[int64]("统计失败", 500)
	}
	return dto.Success(count)
}

// DeleteByTimeBefore 删除指定时间之前的记录
func (s *ApiInterfaceExecutionRecordService) DeleteByTimeBefore(beforeTime int64) dto.ApiData[int64] {
	deletedCount, err := s.repo.DeleteByTimeBefore(beforeTime)
	if err != nil {
		return dto.Error[int64]("删除失败", 500)
	}
	return dto.Success(deletedCount)
}

// convertToDto 转换实体为DTO
func (s *ApiInterfaceExecutionRecordService) convertToDto(record *entity.ApiInterfaceExecutionRecord) dto.ApiInterfaceExecutionRecordDto {
	createTime := util.Format(&record.CreateTime)
	updateTime := util.Format(&record.UpdateTime)

	return dto.ApiInterfaceExecutionRecordDto{
		ID:              &record.ID,
		InterfaceID:     record.InterfaceID,
		ExecutorID:      record.ExecutorID,
		ExecutorName:    &record.ExecutorName,
		RequestParams:   record.RequestParams,
		RequestHeaders:  record.RequestHeaders,
		RequestBody:     record.RequestBody,
		ResponseStatus:  record.ResponseStatus,
		ResponseHeaders: record.ResponseHeaders,
		ResponseBody:    record.ResponseBody,
		ExecutionTime:   record.ExecutionTime,
		Success:         record.Success,
		ErrorMessage:    record.ErrorMessage,
		Remark:          record.Remark,
		ClientIP:        record.ClientIP,
		UserAgent:       record.UserAgent,
		CreateTime:      &createTime,
		UpdateTime:      &updateTime,
	}
}
