package io.infra.market.vertx.entity

/**
 * 角色权限关联实体类
 */
data class RolePermission(
    var id: Long? = null,
    var roleId: Long? = null,
    var permissionId: Long? = null,
    var createTime: Long? = null,
    var updateTime: Long? = null
)

