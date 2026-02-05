package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ActivityTemplateFormDto
import io.infra.market.vertx.dto.ActivityTemplateQueryDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ActivityTemplateService
import io.infra.market.vertx.util.QueryParamUtil
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.infra.market.vertx.extensions.queryParamsTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 活动模板路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class ActivityTemplateRouter @Inject constructor(
    private val activityTemplateService: ActivityTemplateService
) {
    
    fun mount(router: Router, vertx: Vertx) {
        val templateRouter = Router.router(vertx)
        templateRouter.route().handler(AuthMiddleware.create())
        
        templateRouter.get("/activity/template/list").coroutineHandler(vertx) { ctx -> handleGetActivityTemplates(ctx) }
        templateRouter.get("/activity/template/all").coroutineHandler(vertx) { ctx -> handleGetAllActivityTemplates(ctx) }
        templateRouter.get("/activity/template/:id").coroutineHandler(vertx) { ctx -> handleGetActivityTemplate(ctx) }
        templateRouter.post("/activity/template").coroutineHandler(vertx) { ctx -> handleCreateActivityTemplate(ctx) }
        templateRouter.put("/activity/template/:id").coroutineHandler(vertx) { ctx -> handleUpdateActivityTemplate(ctx) }
        templateRouter.delete("/activity/template/:id").coroutineHandler(vertx) { ctx -> handleDeleteActivityTemplate(ctx) }
        templateRouter.put("/activity/template/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        templateRouter.post("/activity/template/:id/copy").coroutineHandler(vertx) { ctx -> handleCopyActivityTemplate(ctx) }
        
        router.route("/*").subRouter(templateRouter)
    }
    
    private suspend fun handleGetActivityTemplates(ctx: RoutingContext) {
        val query = ctx.queryParamsTo<ActivityTemplateQueryDto>(validate = true)
        val result = activityTemplateService.getActivityTemplates(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetAllActivityTemplates(ctx: RoutingContext) {
        val result = activityTemplateService.getAllActivityTemplates()
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetActivityTemplate(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("模板ID无效")
        
        val result = activityTemplateService.getActivityTemplate(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreateActivityTemplate(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ActivityTemplateFormDto>(validate = true)
        val result = activityTemplateService.createActivityTemplate(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateActivityTemplate(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("模板ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ActivityTemplateFormDto>(validate = true)
        val result = activityTemplateService.updateActivityTemplate(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDeleteActivityTemplate(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("模板ID无效")
        
        val result = activityTemplateService.deleteActivityTemplate(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("模板ID无效")
        
        val status = QueryParamUtil.getStatus(ctx)
        
        val result = activityTemplateService.updateActivityTemplateStatus(id, status)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCopyActivityTemplate(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("模板ID无效")
        
        val result = activityTemplateService.copyActivityTemplate(id)
        ResponseUtil.sendResponse(ctx, result)
    }
}
