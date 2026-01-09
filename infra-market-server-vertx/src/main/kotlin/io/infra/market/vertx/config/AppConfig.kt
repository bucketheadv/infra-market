package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject

/**
 * 应用配置管理器
 * 
 * 单例模式，用于管理全局配置
 */
object AppConfig {
    
    private var config: JsonObject = JsonObject()
    
    /**
     * 初始化配置
     */
    fun init(config: JsonObject) {
        AppConfig.config = config
    }
    
    /**
     * 获取 JWT 密钥
     */
    fun getJwtSecretKey(): String {
        return config.getJsonObject("jwt", JsonObject())
            .getString("secretKey", "infra-market-jwt-secret-key-2024-very-long-secret-key-for-security")
    }
    
    /**
     * 获取 JWT 过期时间（毫秒）
     */
    fun getJwtExpirationTime(): Long {
        return config.getJsonObject("jwt", JsonObject())
            .getLong("expirationTime", 3 * 24 * 60 * 60 * 1000L) // 默认3天
    }
    
    /**
     * 获取 AES 默认密钥
     */
    fun getAesDefaultKey(): String {
        return config.getJsonObject("aes", JsonObject())
            .getString("defaultKey", "InfraMarketSecretKey2024")
    }
}

