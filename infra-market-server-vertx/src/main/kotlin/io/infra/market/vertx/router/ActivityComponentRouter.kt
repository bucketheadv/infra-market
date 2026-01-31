package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ActivityComponentFormDto
import io.infra.market.vertx.dto.ActivityComponentQueryDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ActivityComponentService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.infra.market.vertx.extensions.queryParamsTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 活动组件路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class ActivityComponentRouter @Inject constructor(
    private val activityComponentService: ActivityComponentService
) {
    
    fun mount(router: Router, vertx: Vertx) {
        val componentRouter = Router.router(vertx)
        componentRouter.route().handler(AuthMiddleware.create())
        
        componentRouter.get("/activity/component/list").coroutineHandler(vertx) { ctx -> handleGetActivityComponents(ctx) }
        componentRouter.get("/activity/component/all").coroutineHandler(vertx) { ctx -> handleGetAllActivityComponents(ctx) }
        componentRouter.get("/activity/component/:id").coroutineHandler(vertx) { ctx -> handleGetActivityComponent(ctx) }
        componentRouter.post("/activity/component").coroutineHandler(vertx) { ctx -> handleCreateActivityComponent(ctx) }
        componentRouter.put("/activity/component/:id").coroutineHandler(vertx) { ctx -> handleUpdateActivityComponent(ctx) }
        componentRouter.delete("/activity/component/:id").coroutineHandler(vertx) { ctx -> handleDeleteActivityComponent(ctx) }
        componentRouter.put("/activity/component/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        componentRouter.post("/activity/component/:id/copy").coroutineHandler(vertx) { ctx -> handleCopyActivityComponent(ctx) }
        
        router.route("/*").subRouter(componentRouter)
    }
    
    private suspend fun handleGetActivityComponents(ctx: RoutingContext) {
        val query = ctx.queryParamsTo<ActivityComponentQueryDto>(validate = true)
        val result = activityComponentService.getActivityComponents(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetAllActivityComponents(ctx: RoutingContext) {
        val result = activityComponentService.getAllActivityComponents()
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetActivityComponent(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("组件ID无效")
        
        val result = activityComponentService.getActivityComponent(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreateActivityComponent(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ActivityComponentFormDto>(validate = true)
        val result = activityComponentService.createActivityComponent(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateActivityComponent(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("组件ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ActivityComponentFormDto>(validate = true)
        val result = activityComponentService.updateActivityComponent(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDeleteActivityComponent(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("组件ID无效")
        
        val result = activityComponentService.deleteActivityComponent(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("组件ID无效")
        
        val statusStr = ctx.queryParams().get("status")
        if (statusStr.isNullOrBlank()) {
            throw IllegalArgumentException("状态参数不能为空")
        }
        
        val status = when (statusStr) {
            "1" -> 1
            "0" -> 0
            else -> throw IllegalArgumentException("状态值只能是0或1")
        }
        
        val result = activityComponentService.updateActivityComponentStatus(id, status)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCopyActivityComponent(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("组件ID无效")
        
        val result = activityComponentService.copyActivityComponent(id)
        ResponseUtil.sendResponse(ctx, result)
    }
}
