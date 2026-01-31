package io.infra.market.vertx.entity

/**
 * 活动组件实体类
 * 
 * 用于存储活动组件的基本信息和字段/组件配置。
 * 活动组件可以包含多个字段，也可以包含其他组件（嵌套）。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * 表结构说明：
 * - 组件基本信息：名称、描述、状态等
 * - 字段/组件配置：以JSON格式存储组件的所有字段和嵌套组件配置
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ActivityComponent(
    /**
     * 主键ID
     * 自增长主键，唯一标识一个活动组件记录
     */
    var id: Long? = null,
    
    /**
     * 组件名称
     * 组件的显示名称，用于界面展示和识别
     * 非空字段，建议长度不超过100字符
     */
    var name: String? = null,
    
    /**
     * 组件描述
     * 组件的详细说明，用于文档和用户理解
     * 可选字段，建议长度不超过1000字符
     */
    var description: String? = null,
    
    /**
     * 字段/组件配置
     * 以JSON格式存储组件的所有字段和嵌套组件配置
     * 包含字段类型、验证规则、默认值、选项等完整定义
     * 格式：[{"name":"field1","type":"string",...}, {"name":"component1","type":"COMPONENT",...}, ...]
     */
    var fields: String? = null,
    
    /**
     * 组件状态
     * 1-启用，0-禁用
     * 用于控制组件的可用性，默认为1（启用）
     */
    var status: Int? = null,
    
    /**
     * 创建时间
     * 组件记录的创建时间（毫秒时间戳）
     */
    override var createTime: Long? = null,
    
    /**
     * 更新时间
     * 组件记录的最后修改时间（毫秒时间戳）
     */
    override var updateTime: Long? = null
) : TimestampedEntity
