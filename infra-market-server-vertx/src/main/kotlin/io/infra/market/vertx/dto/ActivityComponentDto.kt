package io.infra.market.vertx.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 活动组件DTO
 * 
 * 用于活动组件信息的传输对象，包含组件的基本信息和字段/组件配置。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityComponentDto(
    /**
     * 组件ID
     * 主键，唯一标识一个组件
     */
    var id: Long? = null,

    /**
     * 组件名称
     * 组件的显示名称，用于界面展示和识别
     */
    var name: String? = null,

    /**
     * 组件描述
     * 组件的详细说明，用于文档和用户理解
     */
    var description: String? = null,

    /**
     * 字段/组件配置列表
     * 组件的所有字段和嵌套组件配置，以List格式存储
     */
    var fields: List<Map<String, Any>>? = null,

    /**
     * 组件状态
     * 1-启用，0-禁用
     */
    var status: Int? = null,

    /**
     * 创建时间
     * 组件记录的创建时间，格式化的字符串
     */
    var createTime: String? = null,

    /**
     * 更新时间
     * 组件记录的最后修改时间，格式化的字符串
     */
    var updateTime: String? = null
)

/**
 * 活动组件表单DTO
 * 
 * 用于活动组件创建和更新的表单数据传输对象。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityComponentFormDto(
    /**
     * 组件名称
     * 必填字段，组件的显示名称
     */
    @field:NotBlank(message = "组件名称不能为空")
    @field:Size(min = 1, max = 100, message = "组件名称长度必须在1-100字符之间")
    var name: String? = null,

    /**
     * 组件描述
     * 可选字段，组件的详细说明
     */
    @field:Size(max = 500, message = "组件描述长度不能超过500字符")
    var description: String? = null,

    /**
     * 字段/组件配置列表
     * 组件的所有字段和嵌套组件配置
     */
    var fields: List<Map<String, Any>>? = null,

    /**
     * 组件状态
     * 1-启用，0-禁用
     */
    @field:Min(value = 0, message = "状态值不能小于0")
    @field:Max(value = 1, message = "状态值不能大于1")
    var status: Int? = null
)

/**
 * 活动组件查询DTO
 * 
 * 用于活动组件列表查询的查询条件对象。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityComponentQueryDto(
    /**
     * 组件名称
     * 支持模糊查询
     */
    var name: String? = null,

    /**
     * 组件状态
     * 1-启用，0-禁用
     */
    @field:Min(value = 0, message = "状态值不能小于0")
    @field:Max(value = 1, message = "状态值不能大于1")
    var status: Int? = null,

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
