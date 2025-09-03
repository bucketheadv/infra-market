package io.infra.qk.repository

import io.infra.qk.entity.UserRole
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * 用户角色关联 Repository
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class UserRoleRepository @Inject constructor() : PanacheRepository<UserRole> {
    
    /**
     * 根据用户ID查找角色关联
     */
    fun findByUserId(userId: Long): List<UserRole> {
        return find("userId", userId).list()
    }
    
    /**
     * 根据角色ID查找用户关联
     */
    fun findByRoleId(roleId: Long): List<UserRole> {
        return find("roleId", roleId).list()
    }
    
    /**
     * 删除用户的所有角色关联
     */
    fun deleteByUserId(userId: Long) {
        delete("userId", userId)
    }
    
    /**
     * 删除角色的所有用户关联
     */
    fun deleteByRoleId(roleId: Long) {
        delete("roleId", roleId)
    }
}
