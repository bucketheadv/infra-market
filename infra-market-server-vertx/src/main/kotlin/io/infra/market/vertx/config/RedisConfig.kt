package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * Redis 配置
 */
data class RedisConfig(
    val host: String = "localhost",
    val port: Int = 6379
) {
    companion object {
        fun fromJson(json: JsonObject): RedisConfig {
            return RedisConfig(
                host = json.getString("host", "localhost"),
                port = json.getInteger("port", 6379)
            )
        }
    }
}

