package io.infra.market.dto

import io.infra.market.enums.DataTypeEnum
import io.infra.market.enums.InputTypeEnum

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
     * 组件的所有字段和嵌套组件配置
     */
    var fields: List<ActivityComponentFieldDto>? = null,

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
 * 活动组件字段/组件配置DTO
 * 
 * 用于定义活动组件的字段配置或嵌套组件配置。
 * 支持普通字段和嵌套组件两种类型。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityComponentFieldDto(
    /**
     * 字段/组件名称
     * 字段或组件的唯一标识符，用于数据存储和识别
     */
    var name: String? = null,
    
    /**
     * 字段/组件标签
     * 字段或组件的中文显示名称，用于界面展示和用户理解
     */
    var label: String? = null,
    
    /**
     * 字段类型
     * 字段的数据类型：STRING、INTEGER、ARRAY、JSON_OBJECT、COMPONENT等
     * 当type为COMPONENT时，表示这是一个嵌套组件
     */
    var type: DataTypeEnum? = null,
    
    /**
     * 输入类型
     * 字段在界面上的输入方式：TEXT、SELECT、MULTI_SELECT、NUMBER等
     * 当type为COMPONENT时，此字段无效
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
     * 当type为COMPONENT时，此字段无效
     */
    var defaultValue: Any? = null,
    
    /**
     * 字段描述
     * 字段或组件的详细说明，用于文档和用户理解
     */
    var description: String? = null,
    
    /**
     * 排序号
     * 字段或组件在界面上的显示顺序，数值越小越靠前
     */
    var sort: Int? = null,
    
    /**
     * 下拉选项列表
     * 当输入类型为SELECT或MULTI_SELECT时的选项配置
     * 当type为COMPONENT时，此字段无效
     */
    var options: List<SelectOptionDto>? = null,
    
    /**
     * 是否支持多选
     * 当输入类型为SELECT时，此字段控制是否支持多选
     * 当type为COMPONENT时，此字段无效
     */
    var multiple: Boolean? = null,
    
    /**
     * 最小值
     * 当类型为INTEGER或DOUBLE时的最小值限制
     * 当type为COMPONENT时，此字段无效
     */
    var min: Number? = null,
    
    /**
     * 最大值
     * 当类型为INTEGER或DOUBLE时的最大值限制
     * 当type为COMPONENT时，此字段无效
     */
    var max: Number? = null,
    
    /**
     * 最小长度
     * 当类型为STRING或ARRAY时的最小长度限制
     * 当type为COMPONENT时，此字段无效
     */
    var minLength: Int? = null,
    
    /**
     * 最大长度
     * 当类型为STRING或ARRAY时的最大长度限制
     * 当type为COMPONENT时，此字段无效
     */
    var maxLength: Int? = null,
    
    /**
     * 正则表达式
     * 当类型为STRING时的格式验证规则
     * 当type为COMPONENT时，此字段无效
     */
    var pattern: String? = null,
    
    /**
     * 占位符
     * 输入框的占位符文本
     * 当type为COMPONENT时，此字段无效
     */
    var placeholder: String? = null,
    
    /**
     * 数组元素类型配置
     * 当类型为ARRAY时，定义数组元素的类型和结构
     * 当type为COMPONENT时，此字段无效
     */
    var itemType: ActivityComponentFieldDto? = null,
    
    /**
     * 对象字段配置
     * 当类型为JSON_OBJECT时，定义对象的字段配置
     * 当type为COMPONENT时，此字段无效
     */
    var properties: Map<String, ActivityComponentFieldDto>? = null,
    
    /**
     * 组件ID（当type为COMPONENT时使用）
     * 引用的活动组件ID，用于嵌套组件
     */
    var componentId: Long? = null,
    
    /**
     * 是否是数组组件（当type为COMPONENT时使用）
     * true-该组件可以重复多次（数组形式），false-单个组件
     */
    var isArray: Boolean? = false,
    
    /**
     * 是否支持动态增删（当type为COMPONENT且isArray为true时使用）
     * true-在使用时可以动态添加/删除该组件，false-固定数量
     */
    var allowDynamic: Boolean? = false
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
     * 组件ID
     * 更新时使用，创建时为空
     */
    var id: Long? = null,

    /**
     * 组件名称
     * 必填字段，组件的显示名称
     */
    var name: String? = null,

    /**
     * 组件描述
     * 可选字段，组件的详细说明
     */
    var description: String? = null,

    /**
     * 字段/组件配置列表
     * 组件的所有字段和嵌套组件配置
     */
    var fields: List<ActivityComponentFieldDto>? = null,

    /**
     * 组件状态
     * 1-启用，0-禁用
     */
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
