package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ChangePasswordRequest
import io.infra.market.vertx.dto.LoginRequest
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.AuthService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 认证路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
@Singleton
class AuthRouter @Inject constructor(
    private val authService: AuthService
) {
    
    fun mount(router: Router, vertx: Vertx) {
        val authRouter = Router.router(vertx)
        
        // 登录接口不需要认证
        authRouter.post("/auth/login").coroutineHandler(vertx) { ctx -> handleLogin(ctx) }
        
        // 其他接口需要认证 - 使用精确路径匹配，排除 /login
        authRouter.route("/auth/current/user").handler(AuthMiddleware.create())
        authRouter.route("/auth/user/menus").handler(AuthMiddleware.create())
        authRouter.route("/auth/refresh/token").handler(AuthMiddleware.create())
        authRouter.route("/auth/logout").handler(AuthMiddleware.create())
        authRouter.route("/auth/change/password").handler(AuthMiddleware.create())
        
        authRouter.get("/auth/current/user").coroutineHandler(vertx) { ctx -> handleGetCurrentUser(ctx) }
        authRouter.get("/auth/user/menus").coroutineHandler(vertx) { ctx -> handleGetUserMenus(ctx) }
        authRouter.post("/auth/refresh/token").coroutineHandler(vertx) { ctx -> handleRefreshToken(ctx) }
        authRouter.post("/auth/logout").coroutineHandler(vertx) { ctx -> handleLogout(ctx) }
        authRouter.post("/auth/change/password").coroutineHandler(vertx) { ctx -> handleChangePassword(ctx) }
        
        router.route("/*").subRouter(authRouter)
    }
    
    private suspend fun handleLogin(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val request = body.mapTo<LoginRequest>(validate = true)
        val result = authService.login(request)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetCurrentUser(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        val result = authService.getCurrentUser(uid)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetUserMenus(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        val result = authService.getUserMenus(uid)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleRefreshToken(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        val result = authService.refreshToken(uid)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleLogout(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        val result = authService.logout(uid)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleChangePassword(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        val body = ctx.body().asJsonObject()
        val request = body.mapTo<ChangePasswordRequest>(validate = true)
        val result = authService.changePassword(uid, request)
        ResponseUtil.sendResponse(ctx, result)
    }
}

