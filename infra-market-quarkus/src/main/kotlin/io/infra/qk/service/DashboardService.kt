package io.infra.qk.service

import io.infra.qk.dto.ApiResponse
import io.infra.qk.repository.UserRepository
import io.infra.qk.repository.RoleRepository
import io.infra.qk.repository.PermissionRepository
import io.infra.qk.util.DateTimeUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * 仪表板服务
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class DashboardService @Inject constructor(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository
) {
    
    fun getDashboardData(): ApiResponse<Map<String, Any>> {
        val totalUsers = userRepository.count()
        val totalRoles = roleRepository.count()
        val totalPermissions = permissionRepository.count()
        
        val activeUsers = userRepository.count("status", "active")
        val activeRoles = roleRepository.count("status", "active")
        val activePermissions = permissionRepository.count("status", "active")
        
        val recentUsers = userRepository.find("ORDER BY createTime DESC")
            .range(0, 5)
            .list()
            .map { user ->
                mapOf(
                    "id" to (user.id ?: 0),
                    "username" to (user.username ?: ""),
                    "email" to (user.email ?: ""),
                    "createTime" to DateTimeUtil.formatDateTime(user.createTime)
                )
            }
        
        val dashboardData = mapOf(
            "statistics" to mapOf(
                "totalUsers" to totalUsers,
                "totalRoles" to totalRoles,
                "totalPermissions" to totalPermissions,
                "activeUsers" to activeUsers,
                "activeRoles" to activeRoles,
                "activePermissions" to activePermissions
            ),
            "recentUsers" to recentUsers,
            "systemInfo" to getSystemInfo()
        )
        
        return ApiResponse.success(dashboardData)
    }
    
    private fun getSystemInfo(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        return mapOf(
            "javaVersion" to System.getProperty("java.version"),
            "osName" to System.getProperty("os.name"),
            "osVersion" to System.getProperty("os.version"),
            "totalMemory" to totalMemory,
            "freeMemory" to freeMemory,
            "usedMemory" to usedMemory,
            "maxMemory" to maxMemory,
            "currentTime" to DateTimeUtil.nowString()
        )
    }
}
