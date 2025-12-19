package controller

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/middleware"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type AuthController struct {
	authService *service.AuthService
}

func NewAuthController(authService *service.AuthService) *AuthController {
	return &AuthController{authService: authService}
}

// Login 用户登录
func (c *AuthController) Login(ctx *gin.Context) {
	var req dto.LoginRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.authService.Login(req)
	ctx.JSON(200, result)
}

// GetCurrentUser 获取当前用户信息
func (c *AuthController) GetCurrentUser(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[interface{}]("未登录", 401))
		return
	}

	result := c.authService.GetCurrentUser(uid)
	ctx.JSON(200, result)
}

// GetUserMenus 获取用户菜单
func (c *AuthController) GetUserMenus(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[interface{}]("未登录", 401))
		return
	}

	result := c.authService.GetUserMenus(uid)
	ctx.JSON(200, result)
}

// RefreshToken 刷新token
func (c *AuthController) RefreshToken(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[interface{}]("未登录", 401))
		return
	}

	result := c.authService.RefreshToken(uid)
	ctx.JSON(200, result)
}

// Logout 用户登出
func (c *AuthController) Logout(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[interface{}]("未登录", 401))
		return
	}

	result := c.authService.Logout(uid)
	ctx.JSON(200, result)
}

// ChangePassword 修改密码
func (c *AuthController) ChangePassword(ctx *gin.Context) {
	uid, ok := middleware.GetUIDFromContext(ctx)
	if !ok {
		ctx.JSON(401, dto.Error[interface{}]("未登录", 401))
		return
	}

	var req dto.ChangePasswordRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.authService.ChangePassword(uid, req)
	ctx.JSON(200, result)
}
