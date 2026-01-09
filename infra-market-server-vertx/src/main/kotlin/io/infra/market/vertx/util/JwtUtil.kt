package io.infra.market.vertx.util

import io.infra.market.vertx.config.AppConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 工具类
 */
object JwtUtil {
    
    /**
     * 获取密钥
     */
    private fun getSecretKey(): SecretKey {
        val secretKey = AppConfig.getJwtSecretKey()
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }
    
    /**
     * 获取过期时间
     */
    private fun getExpirationTime(): Long {
        return AppConfig.getJwtExpirationTime()
    }
    
    /**
     * 生成JWT token
     */
    fun generateToken(uid: Long, username: String): String {
        val now = Date()
        val expiration = Date(now.time + getExpirationTime())
        
        return Jwts.builder()
            .subject(uid.toString())
            .claim("username", username)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(getSecretKey())
            .compact()
    }
    
    /**
     * 从token中获取用户ID
     */
    fun getUidFromToken(token: String): Long? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.subject.toLong()
        } catch (_: Exception) {
            null
        }
    }
    
    /**
     * 验证token是否有效
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !claims.expiration.before(Date())
        } catch (_: Exception) {
            false
        }
    }
    
    /**
     * 从token中获取Claims
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }
}

