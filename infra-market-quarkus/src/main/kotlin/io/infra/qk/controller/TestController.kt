package io.infra.qk.controller

import io.infra.qk.annotation.RequiresPermission
import io.infra.qk.dto.ApiResponse
import io.infra.qk.util.AuthThreadLocal
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.slf4j.LoggerFactory

/**
 * 测试控制器
 * 用于验证拦截器功能
 */
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
class TestController {
    
    private val logger = LoggerFactory.getLogger(TestController::class.java)
    
    @GET
    @Path("/auth")
    fun testAuth(): ApiResponse<String> {
        val currentUserId = AuthThreadLocal.getCurrentUserId()
        logger.info("当前用户ID: {}", currentUserId)
        return ApiResponse.success("认证测试成功，当前用户ID: $currentUserId")
    }
    
    @GET
    @Path("/permission")
    @RequiresPermission("test:permission", "测试权限")
    fun testPermission(): ApiResponse<String> {
        return ApiResponse.success("权限测试成功")
    }
    
    @GET
    @Path("/public")
    fun testPublic(): ApiResponse<String> {
        return ApiResponse.success("公开接口测试成功")
    }
    
    @POST
    @Path("/login-test")
    fun testLogin(): ApiResponse<String> {
        return ApiResponse.success("登录测试接口调用成功")
    }
    
    @GET
    @Path("/protected")
    fun testProtected(): ApiResponse<String> {
        return ApiResponse.success("受保护接口测试成功")
    }

    @GET
    @Path("/simple-test")
    fun simpleTest(): ApiResponse<String> {
        return ApiResponse.success("简单测试接口")
    }
    
    @GET
    @Path("/cors-test")
    fun testCors(): ApiResponse<String> {
        return ApiResponse.success("CORS测试成功")
    }
    
}
