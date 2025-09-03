package io.infra.qk.controller

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.PermissionFormDto
import io.infra.qk.dto.PermissionQueryDto
import io.infra.qk.service.PermissionService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

/**
 * 权限控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Path("/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PermissionController @Inject constructor(
    private val permissionService: PermissionService
) {
    
    @GET
    fun getPermissionList(
        @QueryParam("permissionName") permissionName: String?,
        @QueryParam("permissionCode") permissionCode: String?,
        @QueryParam("permissionType") permissionType: String?,
        @QueryParam("parentId") parentId: Long?,
        @QueryParam("status") status: String?,
        @QueryParam("current") @DefaultValue("1") current: Int,
        @QueryParam("size") @DefaultValue("10") size: Int
    ): ApiResponse<*> {
        val query = PermissionQueryDto(permissionName, permissionCode, permissionType, parentId, status, current, size)
        return permissionService.getPermissionList(query)
    }
    
    @GET
    @Path("/tree")
    fun getPermissionTree(): ApiResponse<*> {
        return permissionService.getPermissionTree()
    }
    
    @GET
    @Path("/{id}")
    fun getPermissionById(@PathParam("id") id: Long): ApiResponse<*> {
        return permissionService.getPermissionById(id)
    }
    
    @POST
    fun createPermission(permissionForm: PermissionFormDto): ApiResponse<*> {
        return permissionService.createPermission(permissionForm)
    }
    
    @PUT
    @Path("/{id}")
    fun updatePermission(@PathParam("id") id: Long, permissionForm: PermissionFormDto): ApiResponse<*> {
        return permissionService.updatePermission(id, permissionForm)
    }
    
    @DELETE
    @Path("/{id}")
    fun deletePermission(@PathParam("id") id: Long): ApiResponse<*> {
        return permissionService.deletePermission(id)
    }
    
    @PATCH
    @Path("/{id}/status")
    fun updatePermissionStatus(
        @PathParam("id") id: Long,
        @QueryParam("status") status: String
    ): ApiResponse<*> {
        return permissionService.updatePermissionStatus(id, status)
    }
    
    @DELETE
    @Path("/batch")
    fun batchDeletePermissions(ids: List<Long>): ApiResponse<*> {
        return permissionService.batchDeletePermissions(ids)
    }
}
