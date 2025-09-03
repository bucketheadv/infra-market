package io.infra.qk.controller

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.RoleFormDto
import io.infra.qk.dto.RoleQueryDto
import io.infra.qk.service.RoleService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

/**
 * 角色控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RoleController @Inject constructor(
    private val roleService: RoleService
) {
    
    @GET
    fun getRoleList(
        @QueryParam("roleName") roleName: String?,
        @QueryParam("roleCode") roleCode: String?,
        @QueryParam("status") status: String?,
        @QueryParam("current") @DefaultValue("1") current: Int,
        @QueryParam("size") @DefaultValue("10") size: Int
    ): ApiResponse<*> {
        val query = RoleQueryDto(roleName, roleCode, status, current, size)
        return roleService.getRoleList(query)
    }
    
    @GET
    @Path("/all")
    fun getAllRoles(): ApiResponse<*> {
        return roleService.getAllRoles()
    }
    
    @GET
    @Path("/{id}")
    fun getRoleById(@PathParam("id") id: Long): ApiResponse<*> {
        return roleService.getRoleById(id)
    }
    
    @POST
    fun createRole(roleForm: RoleFormDto): ApiResponse<*> {
        return roleService.createRole(roleForm)
    }
    
    @PUT
    @Path("/{id}")
    fun updateRole(@PathParam("id") id: Long, roleForm: RoleFormDto): ApiResponse<*> {
        return roleService.updateRole(id, roleForm)
    }
    
    @DELETE
    @Path("/{id}")
    fun deleteRole(@PathParam("id") id: Long): ApiResponse<*> {
        return roleService.deleteRole(id)
    }
    
    @PATCH
    @Path("/{id}/status")
    fun updateRoleStatus(
        @PathParam("id") id: Long,
        @QueryParam("status") status: String
    ): ApiResponse<*> {
        return roleService.updateRoleStatus(id, status)
    }
    
    @DELETE
    @Path("/batch")
    fun batchDeleteRoles(ids: List<Long>): ApiResponse<*> {
        return roleService.batchDeleteRoles(ids)
    }
}
