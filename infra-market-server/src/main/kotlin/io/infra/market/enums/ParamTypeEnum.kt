package io.infra.market.enums

/**
 * 参数类型枚举
 */
enum class ParamTypeEnum(val code: String, val description: String) {
    URL_PARAM("URL_PARAM", "URL参数"),
    BODY_PARAM("BODY_PARAM", "Body参数"),
    HEADER_PARAM("HEADER_PARAM", "Header参数");

    companion object {
        fun fromCode(code: String): ParamTypeEnum? {
            return entries.find { it.code == code }
        }
    }
}
