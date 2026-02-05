package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ActivityFormDto
import io.infra.market.vertx.dto.ActivityQueryDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ActivityService
import io.infra.market.vertx.util.QueryParamUtil
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.infra.market.vertx.extensions.queryParamsTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 活动路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class ActivityRouter @Inject constructor(
    private val activityService: ActivityService
) {
    
    fun mount(router: Router, vertx: Vertx) {
        val activityRouter = Router.router(vertx)
        activityRouter.route().handler(AuthMiddleware.create())
        
        activityRouter.get("/activity/list").coroutineHandler(vertx) { ctx -> handleGetActivities(ctx) }
        activityRouter.get("/activity/:id").coroutineHandler(vertx) { ctx -> handleGetActivity(ctx) }
        activityRouter.post("/activity").coroutineHandler(vertx) { ctx -> handleCreateActivity(ctx) }
        activityRouter.put("/activity/:id").coroutineHandler(vertx) { ctx -> handleUpdateActivity(ctx) }
        activityRouter.delete("/activity/:id").coroutineHandler(vertx) { ctx -> handleDeleteActivity(ctx) }
        activityRouter.put("/activity/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        
        router.route("/*").subRouter(activityRouter)
    }
    
    private suspend fun handleGetActivities(ctx: RoutingContext) {
        val query = ctx.queryParamsTo<ActivityQueryDto>(validate = true)
        val result = activityService.getActivities(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetActivity(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("活动ID无效")
        
        val result = activityService.getActivity(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreateActivity(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ActivityFormDto>(validate = true)
        val result = activityService.createActivity(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateActivity(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("活动ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ActivityFormDto>(validate = true)
        val result = activityService.updateActivity(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDeleteActivity(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("活动ID无效")
        
        val result = activityService.deleteActivity(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("活动ID无效")
        
        val status = QueryParamUtil.getStatus(ctx)
        
        val result = activityService.updateActivityStatus(id, status)
        ResponseUtil.sendResponse(ctx, result)
    }
}
