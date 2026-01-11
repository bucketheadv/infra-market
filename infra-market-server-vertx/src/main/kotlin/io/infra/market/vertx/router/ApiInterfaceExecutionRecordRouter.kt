package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ApiInterfaceExecutionRecordQueryDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.ApiInterfaceExecutionRecordService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 接口执行记录路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class ApiInterfaceExecutionRecordRouter @Inject constructor(
    private val apiInterfaceExecutionRecordService: ApiInterfaceExecutionRecordService
) {
    
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
        val body = ctx.body().asJsonObject()
        val query = body.mapTo<ApiInterfaceExecutionRecordQueryDto>(validate = true)
        val result = apiInterfaceExecutionRecordService.list(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDetail(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("记录ID无效")
        
        val result = apiInterfaceExecutionRecordService.detail(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetByExecutorId(ctx: RoutingContext) {
        val executorId = ctx.pathParam("executorId").toLongOrNull()
            ?: throw IllegalArgumentException("执行人ID无效")
        
        val limit = ctx.queryParams().get("limit")?.toIntOrNull() ?: 10
        
        val result = apiInterfaceExecutionRecordService.getByExecutorId(executorId, limit)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetExecutionStats(ctx: RoutingContext) {
        val interfaceId = ctx.pathParam("interfaceId").toLongOrNull()
            ?: throw IllegalArgumentException("接口ID无效")
        
        val result = apiInterfaceExecutionRecordService.getExecutionStats(interfaceId)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetExecutionCount(ctx: RoutingContext) {
        val startTime = ctx.queryParams().get("startTime")?.toLongOrNull()
            ?: throw IllegalArgumentException("开始时间参数无效")
        val endTime = ctx.queryParams().get("endTime")?.toLongOrNull()
            ?: throw IllegalArgumentException("结束时间参数无效")
        
        val result = apiInterfaceExecutionRecordService.getExecutionCount(startTime, endTime)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCleanup(ctx: RoutingContext) {
        val beforeTime = ctx.queryParams().get("beforeTime")?.toLongOrNull()
            ?: throw IllegalArgumentException("时间参数无效")
        
        val result = apiInterfaceExecutionRecordService.cleanup(beforeTime)
        ResponseUtil.sendResponse(ctx, result)
    }
}

