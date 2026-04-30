package controller

import (
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
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.FindPage(query)
	ctx.JSON(200, result)
}

// Detail 获取接口详情
func (c *ApiInterfaceController) Detail(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的接口ID", 400))
		return
	}

	result := c.apiInterfaceService.FindByID(uriParam.ID)
	ctx.JSON(200, result)
}

// Create 创建接口
func (c *ApiInterfaceController) Create(ctx *gin.Context) {
	var form dto.ApiInterfaceFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.Save(form)
	ctx.JSON(200, result)
}

// Update 更新接口
func (c *ApiInterfaceController) Update(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的接口ID", 400))
		return
	}

	var form dto.ApiInterfaceFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.Update(uriParam.ID, form)
	ctx.JSON(200, result)
}

// Delete 删除接口
func (c *ApiInterfaceController) Delete(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的接口ID", 400))
		return
	}

	result := c.apiInterfaceService.Delete(uriParam.ID)
	ctx.JSON(200, result)
}

// UpdateStatus 更新接口状态
func (c *ApiInterfaceController) UpdateStatus(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的接口ID", 400))
		return
	}

	var query dto.StatusQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.apiInterfaceService.UpdateStatus(uriParam.ID, *query.Status)
	ctx.JSON(200, result)
}

// Copy 复制接口
func (c *ApiInterfaceController) Copy(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的接口ID", 400))
		return
	}

	result := c.apiInterfaceService.Copy(uriParam.ID)
	ctx.JSON(200, result)
}

// GetMostUsed 获取最近最热门的接口
func (c *ApiInterfaceController) GetMostUsed(ctx *gin.Context) {
	var query dto.ApiInterfaceMostUsedQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	days := 30
	if query.Days != nil {
		days = *query.Days
	}

	limit := 5
	if query.Limit != nil {
		limit = *query.Limit
	}

	result := c.apiInterfaceService.FindMostUsedInterfaces(days, limit)
	ctx.JSON(200, result)
}

// Execute 执行接口
func (c *ApiInterfaceController) Execute(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[any]("未登录", 401))
		return
	}

	var req dto.ApiExecuteRequestDto
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	clientIP := ctx.ClientIP()
	userAgent := ctx.GetHeader("User-Agent")

	result := c.apiInterfaceService.Execute(req, uid, clientIP, userAgent)
	ctx.JSON(200, result)
}
