package io.infra.market.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
object JwtUtil {
    
    private const val SECRET_KEY = "infra-market-jwt-secret-key-2024-very-long-secret-key-for-security"
    private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000L // 24小时
    
    /**
     * 生成JWT token
     */
    fun generateToken(userId: Long, username: String): String {
        val now = Date()
        val expiration = Date(now.time + EXPIRATION_TIME)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("username", username)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }
    
    /**
     * 从token中获取用户ID
     */
    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.subject.toLong()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从token中获取用户名
     */
    fun getUsernameFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["username"] as String?
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 从token中获取Claims
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body
    }
}
