package io.infra.market.util

/**
 * 认证相关的ThreadLocal工具类
 * @author liuqinglin
 * Date: 2025/8/30
 */
object AuthThreadLocal {
    
    private val userIdHolder = ThreadLocal<Long>()
    
    /**
     * 获取当前用户ID
     */
    fun getCurrentUserId(): Long? = userIdHolder.get()
    
    /**
     * 设置当前用户ID
     */
    fun setCurrentUserId(userId: Long) {
        userIdHolder.set(userId)
    }
    
    /**
     * 清理当前用户ID
     */
    fun clearCurrentUserId() {
        userIdHolder.remove()
    }
}
