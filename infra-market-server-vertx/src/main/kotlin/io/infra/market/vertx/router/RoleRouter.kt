package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.RoleService
import io.infra.market.vertx.util.ResponseUtil
import io.infra.market.vertx.util.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 角色路由
 * 
 * 规则2：路由处理请求，必须用 coroutineHandler 替代原生的 handler
 */
class RoleRouter(private val roleService: RoleService) {
    
    private val logger = LoggerFactory.getLogger(RoleRouter::class.java)
    
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
        try {
            val name = ctx.queryParams().get("name")
            val code = ctx.queryParams().get("code")
            val status = ctx.queryParams().get("status")
            val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
            val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
            
            val result = roleService.getRoles(name, code, status, page, size)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取角色列表失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取角色列表失败", 500)
        }
    }
    
    private suspend fun handleGetAllRoles(ctx: RoutingContext) {
        try {
            val result = roleService.getAllRoles()
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取所有角色失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取所有角色失败", 500)
        }
    }
    
    private suspend fun handleGetRole(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "角色ID无效", 400)
                return
            }
            
            val result = roleService.getRole(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("获取角色失败", e)
            ResponseUtil.error(ctx, e.message ?: "获取角色失败", 500)
        }
    }
    
    private suspend fun handleCreateRole(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val name = body.getString("name") ?: ""
            val code = body.getString("code") ?: ""
            val description = body.getString("description")
            val permissionIds = body.getJsonArray("permissionIds")?.map { (it as Number).toLong() } ?: emptyList()
            
            val result = roleService.createRole(name, code, description, permissionIds)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("创建角色失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleUpdateRole(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "角色ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val name = body.getString("name") ?: ""
            val code = body.getString("code") ?: ""
            val description = body.getString("description")
            val permissionIds = body.getJsonArray("permissionIds")?.map { (it as Number).toLong() } ?: emptyList()
            
            val result = roleService.updateRole(id, name, code, description, permissionIds)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新角色失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
    
    private suspend fun handleDeleteRole(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "角色ID无效", 400)
                return
            }
            
            val result = roleService.deleteRole(id)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("删除角色失败", e)
            ResponseUtil.error(ctx, e.message ?: "删除角色失败", 500)
        }
    }
    
    private suspend fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "角色ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val status = body.getString("status") ?: ""
            
            val result = roleService.updateRoleStatus(id, status)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("更新角色状态失败", e)
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
            
            val result = roleService.batchDeleteRoles(ids)
            ResponseUtil.sendResponse(ctx, result)
        } catch (e: Exception) {
            logger.error("批量删除角色失败", e)
            ResponseUtil.error(ctx, e.message ?: "请求参数错误", 400)
        }
    }
}

