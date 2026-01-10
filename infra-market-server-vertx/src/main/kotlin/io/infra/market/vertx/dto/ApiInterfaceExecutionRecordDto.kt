package io.infra.market.vertx.dto

/**
 * 接口执行记录DTO
 */
data class ApiInterfaceExecutionRecordDto(
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

/**
 * 接口执行记录查询DTO
 */
data class ApiInterfaceExecutionRecordQueryDto(
    var interfaceId: Long? = null,
    var keyword: String? = null,
    var executorId: Long? = null,
    var success: Boolean? = null,
    var minExecutionTime: Long? = null,
    var maxExecutionTime: Long? = null,
    var startTime: Long? = null,
    var endTime: Long? = null,
    var page: Int? = null,
    var size: Int? = null
)

/**
 * 接口执行记录统计DTO
 */
data class ApiInterfaceExecutionRecordStatsDto(
    var totalExecutions: Long = 0,
    var successCount: Long = 0,
    var failureCount: Long = 0,
    var successRate: Double = 0.0,
    var averageExecutionTime: Double = 0.0,
    var minExecutionTime: Long = 0,
    var maxExecutionTime: Long = 0
)

