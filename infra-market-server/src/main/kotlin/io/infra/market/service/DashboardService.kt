package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.RoleDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.util.DateTimeUtil
import org.springframework.boot.SpringBootVersion
import org.springframework.stereotype.Service
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import kotlin.KotlinVersion

@Service
class DashboardService(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val permissionDao: PermissionDao
) {
    
    /**
     * 获取仪表盘数据
     */
    fun getDashboardData(): ApiResponse<DashboardDataDto> {
        val statistics = getStatistics()
        val recentUsers = getRecentUsers()
        val systemInfo = getSystemInfo()
        
        val dashboardData = DashboardDataDto(
            statistics = statistics,
            recentUsers = recentUsers,
            systemInfo = systemInfo
        )
        
        return ApiResponse.success(dashboardData)
    }
    
    /**
     * 获取统计数据
     */
    private fun getStatistics(): DashboardStatisticsDto {
        // 获取用户总数（排除已删除的用户）
        val userCount = userDao.count()
        
        // 获取角色总数（排除已删除的角色）
        val roleCount = roleDao.count()
        
        // 获取权限总数（排除已删除的权限）
        val permissionCount = permissionDao.count()
        
        // 在线用户数（这里简化处理，实际应该从Redis中获取）
        val onlineCount = 0L
        
        return DashboardStatisticsDto(
            userCount = userCount,
            roleCount = roleCount,
            permissionCount = permissionCount,
            onlineCount = onlineCount
        )
    }
    
    /**
     * 获取最近登录用户
     */
    private fun getRecentUsers(): List<RecentUserDto> {
        // 获取最近登录的用户
        val recentUsers = userDao.getRecentLoginUsers(5)
        
        return recentUsers.map { user ->
            RecentUserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                status = user.status,
                lastLoginTime = DateTimeUtil.formatDateTime(user.lastLoginTime)
            )
        }
    }
    
    /**
     * 获取系统信息
     */
    private fun getSystemInfo(): SystemInfoDto {
        return SystemInfoDto(
            systemVersion = "v1.0.0",
            javaVersion = System.getProperty("java.version"),
            springBootVersion = SpringBootVersion.getVersion(),
            kotlinVersion = KotlinVersion.CURRENT.toString(),
            lastUpdate = DateTime.now().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        )
    }
}
