package io.infra.market.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.market.annotation.RequiresPermission
import io.infra.market.dto.ApiData
import io.infra.market.enums.ErrorMessageEnum
import io.infra.market.service.PermissionCheckService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * 权限拦截器
 * 用于拦截带有权限注解的方法并验证权限
 * 
 * @author liuqinglin
 * Date: 2025/1/27
 */
@Component
class PermissionInterceptor(
    private val permissionCheckService: PermissionCheckService,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 如果不是方法处理器，直接放行
        if (handler !is HandlerMethod) {
            return true
        }
        
        // 获取方法上的权限注解
        val permissionAnnotation = handler.getMethodAnnotation(RequiresPermission::class.java) ?: return true
        
        // 如果没有权限注解，直接放行

        // 检查权限
        val hasPermission = if (permissionAnnotation.allowMultiple) {
            // 多个权限，OR关系
            val permissionCodes = permissionAnnotation.value.split(",").map { it.trim() }
            permissionCheckService.hasAnyPermission(permissionCodes)
        } else {
            // 单个权限
            permissionCheckService.hasPermission(permissionAnnotation.value)
        }
        
        // 如果没有权限，返回403错误
        if (!hasPermission) {
            val errorMessage = if (permissionAnnotation.description.isNotBlank()) {
                "${ErrorMessageEnum.PERMISSION_DENIED.message}：${permissionAnnotation.description}"
            } else {
                "${ErrorMessageEnum.PERMISSION_DENIED.message}：需要 ${permissionAnnotation.value} 权限"
            }
            handleForbidden(response, errorMessage)
            return false
        }
        
        return true
    }
    
    /**
     * 处理权限不足的情况
     */
    private fun handleForbidden(response: HttpServletResponse, message: String) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        
        val errorResponse = ApiData.error<Unit>(message, HttpStatus.FORBIDDEN.value())
        val jsonResponse = objectMapper.writeValueAsString(errorResponse)
        
        response.writer.use { writer ->
            writer.write(jsonResponse)
            writer.flush()
        }
    }
}
