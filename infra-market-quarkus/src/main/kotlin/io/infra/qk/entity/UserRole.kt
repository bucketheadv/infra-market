package io.infra.qk.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.util.*

/**
 * 用户角色关联实体类
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Entity
@Table(name = "user_role")
data class UserRole(
    @Column(name = "user_id", nullable = false)
    var userId: Long? = null,
    
    @Column(name = "role_id", nullable = false)
    var roleId: Long? = null,
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    var createTime: Date = Date()
) : PanacheEntity()
