package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 接口管理路由
 */
class ApiInterfaceRouter(private val apiInterfaceService: ApiInterfaceService) {
    
    private val logger = LoggerFactory.getLogger(ApiInterfaceRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val apiInterfaceRouter = Router.router(vertx)
        apiInterfaceRouter.route().handler(AuthMiddleware.create())
        
        apiInterfaceRouter.get("/api/interface/list").handler(::handleList)
        apiInterfaceRouter.get("/api/interface/most/used").handler(::handleGetMostUsed)
        apiInterfaceRouter.get("/api/interface/:id").handler(::handleDetail)
        apiInterfaceRouter.post("/api/interface").handler(::handleCreate)
        apiInterfaceRouter.put("/api/interface/:id").handler(::handleUpdate)
        apiInterfaceRouter.delete("/api/interface/:id").handler(::handleDelete)
        apiInterfaceRouter.put("/api/interface/:id/status").handler(::handleUpdateStatus)
        apiInterfaceRouter.post("/api/interface/:id/copy").handler(::handleCopy)
        apiInterfaceRouter.post("/api/interface/execute").handler(::handleExecute)
        
        router.route("/*").subRouter(apiInterfaceRouter)
    }
    
    private fun handleList(ctx: RoutingContext) {
        val name = ctx.queryParams().get("name")
        val method = ctx.queryParams().get("method")
        val status = ctx.queryParams().get("status")?.toIntOrNull()
        val environment = ctx.queryParams().get("environment")
        val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
        val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
        
        ResponseUtil.handleFuture(ctx, apiInterfaceService.list(name, method, status, environment, page, size), "获取接口列表失败", logger)
    }
    
    private fun handleGetMostUsed(ctx: RoutingContext) {
        val days = ctx.queryParams().get("days")?.toIntOrNull() ?: 30
        val limit = ctx.queryParams().get("limit")?.toIntOrNull() ?: 5
        
        ResponseUtil.handleFuture(ctx, apiInterfaceService.getMostUsedInterfaces(days, limit), "获取热门接口失败", logger)
    }
    
    private fun handleDetail(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "接口ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceService.detail(id), "获取接口详情失败", logger)
    }
    
    private fun handleCreate(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            ResponseUtil.handleFuture(ctx, apiInterfaceService.create(body), "创建接口失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleUpdate(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            ResponseUtil.handleFuture(ctx, apiInterfaceService.update(id, body), "更新接口失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleDelete(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "接口ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceService.delete(id), "删除接口失败", logger)
    }
    
    private fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val status = ctx.queryParams().get("status")?.toIntOrNull() ?: 1
            
            ResponseUtil.handleFuture(ctx, apiInterfaceService.updateStatus(id, status), "更新接口状态失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleCopy(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "接口ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceService.copy(id), "复制接口失败", logger)
    }
    
    private fun handleExecute(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            ResponseUtil.handleFuture(ctx, apiInterfaceService.execute(body), "执行接口失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
}

