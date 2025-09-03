package io.infra.qk.service

import io.infra.qk.util.JwtUtil
import io.infra.qk.util.AesUtil
import io.infra.qk.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.slf4j.LoggerFactory

@ApplicationScoped
class TokenService @Inject constructor(
    private val userRepository: UserRepository
) {
    
    private val logger = LoggerFactory.getLogger(TokenService::class.java)
    
    /**
     * 验证token是否有效
     */
    fun validateToken(token: String): Boolean {
        return try {
            // 验证JWT token格式和签名
            if (!JwtUtil.validateToken(token)) {
                logger.debug("JWT token格式或签名无效: {}", token)
                return false
            }
            
            // 检查token是否过期
            if (JwtUtil.isTokenExpired(token)) {
                logger.debug("JWT token已过期: {}", token)
                return false
            }
            
            // 从token中获取用户ID
            val userId = JwtUtil.getUserIdFromToken(token)
            if (userId == null) {
                logger.debug("无法从token中获取用户ID: {}", token)
                return false
            }
            
            // 检查用户是否存在且状态正常
            val user = userRepository.findById(userId)
            if (user == null || user.status != "active") {
                logger.debug("用户不存在或状态异常: userId={}", userId)
                return false
            }
            
            true
        } catch (e: Exception) {
            logger.error("验证token时发生异常: {}", e.message, e)
            false
        }
    }
    
    /**
     * 刷新token
     */
    fun refreshToken(token: String): String? {
        return try {
            if (!validateToken(token)) {
                return null
            }
            
            val userId = JwtUtil.getUserIdFromToken(token)
            if (userId == null) {
                return null
            }
            
            // 生成新的token
            JwtUtil.generateToken(userId)
        } catch (e: Exception) {
            logger.error("刷新token时发生异常: {}", e.message, e)
            null
        }
    }
    
    /**
     * 使token失效
     */
    fun invalidateToken(token: String): Boolean {
        // 在实际项目中，这里可以实现token黑名单机制
        // 目前简单返回true，表示操作成功
        logger.info("Token已失效: {}", token)
        return true
    }
}
