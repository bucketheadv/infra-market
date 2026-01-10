package io.infra.market.vertx.entity

/**
 * 用户实体类
 */
data class User(
    var id: Long? = null,
    var username: String? = null,
    var password: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var status: String = "active",
    var lastLoginTime: Long? = null,
    override var createTime: Long? = null,
    override var updateTime: Long? = null
) : TimestampedEntity

