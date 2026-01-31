package io.infra.market.vertx.entity

/**
 * 活动模板实体类
 * 
 * 用于存储活动模板的基本信息和字段配置。
 * 活动模板定义了活动的配置结构，包括各种字段类型（整数、字符串、下拉框、数组等）。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * 表结构说明：
 * - 模板基本信息：名称、描述、状态等
 * - 字段配置：以JSON格式存储模板的所有字段配置
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityTemplate(
    /**
     * 主键ID
     * 自增长主键，唯一标识一个活动模板记录
     */
    var id: Long? = null,
    
    /**
     * 模板名称
     * 模板的显示名称，用于界面展示和识别
     * 非空字段，建议长度不超过100字符
     */
    var name: String? = null,
    
    /**
     * 模板描述
     * 模板的详细说明，用于文档和用户理解
     * 可选字段，建议长度不超过1000字符
     */
    var description: String? = null,
    
    /**
     * 字段配置
     * 以JSON格式存储模板的所有字段配置
     * 包含字段类型、验证规则、默认值、选项等完整定义
     * 格式：[{"name":"field1","type":"string",...}, ...]
     */
    var fields: String? = null,
    
    /**
     * 模板状态
     * 1-启用，0-禁用
     * 用于控制模板的可用性，默认为1（启用）
     */
    var status: Int? = null,
    
    /**
     * 创建时间
     * 模板记录的创建时间（毫秒时间戳）
     */
    override var createTime: Long? = null,
    
    /**
     * 更新时间
     * 模板记录的最后修改时间（毫秒时间戳）
     */
    override var updateTime: Long? = null
) : TimestampedEntity
