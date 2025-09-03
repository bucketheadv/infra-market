package io.infra.qk.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*

/**
 * JWT 工具类
 * @author liuqinglin
 * Date: 2025/8/30
 */
object JwtUtil {
    
    private const val SECRET_KEY = "infra-market-jwt-secret-key-2025-very-long-secret-key-for-security"
    private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000L // 24小时
    
    /**
     * 生成JWT token
     */
    fun generateToken(userId: Long, username: String): String {
        val now = Date()
        val expiration = Date(now.time + EXPIRATION_TIME)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("userId", userId)
            .claim("username", username)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }
    
    /**
     * 生成JWT token（仅用户ID）
     */
    fun generateToken(userId: Long): String {
        return generateToken(userId, "user_$userId")
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
     * 检查 Token 是否过期
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
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
