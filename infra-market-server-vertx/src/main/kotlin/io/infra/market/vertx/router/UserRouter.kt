package io.infra.market.vertx.router

import io.infra.market.vertx.dto.BatchRequest
import io.infra.market.vertx.dto.StatusUpdateDto
import io.infra.market.vertx.dto.UserFormDto
import io.infra.market.vertx.dto.UserUpdateDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.UserService
import io.infra.market.vertx.util.QueryParamUtil
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
            val query = QueryParamUtil.buildUserQuery(ctx)
            val result = userService.getUsers(query)
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
            val form = body.mapTo(UserFormDto::class.java)
            val result = userService.createUser(form)
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
            val form = body.mapTo(UserUpdateDto::class.java)
            val result = userService.updateUser(id, form)
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
            val request = body.mapTo(BatchRequest::class.java)
            val result = userService.batchDeleteUsers(request.ids)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("批量删除用户失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

