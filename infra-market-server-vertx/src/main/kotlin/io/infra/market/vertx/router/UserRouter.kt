package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.UserService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 用户路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class UserRouter(private val userService: UserService) {
    
    private val logger = LoggerFactory.getLogger(UserRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val userRouter = Router.router(vertx)
        userRouter.route().handler(AuthMiddleware.create())
        
        userRouter.get("/users").coroutineHandler(vertx) { ctx -> handleGetUsers(ctx) }
        userRouter.get("/users/:id").coroutineHandler(vertx) { ctx -> handleGetUser(ctx) }
        userRouter.post("/users").coroutineHandler(vertx) { ctx -> handleCreateUser(ctx) }
        userRouter.put("/users/:id").coroutineHandler(vertx) { ctx -> handleUpdateUser(ctx) }
        userRouter.delete("/users/:id").coroutineHandler(vertx) { ctx -> handleDeleteUser(ctx) }
        userRouter.patch("/users/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        userRouter.post("/users/:id/reset/password").coroutineHandler(vertx) { ctx -> handleResetPassword(ctx) }
        userRouter.delete("/users/batch").coroutineHandler(vertx) { ctx -> handleBatchDelete(ctx) }
        
        router.route("/*").subRouter(userRouter)
    }
    
    private suspend fun handleGetUsers(ctx: RoutingContext) {
        try {
            val username = ctx.queryParams().get("username")
            val status = ctx.queryParams().get("status")
            val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
            val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
            
            val result = userService.getUsers(username, status, page, size)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取用户列表失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取用户列表失败", 500)
        }
    }
    
    private suspend fun handleGetUser(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "用户ID无效", 400)
                return
            }
            
            val result = userService.getUser(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取用户失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取用户失败", 500)
        }
    }
    
    private suspend fun handleCreateUser(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val username = body.getString("username") ?: ""
            val email = body.getString("email")
            val phone = body.getString("phone")
            val password = body.getString("password")
            val roleIds = body.getJsonArray("roleIds")?.map { (it as Number).toLong() } ?: emptyList()
            
            val result = userService.createUser(username, email, phone, password, roleIds)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("创建用户失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleUpdateUser(ctx: RoutingContext) {
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
            
            val result = userService.updateUser(id, username, email, phone, password, roleIds)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新用户失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleDeleteUser(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "用户ID无效", 400)
                return
            }
            
            val result = userService.deleteUser(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("删除用户失败", e)
            ResponseUtil.error(ctx, e.message ?: "删除用户失败", 500)
        }
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "用户ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val status = body.getString("status") ?: ""
            
            val result = userService.updateStatus(id, status)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新用户状态失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleResetPassword(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "用户ID无效", 400)
                return
            }
            
            val result = userService.resetPassword(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("重置密码失败", e)
            ResponseUtil.error(ctx, e.message ?: "重置密码失败", 500)
        }
    }
    
    private suspend fun handleBatchDelete(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val idsArray = body.getJsonArray("ids")
            val ids = if (idsArray != null) {
                idsArray.map { (it as Number).toLong() }
            } else {
                emptyList()
            }
            
            val result = userService.batchDeleteUsers(ids)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("批量删除用户失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

