package io.infra.market.vertx.router

import io.infra.market.vertx.dto.BatchRequest
import io.infra.market.vertx.dto.RoleFormDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.RoleService
import io.infra.market.vertx.util.QueryParamUtil
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 角色路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class RoleRouter(private val roleService: RoleService) {
    
    fun mount(router: Router, vertx: Vertx) {
        val roleRouter = Router.router(vertx)
        roleRouter.route().handler(AuthMiddleware.create())
        
        roleRouter.get("/roles").coroutineHandler(vertx) { ctx -> handleGetRoles(ctx) }
        roleRouter.get("/roles/all").coroutineHandler(vertx) { ctx -> handleGetAllRoles(ctx) }
        roleRouter.get("/roles/:id").coroutineHandler(vertx) { ctx -> handleGetRole(ctx) }
        roleRouter.post("/roles").coroutineHandler(vertx) { ctx -> handleCreateRole(ctx) }
        roleRouter.put("/roles/:id").coroutineHandler(vertx) { ctx -> handleUpdateRole(ctx) }
        roleRouter.delete("/roles/:id").coroutineHandler(vertx) { ctx -> handleDeleteRole(ctx) }
        roleRouter.patch("/roles/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        roleRouter.delete("/roles/batch").coroutineHandler(vertx) { ctx -> handleBatchDelete(ctx) }
        
        router.route("/*").subRouter(roleRouter)
    }
    
    private suspend fun handleGetRoles(ctx: RoutingContext) {
        val query = QueryParamUtil.buildRoleQuery(ctx)
        val result = roleService.getRoles(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetAllRoles(ctx: RoutingContext) {
        val result = roleService.getAllRoles()
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetRole(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("角色ID无效")
        
        val result = roleService.getRole(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreateRole(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val form = body.mapTo(RoleFormDto::class.java)
        val result = roleService.createRole(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateRole(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("角色ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo(RoleFormDto::class.java)
        val result = roleService.updateRole(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDeleteRole(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("角色ID无效")
        
        val result = roleService.deleteRole(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("角色ID无效")
        
        val body = ctx.body().asJsonObject()
        val status = body.getString("status") ?: throw IllegalArgumentException("状态参数不能为空")
        
        val result = roleService.updateRoleStatus(id, status)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleBatchDelete(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val request = body.mapTo(BatchRequest::class.java)
        val result = roleService.batchDeleteRoles(request.ids)
        ResponseUtil.sendResponse(ctx, result)
    }
}

