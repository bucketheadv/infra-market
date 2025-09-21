package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.RoleDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.repository.dao.ApiInterfaceDao
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
    fun getDashboardData(): ApiData<DashboardDataDto> {
        val statistics = getStatistics()
        val recentUsers = getRecentUsers()
        val systemInfo = getSystemInfo()
        
        val dashboardData = DashboardDataDto(
            statistics = statistics,
            recentUsers = recentUsers,
            systemInfo = systemInfo
        )
        
        return ApiData.success(dashboardData)
    }
    
    /**
     * 获取统计数据
     */
    private fun getStatistics(): DashboardStatisticsDto {
        // 获取当前时间
        val now = DateTime.now()
        // 获取昨天同一时间
        val yesterday = now.minusDays(1)
        
        // 获取各项统计数据
        val userStats = getCountStats(userDao::count, userDao::countBeforeDate, yesterday)
        val roleStats = getCountStats(roleDao::count, roleDao::countBeforeDate, yesterday)
        val permissionStats = getCountStats(permissionDao::count, permissionDao::countBeforeDate, yesterday)
        val interfaceStats = getCountStats(apiInterfaceDao::count, apiInterfaceDao::countBeforeDate, yesterday)
        
        return DashboardStatisticsDto(
            userCount = userStats.first,
            roleCount = roleStats.first,
            permissionCount = permissionStats.first,
            interfaceCount = interfaceStats.first,
            userCountChangePercent = userStats.second,
            roleCountChangePercent = roleStats.second,
            permissionCountChangePercent = permissionStats.second,
            interfaceCountChangePercent = interfaceStats.second
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
        
        return RecentUserDto.fromEntityList(recentUsers)
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
    
    /**
     * 获取统计数据
     * 
     * @param countFunction 获取当前数量的函数
     * @param countBeforeDateFunction 获取指定日期前数量的函数
     * @param date 比较的日期
     * @return Pair<当前数量, 变化百分比>
     */
    private fun getCountStats(
        countFunction: () -> Long,
        countBeforeDateFunction: (DateTime) -> Long,
        date: DateTime
    ): Pair<Long, Double> {
        val currentCount = countFunction()
        val previousCount = countBeforeDateFunction(date)
        val changePercent = calculateChangePercent(previousCount, currentCount)
        return Pair(currentCount, changePercent)
    }
}
