package io.infra.market.vertx.enums

/**
 * 接口环境枚举
 */
enum class EnvironmentEnum(val code: String, val label: String) {
    TEST("TEST", "测试"),
    PRODUCTION("PRODUCTION", "正式");

    companion object {
        fun fromCode(code: String): EnvironmentEnum? {
            return entries.find { it.code == code }
        }
    }
}

