package io.infra.market.util

/**
 * 认证相关的ThreadLocal工具类
 * @author liuqinglin
 * Date: 2025/8/30
 */
object AuthHolder {
    
    private val uidHolder = ThreadLocal<Long>()
    
    /**
     * 获取当前用户ID
     */
    fun getUid(): Long? = uidHolder.get()
    
    /**
     * 设置当前用户ID
     */
    fun setUid(userId: Long) = uidHolder.set(userId)

    /**
     * 清理当前用户ID
     */
    fun clearUid() = uidHolder.remove()
}
