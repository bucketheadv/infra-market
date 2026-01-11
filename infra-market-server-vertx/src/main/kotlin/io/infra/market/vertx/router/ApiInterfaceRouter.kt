package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ApiExecuteRequestDto
import io.infra.market.vertx.dto.ApiInterfaceFormDto
import io.infra.market.vertx.dto.ApiInterfaceQueryDto
import io.infra.market.vertx.dto.MostUsedInterfacesQueryDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.infra.market.vertx.extensions.queryParamsTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 接口管理路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class ApiInterfaceRouter @Inject constructor(
    private val apiInterfaceService: ApiInterfaceService
) {
    
    fun mount(router: Router, vertx: Vertx) {
        val apiInterfaceRouter = Router.router(vertx)
        apiInterfaceRouter.route().handler(AuthMiddleware.create())
        
        apiInterfaceRouter.get("/interface/list").coroutineHandler(vertx) { ctx -> handleList(ctx) }
        apiInterfaceRouter.get("/interface/most/used").coroutineHandler(vertx) { ctx -> handleGetMostUsed(ctx) }
        apiInterfaceRouter.get("/interface/:id").coroutineHandler(vertx) { ctx -> handleDetail(ctx) }
        apiInterfaceRouter.post("/interface").coroutineHandler(vertx) { ctx -> handleCreate(ctx) }
        apiInterfaceRouter.put("/interface/:id").coroutineHandler(vertx) { ctx -> handleUpdate(ctx) }
        apiInterfaceRouter.delete("/interface/:id").coroutineHandler(vertx) { ctx -> handleDelete(ctx) }
        apiInterfaceRouter.put("/interface/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        apiInterfaceRouter.post("/interface/:id/copy").coroutineHandler(vertx) { ctx -> handleCopy(ctx) }
        apiInterfaceRouter.post("/interface/execute").coroutineHandler(vertx) { ctx -> handleExecute(ctx) }
        
        router.route("/*").subRouter(apiInterfaceRouter)
    }
    
    private suspend fun handleList(ctx: RoutingContext) {
        val query = ctx.queryParamsTo<ApiInterfaceQueryDto>(validate = true)
        val result = apiInterfaceService.list(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetMostUsed(ctx: RoutingContext) {
        val query = ctx.queryParamsTo<MostUsedInterfacesQueryDto>()
        val result = apiInterfaceService.getMostUsedInterfaces(query.days, query.limit)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDetail(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val result = apiInterfaceService.detail(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreate(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ApiInterfaceFormDto>(validate = true)
        val result = apiInterfaceService.create(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdate(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo<ApiInterfaceFormDto>(validate = true)
        val result = apiInterfaceService.update(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDelete(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val result = apiInterfaceService.delete(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val status = ctx.queryParams().get("status")?.toIntOrNull() ?: 1
        
        val result = apiInterfaceService.updateStatus(id, status)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCopy(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val result = apiInterfaceService.copy(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleExecute(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val request = body.mapTo<ApiExecuteRequestDto>(validate = true)
        val result = apiInterfaceService.execute(request)
        ResponseUtil.sendResponse(ctx, result)
    }
}

