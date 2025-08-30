package io.infra.market.dto

/**
 * 仪表盘统计数据DTO
 */
data class DashboardStatisticsDto(
    val userCount: Long,
    val roleCount: Long,
    val permissionCount: Long,
    val onlineCount: Long
)

/**
 * 最近登录用户DTO
 */
data class RecentUserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val status: String,
    val lastLoginTime: String
)

/**
 * 系统信息DTO
 */
data class SystemInfoDto(
    val systemVersion: String,
    val javaVersion: String,
    val springBootVersion: String,
    val kotlinVersion: String,
    val lastUpdate: String
)

/**
 * 仪表盘数据DTO
 */
data class DashboardDataDto(
    val statistics: DashboardStatisticsDto,
    val recentUsers: List<RecentUserDto>,
    val systemInfo: SystemInfoDto
)
