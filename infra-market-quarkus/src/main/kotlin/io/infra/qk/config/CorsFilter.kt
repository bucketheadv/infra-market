package io.infra.qk.config

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import org.slf4j.LoggerFactory

/**
 * CORS跨域过滤器
 * 处理跨域请求和OPTIONS预检请求
 */
@Provider
class CorsFilter : ContainerResponseFilter {

    private val logger = LoggerFactory.getLogger(CorsFilter::class.java)

    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        logger.info("CORS过滤器处理响应: {} {}",
                    requestContext.method,
                    requestContext.uriInfo.path)

        // 添加CORS响应头
        responseContext.headers.add("Access-Control-Allow-Origin", "*")
        responseContext.headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
        responseContext.headers.add("Access-Control-Allow-Headers", "accept, authorization, content-type, x-requested-with, origin, access-control-request-method, access-control-request-headers")
        responseContext.headers.add("Access-Control-Expose-Headers", "Content-Disposition, Authorization")
        responseContext.headers.add("Access-Control-Max-Age", "86400")
        responseContext.headers.add("Access-Control-Allow-Credentials", "true")

        // 如果是OPTIONS请求，直接返回200
        if (requestContext.method == "OPTIONS") {
            logger.info("处理OPTIONS预检请求: {}", requestContext.uriInfo.path)
            responseContext.status = 200
        }
    }
}
