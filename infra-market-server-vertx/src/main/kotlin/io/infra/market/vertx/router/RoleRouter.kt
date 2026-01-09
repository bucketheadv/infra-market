package io.infra.market.vertx.router

import io.infra.market.vertx.middleware.AuthMiddleware
import io.infra.market.vertx.service.RoleService
import io.infra.market.vertx.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

/**
 * 角色路由
 */
class RoleRouter(private val roleService: RoleService) {
    
    private val logger = LoggerFactory.getLogger(RoleRouter::class.java)
    
    fun mount(router: Router, vertx: Vertx) {
        val roleRouter = Router.router(vertx)
        roleRouter.route().handler(AuthMiddleware.create())
        
        roleRouter.get("/roles").handler(::handleGetRoles)
        roleRouter.get("/roles/all").handler(::handleGetAllRoles)
        roleRouter.get("/roles/:id").handler(::handleGetRole)
        roleRouter.post("/roles").handler(::handleCreateRole)
        roleRouter.put("/roles/:id").handler(::handleUpdateRole)
        roleRouter.delete("/roles/:id").handler(::handleDeleteRole)
        roleRouter.patch("/roles/:id/status").handler(::handleUpdateStatus)
        roleRouter.delete("/roles/batch").handler(::handleBatchDelete)
        
        router.route("/*").subRouter(roleRouter)
    }
    
    private fun handleGetRoles(ctx: RoutingContext) {
        val name = ctx.queryParams().get("name")
        val code = ctx.queryParams().get("code")
        val status = ctx.queryParams().get("status")
        val page = ctx.queryParams().get("page")?.toIntOrNull() ?: 1
        val size = ctx.queryParams().get("size")?.toIntOrNull() ?: 10
        
        ResponseUtil.handleFuture(ctx, roleService.getRoles(name, code, status, page, size), "获取角色列表失败", logger)
    }
    
    private fun handleGetAllRoles(ctx: RoutingContext) {
        ResponseUtil.handleFuture(ctx, roleService.getAllRoles(), "获取所有角色失败", logger)
    }
    
    private fun handleGetRole(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "角色ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, roleService.getRole(id), "获取角色失败", logger)
    }
    
    private fun handleCreateRole(ctx: RoutingContext) {
        try {
            val body = ctx.body().asJsonObject()
            val name = body.getString("name") ?: ""
            val code = body.getString("code") ?: ""
            val description = body.getString("description")
            val permissionIds = body.getJsonArray("permissionIds")?.map { (it as Number).toLong() } ?: emptyList()
            
            ResponseUtil.handleFuture(ctx, roleService.createRole(name, code, description, permissionIds), "创建角色失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleUpdateRole(ctx: RoutingContext) {
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
            
            ResponseUtil.handleFuture(ctx, roleService.updateRole(id, name, code, description, permissionIds), "更新角色失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
    
    private fun handleDeleteRole(ctx: RoutingContext) {
        val id = ctx.pathParam("id").toLongOrNull()
        if (id == null) {
            ResponseUtil.error(ctx, "角色ID无效", 400)
            return
        }
        
        ResponseUtil.handleFuture(ctx, roleService.deleteRole(id), "删除角色失败", logger)
    }
    
    private fun handleUpdateStatus(ctx: RoutingContext) {
        try {
            val id = ctx.pathParam("id").toLongOrNull()
            if (id == null) {
                ResponseUtil.error(ctx, "角色ID无效", 400)
                return
            }
            
            val body = ctx.body().asJsonObject()
            val status = body.getString("status") ?: ""
            
            ResponseUtil.handleFuture(ctx, roleService.updateRoleStatus(id, status), "更新角色状态失败", logger)
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
            
            ResponseUtil.handleFuture(ctx, roleService.batchDeleteRoles(ids), "批量删除角色失败", logger)
        } catch (e: Exception) {
            ResponseUtil.handleException(ctx, e, "请求参数错误", logger)
        }
    }
}

