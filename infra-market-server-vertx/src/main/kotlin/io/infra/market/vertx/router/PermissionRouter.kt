package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.PermissionService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.extensions.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 权限路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class PermissionRouter(private val permissionService: PermissionService) {
    
    private val logger = LoggerFactory.getLogger(PermissionRouter::class.java)
    
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
        try {
            val name = ctx.queryParams().get("name")
            val code = ctx.queryParams().get("code")
            val type = ctx.queryParams().get("type")
            val status = ctx.queryParams().get("status")
            val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
            val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
            
            val result = permissionService.getPermissions(name, code, type, status, page, size)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取权限列表失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取权限列表失败", 500)
        }
    }
    
    private suspend fun handleGetPermissionTree(ctx: RoutingContext) {
        try {
            val result = permissionService.getPermissionTree()
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取权限树失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取权限树失败", 500)
        }
    }
    
    private suspend fun handleGetPermission(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "权限ID无效", 400)
                return
            }
            
            val result = permissionService.getPermission(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取权限失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取权限失败", 500)
        }
    }
    
    private suspend fun handleCreatePermission(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val name = body.getString("name") ?: ""
            val code = body.getString("code") ?: ""
            val type = body.getString("type") ?: ""
            val parentId = body.getLong("parentId")
            val path = body.getString("path")
            val icon = body.getString("icon")
            val sort = body.getInteger("sort") ?: 0
            
            val result = permissionService.createPermission(name, code, type, parentId, path, icon, sort)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("创建权限失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleUpdatePermission(ctx: RoutingContext) {
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
            
            val result = permissionService.updatePermission(id, name, code, type, parentId, path, icon, sort)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新权限失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleDeletePermission(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "权限ID无效", 400)
                return
            }
            
            val result = permissionService.deletePermission(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("删除权限失败", e)
            ResponseUtil.error(ctx, e.message ?: "删除权限失败", 500)
        }
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "权限ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val status = body.getString("status") ?: ""
            
            val result = permissionService.updatePermissionStatus(id, status)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新权限状态失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
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
            
            val result = permissionService.batchDeletePermissions(ids)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("批量删除权限失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

