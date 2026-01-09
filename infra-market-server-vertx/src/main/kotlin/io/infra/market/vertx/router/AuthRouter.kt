package io.infra.market.vertx.router

import io.infra.market.vertx.dto.ChangePasswordRequest
import io.infra.market.vertx.dto.LoginRequest
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.AuthService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 认证路由
 */
class AuthRouter(private val authService: AuthService) {
    
    private val logger = LoggerFactory.getLogger(AuthRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val authRouter = Router.router(vertx)
        
        // 登录接口不需要认证
        authRouter.post("/auth/login").handler(::handleLogin)
        
        // 其他接口需要认证 - 使用精确路径匹配，排除 /login
        authRouter.route("/auth/current/user").handler(AuthMiddleware.create())
        authRouter.route("/auth/user/menus").handler(AuthMiddleware.create())
        authRouter.route("/auth/refresh/token").handler(AuthMiddleware.create())
        authRouter.route("/auth/logout").handler(AuthMiddleware.create())
        authRouter.route("/auth/change/password").handler(AuthMiddleware.create())
        
        authRouter.get("/auth/current/user").handler(::handleGetCurrentUser)
        authRouter.get("/auth/user/menus").handler(::handleGetUserMenus)
        authRouter.post("/auth/refresh/token").handler(::handleRefreshToken)
        authRouter.post("/auth/logout").handler(::handleLogout)
        authRouter.post("/auth/change/password").handler(::handleChangePassword)
        
        router.route("/*").subRouter(authRouter)
    }
    
    private fun handleLogin(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val request = LoginRequest(
                username = body.getString("username") ?: "",
                password = body.getString("password") ?: ""
            )
            
            ResponseUtil.handleFuture(ctx, authService.login(request), "登录失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleGetCurrentUser(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        ResponseUtil.handleFuture(ctx, authService.getCurrentUser(uid), "获取用户信息失败", logger)
    }
    
    private fun handleGetUserMenus(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        ResponseUtil.handleFuture(ctx, authService.getUserMenus(uid), "获取菜单失败", logger)
    }
    
    private fun handleRefreshToken(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        ResponseUtil.handleFuture(ctx, authService.refreshToken(uid), "刷新Token失败", logger)
    }
    
    private fun handleLogout(ctx: RoutingContext) {
        val uid = ctx.get<Long>("uid")
        ResponseUtil.handleFuture(ctx, authService.logout(uid), "登出失败", logger)
    }
    
    private fun handleChangePassword(ctx: RoutingContext) {
        try {
            val uid = ctx.get<Long>("uid")
            val body = ctx.body().asJsonObject()
            val request = ChangePasswordRequest(
                oldPassword = body.getString("oldPassword") ?: "",
                newPassword = body.getString("newPassword") ?: ""
            )
            
            ResponseUtil.handleFuture(ctx, authService.changePassword(uid, request), "修改密码失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
}

