package io.infra.qk.controller

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.UserFormDto
import io.infra.qk.dto.UserQueryDto
import io.infra.qk.service.UserService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

/**
 * 用户控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController @Inject constructor(
    private val userService: UserService
) {
    
    @GET
    fun getUserList(
        @QueryParam("username") username: String?,
        @QueryParam("status") status: String?,
        @QueryParam("current") @DefaultValue("1") current: Int,
        @QueryParam("size") @DefaultValue("10") size: Int
    ): ApiResponse<*> {
        val query = UserQueryDto(username, status, current, size)
        return userService.getUserList(query)
    }
    
    @GET
    @Path("/{id}")
    fun getUserById(@PathParam("id") id: Long): ApiResponse<*> {
        return userService.getUserById(id)
    }
    
    @POST
    fun createUser(userForm: UserFormDto): ApiResponse<*> {
        return userService.createUser(userForm)
    }
    
    @PUT
    @Path("/{id}")
    fun updateUser(@PathParam("id") id: Long, userForm: UserFormDto): ApiResponse<*> {
        return userService.updateUser(id, userForm)
    }
    
    @DELETE
    @Path("/{id}")
    fun deleteUser(@PathParam("id") id: Long): ApiResponse<*> {
        return userService.deleteUser(id)
    }
    
    @PATCH
    @Path("/{id}/status")
    fun updateUserStatus(
        @PathParam("id") id: Long,
        @QueryParam("status") status: String
    ): ApiResponse<*> {
        return userService.updateUserStatus(id, status)
    }
    
    @DELETE
    @Path("/batch")
    fun batchDeleteUsers(ids: List<Long>): ApiResponse<*> {
        return userService.batchDeleteUsers(ids)
    }
    
    @POST
    @Path("/{id}/reset/password")
    fun resetPassword(@PathParam("id") id: Long): ApiResponse<*> {
        return userService.resetPassword(id)
    }
}
