package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity
import java.util.Date

/**
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Table("user_info")
data class User(
    var username: String? = null,
    var password: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var status: String = "active",
    var lastLoginTime: Date? = null
) : BaseActiveRecordEntity<User, Long>()