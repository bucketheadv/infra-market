package io.infra.market.vertx.entity

/**
 * 角色实体类
 */
data class Role(
    var id: Long? = null,
    var name: String? = null,
    var code: String? = null,
    var description: String? = null,
    var status: String = "active",
    override var createTime: Long? = null,
    override var updateTime: Long? = null
) : TimestampedEntity

