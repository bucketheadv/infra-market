package io.infra.market.vertx.entity

/**
 * 活动实体类
 * 
 * 用于存储活动的基本信息和配置数据。
 * 活动基于活动模板创建，配置数据以JSON格式存储。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * 表结构说明：
 * - 活动基本信息：名称、描述、模板ID、状态等
 * - 配置数据：以JSON格式存储活动的配置数据，根据模板的字段配置动态生成
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class Activity(
    /**
     * 主键ID
     * 自增长主键，唯一标识一个活动记录
     */
    var id: Long? = null,
    
    /**
     * 活动名称
     * 活动的显示名称，用于界面展示和识别
     * 非空字段，建议长度不超过100字符
     */
    var name: String? = null,
    
    /**
     * 活动描述
     * 活动的详细说明，用于文档和用户理解
     * 可选字段，建议长度不超过1000字符
     */
    var description: String? = null,
    
    /**
     * 模板ID
     * 关联到 activity_template 表，标识活动使用的模板
     * 非空字段
     */
    var templateId: Long? = null,
    
    /**
     * 配置数据
     * 以JSON格式存储活动的配置数据
     * 根据模板的字段配置动态生成，格式为键值对
     * 例如：{"field1":"value1","field2":123,"field3":["item1","item2"]}
     */
    var configData: String? = null,
    
    /**
     * 活动状态
     * 1-启用，0-禁用
     * 用于控制活动的可用性，默认为1（启用）
     */
    var status: Int? = null,
    
    /**
     * 创建时间
     * 活动记录的创建时间（毫秒时间戳）
     */
    override var createTime: Long? = null,
    
    /**
     * 更新时间
     * 活动记录的最后修改时间（毫秒时间戳）
     */
    override var updateTime: Long? = null
) : TimestampedEntity
