package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity
import java.util.Date

/**
 * 用户实体类
 * 
 * 用于存储系统用户的基本信息，包括用户名、密码、邮箱、手机号等。
 * 继承自BaseActiveRecordEntity，提供基础的CRUD操作能力。
 * 
 * @author liuqinglin
 * @date 2025/8/15 19:05
 * @since 1.0.0
 */
@Table("user_info")
data class User(
    /**
     * 用户名
     * 用于用户登录和身份识别，必须唯一
     */
    var username: String? = null,
    
    /**
     * 密码
     * 用户登录密码，存储时已加密
     */
    var password: String? = null,
    
    /**
     * 邮箱地址
     * 用于接收系统通知和找回密码
     */
    var email: String? = null,
    
    /**
     * 手机号码
     * 用于接收短信验证码和联系方式
     */
    var phone: String? = null,
    
    /**
     * 用户状态
     * 可选值：active(激活)、inactive(未激活)、disabled(禁用)
     * 默认为active
     */
    var status: String = "active",
    
    /**
     * 最后登录时间
     * 记录用户最后一次成功登录的时间
     */
    var lastLoginTime: Date? = null
) : BaseActiveRecordEntity<User, Long>()