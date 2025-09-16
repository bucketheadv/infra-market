package io.infra.market.dto

import io.infra.market.util.DateTimeUtil
import org.joda.time.DateTime
import java.util.Date

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
     */
    var id: Long? = null,

    /**
     * 接口ID
     */
    var interfaceId: Long? = null,

    /**
     * 接口名称
     * 冗余字段，便于显示
     */
    var interfaceName: String? = null,

    /**
     * 执行人ID
     */
    var executorId: Long? = null,

    /**
     * 执行人姓名
     */
    var executorName: String? = null,

    /**
     * 请求参数JSON
     */
    var requestParams: String? = null,

    /**
     * 请求头JSON
     */
    var requestHeaders: String? = null,

    /**
     * 请求体JSON
     */
    var requestBody: String? = null,

    /**
     * 响应状态码
     */
    var responseStatus: Int? = null,

    /**
     * 响应头JSON
     */
    var responseHeaders: String? = null,

    /**
     * 响应体JSON
     */
    var responseBody: String? = null,

    /**
     * 执行时间（毫秒）
     */
    var executionTime: Long? = null,

    /**
     * 是否成功
     */
    var success: Boolean? = null,

    /**
     * 错误信息
     */
    var errorMessage: String? = null,

    /**
     * 客户端IP
     */
    var clientIp: String? = null,

    /**
     * 用户代理
     */
    var userAgent: String? = null,

    /**
     * 创建时间
     */
    var createTime: Long? = null,

    /**
     * 创建时间格式化字符串
     */
    var createTimeStr: String? = null,

    /**
     * 更新时间
     */
    var updateTime: Long? = null,

    /**
     * 更新时间格式化字符串
     */
    var updateTimeStr: String? = null
) {
    /**
     * 格式化时间字段
     */
    fun formatTimeFields() {
        createTimeStr = DateTimeUtil.formatDateTime(createTime)
        updateTimeStr = DateTimeUtil.formatDateTime(updateTime)
    }
}

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
     * 接口名称
     * 支持模糊查询
     */
    var interfaceName: String? = null,

    /**
     * 执行人ID
     * 精确匹配查询
     */
    var executorId: Long? = null,

    /**
     * 执行人姓名
     * 支持模糊查询
     */
    var executorName: String? = null,

    /**
     * 是否成功
     * 精确匹配查询
     */
    var success: Boolean? = null,

    /**
     * 开始时间
     * 执行时间范围查询的开始时间
     */
    var startTime: DateTime? = null,

    /**
     * 结束时间
     * 执行时间范围查询的结束时间
     */
    var endTime: DateTime? = null,

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
     * 页码
     * 分页查询的页码，从1开始
     */
    var page: Int? = null,

    /**
     * 每页大小
     * 分页查询的每页记录数
     */
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
     * 接口ID
     */
    var interfaceId: Long? = null,

    /**
     * 接口名称
     */
    var interfaceName: String? = null,

    /**
     * 总执行次数
     */
    var totalExecutions: Long = 0,

    /**
     * 成功执行次数
     */
    var successExecutions: Long = 0,

    /**
     * 失败执行次数
     */
    var failedExecutions: Long = 0,

    /**
     * 成功率（百分比）
     */
    var successRate: Double = 0.0,

    /**
     * 平均执行时间（毫秒）
     */
    var avgExecutionTime: Double = 0.0,

    /**
     * 最小执行时间（毫秒）
     */
    var minExecutionTime: Long = 0,

    /**
     * 最大执行时间（毫秒）
     */
    var maxExecutionTime: Long = 0,

    /**
     * 最后执行时间
     */
    var lastExecutionTime: DateTime? = null,

    /**
     * 最后执行时间格式化字符串
     */
    var lastExecutionTimeStr: String? = null
) {
    /**
     * 格式化时间字段
     */
    fun formatTimeFields() {
        lastExecutionTimeStr = DateTimeUtil.formatDateTime(lastExecutionTime?.millis)
    }
}
