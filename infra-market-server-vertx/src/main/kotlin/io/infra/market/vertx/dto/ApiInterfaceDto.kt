package io.infra.market.vertx.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * 接口管理DTO
 * 
 * 用于接口信息的传输对象，包含接口的基本信息和参数配置。
 * 支持RESTful API的完整生命周期管理。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceDto(
    /**
     * 接口ID
     * 主键，唯一标识一个接口
     */
    var id: Long? = null,

    /**
     * 接口名称
     * 接口的显示名称，用于界面展示和识别
     */
    var name: String? = null,

    /**
     * 请求方法
     * HTTP请求方法，如GET、POST、PUT、DELETE等
     */
    var method: String? = null,

    /**
     * 请求URL
     * 接口的完整请求地址，支持路径参数
     */
    var url: String? = null,

    /**
     * 接口描述
     * 接口的详细说明，用于文档和用户理解
     */
    var description: String? = null,

    /**
     * 接口状态
     * 1-启用，0-禁用，用于控制接口的可用性
     */
    var status: Int? = null,

    /**
     * 创建时间
     * 接口记录的创建时间，格式化的字符串（如 "2024-01-01 12:00:00"）
     */
    var createTime: String? = null,

    /**
     * 更新时间
     * 接口记录的最后修改时间，格式化的字符串（如 "2024-01-01 12:00:00"）
     */
    var updateTime: String? = null,

    /**
     * POST类型
     * 当请求方法为POST、PUT、PATCH时的请求体类型
     * 如application/json、application/x-www-form-urlencoded
     */
    var postType: String? = null,

    /**
     * 接口环境
     * 用于标识接口所属的环境，如测试环境、正式环境
     */
    var environment: String? = null,

    /**
     * URL参数列表
     * 接口的URL路径参数和查询参数配置
     */
    var urlParams: List<ApiParamDto>? = null,

    /**
     * Header参数列表
     * 接口的HTTP请求头参数配置
     */
    var headerParams: List<ApiParamDto>? = null,

    /**
     * Body参数列表
     * 接口的请求体参数配置，用于POST、PUT、PATCH请求
     */
    var bodyParams: List<ApiParamDto>? = null,
    
    /**
     * 超时时间（秒）
     * 接口执行时的超时时间，单位为秒
     * 可选字段，默认值为60（60秒）
     * 如果未设置，则使用系统默认超时时间
     */
    var timeout: Long? = null,
    
    /**
     * 取值路径
     * 用于从响应结果中提取特定值的JSONPath表达式
     * 可选字段，支持JSONPath语法，如：$.data.result、$.items[0].name等
     * 如果设置，则返回该路径对应的值，否则返回完整响应体
     */
    var valuePath: String? = null
)

/**
 * 下拉选项DTO
 * 
 * 用于下拉框和多选下拉框的选项配置
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class SelectOptionDto(
    /**
     * 选项值
     * 选项的实际值，用于提交和存储
     */
    var value: String? = null,
    
    /**
     * 选项标签
     * 选项的显示文本，用于界面展示
     */
    var label: String? = null
)

/**
 * 接口参数DTO
 * 
 * 用于定义接口的各种参数配置，包括URL参数、Header参数和Body参数。
 * 支持多种输入类型和数据类型，提供灵活的参数定义能力。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiParamDto(
    /**
     * 参数名称
     * 参数的唯一标识符，用于参数传递和识别
     */
    var name: String? = null,
    
    /**
     * 参数中文名称
     * 参数的中文显示名称，用于界面展示和用户理解
     */
    var chineseName: String? = null,
    
    /**
     * 参数类型
     * 参数所属的类型：URL_PARAM、HEADER_PARAM、BODY_PARAM
     */
    var paramType: String? = null,
    
    /**
     * 输入类型
     * 参数在界面上的输入方式：TEXT、SELECT、CODE、DATE等
     */
    var inputType: String? = null,
    
    /**
     * 数据类型
     * 参数的数据类型：STRING、INTEGER、JSON、ARRAY等
     */
    var dataType: String? = null,
    
    /**
     * 是否必填
     * true-必填，false-可选，用于参数校验
     */
    var required: Boolean? = null,
    
    /**
     * 默认值
     * 参数的默认值，支持字符串、数组等多种类型
     */
    var defaultValue: Any? = null,
    
    /**
     * 是否可变更
     * true-可变更，false-只读，用于控制参数的编辑权限
     */
    var changeable: Boolean? = null,
    
    /**
     * 下拉选项列表
     * 当输入类型为SELECT或MULTI_SELECT时的选项配置
     */
    var options: List<SelectOptionDto>? = null,
    
    /**
     * 参数描述
     * 参数的详细说明，用于文档和用户理解
     */
    var description: String? = null,
    
    /**
     * 排序号
     * 参数在界面上的显示顺序，数值越小越靠前
     */
    var sort: Int? = null
)

