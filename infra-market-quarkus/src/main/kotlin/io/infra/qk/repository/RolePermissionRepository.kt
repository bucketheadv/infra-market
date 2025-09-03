package io.infra.qk.repository

import io.infra.qk.entity.RolePermission
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * 角色权限关联 Repository
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class RolePermissionRepository @Inject constructor() : PanacheRepository<RolePermission> {
    
    /**
     * 根据角色ID查找权限关联
     */
    fun findByRoleId(roleId: Long): List<RolePermission> {
        return find("roleId", roleId).list()
    }
    
    /**
     * 根据权限ID查找角色关联
     */
    fun findByPermissionId(permissionId: Long): List<RolePermission> {
        return find("permissionId", permissionId).list()
    }
    
    /**
     * 根据多个角色ID查找权限关联
     */
    fun findByRoleIds(roleIds: List<Long>): List<RolePermission> {
        if (roleIds.isEmpty()) {
            return emptyList()
        }
        return find("roleId in ?1", roleIds).list()
    }
    
    /**
     * 删除角色的所有权限关联
     */
    fun deleteByRoleId(roleId: Long) {
        delete("roleId", roleId)
    }
    
    /**
     * 删除权限的所有角色关联
     */
    fun deleteByPermissionId(permissionId: Long) {
        delete("permissionId", permissionId)
    }
}
