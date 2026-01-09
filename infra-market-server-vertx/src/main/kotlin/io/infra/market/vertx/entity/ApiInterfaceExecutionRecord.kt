package io.infra.market.vertx.entity

/**
 * 接口执行记录实体类
 */
data class ApiInterfaceExecutionRecord(
    var id: Long? = null,
    var interfaceId: Long? = null,
    var executorId: Long? = null,
    var executorName: String? = null,
    var requestParams: String? = null,
    var requestHeaders: String? = null,
    var requestBody: String? = null,
    var responseStatus: Int? = null,
    var responseHeaders: String? = null,
    var responseBody: String? = null,
    var executionTime: Long? = null,
    var success: Boolean? = null,
    var errorMessage: String? = null,
    var remark: String? = null,
    var clientIp: String? = null,
    var userAgent: String? = null,
    var createTime: Long? = null,
    var updateTime: Long? = null
)

