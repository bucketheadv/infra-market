package io.infra.market.vertx.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory

/**
 * Jackson 配置类
 * 
 * 提供配置好的 ObjectMapper 实例，支持 Kotlin 数据类和 Joda Time。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
object JacksonConfig {
    
    private val logger = LoggerFactory.getLogger(JacksonConfig::class.java)
    
    /**
     * 配置好的 ObjectMapper 实例
     * 已注册 Kotlin 模块和 Joda Time 模块
     */
    val objectMapper: ObjectMapper by lazy {
        jacksonObjectMapper().apply {
            registerModule(JodaModule())
        }
    }
    
    /**
     * 初始化 Jackson 配置
     * 
     * 预初始化 ObjectMapper（延迟初始化会在首次使用时自动执行）
     */
    fun init() {
        try {
            // 预初始化 ObjectMapper，确保模块已注册
            objectMapper
            logger.info("Jackson Kotlin 模块和 Joda Time 模块已配置")
        } catch (e: Exception) {
            logger.warn("初始化 Jackson 配置失败: {}", e.message)
            logger.debug("详细错误信息", e)
        }
    }
}

