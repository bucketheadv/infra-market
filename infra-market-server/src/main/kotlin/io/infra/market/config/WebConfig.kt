package io.infra.market.config

import io.infra.market.interceptor.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authInterceptor: AuthInterceptor
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**") // 拦截所有请求
            .excludePathPatterns(
                "/auth/login", // 登录接口不需要验证
                "/error", // 错误页面不需要验证
                "/favicon.ico", // 图标不需要验证
                "/swagger-ui/**", // Swagger文档不需要验证
                "/v3/api-docs/**" // OpenAPI文档不需要验证
            )
    }
}
