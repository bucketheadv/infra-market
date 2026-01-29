package io.infra.market.dto

/**
 * 活动DTO
 * 
 * 用于活动信息的传输对象，包含活动的基本信息和配置数据。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityDto(
    /**
     * 活动ID
     * 主键，唯一标识一个活动
     */
    var id: Long? = null,

    /**
     * 活动名称
     * 活动的显示名称，用于界面展示和识别
     */
    var name: String? = null,

    /**
     * 活动描述
     * 活动的详细说明，用于文档和用户理解
     */
    var description: String? = null,

    /**
     * 模板ID
     * 关联的活动模板ID
     */
    var templateId: Long? = null,

    /**
     * 模板名称
     * 关联的活动模板名称，用于显示
     */
    var templateName: String? = null,

    /**
     * 配置数据
     * 活动的配置数据，以Map格式存储
     */
    var configData: Map<String, Any>? = null,

    /**
     * 活动状态
     * 1-启用，0-禁用
     */
    var status: Int? = null,

    /**
     * 创建时间
     * 活动记录的创建时间，格式化的字符串
     */
    var createTime: String? = null,

    /**
     * 更新时间
     * 活动记录的最后修改时间，格式化的字符串
     */
    var updateTime: String? = null
)

/**
 * 活动表单DTO
 * 
 * 用于活动创建和更新的表单数据传输对象。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityFormDto(
    /**
     * 活动ID
     * 更新时使用，创建时为空
     */
    var id: Long? = null,

    /**
     * 活动名称
     * 必填字段，活动的显示名称
     */
    var name: String? = null,

    /**
     * 活动描述
     * 可选字段，活动的详细说明
     */
    var description: String? = null,

    /**
     * 模板ID
     * 必填字段，关联的活动模板ID
     */
    var templateId: Long? = null,

    /**
     * 配置数据
     * 活动的配置数据，以Map格式存储
     */
    var configData: Map<String, Any>? = null,

    /**
     * 活动状态
     * 1-启用，0-禁用
     */
    var status: Int? = null
)

/**
 * 活动查询DTO
 * 
 * 用于活动列表查询的查询条件对象。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityQueryDto(
    /**
     * 活动名称
     * 支持模糊查询
     */
    var name: String? = null,

    /**
     * 模板ID
     * 精确匹配查询
     */
    var templateId: Long? = null,

    /**
     * 活动状态
     * 1-启用，0-禁用
     */
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
