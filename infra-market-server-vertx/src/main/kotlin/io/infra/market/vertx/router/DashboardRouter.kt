package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.DashboardService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.util.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 仪表盘路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class DashboardRouter(private val dashboardService: DashboardService) {
    
    private val logger = LoggerFactory.getLogger(DashboardRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val dashboardRouter = Router.router(vertx)
        dashboardRouter.route().handler(AuthMiddleware.create())
        
        dashboardRouter.get("/dashboard/data").coroutineHandler(vertx) { ctx -> handleGetDashboardData(ctx) }
        
        router.route("/*").subRouter(dashboardRouter)
    }
    
    private suspend fun handleGetDashboardData(ctx: RoutingContext) {
        try {
            val result = dashboardService.getDashboardData()
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取仪表盘数据失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取仪表盘数据失败", 500)
        }
    }
}

