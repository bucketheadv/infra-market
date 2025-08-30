package io.infra.market.dao.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Table("user_info")
data class User(var username: String? = null) : BaseActiveRecordEntity<User, Long>()