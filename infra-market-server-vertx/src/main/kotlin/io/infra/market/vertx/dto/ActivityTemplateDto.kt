package io.infra.market.vertx.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 活动模板DTO
 * 
 * 用于活动模板信息的传输对象，包含模板的基本信息和字段配置。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityTemplateDto(
    /**
     * 模板ID
     * 主键，唯一标识一个模板
     */
    var id: Long? = null,

    /**
     * 模板名称
     * 模板的显示名称，用于界面展示和识别
     */
    var name: String? = null,

    /**
     * 模板描述
     * 模板的详细说明，用于文档和用户理解
     */
    var description: String? = null,

    /**
     * 字段配置列表
     * 模板的所有字段配置，以List格式存储
     */
    var fields: List<Map<String, Any>>? = null,

    /**
     * 模板状态
     * 1-启用，0-禁用
     */
    var status: Int? = null,

    /**
     * 创建时间
     * 模板记录的创建时间，格式化的字符串
     */
    var createTime: String? = null,

    /**
     * 更新时间
     * 模板记录的最后修改时间，格式化的字符串
     */
    var updateTime: String? = null
)

/**
 * 活动模板表单DTO
 * 
 * 用于活动模板创建和更新的表单数据传输对象。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityTemplateFormDto(
    /**
     * 模板名称
     * 必填字段，模板的显示名称
     */
    @field:NotBlank(message = "模板名称不能为空")
    @field:Size(min = 1, max = 100, message = "模板名称长度必须在1-100字符之间")
    var name: String? = null,

    /**
     * 模板描述
     * 可选字段，模板的详细说明
     */
    @field:Size(max = 500, message = "模板描述长度不能超过500字符")
    var description: String? = null,

    /**
     * 字段配置列表
     * 模板的所有字段配置
     */
    var fields: List<Map<String, Any>>? = null,

    /**
     * 模板状态
     * 1-启用，0-禁用
     */
    @field:Min(value = 0, message = "状态值不能小于0")
    @field:Max(value = 1, message = "状态值不能大于1")
    var status: Int? = null
)

/**
 * 活动模板查询DTO
 * 
 * 用于活动模板列表查询的查询条件对象。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityTemplateQueryDto(
    /**
     * 模板名称
     * 支持模糊查询
     */
    var name: String? = null,

    /**
     * 模板状态
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
