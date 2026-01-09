package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.DashboardService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 仪表盘路由
 */
class DashboardRouter(private val dashboardService: DashboardService) {
    
    private val logger = LoggerFactory.getLogger(DashboardRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val dashboardRouter = Router.router(vertx)
        dashboardRouter.route().handler(AuthMiddleware.create())
        
        dashboardRouter.get("/dashboard/data").handler(::handleGetDashboardData)
        
        router.route("/*").subRouter(dashboardRouter)
    }
    
    private fun handleGetDashboardData(ctx: RoutingContext) {
        ResponseUtil.handleFuture(ctx, dashboardService.getDashboardData(), "获取仪表盘数据失败", logger)
    }
}

