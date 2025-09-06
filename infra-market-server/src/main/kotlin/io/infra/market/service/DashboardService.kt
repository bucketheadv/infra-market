package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.RoleDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.repository.dao.ApiInterfaceDao
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
    private val permissionDao: PermissionDao,
    private val apiInterfaceDao: ApiInterfaceDao
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
        // 获取当前时间
        val now = DateTime.now()
        // 获取昨天同一时间
        val yesterday = now.minusDays(1)
        
        // 获取用户总数（排除已删除的用户）
        val userCount = userDao.count()
        val userCountYesterday = userDao.countBeforeDate(yesterday)
        val userCountChangePercent = calculateChangePercent(userCountYesterday, userCount)
        
        // 获取角色总数（排除已删除的角色）
        val roleCount = roleDao.count()
        val roleCountYesterday = roleDao.countBeforeDate(yesterday)
        val roleCountChangePercent = calculateChangePercent(roleCountYesterday, roleCount)
        
        // 获取权限总数（排除已删除的权限）
        val permissionCount = permissionDao.count()
        val permissionCountYesterday = permissionDao.countBeforeDate(yesterday)
        val permissionCountChangePercent = calculateChangePercent(permissionCountYesterday, permissionCount)
        
        // 获取接口总数（排除已删除的接口）
        val interfaceCount = apiInterfaceDao.count()
        val interfaceCountYesterday = apiInterfaceDao.countBeforeDate(yesterday)
        val interfaceCountChangePercent = calculateChangePercent(interfaceCountYesterday, interfaceCount)
        
        return DashboardStatisticsDto(
            userCount = userCount,
            roleCount = roleCount,
            permissionCount = permissionCount,
            interfaceCount = interfaceCount,
            userCountChangePercent = userCountChangePercent,
            roleCountChangePercent = roleCountChangePercent,
            permissionCountChangePercent = permissionCountChangePercent,
            interfaceCountChangePercent = interfaceCountChangePercent
        )
    }
    
    /**
     * 计算变化百分比
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 变化百分比，保留2位小数
     */
    private fun calculateChangePercent(oldValue: Long, newValue: Long): Double {
        return if (oldValue == 0L) {
            if (newValue > 0) 100.0 else 0.0
        } else {
            val change = newValue - oldValue
            val percent = (change.toDouble() / oldValue.toDouble()) * 100.0
            String.format("%.2f", percent).toDouble()
        }
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
