package io.infra.market.vertx.entity

/**
 * 权限实体类
 */
data class Permission(
    var id: Long? = null,
    var name: String? = null,
    var code: String? = null,
    var type: String = "menu",
    var parentId: Long? = null,
    var path: String? = null,
    var icon: String? = null,
    var sort: Int = 0,
    var status: String = "active",
    var createTime: Long? = null,
    var updateTime: Long? = null
)

