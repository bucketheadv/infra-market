package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 接口管理路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class ApiInterfaceRouter(private val apiInterfaceService: ApiInterfaceService) {
    
    private val logger = LoggerFactory.getLogger(ApiInterfaceRouter::class.java)
    
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
        try {
            val name = ctx.queryParams().get("name")
            val method = ctx.queryParams().get("method")
            val status = ctx.queryParams().get("status")?.toIntOrNull()
            val environment = ctx.queryParams().get("environment")
            val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
            val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
            
            val result = apiInterfaceService.list(name, method, status, environment, page, size)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取接口列表失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取接口列表失败", 500)
        }
    }
    
    private suspend fun handleGetMostUsed(ctx: RoutingContext) {
        try {
            val days = ctx.queryParams().get("days")?.toIntOrNull() ?: 30
            val limit = ctx.queryParams().get("limit")?.toIntOrNull() ?: 5
            
            val result = apiInterfaceService.getMostUsedInterfaces(days, limit)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取热门接口失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取热门接口失败", 500)
        }
    }
    
    private suspend fun handleDetail(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val result = apiInterfaceService.detail(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取接口详情失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取接口详情失败", 500)
        }
    }
    
    private suspend fun handleCreate(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val result = apiInterfaceService.create(body)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("创建接口失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleUpdate(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val result = apiInterfaceService.update(id, body)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新接口失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleDelete(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val result = apiInterfaceService.delete(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("删除接口失败", e)
            ResponseUtil.error(ctx, e.message ?: "删除接口失败", 500)
        }
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val status = ctx.queryParams().get("status")?.toIntOrNull() ?: 1
            
            val result = apiInterfaceService.updateStatus(id, status)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新接口状态失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleCopy(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val result = apiInterfaceService.copy(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("复制接口失败", e)
            ResponseUtil.error(ctx, e.message ?: "复制接口失败", 500)
        }
    }
    
    private suspend fun handleExecute(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val result = apiInterfaceService.execute(body)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("执行接口失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

