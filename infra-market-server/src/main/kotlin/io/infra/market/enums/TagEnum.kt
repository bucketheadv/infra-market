package io.infra.market.enums

/**
 * 接口标签枚举
 */
enum class TagEnum(val code: String, val label: String) {
    TEST("TEST", "测试"),
    PRODUCTION("PRODUCTION", "正式");

    companion object {
        fun fromCode(code: String): TagEnum? {
            return values().find { it.code == code }
        }
    }
}