/**
 * 接口表单DTO
 * 
 * 用于接口创建和更新的表单数据传输对象。
 * 与ApiInterfaceDto的区别是不包含系统字段（如id、createTime、updateTime、status）。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceFormDto(
    /**
     * 接口ID
     * 更新时使用，创建时为空
     */
    var id: Long? = null,

    /**
     * 接口名称
     * 必填字段，接口的显示名称
     */
    var name: String? = null,

    /**
     * 请求方法
     * 必填字段，HTTP请求方法
     */
    var method: String? = null,

    /**
     * 请求URL
     * 必填字段，接口的完整请求地址
     */
    var url: String? = null,

    /**
     * 接口描述
     * 可选字段，接口的详细说明
     */
    var description: String? = null,

    /**
     * POST类型
     * 当请求方法为POST、PUT、PATCH时的请求体类型
     */
    var postType: String? = null,

    /**
     * 接口环境
     * 用于标识接口所属的环境
     */
    var environment: String? = null,

    /**
     * URL参数列表
     * 接口的URL参数配置
     */
    var urlParams: List<ApiParamDto>? = null,

    /**
     * Header参数列表
     * 接口的Header参数配置
     */
    var headerParams: List<ApiParamDto>? = null,

    /**
     * Body参数列表
     * 接口的Body参数配置
     */
    var bodyParams: List<ApiParamDto>? = null,
    
    /**
     * 超时时间（秒）
     * 接口执行时的超时时间，单位为秒
     * 可选字段，默认值为60（60秒）
     * 如果未设置，则使用系统默认超时时间
     */
    var timeout: Long? = null,
    
    /**
     * 取值路径
     * 用于从响应结果中提取特定值的JSONPath表达式
     * 可选字段，支持JSONPath语法，如：$.data.result、$.items[0].name等
     * 如果设置，则返回该路径对应的值，否则返回完整响应体
     */
    var valuePath: String? = null
)

/**
 * 接口执行请求DTO
 * 
 * 用于接口执行时的请求参数对象。
 * 包含接口ID和实际的参数值。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiExecuteRequestDto(
    /**
     * 接口ID
     * 必填字段，要执行的接口的唯一标识
     */
    var interfaceId: Long? = null,
    
    /**
     * Header参数值
     * 实际的Header参数值，key为参数名，value为参数值
     */
    var headers: Map<String, String>? = null,
    
    /**
     * URL参数值
     * 实际的URL参数值，key为参数名，value为参数值
     */
    var urlParams: Map<String, Any>? = null,
    
    /**
     * Body参数值
     * 实际的Body参数值，key为参数名，value为参数值
     */
    var bodyParams: Map<String, Any>? = null,
    
    /**
     * 超时时间（秒）
     * 可选字段，用于覆盖接口配置的超时时间
     * 如果未设置，则使用接口配置的超时时间或系统默认超时时间
     */
    var timeout: Long? = null,
    
    /**
     * 备注
     * 执行记录的备注信息，选填
     */
    var remark: String? = null
)

/**
 * 接口执行响应DTO
 * 
 * 用于接口执行结果的响应对象。
 * 包含HTTP响应信息和执行状态。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiExecuteResponseDto(
    /**
     * HTTP状态码
     * 目标接口返回的HTTP状态码
     */
    var status: Int? = null,
    
    /**
     * 响应头
     * 目标接口返回的HTTP响应头
     */
    var headers: Map<String, String>? = null,
    
    /**
     * 响应体
     * 目标接口返回的响应内容
     */
    var body: String? = null,
    
    /**
     * 响应时间
     * 接口执行的总耗时，单位毫秒
     */
    var responseTime: Long? = null,
    
    /**
     * 执行是否成功
     * true-成功，false-失败
     */
    var success: Boolean? = null,
    
    /**
     * 错误信息
     * 当执行失败时的错误描述
     */
    var error: String? = null,
    
    /**
     * 提取的值
     * 当设置了valuePath时，返回该路径对应的值
     * 如果valuePath为空或提取失败，则此字段为null
     */
    var extractedValue: String? = null
)

/**
 * 接口查询DTO
 * 
 * 用于接口列表查询的查询条件对象。
 * 支持按名称、方法、状态、标签等条件进行筛选。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiInterfaceQueryDto(
    /**
     * 接口名称
     * 支持模糊查询
     */
    var name: String? = null,

    /**
     * 请求方法
     * 精确匹配查询
     */
    var method: String? = null,

    /**
     * 接口状态
     * 1-启用，0-禁用
     */
    var status: Int? = null,

    /**
     * 接口环境
     * 精确匹配查询
     */
    var environment: String? = null,

    /**
     * 页码
     * 分页查询的页码，从1开始
     */
    @field:Min(value = 1, message = "页码不能小于1")
    var page: Int = 1,

    /**
     * 每页大小
     * 分页查询的每页记录数
     */
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    var size: Int = 10
)

/**
 * 最常用接口查询DTO
 * 
 * 用于查询最常用接口的查询条件对象。
 * 支持按天数统计和限制返回数量。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class MostUsedInterfacesQueryDto(
    /**
     * 统计天数
     * 统计最近N天的接口使用情况，默认为30天
     */
    var days: Int = 30,
    
    /**
     * 返回数量限制
     * 最多返回N条记录，默认为5条
     */
    var limit: Int = 5
)
