package io.infra.market.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Jackson配置类
 * 
 * 配置Jackson的序列化和反序列化行为，包括Joda Time支持。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@Configuration
class JacksonConfig {

    /**
     * 配置ObjectMapper Bean
     * 
     * 注册Kotlin模块和Joda Time模块，支持Kotlin数据类和Joda DateTime的序列化。
     * 
     * @return 配置好的ObjectMapper实例
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JodaModule())
    }
}
