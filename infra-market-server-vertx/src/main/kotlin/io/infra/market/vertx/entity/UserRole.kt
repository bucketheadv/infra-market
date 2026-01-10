package io.infra.market.vertx.entity

/**
 * 用户角色关联实体类
 */
data class UserRole(
    var id: Long? = null,
    var uid: Long? = null,
    var roleId: Long? = null,
    override var createTime: Long? = null,
    override var updateTime: Long? = null
) : TimestampedEntity

