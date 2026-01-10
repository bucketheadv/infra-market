package io.infra.market.vertx.router

import io.infra.market.vertx.dto.BatchRequest
import io.infra.market.vertx.dto.PermissionFormDto
import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.PermissionService
import io.infra.market.vertx.util.QueryParamUtil
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * 权限路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class PermissionRouter(private val permissionService: PermissionService) {
    
    fun mount(router: Router, vertx: Vertx) {
        val permissionRouter = Router.router(vertx)
        permissionRouter.route().handler(AuthMiddleware.create())
        
        permissionRouter.get("/permissions").coroutineHandler(vertx) { ctx -> handleGetPermissions(ctx) }
        permissionRouter.get("/permissions/tree").coroutineHandler(vertx) { ctx -> handleGetPermissionTree(ctx) }
        permissionRouter.get("/permissions/:id").coroutineHandler(vertx) { ctx -> handleGetPermission(ctx) }
        permissionRouter.post("/permissions").coroutineHandler(vertx) { ctx -> handleCreatePermission(ctx) }
        permissionRouter.put("/permissions/:id").coroutineHandler(vertx) { ctx -> handleUpdatePermission(ctx) }
        permissionRouter.delete("/permissions/:id").coroutineHandler(vertx) { ctx -> handleDeletePermission(ctx) }
        permissionRouter.patch("/permissions/:id/status").coroutineHandler(vertx) { ctx -> handleUpdateStatus(ctx) }
        permissionRouter.delete("/permissions/batch").coroutineHandler(vertx) { ctx -> handleBatchDelete(ctx) }
        
        router.route("/*").subRouter(permissionRouter)
    }
    
    private suspend fun handleGetPermissions(ctx: RoutingContext) {
        val query = QueryParamUtil.buildPermissionQuery(ctx)
        val result = permissionService.getPermissions(query)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetPermissionTree(ctx: RoutingContext) {
        val result = permissionService.getPermissionTree()
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleGetPermission(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("权限ID无效")
        
        val result = permissionService.getPermission(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleCreatePermission(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val form = body.mapTo(PermissionFormDto::class.java)
        val result = permissionService.createPermission(form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdatePermission(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("权限ID无效")
        
        val body = ctx.body().asJsonObject()
        val form = body.mapTo(PermissionFormDto::class.java)
        val result = permissionService.updatePermission(id, form)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleDeletePermission(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("权限ID无效")
        
        val result = permissionService.deletePermission(id)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
            ?: throw IllegalArgumentException("权限ID无效")
        
        val body = ctx.body().asJsonObject()
        val status = body.getString("status") ?: throw IllegalArgumentException("状态参数不能为空")
        
        val result = permissionService.updatePermissionStatus(id, status)
        ResponseUtil.sendResponse(ctx, result)
    }
    
    private suspend fun handleBatchDelete(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val request = body.mapTo(BatchRequest::class.java)
        val result = permissionService.batchDeletePermissions(request.ids)
        ResponseUtil.sendResponse(ctx, result)
    }
}

