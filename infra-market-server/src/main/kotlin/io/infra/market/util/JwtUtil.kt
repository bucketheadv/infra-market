package io.infra.market.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
object JwtUtil {
    
    private const val SECRET_KEY = "infra-market-jwt-secret-key-2024-very-long-secret-key-for-security"
    private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000L // 24小时
    
    // 生成密钥
    private val key: SecretKey = Keys.hmacShaKeyFor(SECRET_KEY.toByteArray())
    
    /**
     * 生成JWT token
     */
    fun generateToken(userId: Long, username: String): String {
        val now = Date()
        val expiration = Date(now.time + EXPIRATION_TIME)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }
    
    /**
     * 从token中获取用户ID
     */
    fun getUserIdFromToken(token: String): Long? {
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
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
