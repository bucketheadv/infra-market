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

type ActivityTemplateService struct {
	db           *gorm.DB
	templateRepo *repository.ActivityTemplateRepository
}

func NewActivityTemplateService(
	db *gorm.DB,
	templateRepo *repository.ActivityTemplateRepository,
) *ActivityTemplateService {
	return &ActivityTemplateService{
		db:           db,
		templateRepo: templateRepo,
	}
}

// GetActivityTemplates 获取活动模板列表
func (s *ActivityTemplateService) GetActivityTemplates(query dto.ActivityTemplateQueryDto) dto.ApiData[dto.PageResult[dto.ActivityTemplateDto]] {
	templates, total, err := s.templateRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.ActivityTemplateDto]]("查询失败", http.StatusInternalServerError)
	}

	// 转换为DTO
	templateDtos := make([]dto.ActivityTemplateDto, len(templates))
	for i, template := range templates {
		templateDtos[i] = s.convertTemplateToDto(&template)
	}

	result := dto.PageResult[dto.ActivityTemplateDto]{
		Records: templateDtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

// GetAllActivityTemplates 获取所有活动模板
func (s *ActivityTemplateService) GetAllActivityTemplates() dto.ApiData[[]dto.ActivityTemplateDto] {
	templates, err := s.templateRepo.List()
	if err != nil {
		return dto.Error[[]dto.ActivityTemplateDto]("查询失败", http.StatusInternalServerError)
	}

	// 转换为DTO
	templateDtos := make([]dto.ActivityTemplateDto, len(templates))
	for i, template := range templates {
		templateDtos[i] = s.convertTemplateToDto(&template)
	}

	return dto.Success(templateDtos)
}

// GetActivityTemplate 获取活动模板详情
func (s *ActivityTemplateService) GetActivityTemplate(id uint64) dto.ApiData[dto.ActivityTemplateDto] {
	template, err := s.templateRepo.FindByID(id)
	if err != nil {
		log.Printf("获取活动模板详情失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityTemplateDto]("活动模板不存在", http.StatusNotFound)
	}

	templateDto := s.convertTemplateToDto(template)
	return dto.Success(templateDto)
}

// CreateActivityTemplate 创建活动模板
func (s *ActivityTemplateService) CreateActivityTemplate(form dto.ActivityTemplateFormDto) dto.ApiData[dto.ActivityTemplateDto] {
	// 序列化字段配置
	fieldsJSON, err := s.serializeFields(form.Fields)
	if err != nil {
		log.Printf("序列化字段配置失败: %v\n", err)
		return dto.Error[dto.ActivityTemplateDto]("字段配置格式错误", http.StatusBadRequest)
	}

	status := 1
	if form.Status != nil {
		status = *form.Status
	}

	template := &entity.ActivityTemplate{
		Name:        form.Name,
		Description: form.Description,
		Fields:      fieldsJSON,
		Status:      status,
	}

	if err := s.templateRepo.Create(template); err != nil {
		log.Printf("创建活动模板失败: %v\n", err)
		return dto.Error[dto.ActivityTemplateDto]("创建活动模板失败", http.StatusInternalServerError)
	}

	templateDto := s.convertTemplateToDto(template)
	return dto.Success(templateDto)
}

// UpdateActivityTemplate 更新活动模板
func (s *ActivityTemplateService) UpdateActivityTemplate(id uint64, form dto.ActivityTemplateFormDto) dto.ApiData[dto.ActivityTemplateDto] {
	template, err := s.templateRepo.FindByID(id)
	if err != nil {
		log.Printf("更新活动模板失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityTemplateDto]("活动模板不存在", http.StatusNotFound)
	}

	// 序列化字段配置
	var fieldsJSON *string
	if form.Fields != nil {
		jsonStr, err := s.serializeFields(form.Fields)
		if err != nil {
			log.Printf("序列化字段配置失败: %v\n", err)
			return dto.Error[dto.ActivityTemplateDto]("字段配置格式错误", http.StatusBadRequest)
		}
		fieldsJSON = jsonStr
	}

	template.Name = form.Name
	if form.Description != nil {
		template.Description = form.Description
	}
	if fieldsJSON != nil {
		template.Fields = fieldsJSON
	}
	if form.Status != nil {
		template.Status = *form.Status
	}

	if err := s.templateRepo.Update(template); err != nil {
		log.Printf("更新活动模板失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityTemplateDto]("更新活动模板失败", http.StatusInternalServerError)
	}

	templateDto := s.convertTemplateToDto(template)
	return dto.Success(templateDto)
}

// DeleteActivityTemplate 删除活动模板
func (s *ActivityTemplateService) DeleteActivityTemplate(id uint64) dto.ApiData[any] {
	_, err := s.templateRepo.FindByID(id)
	if err != nil {
		log.Printf("删除活动模板失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("活动模板不存在", http.StatusNotFound)
	}

	if err := s.templateRepo.Delete(id); err != nil {
		log.Printf("删除活动模板失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除活动模板失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// UpdateActivityTemplateStatus 更新活动模板状态
func (s *ActivityTemplateService) UpdateActivityTemplateStatus(id uint64, status int) dto.ApiData[dto.ActivityTemplateDto] {
	template, err := s.templateRepo.FindByID(id)
	if err != nil {
		log.Printf("更新活动模板状态失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityTemplateDto]("活动模板不存在", http.StatusNotFound)
	}

	template.Status = status
	if err := s.templateRepo.Update(template); err != nil {
		log.Printf("更新活动模板状态失败，模板ID: %d, 状态: %d, 错误: %v\n", id, status, err)
		return dto.Error[dto.ActivityTemplateDto]("更新状态失败", http.StatusInternalServerError)
	}

	templateDto := s.convertTemplateToDto(template)
	return dto.Success(templateDto)
}

// CopyActivityTemplate 复制活动模板
func (s *ActivityTemplateService) CopyActivityTemplate(id uint64) dto.ApiData[dto.ActivityTemplateDto] {
	template, err := s.templateRepo.FindByID(id)
	if err != nil {
		log.Printf("复制活动模板失败，模板ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityTemplateDto]("活动模板不存在", http.StatusNotFound)
	}

	newTemplate := &entity.ActivityTemplate{
		Name:        template.Name + "_副本",
		Description: template.Description,
		Fields:      template.Fields,
		Status:      0,
	}

	if err := s.templateRepo.Create(newTemplate); err != nil {
		log.Printf("复制活动模板失败: %v\n", err)
		return dto.Error[dto.ActivityTemplateDto]("复制活动模板失败", http.StatusInternalServerError)
	}

	templateDto := s.convertTemplateToDto(newTemplate)
	return dto.Success(templateDto)
}

// convertTemplateToDto 转换活动模板实体为DTO
func (s *ActivityTemplateService) convertTemplateToDto(template *entity.ActivityTemplate) dto.ActivityTemplateDto {
	fields := s.parseFields(template.Fields)

	return dto.ActivityTemplateDto{
		ID:          template.ID,
		Name:        template.Name,
		Description: template.Description,
		Fields:      fields,
		Status:      template.Status,
		CreateTime:  util.Format(&template.CreateTime),
		UpdateTime:  util.Format(&template.UpdateTime),
	}
}

// serializeFields 序列化字段配置为JSON字符串
func (s *ActivityTemplateService) serializeFields(fields []dto.ActivityTemplateFieldDto) (*string, error) {
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

// parseFields 解析字段配置JSON字符串
func (s *ActivityTemplateService) parseFields(fieldsJSON *string) []dto.ActivityTemplateFieldDto {
	if fieldsJSON == nil || *fieldsJSON == "" {
		return nil
	}

	var fields []dto.ActivityTemplateFieldDto
	if err := json.Unmarshal([]byte(*fieldsJSON), &fields); err != nil {
		log.Printf("解析字段配置失败: %v\n", err)
		return nil
	}

	return fields
}
