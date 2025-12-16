package io.infra.market.service

import io.infra.market.util.JwtUtil
import io.infra.structure.redis.core.JedisTemplate
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jedisTemplate: JedisTemplate
) {
    
    private val tokenPrefix = "token:"
    private val tokenExpireTime: Long = 3 * 24 * 60 * 60 // 3天（秒）
    
    /**
     * 保存token到Redis
     */
    fun saveToken(uid: Long, token: String) {
        val key = "$tokenPrefix$uid"
        jedisTemplate.setex(key, tokenExpireTime, token)
    }
    
    /**
     * 从Redis获取token
     */
    fun getToken(uid: Long): String? {
        val key = "$tokenPrefix$uid"
        return jedisTemplate.get(key)
    }
    
    /**
     * 验证token是否有效
     */
    fun validateToken(token: String): Boolean {
        val uid = JwtUtil.getUidFromToken(token) ?: return false
        
        // 验证JWT token本身
        if (!JwtUtil.validateToken(token)) {
            return false
        }
        
        // 验证Redis中的token
        val storedToken = getToken(uid)
        return storedToken == token
    }
    
    /**
     * 删除token
     */
    fun deleteToken(uid: Long) {
        val key = "$tokenPrefix$uid"
        jedisTemplate.del(key)
    }
    
    /**
     * 刷新token
     */
    fun refreshToken(uid: Long, username: String): String {
        val newToken = JwtUtil.generateToken(uid, username)
        saveToken(uid, newToken)
        return newToken
    }
}
