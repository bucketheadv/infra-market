package service

import (
	"encoding/json"
	"log"
	"net/http"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

type ActivityComponentService struct {
	db              *gorm.DB
	componentRepo   *repository.ActivityComponentRepository
}

func NewActivityComponentService(
	db *gorm.DB,
	componentRepo *repository.ActivityComponentRepository,
) *ActivityComponentService {
	return &ActivityComponentService{
		db:              db,
		componentRepo:   componentRepo,
	}
}

// GetActivityComponents 获取活动组件列表
func (s *ActivityComponentService) GetActivityComponents(query dto.ActivityComponentQueryDto) dto.ApiData[dto.PageResult[dto.ActivityComponentDto]] {
	components, total, err := s.componentRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.ActivityComponentDto]]("查询失败", http.StatusInternalServerError)
	}

	// 转换为DTO
	componentDtos := make([]dto.ActivityComponentDto, len(components))
	for i, component := range components {
		componentDtos[i] = s.convertComponentToDto(&component)
	}

	result := dto.PageResult[dto.ActivityComponentDto]{
		Records: componentDtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

// GetAllActivityComponents 获取所有活动组件
func (s *ActivityComponentService) GetAllActivityComponents() dto.ApiData[[]dto.ActivityComponentDto] {
	components, err := s.componentRepo.List()
	if err != nil {
		return dto.Error[[]dto.ActivityComponentDto]("查询失败", http.StatusInternalServerError)
	}

	// 转换为DTO
	componentDtos := make([]dto.ActivityComponentDto, len(components))
	for i, component := range components {
		componentDtos[i] = s.convertComponentToDto(&component)
	}

	return dto.Success(componentDtos)
}

// GetActivityComponent 获取活动组件详情
func (s *ActivityComponentService) GetActivityComponent(id uint64) dto.ApiData[dto.ActivityComponentDto] {
	component, err := s.componentRepo.FindByID(id)
	if err != nil {
		log.Printf("获取活动组件详情失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityComponentDto]("活动组件不存在", http.StatusNotFound)
	}

	componentDto := s.convertComponentToDto(component)
	return dto.Success(componentDto)
}

// CreateActivityComponent 创建活动组件
func (s *ActivityComponentService) CreateActivityComponent(form dto.ActivityComponentFormDto) dto.ApiData[dto.ActivityComponentDto] {
	// 序列化字段/组件配置
	fieldsJSON, err := s.serializeFields(form.Fields)
	if err != nil {
		log.Printf("序列化字段/组件配置失败: %v\n", err)
		return dto.Error[dto.ActivityComponentDto]("字段/组件配置格式错误", http.StatusBadRequest)
	}

	status := 1
	if form.Status != nil {
		status = *form.Status
	}

	component := &entity.ActivityComponent{
		Name:        form.Name,
		Description: form.Description,
		Fields:      fieldsJSON,
		Status:      status,
	}

	if err := s.componentRepo.Create(component); err != nil {
		log.Printf("创建活动组件失败: %v\n", err)
		return dto.Error[dto.ActivityComponentDto]("创建活动组件失败", http.StatusInternalServerError)
	}

	componentDto := s.convertComponentToDto(component)
	return dto.Success(componentDto)
}

// UpdateActivityComponent 更新活动组件
func (s *ActivityComponentService) UpdateActivityComponent(id uint64, form dto.ActivityComponentFormDto) dto.ApiData[dto.ActivityComponentDto] {
	component, err := s.componentRepo.FindByID(id)
	if err != nil {
		log.Printf("更新活动组件失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityComponentDto]("活动组件不存在", http.StatusNotFound)
	}

	// 序列化字段/组件配置
	var fieldsJSON *string
	if form.Fields != nil {
		jsonStr, err := s.serializeFields(form.Fields)
		if err != nil {
			log.Printf("序列化字段/组件配置失败: %v\n", err)
			return dto.Error[dto.ActivityComponentDto]("字段/组件配置格式错误", http.StatusBadRequest)
		}
		fieldsJSON = jsonStr
	}

	component.Name = form.Name
	if form.Description != nil {
		component.Description = form.Description
	}
	if fieldsJSON != nil {
		component.Fields = fieldsJSON
	}
	if form.Status != nil {
		component.Status = *form.Status
	}

	if err := s.componentRepo.Update(component); err != nil {
		log.Printf("更新活动组件失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityComponentDto]("更新活动组件失败", http.StatusInternalServerError)
	}

	componentDto := s.convertComponentToDto(component)
	return dto.Success(componentDto)
}

// DeleteActivityComponent 删除活动组件
func (s *ActivityComponentService) DeleteActivityComponent(id uint64) dto.ApiData[any] {
	_, err := s.componentRepo.FindByID(id)
	if err != nil {
		log.Printf("删除活动组件失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("活动组件不存在", http.StatusNotFound)
	}

	if err := s.componentRepo.Delete(id); err != nil {
		log.Printf("删除活动组件失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除活动组件失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// UpdateActivityComponentStatus 更新活动组件状态
func (s *ActivityComponentService) UpdateActivityComponentStatus(id uint64, status int) dto.ApiData[dto.ActivityComponentDto] {
	component, err := s.componentRepo.FindByID(id)
	if err != nil {
		log.Printf("更新活动组件状态失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityComponentDto]("活动组件不存在", http.StatusNotFound)
	}

	component.Status = status
	if err := s.componentRepo.Update(component); err != nil {
		log.Printf("更新活动组件状态失败，组件ID: %d, 状态: %d, 错误: %v\n", id, status, err)
		return dto.Error[dto.ActivityComponentDto]("更新状态失败", http.StatusInternalServerError)
	}

	componentDto := s.convertComponentToDto(component)
	return dto.Success(componentDto)
}

// CopyActivityComponent 复制活动组件
func (s *ActivityComponentService) CopyActivityComponent(id uint64) dto.ApiData[dto.ActivityComponentDto] {
	component, err := s.componentRepo.FindByID(id)
	if err != nil {
		log.Printf("复制活动组件失败，组件ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityComponentDto]("活动组件不存在", http.StatusNotFound)
	}

	newComponent := &entity.ActivityComponent{
		Name:        component.Name + "_副本",
		Description: component.Description,
		Fields:      component.Fields,
		Status:      0,
	}

	if err := s.componentRepo.Create(newComponent); err != nil {
		log.Printf("复制活动组件失败: %v\n", err)
		return dto.Error[dto.ActivityComponentDto]("复制活动组件失败", http.StatusInternalServerError)
	}

	componentDto := s.convertComponentToDto(newComponent)
	return dto.Success(componentDto)
}

// convertComponentToDto 转换活动组件实体为DTO
func (s *ActivityComponentService) convertComponentToDto(component *entity.ActivityComponent) dto.ActivityComponentDto {
	fields := s.parseFields(component.Fields)

	return dto.ActivityComponentDto{
		ID:          component.ID,
		Name:        component.Name,
		Description: component.Description,
		Fields:      fields,
		Status:      component.Status,
		CreateTime:  util.Format(&component.CreateTime),
		UpdateTime:  util.Format(&component.UpdateTime),
	}
}

// serializeFields 序列化字段/组件配置为JSON字符串
func (s *ActivityComponentService) serializeFields(fields []dto.ActivityComponentFieldDto) (*string, error) {
	if fields == nil {
		return nil, nil
	}

	jsonBytes, err := json.Marshal(fields)
	if err != nil {
		return nil, err
	}

	jsonStr := string(jsonBytes)
	return &jsonStr, nil
}

// parseFields 解析字段/组件配置JSON字符串
func (s *ActivityComponentService) parseFields(fieldsJSON *string) []dto.ActivityComponentFieldDto {
	if fieldsJSON == nil || *fieldsJSON == "" {
		return nil
	}

	var fields []dto.ActivityComponentFieldDto
	if err := json.Unmarshal([]byte(*fieldsJSON), &fields); err != nil {
		log.Printf("解析字段/组件配置失败: %v\n", err)
		return nil
	}

	return fields
}
