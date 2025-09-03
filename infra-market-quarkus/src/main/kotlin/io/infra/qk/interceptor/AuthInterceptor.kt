package io.infra.qk.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.qk.dto.ApiResponse
import io.infra.qk.service.TokenService
import io.infra.qk.util.JwtUtil
import io.infra.qk.util.AuthThreadLocal
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InvocationContext
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.slf4j.LoggerFactory

/**
 * 认证拦截器
 * 用于拦截需要认证的请求并验证JWT token
 */
@Provider
@PreMatching
@ApplicationScoped
class AuthInterceptor @Inject constructor(
    private val tokenService: TokenService,
    private val objectMapper: ObjectMapper
) : ContainerRequestFilter {

    private val logger = LoggerFactory.getLogger(AuthInterceptor::class.java)

    override fun filter(requestContext: ContainerRequestContext) {
        // 全局日志 - 确保所有请求都被拦截
        logger.info("=== 拦截器开始处理请求 ===")

        // 获取请求路径
        val path = requestContext.uriInfo.path
        val method = requestContext.method
        val absolutePath = requestContext.uriInfo.absolutePath.path
        val requestUri = requestContext.uriInfo.requestUri.path
        val baseUri = requestContext.uriInfo.baseUri.path
        val requestUriString = requestContext.uriInfo.requestUri.toString()

        logger.info("=== 完整请求信息 ===")
        logger.info("方法: {}", method)
        logger.info("路径: {}", path)
        logger.info("绝对路径: {}", absolutePath)
        logger.info("请求URI: {}", requestUri)
        logger.info("基础URI: {}", baseUri)
        logger.info("完整请求URI: {}", requestUriString)
        logger.info("=== 请求信息结束 ===")

        logger.debug("认证拦截器处理请求: {} {}", method, path)
        logger.info("请求路径详情: 原始路径='{}', 绝对路径='{}', 请求URI='{}', 方法='{}'",
                   path, absolutePath, requestUri, method)

        // 检查是否为公开接口（不需要认证）
        if (isPublicPath(path, method)) {
            logger.debug("跳过公开接口: {} {}", method, path)
            return
        }

        // 跳过OPTIONS预检请求
        if (method == "OPTIONS") {
            logger.debug("跳过OPTIONS预检请求: {}", path)
            return
        }

        // 获取请求头中的token
        val authHeader = requestContext.getHeaderString("Authorization")
        logger.info("Authorization头: '{}'", authHeader)

        val token = authHeader?.removePrefix("Bearer ")
        logger.info("提取的token: '{}'", token?.take(20) + "...")

        if (token.isNullOrBlank()) {
            logger.warn("未提供token，请求路径: {} {}", method, path)
            handleUnauthorized(requestContext, "未提供token")
            return
        }

        // 验证token
        logger.info("开始验证token: {}", token.take(20) + "...")
        if (!tokenService.validateToken(token)) {
            logger.warn("token验证失败: {}", token.take(20) + "...")
            handleUnauthorized(requestContext, "token无效或已过期")
            return
        }

        logger.info("token验证成功")

        // 从token中解析用户ID并保存到ThreadLocal
        val userId = JwtUtil.getUserIdFromToken(token)
        if (userId != null) {
            AuthThreadLocal.setCurrentUserId(userId)
            logger.debug("用户认证成功: userId={}", userId)
        } else {
            logger.warn("无法从token中解析用户ID")
            handleUnauthorized(requestContext, "token格式错误")
            return
        }
    }

    /**
     * 判断是否为公开接口（不需要认证）
     */
    private fun isPublicPath(path: String, method: String): Boolean {
        logger.debug("检查是否为公开接口: {} {}", method, path)

        // 登录接口 - 最关键的公开接口
        if (method == "POST" && (path == "auth/login" || path == "/auth/login")) {
            logger.debug("识别为登录接口，跳过认证")
            return true
        }

        // 注册接口
        if (method == "POST" && (path == "auth/register" || path == "/auth/register")) {
            logger.debug("识别为注册接口，跳过认证")
            return true
        }

        // 公开测试接口
        if (method == "GET" && (path == "test/public" || path == "/test/public")) {
            logger.debug("识别为公开测试接口，跳过认证")
            return true
        }

        // 健康检查接口
        if (method == "GET" && (path == "health" || path == "/health" || path == "metrics" || path == "/metrics")) {
            logger.debug("识别为健康检查接口，跳过认证")
            return true
        }

        logger.debug("接口需要认证: {} {}", method, path)
        return false
    }

    /**
     * 处理未授权的情况
     */
    private fun handleUnauthorized(requestContext: ContainerRequestContext, message: String) {
        logger.debug("认证失败: {}", message)

        val apiResponse = ApiResponse.error<Unit>(message)
        val jsonResponse = try {
            objectMapper.writeValueAsString(apiResponse)
        } catch (e: Exception) {
            """{"success":false,"message":"$message","data":null}"""
        }

        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(jsonResponse)
                .type(MediaType.APPLICATION_JSON)
                .build()
        )
    }
}
