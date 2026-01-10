package io.infra.market.vertx.exception

import io.infra.market.vertx.util.ResponseUtil
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 全局异常处理器
 * 
 * 类似于 Spring Boot 的 @ControllerAdvice
 * 统一处理所有路由中的异常
 */
object GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 注册全局异常处理器到 Router
     */
    fun register(router: Router) {
        router.errorHandler(500) { ctx ->
            handleException(ctx)
        }
        
        router.errorHandler(400) { ctx ->
            handleException(ctx)
        }
        
        // 处理其他状态码的异常
        router.errorHandler(404) { ctx ->
            ResponseUtil.error(ctx, "接口不存在", 404)
        }
    }
    
    /**
     * 处理异常
     */
    private fun handleException(ctx: RoutingContext) {
        val failure = ctx.failure()
        
        if (failure != null) {
            logger.error("处理请求时发生异常: ${ctx.request().method()} ${ctx.request().path()}", failure)
            
            // 根据异常类型返回不同的错误信息
            val result = when (failure) {
                is IllegalArgumentException -> Pair(failure.message ?: "请求参数错误", 400)
                is IllegalStateException -> Pair(failure.message ?: "操作失败", 400)
                is NullPointerException -> Pair("缺少必需参数", 400)
                else -> Pair(failure.message ?: "服务器内部错误", 500)
            }
            
            ResponseUtil.error(ctx, result.first, result.second)
        } else {
            // 没有异常对象，可能是手动调用的 ctx.fail(statusCode)
            val statusCode = ctx.statusCode()
            val message = when (statusCode) {
                400 -> "请求参数错误"
                401 -> "未授权"
                403 -> "禁止访问"
                404 -> "资源不存在"
                500 -> "服务器内部错误"
                else -> "请求失败"
            }
            ResponseUtil.error(ctx, message, statusCode)
        }
    }
}

