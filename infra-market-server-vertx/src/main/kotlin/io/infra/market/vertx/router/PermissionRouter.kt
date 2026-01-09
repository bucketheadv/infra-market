package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.PermissionService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 权限路由
 */
class PermissionRouter(private val permissionService: PermissionService) {
    
    private val logger = LoggerFactory.getLogger(PermissionRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val permissionRouter = Router.router(vertx)
        permissionRouter.route().handler(AuthMiddleware.create())
        
        permissionRouter.get("/permissions").handler(::handleGetPermissions)
        permissionRouter.get("/permissions/tree").handler(::handleGetPermissionTree)
        permissionRouter.get("/permissions/:id").handler(::handleGetPermission)
        permissionRouter.post("/permissions").handler(::handleCreatePermission)
        permissionRouter.put("/permissions/:id").handler(::handleUpdatePermission)
        permissionRouter.delete("/permissions/:id").handler(::handleDeletePermission)
        permissionRouter.patch("/permissions/:id/status").handler(::handleUpdateStatus)
        permissionRouter.delete("/permissions/batch").handler(::handleBatchDelete)
        
        router.route("/*").subRouter(permissionRouter)
    }
    
    private fun handleGetPermissions(ctx: RoutingContext) {
        val name = ctx.queryParams().get("name")
        val code = ctx.queryParams().get("code")
        val type = ctx.queryParams().get("type")
        val status = ctx.queryParams().get("status")
        val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
        val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
        
        ResponseUtil.handleFuture(ctx, permissionService.getPermissions(name, code, type, status, page, size), "获取权限列表失败", logger)
    }
    
    private fun handleGetPermissionTree(ctx: RoutingContext) {
        ResponseUtil.handleFuture(ctx, permissionService.getPermissionTree(), "获取权限树失败", logger)
    }
    
    private fun handleGetPermission(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "权限ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, permissionService.getPermission(id), "获取权限失败", logger)
    }
    
    private fun handleCreatePermission(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val name = body.getString("name") ?: ""
            val code = body.getString("code") ?: ""
            val type = body.getString("type") ?: ""
            val parentId = body.getLong("parentId")
            val path = body.getString("path")
            val icon = body.getString("icon")
            val sort = body.getInteger("sort") ?: 0
            
            ResponseUtil.handleFuture(ctx, permissionService.createPermission(name, code, type, parentId, path, icon, sort), "创建权限失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleUpdatePermission(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "权限ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val name = body.getString("name") ?: ""
            val code = body.getString("code") ?: ""
            val type = body.getString("type") ?: ""
            val parentId = body.getLong("parentId")
            val path = body.getString("path")
            val icon = body.getString("icon")
            val sort = body.getInteger("sort") ?: 0
            
            ResponseUtil.handleFuture(ctx, permissionService.updatePermission(id, name, code, type, parentId, path, icon, sort), "更新权限失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleDeletePermission(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "权限ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, permissionService.deletePermission(id), "删除权限失败", logger)
    }
    
    private fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "权限ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val status = body.getString("status") ?: ""
            
            ResponseUtil.handleFuture(ctx, permissionService.updatePermissionStatus(id, status), "更新权限状态失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
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
            
            ResponseUtil.handleFuture(ctx, permissionService.batchDeletePermissions(ids), "批量删除权限失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
}

