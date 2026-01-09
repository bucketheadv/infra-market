package io.infra.market.vertx.service

import io.infra.market.vertx.util.JwtUtil
import io.vertx.core.Future
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI

/**
 * Token 服务
 */
class TokenService(
    private val redis: Redis,
    private val redisAPI: RedisAPI
) {
    
    fun saveToken(uid: Long, token: String): Future<Void> {
        val key = "token:${uid}"
        return redisAPI.set(listOf(key, token, "EX", "259200")) // 3天过期
            .map { null }
    }
    
    fun deleteToken(uid: Long): Future<Void> {
        val key = "token:${uid}"
        return redisAPI.del(listOf(key))
            .map { null }
    }
    
    fun refreshToken(uid: Long, username: String): Future<String> {
        val newToken = JwtUtil.generateToken(uid, username)
        return saveToken(uid, newToken)
            .map { newToken }
    }
    
    fun validateToken(uid: Long, token: String): Future<Boolean> {
        val key = "token:${uid}"
        return redisAPI.get(key)
            .map { response ->
                response != null && response.toString() == token && JwtUtil.validateToken(token)
            }
            .recover { Future.succeededFuture(false) }
    }
}

