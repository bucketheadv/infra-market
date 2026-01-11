package io.infra.market.vertx.entity

/**
 * 角色实体类
 * 
 * 用于定义系统中的角色信息，角色是权限管理的基础单元。
 * 通过角色可以给用户分配不同的权限，实现基于角色的访问控制(RBAC)。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class Role(
    /**
     * 角色ID
     * 主键，唯一标识一个角色
     */
    var id: Long? = null,
    
    /**
     * 角色名称
     * 角色的显示名称，如"管理员"、"普通用户"等
     */
    var name: String? = null,
    
    /**
     * 角色编码
     * 角色的唯一标识码，用于程序内部识别，如"ADMIN"、"USER"等
     */
    var code: String? = null,
    
    /**
     * 角色描述
     * 对角色功能的详细说明，帮助理解角色的职责和权限范围
     */
    var description: String? = null,
    
    /**
     * 角色状态
     * 可选值：active(激活)、inactive(未激活)、disabled(禁用)
     * 默认为active
     */
    var status: String = "active",
    
    /**
     * 创建时间
     * 角色记录的创建时间（毫秒时间戳）
     */
    override var createTime: Long? = null,
    
    /**
     * 更新时间
     * 角色信息最后更新的时间（毫秒时间戳）
     */
    override var updateTime: Long? = null
) : TimestampedEntity
