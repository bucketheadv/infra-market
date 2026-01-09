package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceExecutionRecordService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 接口执行记录路由
 */
class ApiInterfaceExecutionRecordRouter(private val apiInterfaceExecutionRecordService: ApiInterfaceExecutionRecordService) {
    
    private val logger = LoggerFactory.getLogger(ApiInterfaceExecutionRecordRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val recordRouter = Router.router(vertx)
        recordRouter.route().handler(AuthMiddleware.create())
        
        recordRouter.post("/api/interface/execution/record/list").handler(::handleList)
        recordRouter.get("/api/interface/execution/record/:id").handler(::handleDetail)
        recordRouter.get("/api/interface/execution/record/executor/:executorId").handler(::handleGetByExecutorId)
        recordRouter.get("/api/interface/execution/record/stats/:interfaceId").handler(::handleGetExecutionStats)
        recordRouter.get("/api/interface/execution/record/count").handler(::handleGetExecutionCount)
        recordRouter.delete("/api/interface/execution/record/cleanup").handler(::handleCleanup)
        
        router.route("/*").subRouter(recordRouter)
    }
    
    private fun handleList(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            ResponseUtil.handleFuture(ctx, apiInterfaceExecutionRecordService.list(body), "获取执行记录列表失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleDetail(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "记录ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceExecutionRecordService.detail(id), "获取执行记录详情失败", logger)
    }
    
    private fun handleGetByExecutorId(ctx: RoutingContext) {
        val executorId = ctx.pathParam("executorId").toLongOrNull()
        if (executorId == null) {
            ResponseUtil.error(ctx, "执行人ID无效", 400)
            return
        }
        
        val limit = ctx.queryParams().get("limit")?.toIntOrNull() ?: 10
        
        ResponseUtil.handleFuture(ctx, apiInterfaceExecutionRecordService.getByExecutorId(executorId, limit), "获取执行记录失败", logger)
    }
    
    private fun handleGetExecutionStats(ctx: RoutingContext) {
        val interfaceId = ctx.pathParam("interfaceId").toLongOrNull()
        if (interfaceId == null) {
            ResponseUtil.error(ctx, "接口ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceExecutionRecordService.getExecutionStats(interfaceId), "获取执行统计失败", logger)
    }
    
    private fun handleGetExecutionCount(ctx: RoutingContext) {
        val startTime = ctx.queryParams().get("startTime")?.toLongOrNull()
        val endTime = ctx.queryParams().get("endTime")?.toLongOrNull()
        
        if (startTime == null || endTime == null) {
            ResponseUtil.error(ctx, "时间参数无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceExecutionRecordService.getExecutionCount(startTime, endTime), "获取执行记录数量失败", logger)
    }
    
    private fun handleCleanup(ctx: RoutingContext) {
        val beforeTime = ctx.queryParams().get("beforeTime")?.toLongOrNull()
        if (beforeTime == null) {
            ResponseUtil.error(ctx, "时间参数无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, apiInterfaceExecutionRecordService.cleanup(beforeTime), "清理执行记录失败", logger)
    }
}

