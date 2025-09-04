package io.infra.market.enums

/**
 * HTTP请求方法枚举
 */
enum class HttpMethodEnum(val code: String, val description: String) {
    GET("GET", "GET请求"),
    POST("POST", "POST请求"),
    PUT("PUT", "PUT请求"),
    DELETE("DELETE", "DELETE请求"),
    PATCH("PATCH", "PATCH请求"),
    HEAD("HEAD", "HEAD请求"),
    OPTIONS("OPTIONS", "OPTIONS请求");

    companion object {
        fun fromCode(code: String): HttpMethodEnum? {
            return values().find { it.code == code }
        }
    }
}
