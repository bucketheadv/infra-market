package io.infra.qk.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.util.*

/**
 * 角色实体类
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Entity
@Table(name = "role_info")
data class Role(
    @Column(name = "name", nullable = false, unique = true, length = 50)
    var name: String? = null,
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    var code: String? = null,
    
    @Column(name = "description", length = 255)
    var description: String? = null,
    
    @Column(name = "status", length = 20)
    var status: String = "active",
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    var createTime: Date = Date(),
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    var updateTime: Date = Date()
) : PanacheEntity()
