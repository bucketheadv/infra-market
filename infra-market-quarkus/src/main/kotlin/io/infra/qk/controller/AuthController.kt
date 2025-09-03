package io.infra.qk.controller

import io.infra.qk.dto.LoginRequest
import io.infra.qk.dto.ChangePasswordRequest
import io.infra.qk.dto.ApiResponse
import io.infra.qk.service.AuthService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken

/**
 * 认证控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AuthController @Inject constructor(
    private val authService: AuthService,
    private val jwt: JsonWebToken
) {
    
    @POST
    @Path("/login")
    fun login(request: LoginRequest): ApiResponse<*> {
        return authService.login(request)
    }
    
    @GET
    @Path("/current/user")
    fun getCurrentUser(): ApiResponse<*> {
        val userId = getCurrentUserId()
        return authService.getCurrentUser(userId)
    }
    
    @GET
    @Path("/user/menus")
    fun getUserMenus(): ApiResponse<*> {
        val userId = getCurrentUserId()
        return authService.getUserMenus(userId)
    }
    
    @POST
    @Path("/refresh/token")
    fun refreshToken(): ApiResponse<*> {
        val userId = getCurrentUserId()
        return authService.refreshToken(userId)
    }
    
    @POST
    @Path("/logout")
    fun logout(): ApiResponse<*> {
        val userId = getCurrentUserId()
        return authService.logout(userId)
    }
    
    @POST
    @Path("/change/password")
    fun changePassword(request: ChangePasswordRequest): ApiResponse<*> {
        val userId = getCurrentUserId()
        return authService.changePassword(userId, request)
    }
    
    private fun getCurrentUserId(): Long {
        return jwt.getClaim("userId") ?: 0L
    }
}
