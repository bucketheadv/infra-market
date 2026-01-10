package io.infra.market.vertx.config

import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 配置加载器
 * 
 * 支持从以下位置加载配置（按优先级）：
 * 1. 系统属性指定的配置文件路径
 * 2. 当前目录下的 application.json
 * 3. classpath 下的 application.json
 */
object ConfigLoader {
    
    private val logger = LoggerFactory.getLogger(ConfigLoader::class.java)
    private const val CONFIG_FILE_PROPERTY = "vertx.config.file"
    private const val CONFIG_FILE_NAME = "application.json"
    
    /**
     * 加载配置
     * 
     * @param defaultConfig 默认配置（如果配置文件不存在时使用）
     * @return 配置对象
     */
    fun loadConfig(defaultConfig: ApplicationConfig = ApplicationConfig.default()): ApplicationConfig {
        val jsonConfig = try {
            // 1. 尝试从系统属性指定的路径加载
            val configFileProperty = System.getProperty(CONFIG_FILE_PROPERTY)
            if (configFileProperty != null) {
                val file = File(configFileProperty)
                if (file.exists() && file.isFile) {
                    logger.info("从系统属性指定的路径加载配置文件: {}", configFileProperty)
                    return loadFromFile(file)
                } else {
                    logger.warn("系统属性指定的配置文件不存在: {}", configFileProperty)
                }
            }
            
            // 2. 尝试从当前目录加载
            val currentDirFile = File(CONFIG_FILE_NAME)
            if (currentDirFile.exists() && currentDirFile.isFile) {
                logger.info("从当前目录加载配置文件: {}", currentDirFile.absolutePath)
                return loadFromFile(currentDirFile)
            }
            
            // 3. 尝试从 classpath 加载
            val classpathResource = ConfigLoader::class.java.classLoader.getResourceAsStream(CONFIG_FILE_NAME)
            if (classpathResource != null) {
                logger.info("从 classpath 加载配置文件: {}", CONFIG_FILE_NAME)
                val content = classpathResource.bufferedReader().use { it.readText() }
                JsonObject(content)
            } else {
                logger.warn("未找到配置文件，使用默认配置")
                null
            }
        } catch (e: Exception) {
            logger.error("加载配置文件失败，使用默认配置", e)
            null
        }
        
        return if (jsonConfig != null) {
            mergeWithDefaults(jsonConfig, defaultConfig)
        } else {
            defaultConfig
        }
    }
    
    /**
     * 从文件加载配置
     */
    private fun loadFromFile(file: File): ApplicationConfig {
        val content = Files.readString(Paths.get(file.absolutePath))
        val jsonConfig = JsonObject(content)
        return ApplicationConfig.fromJson(jsonConfig)
    }
    
    /**
     * 合并配置，确保所有必需的配置项都存在
     */
    private fun mergeWithDefaults(jsonConfig: JsonObject, defaults: ApplicationConfig): ApplicationConfig {
        val mergedJson = JsonObject()
        
        // 合并 server 配置
        val serverJson = jsonConfig.getJsonObject("server")
        mergedJson.put("server", serverJson ?: JsonObject().put("port", defaults.server.port))
        
        // 合并 database 配置
        val databaseJson = jsonConfig.getJsonObject("database")
        if (databaseJson != null) {
            mergedJson.put("database", databaseJson)
        } else {
            val defaultDbJson = JsonObject()
                .put("host", defaults.database.host)
                .put("port", defaults.database.port)
                .put("maxPoolSize", defaults.database.maxPoolSize)
                .put("charset", defaults.database.charset)
                .put("collation", defaults.database.collation)
            if (defaults.database.database != null) {
                defaultDbJson.put("database", defaults.database.database)
            }
            if (defaults.database.username != null) {
                defaultDbJson.put("username", defaults.database.username)
            }
            if (defaults.database.password != null) {
                defaultDbJson.put("password", defaults.database.password)
            }
            mergedJson.put("database", defaultDbJson)
        }
        
        // 合并 redis 配置
        val redisJson = jsonConfig.getJsonObject("redis")
        if (redisJson != null) {
            mergedJson.put("redis", redisJson)
        } else {
            mergedJson.put("redis", JsonObject()
                .put("host", defaults.redis.host)
                .put("port", defaults.redis.port)
            )
        }
        
        // 合并 jwt 配置
        val jwtJson = jsonConfig.getJsonObject("jwt")
        if (jwtJson != null) {
            mergedJson.put("jwt", jwtJson)
        } else {
            val defaultJwtJson = JsonObject()
                .put("expirationTime", defaults.jwt.expirationTime)
            if (defaults.jwt.secretKey != null) {
                defaultJwtJson.put("secretKey", defaults.jwt.secretKey)
            }
            mergedJson.put("jwt", defaultJwtJson)
        }
        
        // 合并 aes 配置
        val aesJson = jsonConfig.getJsonObject("aes")
        if (aesJson != null) {
            mergedJson.put("aes", aesJson)
        } else {
            val defaultAesJson = JsonObject()
            if (defaults.aes.defaultKey != null) {
                defaultAesJson.put("defaultKey", defaults.aes.defaultKey)
            }
            mergedJson.put("aes", defaultAesJson)
        }
        
        return ApplicationConfig.fromJson(mergedJson)
    }
}

