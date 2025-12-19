package controller

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type UserController struct {
	userService *service.UserService
}

func NewUserController(userService *service.UserService) *UserController {
	return &UserController{userService: userService}
}

// GetUsers 获取用户列表
func (c *UserController) GetUsers(ctx *gin.Context) {
	var query dto.UserQueryDto
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.userService.GetUsers(query)
	ctx.JSON(200, result)
}

// GetUser 获取用户详情
func (c *UserController) GetUser(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的用户ID", 400))
		return
	}

	result := c.userService.GetUser(uriParam.ID)
	ctx.JSON(200, result)
}

// CreateUser 创建用户
func (c *UserController) CreateUser(ctx *gin.Context) {
	var form dto.UserFormDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.userService.CreateUser(form)
	ctx.JSON(200, result)
}

// UpdateUser 更新用户
func (c *UserController) UpdateUser(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的用户ID", 400))
		return
	}

	var form dto.UserUpdateDto
	if err := ctx.ShouldBindJSON(&form); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.userService.UpdateUser(uriParam.ID, form)
	ctx.JSON(200, result)
}

// DeleteUser 删除用户
func (c *UserController) DeleteUser(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的用户ID", 400))
		return
	}

	result := c.userService.DeleteUser(uriParam.ID)
	ctx.JSON(200, result)
}

// UpdateUserStatus 更新用户状态
func (c *UserController) UpdateUserStatus(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的用户ID", 400))
		return
	}

	var statusDto dto.StatusUpdateDto
	if err := ctx.ShouldBindJSON(&statusDto); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.userService.UpdateUserStatus(uriParam.ID, statusDto.Status)
	ctx.JSON(200, result)
}

// ResetPassword 重置密码
func (c *UserController) ResetPassword(ctx *gin.Context) {
	var uriParam dto.IDUriParam
	if err := ctx.ShouldBindUri(&uriParam); err != nil {
		ctx.JSON(400, dto.Error[any]("无效的用户ID", 400))
		return
	}

	result := c.userService.ResetPassword(uriParam.ID)
	ctx.JSON(200, result)
}

// BatchDeleteUsers 批量删除用户
func (c *UserController) BatchDeleteUsers(ctx *gin.Context) {
	var req dto.BatchRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[any]("参数校验失败", 400))
		return
	}

	result := c.userService.BatchDeleteUsers(req.IDs)
	ctx.JSON(200, result)
}
