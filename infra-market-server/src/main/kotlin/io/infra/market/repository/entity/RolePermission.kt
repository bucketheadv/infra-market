package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 角色权限关联实体类
 * 
 * 用于维护角色和权限之间的多对多关系。
 * 一个角色可以拥有多个权限，一个权限也可以分配给多个角色。
 * 这是RBAC权限模型中的关键关联表，通过此表实现角色权限的分配。
 * 结合UserRole表，可以最终确定用户拥有的所有权限。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@Table("role_permission")
data class RolePermission(
    /**
     * 角色ID
     * 关联到Role实体的主键，标识被分配权限的角色
     */
    var roleId: Long? = null,
    
    /**
     * 权限ID
     * 关联到Permission实体的主键，标识分配给角色的权限
     */
    var permissionId: Long? = null
) : BaseActiveRecordEntity<RolePermission, Long>()
