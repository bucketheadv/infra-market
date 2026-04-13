package controller

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type ActivityController struct {
	activityService *service.ActivityService
}

func NewActivityController(activityService *service.ActivityService) *ActivityController {
	return &ActivityController{activityService: activityService}
}

// List 获取活动列表
func (c *ActivityController) List(ctx *gin.Context) {
	var query dto.ActivityQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.activityService.GetActivities(query)
	ctx.JSON(200, result)
}

// Detail 获取活动详情
func (c *ActivityController) Detail(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的活动ID", 400))
		return
	}

	result := c.activityService.GetActivity(uriParam.ID)
	ctx.JSON(200, result)
}

// Create 创建活动
func (c *ActivityController) Create(ctx *gin.Context) {
	var form dto.ActivityFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.activityService.CreateActivity(form)
	ctx.JSON(200, result)
}

// Update 更新活动
func (c *ActivityController) Update(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的活动ID", 400))
		return
	}

	var form dto.ActivityFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.activityService.UpdateActivity(uriParam.ID, form)
	ctx.JSON(200, result)
}

// Delete 删除活动
func (c *ActivityController) Delete(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的活动ID", 400))
		return
	}

	result := c.activityService.DeleteActivity(uriParam.ID)
	ctx.JSON(200, result)
}

// UpdateStatus 更新活动状态
func (c *ActivityController) UpdateStatus(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的活动ID", 400))
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

	result := c.activityService.UpdateActivityStatus(uriParam.ID, status)
	ctx.JSON(200, result)
}
