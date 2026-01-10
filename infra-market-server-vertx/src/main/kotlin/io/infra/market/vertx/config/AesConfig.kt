package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * AES 配置
 */
data class AesConfig(
    val defaultKey: String?
) {
    companion object {
        fun fromJson(json: JsonObject?): AesConfig {
            return if (json != null) {
                AesConfig(
                    defaultKey = json.getString("defaultKey")
                )
            } else {
                AesConfig(defaultKey = null)
            }
        }
    }
}

