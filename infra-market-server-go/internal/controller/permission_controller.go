package controller

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type PermissionController struct {
	permissionService *service.PermissionService
}

func NewPermissionController(permissionService *service.PermissionService) *PermissionController {
	return &PermissionController{permissionService: permissionService}
}

// GetPermissions 获取权限列表
func (c *PermissionController) GetPermissions(ctx *gin.Context) {
	var query dto.PermissionQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.permissionService.GetPermissions(query)
	ctx.JSON(200, result)
}

// GetPermissionTree 获取权限树
func (c *PermissionController) GetPermissionTree(ctx *gin.Context) {
	result := c.permissionService.GetPermissionTree()
	ctx.JSON(200, result)
}

// GetPermission 获取权限详情
func (c *PermissionController) GetPermission(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的权限ID", 400))
		return
	}

	result := c.permissionService.GetPermission(uriParam.ID)
	ctx.JSON(200, result)
}

// CreatePermission 创建权限
func (c *PermissionController) CreatePermission(ctx *gin.Context) {
	var form dto.PermissionFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.permissionService.CreatePermission(form)
	ctx.JSON(200, result)
}

// UpdatePermission 更新权限
func (c *PermissionController) UpdatePermission(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的权限ID", 400))
		return
	}

	var form dto.PermissionFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.permissionService.UpdatePermission(uriParam.ID, form)
	ctx.JSON(200, result)
}

// DeletePermission 删除权限
func (c *PermissionController) DeletePermission(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的权限ID", 400))
		return
	}

	result := c.permissionService.DeletePermission(uriParam.ID)
	ctx.JSON(200, result)
}

// UpdatePermissionStatus 更新权限状态
func (c *PermissionController) UpdatePermissionStatus(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的权限ID", 400))
		return
	}

	var statusDto dto.StatusUpdateDto
	if err := ctx.ShouldBindJSON(&statusDto); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.permissionService.UpdatePermissionStatus(uriParam.ID, statusDto.Status)
	ctx.JSON(200, result)
}

// BatchDeletePermissions 批量删除权限
func (c *PermissionController) BatchDeletePermissions(ctx *gin.Context) {
	var req dto.BatchRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	// 这里需要实现批量删除逻辑
	ctx.JSON(200, dto.Success[any](nil))
}
