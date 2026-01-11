package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.DashboardService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 仪表盘路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class DashboardRouter @Inject constructor(
    private val dashboardService: DashboardService
) {
    
    fun mount(router: Router, vertx: Vertx) {
        val dashboardRouter = Router.router(vertx)
        dashboardRouter.route().handler(AuthMiddleware.create())
        
        dashboardRouter.get("/dashboard/data").coroutineHandler(vertx) { ctx -> handleGetDashboardData(ctx) }
        
        router.route("/*").subRouter(dashboardRouter)
    }
    
    private suspend fun handleGetDashboardData(ctx: RoutingContext) {
        val result = dashboardService.getDashboardData()
        ResponseUtil.sendResponse(ctx, result)
    }
}

