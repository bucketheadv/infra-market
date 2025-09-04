package io.infra.market.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * POST请求类型枚举
 */
enum class PostTypeEnum(@JsonValue val code: String, val description: String) {
    APPLICATION_JSON("application/json", "application/json"),
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded", "application/x-www-form-urlencoded");

    companion object {
        @JsonCreator
        fun fromCode(code: String): PostTypeEnum? {
            return entries.find { it.code == code }
        }
    }
}
