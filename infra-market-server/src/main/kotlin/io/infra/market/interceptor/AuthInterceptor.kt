package io.infra.market.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.market.dto.ApiResponse
import io.infra.market.service.TokenService
import io.infra.market.util.JwtUtil
import io.infra.market.util.AuthThreadLocal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val tokenService: TokenService,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 获取请求头中的token
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
        
        if (token.isNullOrBlank()) {
            handleUnauthorized(response, "未提供token")
            return false
        }
        
        // 验证token
        if (!tokenService.validateToken(token)) {
            handleUnauthorized(response, "token无效或已过期")
            return false
        }
        
        // 从token中解析用户ID并保存到ThreadLocal
        val userId = JwtUtil.getUserIdFromToken(token)
        if (userId != null) {
            AuthThreadLocal.setCurrentUserId(userId)
        }
        
        return true
    }
    
    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        // 清理ThreadLocal，避免内存泄漏
        AuthThreadLocal.clearCurrentUserId()
    }
    
    private fun handleUnauthorized(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        
        val apiResponse = ApiResponse.error<Unit>(message)
        val jsonResponse = objectMapper.writeValueAsString(apiResponse)
        
        response.writer.write(jsonResponse)
    }
}
