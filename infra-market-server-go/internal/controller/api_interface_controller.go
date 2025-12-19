package controller

import (
	"strconv"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/middleware"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type ApiInterfaceController struct {
	apiInterfaceService *service.ApiInterfaceService
}

func NewApiInterfaceController(apiInterfaceService *service.ApiInterfaceService) *ApiInterfaceController {
	return &ApiInterfaceController{apiInterfaceService: apiInterfaceService}
}

// List 获取接口列表
func (c *ApiInterfaceController) List(ctx *gin.Context) {
	var query dto.ApiInterfaceQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.FindPage(query)
	ctx.JSON(200, result)
}

// Detail 获取接口详情
func (c *ApiInterfaceController) Detail(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的接口ID", 400))
		return
	}

	result := c.apiInterfaceService.FindByID(id)
	ctx.JSON(200, result)
}

// Create 创建接口
func (c *ApiInterfaceController) Create(ctx *gin.Context) {
	var form dto.ApiInterfaceFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.Save(form)
	ctx.JSON(200, result)
}

// Update 更新接口
func (c *ApiInterfaceController) Update(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的接口ID", 400))
		return
	}

	var form dto.ApiInterfaceFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.Update(id, form)
	ctx.JSON(200, result)
}

// Delete 删除接口
func (c *ApiInterfaceController) Delete(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的接口ID", 400))
		return
	}

	result := c.apiInterfaceService.Delete(id)
	ctx.JSON(200, result)
}

// UpdateStatus 更新接口状态
func (c *ApiInterfaceController) UpdateStatus(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的接口ID", 400))
		return
	}

	statusStr := ctx.Query("status")
	status, err := strconv.Atoi(statusStr)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的状态值", 400))
		return
	}

	result := c.apiInterfaceService.UpdateStatus(id, status)
	ctx.JSON(200, result)
}

// Copy 复制接口
func (c *ApiInterfaceController) Copy(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的接口ID", 400))
		return
	}

	result := c.apiInterfaceService.Copy(id)
	ctx.JSON(200, result)
}

// GetMostUsed 获取最近最热门的接口
func (c *ApiInterfaceController) GetMostUsed(ctx *gin.Context) {
	daysStr := ctx.DefaultQuery("days", "30")
	limitStr := ctx.DefaultQuery("limit", "5")

	days, err := strconv.Atoi(daysStr)
	if err != nil {
		days = 30
	}

	limit, err := strconv.Atoi(limitStr)
	if err != nil {
		limit = 5
	}

	result := c.apiInterfaceService.FindMostUsedInterfaces(days, limit)
	ctx.JSON(200, result)
}

// Execute 执行接口
func (c *ApiInterfaceController) Execute(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[interface{}]("未登录", 401))
		return
	}

	var req dto.ApiExecuteRequestDto
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	clientIP := ctx.ClientIP()
	userAgent := ctx.GetHeader("User-Agent")

	result := c.apiInterfaceService.Execute(req, uid, clientIP, userAgent)
	ctx.JSON(200, result)
}
