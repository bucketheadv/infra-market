package io.infra.market.vertx.extensions

import io.infra.market.vertx.config.JacksonConfig
import io.infra.market.vertx.util.ValidationUtil
import io.vertx.core.MultiMap
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * JsonObject 扩展函数
 * 
 * 提供支持 Kotlin 数据类的 mapTo 方法
 */

/**
 * 将 JsonObject 转换为 Kotlin 数据类
 * 
 * 使用配置好的 ObjectMapper（已注册 Kotlin 模块）进行反序列化
 * 
 * @param T 目标类型
 * @param validate 是否进行参数验证（类似 Spring 的 @Valid），默认为 false
 * @return 转换后的对象
 */
inline fun <reified T> JsonObject.mapTo(validate: Boolean = false): T {
    // 使用 ObjectMapper 的 readValue 方法，将 JsonObject 的字符串表示反序列化
    val obj = JacksonConfig.objectMapper.readValue(this.encode(), T::class.java)
    
    // 如果启用验证，则进行参数校验
    if (validate) {
        ValidationUtil.validateOrThrow(obj)
    }
    
    return obj
}

/**
 * 将 MultiMap（查询参数）转换为 JsonObject
 * 
 * @return JsonObject 对象
 */
fun MultiMap.toJsonObject(): JsonObject {
    val json = JsonObject()
    this.forEach { entry ->
        json.put(entry.key, entry.value)
    }
    return json
}

/**
 * 从 RoutingContext 的查询参数转换为 Kotlin 数据类
 * 
 * 查询参数中的字符串会自动转换为对应的类型（数字、布尔值等）
 * Jackson 会自动处理类型转换
 * 
 * @param T 目标类型
 * @param validate 是否进行参数验证（类似 Spring 的 @Valid），默认为 false
 * @return 转换后的对象
 */
inline fun <reified T> RoutingContext.queryParamsTo(validate: Boolean = false): T {
    val json = JsonObject()
    this.queryParams().forEach { entry ->
        val value = entry.value
        // Jackson 会自动将字符串转换为对应的类型（Int, Long, Boolean 等）
        json.put(entry.key, value)
    }
    return json.mapTo<T>(validate)
}

