package io.infra.market.vertx.entity

/**
 * 角色权限关联实体类
 * 
 * 用于维护角色和权限之间的多对多关系。
 * 一个角色可以拥有多个权限，一个权限也可以分配给多个角色。
 * 这是RBAC权限模型中的关键关联表，通过此表实现角色权限的分配。
 * 结合UserRole表，可以最终确定用户拥有的所有权限。
 * 实现TimestampedEntity接口，提供创建时间和更新时间字段。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class RolePermission(
    /**
     * 主键ID
     * 关联记录的唯一标识
     */
    var id: Long? = null,
    
    /**
     * 角色ID
     * 关联到Role实体的主键，标识被分配权限的角色
     */
    var roleId: Long? = null,
    
    /**
     * 权限ID
     * 关联到Permission实体的主键，标识分配给角色的权限
     */
    var permissionId: Long? = null,
    
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
