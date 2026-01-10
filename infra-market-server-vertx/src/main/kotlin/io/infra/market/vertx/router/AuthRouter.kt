package io.infra.market.vertx.router

import io.infra.market.vertx.dto.ChangePasswordRequest
import io.infra.market.vertx.dto.LoginRequest
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.AuthService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.util.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 认证路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class AuthRouter(private val authService: AuthService) {
    
    private val logger = LoggerFactory.getLogger(AuthRouter::class.java)
    
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
        try {
            val body = ctx.body().asJsonObject()
            val request = LoginRequest(
                username = body.getString("username") ?: "",
                password = body.getString("password") ?: ""
            )
            
            val result = authService.login(request)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("登录失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleGetCurrentUser(ctx: RoutingContext) {
        try {
            val uid = ctx.get<Long>("uid")
            val result = authService.getCurrentUser(uid)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取用户信息失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取用户信息失败", 500)
        }
    }
    
    private suspend fun handleGetUserMenus(ctx: RoutingContext) {
        try {
            val uid = ctx.get<Long>("uid")
            val result = authService.getUserMenus(uid)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取菜单失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取菜单失败", 500)
        }
    }
    
    private suspend fun handleRefreshToken(ctx: RoutingContext) {
        try {
            val uid = ctx.get<Long>("uid")
            val result = authService.refreshToken(uid)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("刷新Token失败", e)
            ResponseUtil.error(ctx, e.message ?: "刷新Token失败", 500)
        }
    }
    
    private suspend fun handleLogout(ctx: RoutingContext) {
        try {
            val uid = ctx.get<Long>("uid")
            val result = authService.logout(uid)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("登出失败", e)
            ResponseUtil.error(ctx, e.message ?: "登出失败", 500)
        }
    }
    
    private suspend fun handleChangePassword(ctx: RoutingContext) {
        try {
            val uid = ctx.get<Long>("uid")
            val body = ctx.body().asJsonObject()
            val request = ChangePasswordRequest(
                oldPassword = body.getString("oldPassword") ?: "",
                newPassword = body.getString("newPassword") ?: ""
            )
            
            val result = authService.changePassword(uid, request)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("修改密码失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

