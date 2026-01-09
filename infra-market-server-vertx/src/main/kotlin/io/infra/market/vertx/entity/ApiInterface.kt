package io.infra.market.vertx.entity

/**
 * 接口管理实体类
 */
data class ApiInterface(
    var id: Long? = null,
    var name: String? = null,
    var method: String? = null,
    var url: String? = null,
    var description: String? = null,
    var postType: String? = null,
    var params: String? = null,
    var status: Int? = null,
    var environment: String? = null,
    var timeout: Long? = null,
    var valuePath: String? = null,
    var createTime: Long? = null,
    var updateTime: Long? = null
)

