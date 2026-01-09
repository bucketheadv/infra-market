package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.UserService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 用户路由
 */
class UserRouter(private val userService: UserService) {
    
    private val logger = LoggerFactory.getLogger(UserRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val userRouter = Router.router(vertx)
        userRouter.route().handler(AuthMiddleware.create())
        
        userRouter.get("/users").handler(::handleGetUsers)
        userRouter.get("/users/:id").handler(::handleGetUser)
        userRouter.post("/users").handler(::handleCreateUser)
        userRouter.put("/users/:id").handler(::handleUpdateUser)
        userRouter.delete("/users/:id").handler(::handleDeleteUser)
        userRouter.patch("/users/:id/status").handler(::handleUpdateStatus)
        userRouter.post("/users/:id/reset/password").handler(::handleResetPassword)
        userRouter.delete("/users/batch").handler(::handleBatchDelete)
        
        router.route("/*").subRouter(userRouter)
    }
    
    private fun handleGetUsers(ctx: RoutingContext) {
        val username = ctx.queryParams().get("username")
        val status = ctx.queryParams().get("status")
        val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
        val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
        
        ResponseUtil.handleFuture(ctx, userService.getUsers(username, status, page, size), "获取用户列表失败", logger)
    }
    
    private fun handleGetUser(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "用户ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, userService.getUser(id), "获取用户失败", logger)
    }
    
    private fun handleCreateUser(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val username = body.getString("username") ?: ""
            val email = body.getString("email")
            val phone = body.getString("phone")
            val password = body.getString("password")
            val roleIds = body.getJsonArray("roleIds")?.map { (it as Number).toLong() } ?: emptyList()
            
            ResponseUtil.handleFuture(ctx, userService.createUser(username, email, phone, password, roleIds), "创建用户失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleUpdateUser(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "用户ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val username = body.getString("username") ?: ""
            val email = body.getString("email")
            val phone = body.getString("phone")
            val password = body.getString("password")
            val roleIds = body.getJsonArray("roleIds")?.map { (it as Number).toLong() } ?: emptyList()
            
            ResponseUtil.handleFuture(ctx, userService.updateUser(id, username, email, phone, password, roleIds), "更新用户失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleDeleteUser(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "用户ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, userService.deleteUser(id), "删除用户失败", logger)
    }
    
    private fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "用户ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val status = body.getString("status") ?: ""
            
            ResponseUtil.handleFuture(ctx, userService.updateStatus(id, status), "更新用户状态失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleResetPassword(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "用户ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, userService.resetPassword(id), "重置密码失败", logger)
    }
    
    private fun handleBatchDelete(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val idsArray = body.getJsonArray("ids")
            val ids = if (idsArray != null) {
                idsArray.map { (it as Number).toLong() }
            } else {
                emptyList()
            }
            
            ResponseUtil.handleFuture(ctx, userService.batchDeleteUsers(ids), "批量删除用户失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
}

