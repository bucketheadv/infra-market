package router

import (
	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/container"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// SetupRouter 设置路由（使用依赖注入）
func SetupRouter(db *gorm.DB, cfg *config.Config) (*gin.Engine, error) {
	c, err := container.NewContainer(db, cfg)
	if err != nil {
		return nil, err
	}
	return c.SetupRouter()
}
