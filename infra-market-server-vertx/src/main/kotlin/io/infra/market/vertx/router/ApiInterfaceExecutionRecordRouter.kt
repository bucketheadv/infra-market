package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceExecutionRecordService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 接口执行记录路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class ApiInterfaceExecutionRecordRouter(private val apiInterfaceExecutionRecordService: ApiInterfaceExecutionRecordService) {
    
    private val logger = LoggerFactory.getLogger(ApiInterfaceExecutionRecordRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val recordRouter = Router.router(vertx)
        recordRouter.route().handler(AuthMiddleware.create())
        
        recordRouter.post("/interface/execution/record/list").coroutineHandler(vertx) { ctx -> handleList(ctx) }
        recordRouter.get("/interface/execution/record/:id").coroutineHandler(vertx) { ctx -> handleDetail(ctx) }
        recordRouter.get("/interface/execution/record/executor/:executorId").coroutineHandler(vertx) { ctx -> handleGetByExecutorId(ctx) }
        recordRouter.get("/interface/execution/record/stats/:interfaceId").coroutineHandler(vertx) { ctx -> handleGetExecutionStats(ctx) }
        recordRouter.get("/interface/execution/record/count").coroutineHandler(vertx) { ctx -> handleGetExecutionCount(ctx) }
        recordRouter.delete("/interface/execution/record/cleanup").coroutineHandler(vertx) { ctx -> handleCleanup(ctx) }
        
        router.route("/*").subRouter(recordRouter)
    }
    
    private suspend fun handleList(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val result = apiInterfaceExecutionRecordService.list(body)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取执行记录列表失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleDetail(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "记录ID无效", 400)
                return
            }
            
            val result = apiInterfaceExecutionRecordService.detail(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取执行记录详情失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取执行记录详情失败", 500)
        }
    }
    
    private suspend fun handleGetByExecutorId(ctx: RoutingContext) {
        try {
            val executorId = ctx.pathParam("executorId").toLongOrNull()
            if (executorId == null) {
                ResponseUtil.error(ctx, "执行人ID无效", 400)
                return
            }
            
            val limit = ctx.queryParams().get("limit")?.toIntOrNull() ?: 10
            
            val result = apiInterfaceExecutionRecordService.getByExecutorId(executorId, limit)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取执行记录失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取执行记录失败", 500)
        }
    }
    
    private suspend fun handleGetExecutionStats(ctx: RoutingContext) {
        try {
            val interfaceId = ctx.pathParam("interfaceId").toLongOrNull()
            if (interfaceId == null) {
                ResponseUtil.error(ctx, "接口ID无效", 400)
                return
            }
            
            val result = apiInterfaceExecutionRecordService.getExecutionStats(interfaceId)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取执行统计失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取执行统计失败", 500)
        }
    }
    
    private suspend fun handleGetExecutionCount(ctx: RoutingContext) {
        try {
            val startTime = ctx.queryParams().get("startTime")?.toLongOrNull()
            val endTime = ctx.queryParams().get("endTime")?.toLongOrNull()
            
            if (startTime == null || endTime == null) {
                ResponseUtil.error(ctx, "时间参数无效", 400)
                return
            }
            
            val result = apiInterfaceExecutionRecordService.getExecutionCount(startTime, endTime)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取执行记录数量失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取执行记录数量失败", 500)
        }
    }
    
    private suspend fun handleCleanup(ctx: RoutingContext) {
        try {
            val beforeTime = ctx.queryParams().get("beforeTime")?.toLongOrNull()
            if (beforeTime == null) {
                ResponseUtil.error(ctx, "时间参数无效", 400)
                return
            }
            
            val result = apiInterfaceExecutionRecordService.cleanup(beforeTime)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("清理执行记录失败", e)
            ResponseUtil.error(ctx, e.message ?: "清理执行记录失败", 500)
        }
    }
}

