package io.infra.market.service

import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.enums.StatusEnum
import io.infra.market.util.AuthHolder
import org.springframework.stereotype.Service

/**
 * 权限验证服务
 * 用于检查用户是否拥有特定权限
 * 
 * @author liuqinglin
 * Date: 2025/1/27
 */
@Service
class PermissionCheckService(
    private val userRoleDao: UserRoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val permissionDao: PermissionDao
) {
    
    /**
     * 检查当前用户是否拥有指定权限
     * 
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    fun hasPermission(permissionCode: String): Boolean {
        val uid = AuthHolder.getUid() ?: return false
        return hasPermission(uid, permissionCode)
    }
    
    /**
     * 检查指定用户是否拥有指定权限
     * 
     * @param uid 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    fun hasPermission(uid: Long, permissionCode: String): Boolean {
        // 获取用户角色
        val userRoles = userRoleDao.findByUid(uid)
        val roleIds = userRoles.mapNotNull { it.roleId }
        
        if (roleIds.isEmpty()) {
            return false
        }
        
        // 获取角色权限
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
        
        if (permissionIds.isEmpty()) {
            return false
        }
        
        // 检查权限是否存在且有效
        val permissions = permissionDao.findByIds(permissionIds.toList())
            .filter { it.status == StatusEnum.ACTIVE.code }
            .map { it.code ?: "" }
        
        return permissions.contains(permissionCode)
    }
    
    /**
     * 检查当前用户是否拥有多个权限中的任意一个（OR关系）
     * 
     * @param permissionCodes 权限编码列表
     * @return 是否拥有任意一个权限
     */
    fun hasAnyPermission(permissionCodes: List<String>): Boolean {
        val uid = AuthHolder.getUid() ?: return false
        return permissionCodes.any { hasPermission(uid, it) }
    }
    
}
