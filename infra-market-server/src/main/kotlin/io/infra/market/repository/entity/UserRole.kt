package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 用户角色关联实体类
 * 
 * 用于维护用户和角色之间的多对多关系。
 * 一个用户可以拥有多个角色，一个角色也可以分配给多个用户。
 * 这是RBAC权限模型中的核心关联表，通过此表实现用户角色的分配。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@Table("user_role")
data class UserRole(
    /**
     * 用户ID
     * 关联到User实体的主键，标识被分配角色的用户
     */
    var uid: Long? = null,

    /**
     * 角色ID
     * 关联到Role实体的主键，标识分配给用户的角色
     */
    var roleId: Long? = null
) : BaseActiveRecordEntity<UserRole, Long>()
