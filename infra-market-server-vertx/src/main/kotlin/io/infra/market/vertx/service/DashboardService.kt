package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.DashboardDataDto
import io.infra.market.vertx.dto.DashboardStatisticsDto
import io.infra.market.vertx.dto.RecentUserDto
import io.infra.market.vertx.dto.SystemInfoDto
import io.infra.market.vertx.entity.User
import io.infra.market.vertx.repository.ApiInterfaceDao
import io.infra.market.vertx.repository.PermissionDao
import io.infra.market.vertx.repository.RoleDao
import io.infra.market.vertx.repository.UserDao
import io.infra.market.vertx.util.TimeUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 仪表盘服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class DashboardService(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val permissionDao: PermissionDao,
    private val apiInterfaceDao: ApiInterfaceDao
) {
    
    suspend fun getDashboardData(): ApiData<DashboardDataDto> {
        val userCount: Long
        val roleCount: Long
        val permissionCount: Long
        val interfaceCount: Long
        val recentUsers: List<User>
        
        coroutineScope {
            val userCountDeferred = async { userDao.countByStatus(null) }
            val roleCountDeferred = async { roleDao.count() }
            val permissionCountDeferred = async { permissionDao.count() }
            val interfaceCountDeferred = async { apiInterfaceDao.count() }
            val recentUsersDeferred = async { userDao.findRecentUsers(10) }
            
            val results = awaitAll(
                userCountDeferred,
                roleCountDeferred,
                permissionCountDeferred,
                interfaceCountDeferred,
                recentUsersDeferred
            )
            
            userCount = results[0] as Long
            roleCount = results[1] as Long
            permissionCount = results[2] as Long
            interfaceCount = results[3] as Long
            @Suppress("unchecked_cast")
            recentUsers = results[4] as List<User>
        }
        
        val statistics = DashboardStatisticsDto(
            userCount = userCount,
            roleCount = roleCount,
            permissionCount = permissionCount,
            interfaceCount = interfaceCount,
            userCountChangePercent = 0.0,
            roleCountChangePercent = 0.0,
            permissionCountChangePercent = 0.0,
            interfaceCountChangePercent = 0.0
        )
        
        val recentUserDtos = recentUsers.map { user ->
            RecentUserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                status = user.status,
                lastLoginTime = TimeUtil.format(user.lastLoginTime)
            )
        }
        
        val systemInfo = SystemInfoDto(
            systemVersion = "1.0.0",
            javaVersion = System.getProperty("java.version"),
            springBootVersion = "N/A",
            kotlinVersion = "1.9.24",
            lastUpdate = TimeUtil.format(System.currentTimeMillis())
        )
        
        val dashboardData = DashboardDataDto(
            statistics = statistics,
            recentUsers = recentUserDtos,
            systemInfo = systemInfo
        )
        
        return ApiData.success(dashboardData)
    }
}
