package io.infra.market.repository.entity

import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.KeyType
import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 接口管理实体类
 * 
 * 对应数据库表 api_interface，用于存储接口的基本信息和参数配置。
 * 继承自 BaseActiveRecordEntity，提供基础的CRUD操作能力。
 * 
 * 表结构说明：
 * - 接口基本信息：名称、方法、URL、描述等
 * - 参数配置：以JSON格式存储URL参数、Header参数、Body参数
 * - 系统字段：状态、标签、创建时间、更新时间等
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@Table("api_interface")
data class ApiInterface(
    /**
     * 主键ID
     * 自增长主键，唯一标识一个接口记录
     */
    @Id(keyType = KeyType.Auto)
    override var id: Long? = null,
    
    /**
     * 接口名称
     * 接口的显示名称，用于界面展示和识别
     * 非空字段，建议长度不超过100字符
     */
    var name: String? = null,
    
    /**
     * 请求方法
     * HTTP请求方法，如GET、POST、PUT、DELETE等
     * 非空字段，存储枚举值对应的字符串
     */
    var method: String? = null,
    
    /**
     * 请求URL
     * 接口的完整请求地址，支持路径参数
     * 非空字段，建议长度不超过500字符
     */
    var url: String? = null,
    
    /**
     * 接口描述
     * 接口的详细说明，用于文档和用户理解
     * 可选字段，建议长度不超过1000字符
     */
    var description: String? = null,
    
    /**
     * POST类型
     * 当请求方法为POST、PUT、PATCH时的请求体类型
     * 可选字段，如application/json、application/x-www-form-urlencoded
     */
    var postType: String? = null,
    
    /**
     * 参数配置
     * 以JSON格式存储接口的所有参数配置
     * 包含URL参数、Header参数、Body参数的完整定义
     * 格式：[{"name":"param1","paramType":"URL_PARAM",...}, ...]
     */
    var params: String? = null,
    
    /**
     * 接口状态
     * 1-启用，0-禁用
     * 用于控制接口的可用性，默认为1（启用）
     */
    var status: Int? = null,
    
    /**
     * 接口环境
     * 用于标识接口所属的环境，如测试环境、正式环境
     * 可选字段，存储枚举值对应的字符串
     */
    var environment: String? = null,
    
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
    var valuePath: String? = null,
) : BaseActiveRecordEntity<ApiInterface, Long>()
