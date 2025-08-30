package io.infra.market.service

import io.infra.market.util.JwtUtil
import io.infra.structure.redis.core.JedisTemplate
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jedisTemplate: JedisTemplate
) {
    
    private val TOKEN_PREFIX = "token:"
    private val TOKEN_EXPIRE_TIME: Long = 24 * 60 * 60 // 24小时（秒）
    
    /**
     * 保存token到Redis
     */
    fun saveToken(userId: Long, token: String) {
        val key = "$TOKEN_PREFIX$userId"
        jedisTemplate.setex(key, TOKEN_EXPIRE_TIME, token)
    }
    
    /**
     * 从Redis获取token
     */
    fun getToken(userId: Long): String? {
        val key = "$TOKEN_PREFIX$userId"
        return jedisTemplate.get(key)
    }
    
    /**
     * 验证token是否有效
     */
    fun validateToken(token: String): Boolean {
        val userId = JwtUtil.getUserIdFromToken(token) ?: return false
        
        // 验证JWT token本身
        if (!JwtUtil.validateToken(token)) {
            return false
        }
        
        // 验证Redis中的token
        val storedToken = getToken(userId)
        return storedToken == token
    }
    
    /**
     * 删除token
     */
    fun deleteToken(userId: Long) {
        val key = "$TOKEN_PREFIX$userId"
        jedisTemplate.del(key)
    }
    
    /**
     * 刷新token
     */
    fun refreshToken(userId: Long, username: String): String {
        val newToken = JwtUtil.generateToken(userId, username)
        saveToken(userId, newToken)
        return newToken
    }
}
