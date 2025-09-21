package io.infra.market.dto

import io.infra.market.repository.entity.User
import io.infra.market.util.TimeUtil

/**
 * 仪表盘统计数据DTO
 * 
 * 用于展示系统各项统计数据的DTO。
 * 包含用户、角色、权限和接口的数量统计，以及较昨日的变化情况。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class DashboardStatisticsDto(
    /**
     * 用户总数
     * 系统中注册用户的总数量
     */
    val userCount: Long,
    
    /**
     * 角色总数
     * 系统中定义的角色总数量
     */
    val roleCount: Long,
    
    /**
     * 权限总数
     * 系统中定义的权限总数量
     */
    val permissionCount: Long,
    
    /**
     * 接口总数
     * 系统中定义的接口总数量
     */
    val interfaceCount: Long,
    
    /**
     * 用户总数较昨日变化百分比
     * 正数表示增长，负数表示减少，0表示无变化
     */
    val userCountChangePercent: Double,
    
    /**
     * 角色总数较昨日变化百分比
     * 正数表示增长，负数表示减少，0表示无变化
     */
    val roleCountChangePercent: Double,
    
    /**
     * 权限总数较昨日变化百分比
     * 正数表示增长，负数表示减少，0表示无变化
     */
    val permissionCountChangePercent: Double,
    
    /**
     * 接口总数较昨日变化百分比
     * 正数表示增长，负数表示减少，0表示无变化
     */
    val interfaceCountChangePercent: Double
)

/**
 * 最近登录用户DTO
 * 
 * 用于展示最近登录用户信息的DTO。
 * 包含用户的基本信息和最后登录时间。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class RecentUserDto(
    /**
     * 用户ID
     * 用户的唯一标识
     */
    val id: Long,
    
    /**
     * 用户名
     * 用户的登录用户名
     */
    val username: String,
    
    /**
     * 邮箱地址
     * 用户的邮箱地址，可以为null
     */
    val email: String?,
    
    /**
     * 用户状态
     * 用户的状态，如active、inactive等
     */
    val status: String,
    
    /**
     * 最后登录时间
     * 用户最后一次登录的时间，格式化的字符串
     */
    val lastLoginTime: String
) {
    companion object {
        /**
         * 从User实体转换为RecentUserDto
         * 
         * @param user 用户实体
         * @return RecentUserDto
         */
        fun fromEntity(user: User): RecentUserDto {
            return RecentUserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                status = user.status,
                lastLoginTime = TimeUtil.format(user.lastLoginTime)
            )
        }
        
        /**
         * 批量从User实体列表转换为RecentUserDto列表
         * 
         * @param users 用户实体列表
         * @return RecentUserDto列表
         */
        fun fromEntityList(users: List<User>): List<RecentUserDto> {
            return users.map { fromEntity(it) }
        }
    }
}

/**
 * 系统信息DTO
 * 
 * 用于展示系统版本信息的DTO。
 * 包含系统各个组件的版本号和最后更新时间。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class SystemInfoDto(
    /**
     * 系统版本
     * 当前系统的版本号
     */
    val systemVersion: String,
    
    /**
     * Java版本
     * 运行环境的Java版本
     */
    val javaVersion: String,
    
    /**
     * Spring Boot版本
     * 使用的Spring Boot框架版本
     */
    val springBootVersion: String,
    
    /**
     * Kotlin版本
     * 使用的Kotlin语言版本
     */
    val kotlinVersion: String,
    
    /**
     * 最后更新时间
     * 系统最后更新的时间，格式化的字符串
     */
    val lastUpdate: String
)

/**
 * 仪表盘数据DTO
 * 
 * 用于封装仪表盘页面所需的所有数据。
 * 包含统计数据、最近登录用户和系统信息。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class DashboardDataDto(
    /**
     * 统计数据
     * 系统的各项统计数据
     */
    val statistics: DashboardStatisticsDto,
    
    /**
     * 最近登录用户列表
     * 最近登录的用户信息列表
     */
    val recentUsers: List<RecentUserDto>,
    
    /**
     * 系统信息
     * 系统的版本和更新信息
     */
    val systemInfo: SystemInfoDto
)
