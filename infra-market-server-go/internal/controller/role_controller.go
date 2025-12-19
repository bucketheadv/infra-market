package controller

import (
	"strconv"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type RoleController struct {
	roleService *service.RoleService
}

func NewRoleController(roleService *service.RoleService) *RoleController {
	return &RoleController{roleService: roleService}
}

// GetRoles 获取角色列表
func (c *RoleController) GetRoles(ctx *gin.Context) {
	var query dto.RoleQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.roleService.GetRoles(query)
	ctx.JSON(200, result)
}

// GetAllRoles 获取所有激活的角色
func (c *RoleController) GetAllRoles(ctx *gin.Context) {
	result := c.roleService.GetAllRoles()
	ctx.JSON(200, result)
}

// GetRole 获取角色详情
func (c *RoleController) GetRole(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的角色ID", 400))
		return
	}

	result := c.roleService.GetRole(id)
	ctx.JSON(200, result)
}

// CreateRole 创建角色
func (c *RoleController) CreateRole(ctx *gin.Context) {
	var form dto.RoleFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.roleService.CreateRole(form)
	ctx.JSON(200, result)
}

// UpdateRole 更新角色
func (c *RoleController) UpdateRole(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的角色ID", 400))
		return
	}

	var form dto.RoleFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.roleService.UpdateRole(id, form)
	ctx.JSON(200, result)
}

// DeleteRole 删除角色
func (c *RoleController) DeleteRole(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的角色ID", 400))
		return
	}

	result := c.roleService.DeleteRole(id)
	ctx.JSON(200, result)
}

// UpdateRoleStatus 更新角色状态
func (c *RoleController) UpdateRoleStatus(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的角色ID", 400))
		return
	}

	var statusDto dto.StatusUpdateDto
	if err := ctx.ShouldBindJSON(&statusDto); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.roleService.UpdateRoleStatus(id, statusDto.Status)
	ctx.JSON(200, result)
}

// BatchDeleteRoles 批量删除角色
func (c *RoleController) BatchDeleteRoles(ctx *gin.Context) {
	var req dto.BatchRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	// 这里需要实现批量删除逻辑
	ctx.JSON(200, dto.Success[interface{}](nil))
}
