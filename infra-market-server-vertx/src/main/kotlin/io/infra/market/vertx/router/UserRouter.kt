package io.infra.market.vertx.router

import io.infra.market.vertx.dto.BatchRequest
import io.infra.market.vertx.dto.StatusUpdateDto
import io.infra.market.vertx.dto.UserFormDto
import io.infra.market.vertx.dto.UserQueryDto
import io.infra.market.vertx.dto.UserUpdateDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.UserService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.infra.market.vertx.extensions.mapTo
import io.infra.market.vertx.extensions.queryParamsTo
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 用户路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class UserRouter(private val userService: UserService) {
    
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
        val query = ctx.queryParamsTo<UserQueryDto>(validate = true)
        val result = userService.getUsers(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetUser(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("用户ID无效")
        
        val result = userService.getUser(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreateUser(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        // 使用 validate = true 启用参数验证（类似 Spring 的 @Valid）
        val form = body.mapTo<UserFormDto>(validate = true)
        val result = userService.createUser(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateUser(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("用户ID无效")
        
        val body = ctx.body().asJsonObject()
        // 使用 validate = true 启用参数验证（类似 Spring 的 @Valid）
        val form = body.mapTo<UserUpdateDto>(validate = true)
        val result = userService.updateUser(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDeleteUser(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("用户ID无效")
        
        val result = userService.deleteUser(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("用户ID无效")
        
        val body = ctx.body().asJsonObject()
        val request = body.mapTo<StatusUpdateDto>(validate = true)
        val result = userService.updateStatus(id, request.status)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleResetPassword(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("用户ID无效")
        
        val result = userService.resetPassword(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleBatchDelete(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val request = body.mapTo<BatchRequest>(validate = true)
        val result = userService.batchDeleteUsers(request.ids)
        ResponseUtil.sendResponse(ctx, result)
    }
}

