package io.infra.qk.service

import io.infra.qk.util.AuthThreadLocal
import io.infra.qk.repository.UserRoleRepository
import io.infra.qk.repository.RolePermissionRepository
import io.infra.qk.repository.PermissionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.slf4j.LoggerFactory

@ApplicationScoped
class PermissionCheckService @Inject constructor(
    private val userRoleRepository: UserRoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val permissionRepository: PermissionRepository
) {
    
    private val logger = LoggerFactory.getLogger(PermissionCheckService::class.java)
    
    /**
     * 检查当前用户是否具有指定权限
     */
    fun hasPermission(permissionCode: String): Boolean {
        val currentUserId = AuthThreadLocal.getCurrentUserId()
        if (currentUserId == null) {
            logger.debug("当前用户ID为空，无法检查权限")
            return false
        }
        
        return try {
            // 获取用户的所有角色
            val userRoles = userRoleRepository.findByUserId(currentUserId)
            if (userRoles.isEmpty()) {
                logger.debug("用户 {} 没有分配任何角色", currentUserId)
                return false
            }
            
            // 获取角色对应的权限
            val roleIds = userRoles.mapNotNull { it.roleId }
            val rolePermissions = rolePermissionRepository.findByRoleIds(roleIds)
            
            // 检查是否包含指定权限
            val hasPermission = rolePermissions.any { rolePermission ->
                rolePermission.permissionId?.let { permissionId ->
                    val permission = permissionRepository.findById(permissionId)
                    permission?.code == permissionCode && permission.status == "active"
                } ?: false
            }
            
            logger.debug("用户 {} 权限检查结果: {} -> {}", currentUserId, permissionCode, hasPermission)
            hasPermission
        } catch (e: Exception) {
            logger.error("检查用户权限时发生异常: userId={}, permissionCode={}", currentUserId, permissionCode, e)
            false
        }
    }
    
    /**
     * 检查当前用户是否具有任意一个指定权限（OR关系）
     */
    fun hasAnyPermission(permissionCodes: List<String>): Boolean {
        return permissionCodes.any { hasPermission(it) }
    }
    
    /**
     * 检查当前用户是否具有所有指定权限（AND关系）
     */
    fun hasAllPermissions(permissionCodes: List<String>): Boolean {
        return permissionCodes.all { hasPermission(it) }
    }
    
    /**
     * 获取当前用户的所有权限代码
     */
    fun getCurrentUserPermissions(): List<String> {
        val currentUserId = AuthThreadLocal.getCurrentUserId()
        if (currentUserId == null) {
            return emptyList()
        }
        
        return try {
            // 获取用户的所有角色
            val userRoles = userRoleRepository.findByUserId(currentUserId)
            if (userRoles.isEmpty()) {
                return emptyList()
            }
            
            // 获取角色对应的权限
            val roleIds = userRoles.mapNotNull { it.roleId }
            val rolePermissions = rolePermissionRepository.findByRoleIds(roleIds)
            
            // 返回权限代码列表
            rolePermissions.mapNotNull { rolePermission ->
                rolePermission.permissionId?.let { permissionId ->
                    val permission = permissionRepository.findById(permissionId)
                    if (permission?.status == "active") permission.code else null
                }
            }.distinct()
        } catch (e: Exception) {
            logger.error("获取用户权限时发生异常: userId={}", currentUserId, e)
            emptyList()
        }
    }
}
