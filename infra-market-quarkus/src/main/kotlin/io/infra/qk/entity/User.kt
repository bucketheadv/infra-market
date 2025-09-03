package io.infra.qk.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import org.joda.time.DateTime
import java.util.*

/**
 * 用户实体类
 * @author liuqinglin
 * Date: 2025/8/15 19:05
 */
@Entity
@Table(name = "user_info")
data class User(
    @Column(name = "username", nullable = false, unique = true, length = 50)
    var username: String? = null,
    
    @Column(name = "password", nullable = false, length = 255)
    var password: String? = null,
    
    @Column(name = "email", length = 100)
    var email: String? = null,
    
    @Column(name = "phone", length = 20)
    var phone: String? = null,
    
    @Column(name = "status", length = 20)
    var status: String = "active",
    
    @Column(name = "last_login_time")
    @Temporal(TemporalType.TIMESTAMP)
    var lastLoginTime: Date? = null,
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    var createTime: Date = Date(),
    
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    var updateTime: Date = Date()
) : PanacheEntity()
