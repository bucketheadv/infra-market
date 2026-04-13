package controller

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type ActivityTemplateController struct {
	templateService *service.ActivityTemplateService
}

func NewActivityTemplateController(templateService *service.ActivityTemplateService) *ActivityTemplateController {
	return &ActivityTemplateController{templateService: templateService}
}

// List 获取活动模板列表
func (c *ActivityTemplateController) List(ctx *gin.Context) {
	var query dto.ActivityTemplateQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.templateService.GetActivityTemplates(query)
	ctx.JSON(200, result)
}

// GetAll 获取所有启用的活动模板列表
func (c *ActivityTemplateController) GetAll(ctx *gin.Context) {
	result := c.templateService.GetAllActivityTemplates()
	if result.Code == 200 {
		// 只返回启用的模板
		templates := result.Data
		enabledTemplates := make([]dto.ActivityTemplateDto, 0)
		for _, template := range templates {
			if template.Status == 1 {
				enabledTemplates = append(enabledTemplates, template)
			}
		}
		ctx.JSON(200, dto.Success(enabledTemplates))
		return
	}
	ctx.JSON(200, result)
}

// Detail 获取活动模板详情
func (c *ActivityTemplateController) Detail(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的模板ID", 400))
		return
	}

	result := c.templateService.GetActivityTemplate(uriParam.ID)
	ctx.JSON(200, result)
}

// Create 创建活动模板
func (c *ActivityTemplateController) Create(ctx *gin.Context) {
	var form dto.ActivityTemplateFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.templateService.CreateActivityTemplate(form)
	ctx.JSON(200, result)
}

// Update 更新活动模板
func (c *ActivityTemplateController) Update(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的模板ID", 400))
		return
	}

	var form dto.ActivityTemplateFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.templateService.UpdateActivityTemplate(uriParam.ID, form)
	ctx.JSON(200, result)
}

// Delete 删除活动模板
func (c *ActivityTemplateController) Delete(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的模板ID", 400))
		return
	}

	result := c.templateService.DeleteActivityTemplate(uriParam.ID)
	ctx.JSON(200, result)
}

// UpdateStatus 更新活动模板状态
func (c *ActivityTemplateController) UpdateStatus(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的模板ID", 400))
		return
	}

	statusStr := ctx.Query("status")
	if statusStr == "" {
		ctx.JSON(400, dto.Error[any]("状态参数不能为空", 400))
		return
	}

	// 将状态字符串转换为整数
	status := 0
	if statusStr == "1" {
		status = 1
	}

	result := c.templateService.UpdateActivityTemplateStatus(uriParam.ID, status)
	ctx.JSON(200, result)
}

// Copy 复制活动模板
func (c *ActivityTemplateController) Copy(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的模板ID", 400))
		return
	}

	result := c.templateService.CopyActivityTemplate(uriParam.ID)
	ctx.JSON(200, result)
}
