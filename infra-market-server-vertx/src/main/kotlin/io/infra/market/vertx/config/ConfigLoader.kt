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
    fun loadConfig(defaultConfig: JsonObject = JsonObject()): JsonObject {
        val config = try {
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
                defaultConfig
            }
        } catch (e: Exception) {
            logger.error("加载配置文件失败，使用默认配置", e)
            defaultConfig
        }
        
        return mergeWithDefaults(config, defaultConfig)
    }
    
    /**
     * 从文件加载配置
     */
    private fun loadFromFile(file: File): JsonObject {
        val content = Files.readString(Paths.get(file.absolutePath))
        return JsonObject(content)
    }
    
    /**
     * 合并配置，确保所有必需的配置项都存在
     */
    private fun mergeWithDefaults(config: JsonObject, defaults: JsonObject): JsonObject {
        val merged = JsonObject(defaults.encode())
        
        // 合并 server 配置
        if (config.containsKey("server")) {
            merged.put("server", config.getJsonObject("server"))
        } else if (!merged.containsKey("server")) {
            merged.put("server", JsonObject().put("port", 8080))
        }
        
        // 合并 database 配置
        if (config.containsKey("database")) {
            merged.put("database", config.getJsonObject("database"))
        } else if (!merged.containsKey("database")) {
            merged.put("database", JsonObject()
                .put("host", "localhost")
                .put("port", 3306)
                .put("database", "infra_market")
                .put("username", "root")
                .put("password", "123456")
                .put("maxPoolSize", 10)
                .put("charset", "utf8mb4")
                .put("collation", "utf8mb4_unicode_ci")
            )
        }
        
        // 合并 redis 配置
        if (config.containsKey("redis")) {
            merged.put("redis", config.getJsonObject("redis"))
        } else if (!merged.containsKey("redis")) {
            merged.put("redis", JsonObject()
                .put("host", "localhost")
                .put("port", 6379)
            )
        }
        
        // 合并 jwt 配置
        if (config.containsKey("jwt")) {
            merged.put("jwt", config.getJsonObject("jwt"))
        }
        
        // 合并 aes 配置
        if (config.containsKey("aes")) {
            merged.put("aes", config.getJsonObject("aes"))
        }
        
        return merged
    }
}

