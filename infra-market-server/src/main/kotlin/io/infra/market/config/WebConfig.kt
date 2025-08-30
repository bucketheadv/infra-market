package io.infra.market.config

import io.infra.market.interceptor.AuthInterceptor
import io.infra.market.interceptor.PermissionInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authInterceptor: AuthInterceptor,
    private val permissionInterceptor: PermissionInterceptor
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        // 认证拦截器 - 验证token
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**") // 拦截所有请求
            .excludePathPatterns(
                "/auth/login", // 登录接口不需要验证
                "/error", // 错误页面不需要验证
                "/favicon.ico", // 图标不需要验证
                "/swagger-ui/**", // Swagger文档不需要验证
                "/v3/api-docs/**" // OpenAPI文档不需要验证
            )
            .order(1) // 优先级1，先执行认证
        
        // 权限拦截器 - 验证权限
        registry.addInterceptor(permissionInterceptor)
            .addPathPatterns("/**") // 拦截所有请求
            .excludePathPatterns(
                "/auth/login", // 登录接口不需要验证
                "/auth/logout", // 登出接口不需要验证
                "/error", // 错误页面不需要验证
                "/favicon.ico", // 图标不需要验证
                "/swagger-ui/**", // Swagger文档不需要验证
                "/v3/api-docs/**" // OpenAPI文档不需要验证
            )
            .order(2) // 优先级2，后执行权限验证
    }
}
