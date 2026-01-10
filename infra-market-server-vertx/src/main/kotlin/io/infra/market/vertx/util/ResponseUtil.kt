package io.infra.market.vertx.util

import io.infra.market.vertx.dto.ApiData
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger

/**
 * 响应工具类
 * 
 * 用于统一处理 HTTP 响应，避免重复代码
 */
object ResponseUtil {
    
    private const val CONTENT_TYPE_JSON = "application/json"
    
    /**
     * 发送成功响应
     */
    fun <T> success(ctx: RoutingContext, data: T, statusCode: Int = 200) {
        val result = if (statusCode == 200) {
            ApiData.success(data)
        } else {
            ApiData(code = statusCode, message = "success", data = data)
        }
        sendResponse(ctx, result)
    }
    
    /**
     * 发送错误响应
     */
    fun error(ctx: RoutingContext, message: String, statusCode: Int = 500) {
        val result = ApiData.error<Any?>(message, statusCode)
        sendResponse(ctx, result)
    }
    
    /**
     * 处理 Future 结果并发送响应
     */
    fun <T> handleFuture(
        ctx: RoutingContext,
        future: Future<ApiData<T>>,
        errorMessage: String = "操作失败",
        logger: Logger? = null
    ) {
        future
            .onSuccess { result ->
                sendResponse(ctx, result)
            }
            .onFailure { error ->
                logger?.error(errorMessage, error)
                this.error(ctx, error.message ?: errorMessage, 500)
            }
    }
    
    /**
     * 处理异常并发送错误响应
     */
    fun handleException(
        ctx: RoutingContext,
        exception: Exception,
        message: String = "请求参数错误",
        logger: Logger? = null
    ) {
        logger?.error(message, exception)
        error(ctx, message, 400)
    }
    
    /**
     * 发送响应（自动从 ApiData 中获取状态码）
     */
    fun <T> sendResponse(ctx: RoutingContext, result: ApiData<T>) {
        ctx.response()
            .setStatusCode(result.code)
            .putHeader("Content-Type", CONTENT_TYPE_JSON)
            .end(JsonObject.mapFrom(result).encode())
    }
}

