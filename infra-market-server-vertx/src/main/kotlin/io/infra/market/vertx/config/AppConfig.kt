package io.infra.market.vertx.config

/**
 * 应用配置管理器
 * 
 * 单例模式，用于管理全局配置
 */
object AppConfig {
    
    private var config: ApplicationConfig = ApplicationConfig.default()
    
    /**
     * 初始化配置
     */
    fun init(config: ApplicationConfig) {
        AppConfig.config = config
    }
    
    /**
     * 获取 JWT 密钥
     */
    fun getJwtSecretKey(): String? {
        return config.jwt.secretKey
    }
    
    /**
     * 获取 JWT 过期时间（毫秒）
     */
    fun getJwtExpirationTime(): Long {
        return config.jwt.expirationTime
    }
    
    /**
     * 获取 AES 默认密钥
     */
    fun getAesDefaultKey(): String? {
        return config.aes.defaultKey
    }
}

