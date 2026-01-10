package io.infra.market.vertx.router

import io.infra.market.vertx.dto.ApiExecuteRequestDto
import io.infra.market.vertx.dto.ApiInterfaceFormDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 接口管理路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class ApiInterfaceRouter(private val apiInterfaceService: ApiInterfaceService) {
    
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
        val name = ctx.queryParams().get("name")
        val method = ctx.queryParams().get("method")
        val status = ctx.queryParams().get("status")?.toIntOrNull()
        val environment = ctx.queryParams().get("environment")
        val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
        val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
        
        val result = apiInterfaceService.list(name, method, status, environment, page, size)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetMostUsed(ctx: RoutingContext) {
        val days = ctx.queryParams().get("days")?.toIntOrNull() ?: 30
        val limit = ctx.queryParams().get("limit")?.toIntOrNull() ?: 5
        
        val result = apiInterfaceService.getMostUsedInterfaces(days, limit)
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
        val form = body.mapTo(ApiInterfaceFormDto::class.java)
        val result = apiInterfaceService.create(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdate(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo(ApiInterfaceFormDto::class.java)
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
        val request = body.mapTo(ApiExecuteRequestDto::class.java)
        val result = apiInterfaceService.execute(request)
        ResponseUtil.sendResponse(ctx, result)
    }
}

