package controller

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type ActivityComponentController struct {
	componentService *service.ActivityComponentService
}

func NewActivityComponentController(componentService *service.ActivityComponentService) *ActivityComponentController {
	return &ActivityComponentController{componentService: componentService}
}

// List 获取活动组件列表
func (c *ActivityComponentController) List(ctx *gin.Context) {
	var query dto.ActivityComponentQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.componentService.GetActivityComponents(query)
	ctx.JSON(200, result)
}

// GetAll 获取所有启用的活动组件列表
func (c *ActivityComponentController) GetAll(ctx *gin.Context) {
	result := c.componentService.GetAllActivityComponents()
	if result.Code == 200 {
		// 只返回启用的组件
		components := result.Data
		enabledComponents := make([]dto.ActivityComponentDto, 0)
		for _, component := range components {
			if component.Status == 1 {
				enabledComponents = append(enabledComponents, component)
			}
		}
		ctx.JSON(200, dto.Success(enabledComponents))
		return
	}
	ctx.JSON(200, result)
}

// Detail 获取活动组件详情
func (c *ActivityComponentController) Detail(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的组件ID", 400))
		return
	}

	result := c.componentService.GetActivityComponent(uriParam.ID)
	ctx.JSON(200, result)
}

// Create 创建活动组件
func (c *ActivityComponentController) Create(ctx *gin.Context) {
	var form dto.ActivityComponentFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.componentService.CreateActivityComponent(form)
	ctx.JSON(200, result)
}

// Update 更新活动组件
func (c *ActivityComponentController) Update(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的组件ID", 400))
		return
	}

	var form dto.ActivityComponentFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.componentService.UpdateActivityComponent(uriParam.ID, form)
	ctx.JSON(200, result)
}

// Delete 删除活动组件
func (c *ActivityComponentController) Delete(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的组件ID", 400))
		return
	}

	result := c.componentService.DeleteActivityComponent(uriParam.ID)
	ctx.JSON(200, result)
}

// UpdateStatus 更新活动组件状态
func (c *ActivityComponentController) UpdateStatus(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的组件ID", 400))
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

	result := c.componentService.UpdateActivityComponentStatus(uriParam.ID, status)
	ctx.JSON(200, result)
}

// Copy 复制活动组件
func (c *ActivityComponentController) Copy(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的组件ID", 400))
		return
	}

	result := c.componentService.CopyActivityComponent(uriParam.ID)
	ctx.JSON(200, result)
}
