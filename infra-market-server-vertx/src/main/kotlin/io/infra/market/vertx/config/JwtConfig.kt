package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * JWT 配置
 */
data class JwtConfig(
    val secretKey: String?,
    val expirationTime: Long = 3 * 24 * 60 * 60 * 1000L // 默认3天
) {
    companion object {
        fun fromJson(json: JsonObject?): JwtConfig {
            return if (json != null) {
                JwtConfig(
                    secretKey = json.getString("secretKey"),
                    expirationTime = json.getLong("expirationTime", 3 * 24 * 60 * 60 * 1000L)
                )
            } else {
                JwtConfig(secretKey = null)
            }
        }
    }
}

