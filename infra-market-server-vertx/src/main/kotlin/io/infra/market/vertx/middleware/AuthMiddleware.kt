package io.infra.market.vertx.middleware

import io.infra.market.vertx.util.JwtUtil
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 认证中间件
 */
object AuthMiddleware {
    
    private val logger = LoggerFactory.getLogger(AuthMiddleware::class.java)
    
    fun create(): Handler<RoutingContext> {
        return Handler { ctx ->
            val token = extractToken(ctx)
            
            if (token == null) {
                ctx.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end("""{"code":401,"message":"未授权，请先登录"}""")
                return@Handler
            }
            
            val uid = JwtUtil.getUidFromToken(token)
            if (uid == null || !JwtUtil.validateToken(token)) {
                ctx.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end("""{"code":401,"message":"Token无效或已过期"}""")
                return@Handler
            }
            
            ctx.put("uid", uid)
            ctx.put("token", token)
            ctx.next()
        }
    }
    
    private fun extractToken(ctx: RoutingContext): String? {
        val authHeader = ctx.request().getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }
        return ctx.request().getParam("token")
    }
}

