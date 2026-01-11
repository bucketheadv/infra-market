package io.infra.market.vertx.entity

/**
 * 接口执行记录实体类
 * 
 * 用于记录接口执行的相关信息，包括执行人、参数、返回值等数据。
 * 支持大文本字段存储，适用于参数和返回值可能长度很大的场景。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceExecutionRecord(
    /**
     * 主键ID
     * 执行记录的唯一标识
     */
    var id: Long? = null,

    /**
     * 接口ID
     * 关联到 api_interface 表
     */
    var interfaceId: Long? = null,

    /**
     * 执行人ID
     * 关联到 user_info 表
     */
    var executorId: Long? = null,

    /**
     * 执行人姓名
     * 冗余字段，便于查询和显示
     */
    var executorName: String? = null,

    /**
     * 请求参数JSON
     * 存储URL参数和查询参数
     */
    var requestParams: String? = null,

    /**
     * 请求头JSON
     * 存储HTTP请求头信息
     */
    var requestHeaders: String? = null,

    /**
     * 请求体JSON
     * 存储POST/PUT请求的请求体数据
     */
    var requestBody: String? = null,

    /**
     * 响应状态码
     * HTTP响应状态码
     */
    var responseStatus: Int? = null,

    /**
     * 响应头JSON
     * 存储HTTP响应头信息
     */
    var responseHeaders: String? = null,

    /**
     * 响应体JSON
     * 存储接口返回的响应数据
     */
    var responseBody: String? = null,

    /**
     * 执行时间（毫秒）
     * 接口执行耗时
     */
    var executionTime: Long? = null,

    /**
     * 是否成功
     * 1-成功，0-失败
     */
    var success: Boolean? = null,

    /**
     * 错误信息
     * 执行失败时的错误描述
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
     * 执行记录的创建时间（毫秒时间戳）
     */
    override var createTime: Long? = null,

    /**
     * 更新时间
     * 执行记录的最后修改时间（毫秒时间戳）
     */
    override var updateTime: Long? = null
) : TimestampedEntity
