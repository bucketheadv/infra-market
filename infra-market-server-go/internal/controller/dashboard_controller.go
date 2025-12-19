package controller

import (
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type DashboardController struct {
	dashboardService *service.DashboardService
}

func NewDashboardController(dashboardService *service.DashboardService) *DashboardController {
	return &DashboardController{dashboardService: dashboardService}
}

// GetDashboardData 获取仪表盘数据
func (c *DashboardController) GetDashboardData(ctx *gin.Context) {
	result := c.dashboardService.GetDashboardData()
	ctx.JSON(200, result)
}
