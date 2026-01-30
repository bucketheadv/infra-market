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

type ActivityService struct {
	db                *gorm.DB
	activityRepo      *repository.ActivityRepository
	templateRepo      *repository.ActivityTemplateRepository
}

func NewActivityService(
	db *gorm.DB,
	activityRepo *repository.ActivityRepository,
	templateRepo *repository.ActivityTemplateRepository,
) *ActivityService {
	return &ActivityService{
		db:                db,
		activityRepo:      activityRepo,
		templateRepo:      templateRepo,
	}
}

// GetActivities 获取活动列表
func (s *ActivityService) GetActivities(query dto.ActivityQueryDto) dto.ApiData[dto.PageResult[dto.ActivityDto]] {
	activities, total, err := s.activityRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.ActivityDto]]("查询失败", http.StatusInternalServerError)
	}

	// 批量获取模板信息
	templateIDs := make(map[uint64]bool)
	for _, activity := range activities {
		templateIDs[activity.TemplateID] = true
	}

	templateMap := make(map[uint64]*entity.ActivityTemplate)
	for templateID := range templateIDs {
		template, err := s.templateRepo.FindByID(templateID)
		if err == nil {
			templateMap[templateID] = template
		}
	}

	// 转换为DTO
	activityDtos := make([]dto.ActivityDto, len(activities))
	for i, activity := range activities {
		template := templateMap[activity.TemplateID]
		var templateName *string
		if template != nil {
			templateName = &template.Name
		}
		activityDtos[i] = s.convertActivityToDto(&activity, templateName)
	}

	result := dto.PageResult[dto.ActivityDto]{
		Records: activityDtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

// GetActivity 获取活动详情
func (s *ActivityService) GetActivity(id uint64) dto.ApiData[dto.ActivityDto] {
	activity, err := s.activityRepo.FindByID(id)
	if err != nil {
		log.Printf("获取活动详情失败，活动ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityDto]("活动不存在", http.StatusNotFound)
	}

	// 获取模板信息
	var templateName *string
	template, err := s.templateRepo.FindByID(activity.TemplateID)
	if err == nil {
		templateName = &template.Name
	}

	activityDto := s.convertActivityToDto(activity, templateName)
	return dto.Success(activityDto)
}

// CreateActivity 创建活动
func (s *ActivityService) CreateActivity(form dto.ActivityFormDto) dto.ApiData[dto.ActivityDto] {
	// 验证模板是否存在
	template, err := s.templateRepo.FindByID(form.TemplateID)
	if err != nil {
		return dto.Error[dto.ActivityDto]("活动模板不存在", http.StatusBadRequest)
	}

	// 验证配置数据
	if err := s.validateConfigData(form.ConfigData, template); err != nil {
		return dto.Error[dto.ActivityDto](err.Error(), http.StatusBadRequest)
	}

	// 序列化配置数据
	configDataJSON, err := s.serializeConfigData(form.ConfigData)
	if err != nil {
		log.Printf("序列化配置数据失败: %v\n", err)
		return dto.Error[dto.ActivityDto]("配置数据格式错误", http.StatusBadRequest)
	}

	status := 1
	if form.Status != nil {
		status = *form.Status
	}

	activity := &entity.Activity{
		Name:        form.Name,
		Description: form.Description,
		TemplateID:  form.TemplateID,
		ConfigData:  configDataJSON,
		Status:      status,
	}

	if err := s.activityRepo.Create(activity); err != nil {
		log.Printf("创建活动失败: %v\n", err)
		return dto.Error[dto.ActivityDto]("创建活动失败", http.StatusInternalServerError)
	}

	activityDto := s.convertActivityToDto(activity, &template.Name)
	return dto.Success(activityDto)
}

// UpdateActivity 更新活动
func (s *ActivityService) UpdateActivity(id uint64, form dto.ActivityFormDto) dto.ApiData[dto.ActivityDto] {
	activity, err := s.activityRepo.FindByID(id)
	if err != nil {
		log.Printf("更新活动失败，活动ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityDto]("活动不存在", http.StatusNotFound)
	}

	// 验证模板是否存在
	if form.TemplateID != 0 {
		template, err := s.templateRepo.FindByID(form.TemplateID)
		if err != nil {
			return dto.Error[dto.ActivityDto]("活动模板不存在", http.StatusBadRequest)
		}

		// 验证配置数据
		if err := s.validateConfigData(form.ConfigData, template); err != nil {
			return dto.Error[dto.ActivityDto](err.Error(), http.StatusBadRequest)
		}
	}

	// 序列化配置数据
	var configDataJSON *string
	if form.ConfigData != nil {
		jsonStr, err := s.serializeConfigData(form.ConfigData)
		if err != nil {
			log.Printf("序列化配置数据失败: %v\n", err)
			return dto.Error[dto.ActivityDto]("配置数据格式错误", http.StatusBadRequest)
		}
		configDataJSON = jsonStr
	}

	activity.Name = form.Name
	if form.Description != nil {
		activity.Description = form.Description
	}
	if form.TemplateID != 0 {
		activity.TemplateID = form.TemplateID
	}
	if configDataJSON != nil {
		activity.ConfigData = configDataJSON
	}
	if form.Status != nil {
		activity.Status = *form.Status
	}

	if err := s.activityRepo.Update(activity); err != nil {
		log.Printf("更新活动失败，活动ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityDto]("更新活动失败", http.StatusInternalServerError)
	}

	// 获取模板名称
	var templateName *string
	template, err := s.templateRepo.FindByID(activity.TemplateID)
	if err == nil {
		templateName = &template.Name
	}

	activityDto := s.convertActivityToDto(activity, templateName)
	return dto.Success(activityDto)
}

// DeleteActivity 删除活动
func (s *ActivityService) DeleteActivity(id uint64) dto.ApiData[any] {
	_, err := s.activityRepo.FindByID(id)
	if err != nil {
		log.Printf("删除活动失败，活动ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("活动不存在", http.StatusNotFound)
	}

	if err := s.activityRepo.Delete(id); err != nil {
		log.Printf("删除活动失败，活动ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除活动失败", http.StatusInternalServerError)
	}

	return dto.Success[any](nil)
}

// UpdateActivityStatus 更新活动状态
func (s *ActivityService) UpdateActivityStatus(id uint64, status int) dto.ApiData[dto.ActivityDto] {
	activity, err := s.activityRepo.FindByID(id)
	if err != nil {
		log.Printf("更新活动状态失败，活动ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ActivityDto]("活动不存在", http.StatusNotFound)
	}

	activity.Status = status
	if err := s.activityRepo.Update(activity); err != nil {
		log.Printf("更新活动状态失败，活动ID: %d, 状态: %d, 错误: %v\n", id, status, err)
		return dto.Error[dto.ActivityDto]("更新状态失败", http.StatusInternalServerError)
	}

	// 获取模板名称
	var templateName *string
	template, err := s.templateRepo.FindByID(activity.TemplateID)
	if err == nil {
		templateName = &template.Name
	}

	activityDto := s.convertActivityToDto(activity, templateName)
	return dto.Success(activityDto)
}

// convertActivityToDto 转换活动实体为DTO
func (s *ActivityService) convertActivityToDto(activity *entity.Activity, templateName *string) dto.ActivityDto {
	configData := s.parseConfigData(activity.ConfigData)

	return dto.ActivityDto{
		ID:           activity.ID,
		Name:         activity.Name,
		Description:  activity.Description,
		TemplateID:   activity.TemplateID,
		TemplateName: templateName,
		ConfigData:   configData,
		Status:       activity.Status,
		CreateTime:   util.Format(&activity.CreateTime),
		UpdateTime:   util.Format(&activity.UpdateTime),
	}
}

// serializeConfigData 序列化配置数据为JSON字符串
func (s *ActivityService) serializeConfigData(configData map[string]any) (*string, error) {
	if configData == nil {
		return nil, nil
	}

	jsonBytes, err := json.Marshal(configData)
	if err != nil {
		return nil, err
	}

	jsonStr := string(jsonBytes)
	return &jsonStr, nil
}

// parseConfigData 解析配置数据JSON字符串
func (s *ActivityService) parseConfigData(configDataJSON *string) map[string]any {
	if configDataJSON == nil || *configDataJSON == "" {
		return nil
	}

	var configData map[string]any
	if err := json.Unmarshal([]byte(*configDataJSON), &configData); err != nil {
		log.Printf("解析配置数据失败: %v\n", err)
		return nil
	}

	return configData
}

// validateConfigData 验证配置数据是否符合模板的字段配置
func (s *ActivityService) validateConfigData(configData map[string]any, template *entity.ActivityTemplate) error {
	if template.Fields == nil || *template.Fields == "" {
		return nil
	}

	var fields []dto.ActivityTemplateFieldDto
	if err := json.Unmarshal([]byte(*template.Fields), &fields); err != nil {
		log.Printf("解析模板字段配置失败: %v\n", err)
		return nil
	}

	// 验证必填字段
	for _, field := range fields {
		if field.Required != nil && *field.Required {
			fieldName := field.Name
			if fieldName == "" {
				continue
			}

			value, exists := configData[fieldName]
			if !exists || value == nil {
				displayName := fieldName
				if field.Label != nil && *field.Label != "" {
					displayName = *field.Label + "（" + fieldName + "）"
				}
				return &ValidationError{Message: "字段 " + displayName + " 为必填项，不能为空"}
			}

			// 检查字符串是否为空
			if strValue, ok := value.(string); ok && strValue == "" {
				displayName := fieldName
				if field.Label != nil && *field.Label != "" {
					displayName = *field.Label + "（" + fieldName + "）"
				}
				return &ValidationError{Message: "字段 " + displayName + " 为必填项，不能为空"}
			}

			// 检查数组是否为空
			if arrValue, ok := value.([]any); ok && len(arrValue) == 0 {
				displayName := fieldName
				if field.Label != nil && *field.Label != "" {
					displayName = *field.Label + "（" + fieldName + "）"
				}
				return &ValidationError{Message: "字段 " + displayName + " 为必填项，不能为空"}
			}
		}
	}

	return nil
}

// ValidationError 验证错误
type ValidationError struct {
	Message string
}

func (e *ValidationError) Error() string {
	return e.Message
}
