package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * 数据库配置
 */
data class DatabaseConfig(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "infra_market",
    val username: String = "root",
    val password: String = "123456",
    val maxPoolSize: Int = 10,
    val charset: String = "utf8mb4",
    val collation: String = "utf8mb4_unicode_ci"
) {
    companion object {
        fun fromJson(json: JsonObject): DatabaseConfig {
            return DatabaseConfig(
                host = json.getString("host", "localhost"),
                port = json.getInteger("port", 3306),
                database = json.getString("database", "infra_market"),
                username = json.getString("username", "root"),
                password = json.getString("password", "123456"),
                maxPoolSize = json.getInteger("maxPoolSize", 10),
                charset = json.getString("charset", "utf8mb4"),
                collation = json.getString("collation", "utf8mb4_unicode_ci")
            )
        }
    }
}

