package io.infra.market.vertx.entity

/**
 * 用户角色关联实体类
 * 
 * 用于维护用户和角色之间的多对多关系。
 * 一个用户可以拥有多个角色，一个角色也可以分配给多个用户。
 * 这是RBAC权限模型中的核心关联表，通过此表实现用户角色的分配。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class UserRole(
    /**
     * 主键ID
     * 关联记录的唯一标识
     */
    var id: Long? = null,
    
    /**
     * 用户ID
     * 关联到User实体的主键，标识被分配角色的用户
     */
    var uid: Long? = null,

    /**
     * 角色ID
     * 关联到Role实体的主键，标识分配给用户的角色
     */
    var roleId: Long? = null,
    
    /**
     * 创建时间
     * 关联记录的创建时间（毫秒时间戳）
     */
    override var createTime: Long? = null,
    
    /**
     * 更新时间
     * 关联记录的最后修改时间（毫秒时间戳）
     */
    override var updateTime: Long? = null
) : TimestampedEntity
