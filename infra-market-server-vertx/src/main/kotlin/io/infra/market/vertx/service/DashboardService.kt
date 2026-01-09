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
import io.infra.market.vertx.util.FutureUtil
import io.infra.market.vertx.util.TimeUtil
import io.vertx.core.Future

/**
 * 仪表盘服务
 */
class DashboardService(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val permissionDao: PermissionDao,
    private val apiInterfaceDao: ApiInterfaceDao
) {
    
    fun getDashboardData(): Future<ApiData<DashboardDataDto>> {
        return FutureUtil.all(
            userDao.countByStatus(null),
            roleDao.count(),
            permissionDao.count(),
            apiInterfaceDao.count(),
            userDao.findRecentUsers(10)
        )
            .map { composite ->
                val userCount = composite.resultAt<Long>(0)
                val roleCount = composite.resultAt<Long>(1)
                val permissionCount = composite.resultAt<Long>(2)
                val interfaceCount = composite.resultAt<Long>(3)
                val recentUsers = composite.resultAt<List<User>>(4)
                
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
                
                ApiData.success(dashboardData)
            }
    }
}

