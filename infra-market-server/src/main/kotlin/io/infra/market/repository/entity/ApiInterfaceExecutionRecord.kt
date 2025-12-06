package io.infra.market.repository.entity

import com.mybatisflex.annotation.Column
import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.KeyType
import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 接口执行记录实体类
 * 
 * 用于记录接口执行的相关信息，包括执行人、参数、返回值等数据。
 * 支持大文本字段存储，适用于参数和返回值可能长度很大的场景。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@Table("api_interface_execution_record")
data class ApiInterfaceExecutionRecord(
    @Id(keyType = KeyType.Auto)
    override var id: Long? = null,

    /**
     * 接口ID
     * 关联到 api_interface 表
     */
    @Column("interface_id")
    var interfaceId: Long? = null,

    /**
     * 执行人ID
     * 关联到 user_info 表
     */
    @Column("executor_id")
    var executorId: Long? = null,

    /**
     * 执行人姓名
     * 冗余字段，便于查询和显示
     */
    @Column("executor_name")
    var executorName: String? = null,

    /**
     * 请求参数JSON
     * 存储URL参数和查询参数
     */
    @Column("request_params")
    var requestParams: String? = null,

    /**
     * 请求头JSON
     * 存储HTTP请求头信息
     */
    @Column("request_headers")
    var requestHeaders: String? = null,

    /**
     * 请求体JSON
     * 存储POST/PUT请求的请求体数据
     */
    @Column("request_body")
    var requestBody: String? = null,

    /**
     * 响应状态码
     * HTTP响应状态码
     */
    @Column("response_status")
    var responseStatus: Int? = null,

    /**
     * 响应头JSON
     * 存储HTTP响应头信息
     */
    @Column("response_headers")
    var responseHeaders: String? = null,

    /**
     * 响应体JSON
     * 存储接口返回的响应数据
     */
    @Column("response_body")
    var responseBody: String? = null,

    /**
     * 执行时间（毫秒）
     * 接口执行耗时
     */
    @Column("execution_time")
    var executionTime: Long? = null,

    /**
     * 是否成功
     * 1-成功，0-失败
     */
    @Column("success")
    var success: Boolean? = null,

    /**
     * 错误信息
     * 执行失败时的错误描述
     */
    @Column("error_message")
    var errorMessage: String? = null,

    /**
     * 备注
     * 执行记录的备注信息，选填
     */
    @Column("remark")
    var remark: String? = null,

    /**
     * 客户端IP
     * 执行请求的客户端IP地址
     */
    @Column("client_ip")
    var clientIp: String? = null,

    /**
     * 用户代理
     * 客户端浏览器信息
     */
    @Column("user_agent")
    var userAgent: String? = null
) : BaseActiveRecordEntity<ApiInterfaceExecutionRecord, Long>()
