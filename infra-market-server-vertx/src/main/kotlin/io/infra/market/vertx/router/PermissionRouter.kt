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
            val query = QueryParamUtil.buildPermissionQuery(ctx)
            val result = permissionService.getPermissions(query)
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
            val form = body.mapTo(PermissionFormDto::class.java)
            val result = permissionService.createPermission(form)
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
            val form = body.mapTo(PermissionFormDto::class.java)
            val result = permissionService.updatePermission(id, form)
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
            val request = body.mapTo(BatchRequest::class.java)
            val result = permissionService.batchDeletePermissions(request.ids)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("批量删除权限失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

