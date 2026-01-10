package io.infra.market.vertx.util

import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Route 扩展函数
 * 用于在 Vert.x 中将 suspend 函数作为路由处理器
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
fun Route.coroutineHandler(vertx: Vertx, handler: suspend (RoutingContext) -> Unit): Route {
    return this.handler { ctx ->
        CoroutineScope(vertx.dispatcher()).launch {
            try {
                handler(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}

