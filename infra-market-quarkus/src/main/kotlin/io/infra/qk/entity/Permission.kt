package io.infra.qk.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.util.*

/**
 * 权限实体类
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Entity
@Table(name = "permission_info")
data class Permission(
    @Column(name = "name", nullable = false, length = 100)
    var name: String? = null,
    
    @Column(name = "code", nullable = false, unique = true, length = 100)
    var code: String? = null,
    
    @Column(name = "type", nullable = false, length = 20)
    var type: String = "menu",
    
    @Column(name = "parent_id")
    var parentId: Long? = null,
    
    @Column(name = "path", length = 255)
    var path: String? = null,
    
    @Column(name = "icon", length = 100)
    var icon: String? = null,
    
    @Column(name = "sort")
    var sort: Int = 0,
    
    @Column(name = "status", length = 20)
    var status: String = "active",
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    var createTime: Date = Date(),
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    var updateTime: Date = Date()
) : PanacheEntity()
