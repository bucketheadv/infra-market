package io.infra.market.vertx.enums

/**
 * 权限类型枚举
 */
enum class PermissionTypeEnum(val code: String, val desc: String) {
    MENU("menu", "菜单"),
    BUTTON("button", "按钮");
    
    companion object {
        fun fromCode(code: String): PermissionTypeEnum? {
            return entries.find { it.code == code }
        }
    }
}

