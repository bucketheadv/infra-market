package io.infra.market.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * 输入类型枚举
 */
enum class InputTypeEnum(@JsonValue val code: String, val description: String) {
    TEXT("TEXT", "文本框"),
    SELECT("SELECT", "下拉框"),
    MULTI_SELECT("MULTI_SELECT", "多选下拉框"),
    DATE("DATE", "日期"),
    DATETIME("DATETIME", "日期时间"),
    NUMBER("NUMBER", "数字"),
    TEXTAREA("TEXTAREA", "多行文本"),
    CODE("CODE", "代码编辑器"),
    PASSWORD("PASSWORD", "密码"),
    EMAIL("EMAIL", "邮箱"),
    URL("URL", "URL"),
    CHECKBOX("CHECKBOX", "复选框");

    companion object {
        @JsonCreator
        fun fromCode(code: String): InputTypeEnum? {
            return entries.find { it.code == code }
        }
    }
}
