package io.infra.market.vertx.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * 接口执行记录DTO
 * 
 * 用于接口执行记录的数据传输对象。
 * 包含执行记录的基本信息和执行结果。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceExecutionRecordDto(
    /**
     * 主键ID
     * 执行记录的唯一标识
     */
    var id: Long? = null,

    /**
     * 接口ID
     * 关联到接口表的ID
     */
    var interfaceId: Long? = null,

    /**
     * 执行人ID
     * 执行接口的用户ID
     */
    var executorId: Long? = null,

    /**
     * 执行人姓名
     * 执行接口的用户姓名，冗余字段便于显示
     */
    var executorName: String? = null,

    /**
     * 请求参数JSON
     * 存储URL参数和查询参数的JSON字符串
     */
    var requestParams: String? = null,

    /**
     * 请求头JSON
     * 存储HTTP请求头信息的JSON字符串
     */
    var requestHeaders: String? = null,

    /**
     * 请求体JSON
     * 存储POST/PUT请求的请求体数据的JSON字符串
     */
    var requestBody: String? = null,

    /**
     * 响应状态码
     * 目标接口返回的HTTP响应状态码
     */
    var responseStatus: Int? = null,

    /**
     * 响应头JSON
     * 存储HTTP响应头信息的JSON字符串
     */
    var responseHeaders: String? = null,

    /**
     * 响应体JSON
     * 存储接口返回的响应数据的JSON字符串
     */
    var responseBody: String? = null,

    /**
     * 执行时间（毫秒）
     * 接口执行的总耗时，单位为毫秒
     */
    var executionTime: Long? = null,

    /**
     * 是否成功
     * true-成功，false-失败
     */
    var success: Boolean? = null,

    /**
     * 错误信息
     * 执行失败时的错误描述，成功时为null
     */
    var errorMessage: String? = null,

    /**
     * 备注
     * 执行记录的备注信息，选填
     */
    var remark: String? = null,

    /**
     * 客户端IP
     * 执行请求的客户端IP地址
     */
    var clientIp: String? = null,

    /**
     * 用户代理
     * 客户端浏览器信息
     */
    var userAgent: String? = null,

    /**
     * 创建时间
     * 执行记录的创建时间，毫秒时间戳
     */
    var createTime: Long? = null,

    /**
     * 更新时间
     * 执行记录的最后修改时间，毫秒时间戳
     */
    var updateTime: Long? = null
)

/**
 * 接口执行记录查询DTO
 * 
 * 用于接口执行记录的查询条件。
 * 支持按接口、执行人、执行时间等条件查询。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceExecutionRecordQueryDto(
    /**
     * 接口ID
     * 精确匹配查询
     */
    var interfaceId: Long? = null,

    /**
     * 关键字
     * 支持在执行人姓名、错误信息、备注等字段中模糊查询
     */
    var keyword: String? = null,

    /**
     * 执行人ID
     * 精确匹配查询
     */
    var executorId: Long? = null,

    /**
     * 是否成功
     * 精确匹配查询
     */
    var success: Boolean? = null,

    /**
     * 最小执行时间（毫秒）
     * 执行耗时范围查询的最小值
     */
    var minExecutionTime: Long? = null,

    /**
     * 最大执行时间（毫秒）
     * 执行耗时范围查询的最大值
     */
    var maxExecutionTime: Long? = null,

    /**
     * 开始时间
     * 执行时间范围查询的开始时间，毫秒时间戳
     */
    var startTime: Long? = null,

    /**
     * 结束时间
     * 执行时间范围查询的结束时间，毫秒时间戳
     */
    var endTime: Long? = null,

    /**
     * 页码
     * 分页查询的页码，从1开始
     */
    @field:Min(value = 1, message = "页码不能小于1")
    var page: Int? = null,

    /**
     * 每页大小
     * 分页查询的每页记录数
     */
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    var size: Int? = null
)

/**
 * 接口执行记录统计DTO
 * 
 * 用于接口执行记录的统计信息。
 * 包含执行次数、成功率、平均执行时间等统计数据。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceExecutionRecordStatsDto(
    /**
     * 总执行次数
     * 接口的总执行次数
     */
    var totalExecutions: Long = 0,

    /**
     * 成功执行次数
     * 接口成功执行的次数
     */
    var successCount: Long = 0,

    /**
     * 失败执行次数
     * 接口失败执行的次数
     */
    var failureCount: Long = 0,

    /**
     * 成功率（百分比）
     * 成功执行次数占总执行次数的百分比，范围0-100
     */
    var successRate: Double = 0.0,

    /**
     * 平均执行时间（毫秒）
     * 所有执行记录的平均耗时，单位为毫秒
     */
    var averageExecutionTime: Double = 0.0,

    /**
     * 最小执行时间（毫秒）
     * 所有执行记录中的最小耗时，单位为毫秒
     */
    var minExecutionTime: Long = 0,

    /**
     * 最大执行时间（毫秒）
     * 所有执行记录中的最大耗时，单位为毫秒
     */
    var maxExecutionTime: Long = 0
)
