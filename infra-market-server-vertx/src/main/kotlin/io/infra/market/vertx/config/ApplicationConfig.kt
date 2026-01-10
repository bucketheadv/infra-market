package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * 应用配置
 * 
 * 包含所有配置项的统一配置类
 */
data class ApplicationConfig(
    val server: ServerConfig,
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val jwt: JwtConfig,
    val aes: AesConfig
) {
    companion object {
        fun fromJson(json: JsonObject): ApplicationConfig {
            return ApplicationConfig(
                server = ServerConfig.fromJson(json.getJsonObject("server", JsonObject())),
                database = DatabaseConfig.fromJson(json.getJsonObject("database", JsonObject())),
                redis = RedisConfig.fromJson(json.getJsonObject("redis", JsonObject())),
                jwt = JwtConfig.fromJson(json.getJsonObject("jwt")),
                aes = AesConfig.fromJson(json.getJsonObject("aes"))
            )
        }
        
        fun default(): ApplicationConfig {
            return ApplicationConfig(
                server = ServerConfig(),
                database = DatabaseConfig(
                    host = "localhost",
                    port = 3306,
                    database = null,
                    username = null,
                    password = null,
                    maxPoolSize = 10,
                    charset = "utf8mb4",
                    collation = "utf8mb4_unicode_ci"
                ),
                redis = RedisConfig(),
                jwt = JwtConfig(secretKey = null),
                aes = AesConfig(defaultKey = null)
            )
        }
    }
}

