package middleware

import (
	"net/http"
	"strings"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/bucketheadv/infra-market/internal/util"
	"github.com/gin-gonic/gin"
)

// AuthMiddleware 认证中间件
func AuthMiddleware(tokenService *service.TokenService) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 获取token
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.JSON(http.StatusUnauthorized, dto.Error[any]("登录已过期，请重新登录", 401))
			c.Abort()
			return
		}

		token := strings.TrimPrefix(authHeader, "Bearer ")
		if token == "" {
			c.JSON(http.StatusUnauthorized, dto.Error[any]("登录已过期，请重新登录", 401))
			c.Abort()
			return
		}

		// 验证token
		if !tokenService.ValidateToken(token) {
			c.JSON(http.StatusUnauthorized, dto.Error[any]("登录已过期，请重新登录", 401))
			c.Abort()
			return
		}

		// 将token保存到context，供后续使用
		c.Set("token", token)
		c.Next()
	}
}

// GetUIDFromContext 从Context获取用户ID
func GetUIDFromContext(c *gin.Context) (uint64, bool) {
	token, exists := c.Get("token")
	if !exists {
		return 0, false
	}

	tokenStr, ok := token.(string)
	if !ok {
		return 0, false
	}

	// 从token中解析UID
	uid, err := util.GetUIDFromToken(tokenStr)
	if err != nil {
		return 0, false
	}

	return uid, true
}
