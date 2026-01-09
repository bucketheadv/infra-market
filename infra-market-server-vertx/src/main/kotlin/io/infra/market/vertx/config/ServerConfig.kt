package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * 服务器配置
 */
data class ServerConfig(
    val port: Int = 8080
) {
    companion object {
        fun fromJson(json: JsonObject): ServerConfig {
            return ServerConfig(
                port = json.getInteger("port", 8080)
            )
        }
    }
}

