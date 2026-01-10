package io.infra.market.vertx.entity

/**
 * 角色权限关联实体类
 */
data class RolePermission(
    var id: Long? = null,
    var roleId: Long? = null,
    var permissionId: Long? = null,
    override var createTime: Long? = null,
    override var updateTime: Long? = null
) : TimestampedEntity

