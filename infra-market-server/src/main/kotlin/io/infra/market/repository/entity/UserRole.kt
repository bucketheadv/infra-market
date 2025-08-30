package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 用户角色关联实体类
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Table("user_role")
data class UserRole(
    var userId: Long? = null,
    var roleId: Long? = null
) : BaseActiveRecordEntity<UserRole, Long>()
