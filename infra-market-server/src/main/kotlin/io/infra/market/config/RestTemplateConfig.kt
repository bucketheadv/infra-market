package io.infra.market.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.http.client.SimpleClientHttpRequestFactory
import java.util.concurrent.TimeUnit

/**
 * RestTemplate配置
 */
@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(30000) // 连接超时30秒
        factory.setReadTimeout(60000) // 读取超时60秒
        
        return RestTemplate(factory)
    }
}
