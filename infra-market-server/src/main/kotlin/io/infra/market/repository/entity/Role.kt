package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 角色实体类
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Table("role_info")
data class Role(
    var name: String? = null,
    var code: String? = null,
    var description: String? = null,
    var status: String = "active"
) : BaseActiveRecordEntity<Role, Long>()
