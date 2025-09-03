package io.infra.qk.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.util.*

/**
 * 角色权限关联实体类
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Entity
@Table(name = "role_permission")
data class RolePermission(
    @Column(name = "role_id", nullable = false)
    var roleId: Long? = null,
    
    @Column(name = "permission_id", nullable = false)
    var permissionId: Long? = null,
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    var createTime: Date = Date()
) : PanacheEntity()
