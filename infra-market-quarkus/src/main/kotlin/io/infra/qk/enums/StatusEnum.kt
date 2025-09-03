package io.infra.qk.enums

/**
 * 状态枚举
 * @author liuqinglin
 * Date: 2025/8/30
 */
enum class StatusEnum(val code: String, val desc: String) {
    ACTIVE("active", "启用"),
    INACTIVE("inactive", "禁用"),
    DELETED("deleted", "已删除");
    
    companion object {
        fun fromCode(code: String): StatusEnum? {
            return values().find { it.code == code }
        }
    }
}
