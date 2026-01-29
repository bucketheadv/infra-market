package io.infra.market.dto

import io.infra.market.enums.DataTypeEnum
import io.infra.market.enums.InputTypeEnum

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
     * 模板的所有字段配置
     */
    var fields: List<ActivityTemplateFieldDto>? = null,

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
 * 活动模板字段配置DTO
 * 
 * 用于定义活动模板的字段配置，支持多种数据类型和输入类型。
 * 支持嵌套的数组和对象结构。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityTemplateFieldDto(
    /**
     * 字段名称
     * 字段的唯一标识符，用于数据存储和识别
     */
    var name: String? = null,
    
    /**
     * 字段中文名称
     * 字段的中文显示名称，用于界面展示和用户理解
     */
    var label: String? = null,
    
    /**
     * 字段类型
     * 字段的数据类型：STRING、INTEGER、ARRAY、JSON_OBJECT等
     */
    var type: DataTypeEnum? = null,
    
    /**
     * 输入类型
     * 字段在界面上的输入方式：TEXT、SELECT、MULTI_SELECT、NUMBER等
     */
    var inputType: InputTypeEnum? = null,
    
    /**
     * 是否必填
     * true-必填，false-可选，用于参数校验
     */
    var required: Boolean? = null,
    
    /**
     * 默认值
     * 字段的默认值，支持字符串、数字、数组、对象等多种类型
     */
    var defaultValue: Any? = null,
    
    /**
     * 字段描述
     * 字段的详细说明，用于文档和用户理解
     */
    var description: String? = null,
    
    /**
     * 排序号
     * 字段在界面上的显示顺序，数值越小越靠前
     */
    var sort: Int? = null,
    
    /**
     * 下拉选项列表
     * 当输入类型为SELECT或MULTI_SELECT时的选项配置
     */
    var options: List<SelectOptionDto>? = null,
    
    /**
     * 是否支持多选
     * 当输入类型为SELECT时，此字段控制是否支持多选
     * true-多选，false-单选
     */
    var multiple: Boolean? = null,
    
    /**
     * 最小值
     * 当类型为INTEGER或DOUBLE时的最小值限制
     */
    var min: Number? = null,
    
    /**
     * 最大值
     * 当类型为INTEGER或DOUBLE时的最大值限制
     */
    var max: Number? = null,
    
    /**
     * 最小长度
     * 当类型为STRING或ARRAY时的最小长度限制
     */
    var minLength: Int? = null,
    
    /**
     * 最大长度
     * 当类型为STRING或ARRAY时的最大长度限制
     */
    var maxLength: Int? = null,
    
    /**
     * 正则表达式
     * 当类型为STRING时的格式验证规则
     */
    var pattern: String? = null,
    
    /**
     * 占位符
     * 输入框的占位符文本
     */
    var placeholder: String? = null,
    
    /**
     * 数组元素类型配置
     * 当类型为ARRAY时，定义数组元素的类型和结构
     * 如果数组元素是对象，则包含对象的字段配置
     * 如果数组元素是数组，则递归定义
     */
    var itemType: ActivityTemplateFieldDto? = null,
    
    /**
     * 对象字段配置
     * 当类型为JSON_OBJECT时，定义对象的字段配置
     */
    var properties: Map<String, ActivityTemplateFieldDto>? = null
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
     * 模板ID
     * 更新时使用，创建时为空
     */
    var id: Long? = null,

    /**
     * 模板名称
     * 必填字段，模板的显示名称
     */
    var name: String? = null,

    /**
     * 模板描述
     * 可选字段，模板的详细说明
     */
    var description: String? = null,

    /**
     * 字段配置列表
     * 模板的所有字段配置
     */
    var fields: List<ActivityTemplateFieldDto>? = null,

    /**
     * 模板状态
     * 1-启用，0-禁用
     */
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
