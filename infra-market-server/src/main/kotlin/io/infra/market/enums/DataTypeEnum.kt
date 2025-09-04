package io.infra.market.enums

/**
 * 数据类型枚举
 */
enum class DataTypeEnum(val code: String, val description: String) {
    STRING("STRING", "字符串"),
    INTEGER("INTEGER", "整数"),
    LONG("LONG", "长整数"),
    DOUBLE("DOUBLE", "双精度浮点数"),
    BOOLEAN("BOOLEAN", "布尔值"),
    DATE("DATE", "日期"),
    DATETIME("DATETIME", "日期时间"),
    JSON("JSON", "JSON对象");

    companion object {
        fun fromCode(code: String): DataTypeEnum? {
            return values().find { it.code == code }
        }
    }
}
