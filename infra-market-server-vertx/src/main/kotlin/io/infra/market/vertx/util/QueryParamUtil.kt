package io.infra.market.vertx.util

import io.vertx.ext.web.RoutingContext

/**
 * 查询参数工具类
 * 
 * 注意：现在推荐使用 RoutingContext.queryParamsTo<T>() 扩展函数直接转换查询参数
 * 这个工具类保留了一些基础方法，供特殊场景使用
 */
object QueryParamUtil {
    
    /**
     * 从 RoutingContext 获取字符串查询参数
     */
    fun getString(ctx: RoutingContext, key: String): String? {
        val value: String? = ctx.queryParams().get(key)
        return value?.takeIf { it.isNotBlank() }
    }
    
    /**
     * 从 RoutingContext 获取整数查询参数
     */
    fun getInt(ctx: RoutingContext, key: String, default: Int? = null): Int? {
        val value = ctx.queryParams().get(key)?.toIntOrNull()
        return value ?: default
    }
    
    /**
     * 从 RoutingContext 获取长整数查询参数
     */
    fun getLong(ctx: RoutingContext, key: String, default: Long? = null): Long? {
        val value = ctx.queryParams().get(key)?.toLongOrNull()
        return value ?: default
    }
    
    /**
     * 从 RoutingContext 获取并验证状态参数
     * 状态值只能是0或1
     * 
     * @param ctx RoutingContext
     * @param paramName 参数名称，默认为"status"
     * @return 状态值（0或1）
     * @throws IllegalArgumentException 如果参数为空或值不是0或1
     */
    fun getStatus(ctx: RoutingContext, paramName: String = "status"): Int {
        val statusStr = ctx.queryParams().get(paramName)
        if (statusStr.isNullOrBlank()) {
            throw IllegalArgumentException("状态参数不能为空")
        }
        
        return when (statusStr) {
            "1" -> 1
            "0" -> 0
            else -> throw IllegalArgumentException("状态值只能是0或1")
        }
    }
}

