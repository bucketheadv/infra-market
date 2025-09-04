package io.infra.market.enums

/**
 * 权限类型枚举
 * @author liuqinglin
 * Date: 2025/8/30
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




