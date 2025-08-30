package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 角色权限关联实体类
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Table("role_permission")
data class RolePermission(
    var roleId: Long? = null,
    var permissionId: Long? = null
) : BaseActiveRecordEntity<RolePermission, Long>()
