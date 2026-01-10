package io.infra.market.vertx.service

import io.infra.market.vertx.util.JwtUtil
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.redis.client.RedisAPI

/**
 * Token 服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class TokenService(
    private val redisAPI: RedisAPI
) {
    
    suspend fun saveToken(uid: Long, token: String) {
        val key = "token:${uid}"
        redisAPI.set(listOf(key, token, "EX", "259200")).awaitForResult() // 3天过期
    }
    
    suspend fun deleteToken(uid: Long) {
        val key = "token:${uid}"
        redisAPI.del(listOf(key)).awaitForResult()
    }
    
    suspend fun refreshToken(uid: Long, username: String): String {
        val newToken = JwtUtil.generateToken(uid, username)
        saveToken(uid, newToken)
        return newToken
    }
    
    suspend fun validateToken(uid: Long, token: String): Boolean {
        val key = "token:${uid}"
        return try {
            val response = redisAPI.get(key).awaitForResult()
            response != null && response.toString() == token && JwtUtil.validateToken(token)
        } catch (_: Exception) {
            false
        }
    }
}

