package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.inList
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.repository.entity.RolePermission
import io.infra.market.repository.mapper.RolePermissionMapper
import org.springframework.stereotype.Repository

/**
 * 角色权限关联DAO
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Repository
class RolePermissionDao : ServiceImpl<RolePermissionMapper, RolePermission>() {
    
    fun findByRoleId(roleId: Long): List<RolePermission> {
        val query = query().whereWith {
            RolePermission::roleId.eq(roleId)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun findByRoleIds(roleIds: List<Long>): List<RolePermission> {
        if (roleIds.isEmpty()) {
            return emptyList()
        }
        val query = query().whereWith {
            RolePermission::roleId.inList(roleIds)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun deleteByRoleId(roleId: Long): Int {
        val query = query().whereWith {
            RolePermission::roleId.eq(roleId)
        }
        return mapper.deleteByQuery(query)
    }
    
    fun countByPermissionId(permissionId: Long): Long {
        val query = query().whereWith {
            RolePermission::permissionId.eq(permissionId)
        }
        return mapper.selectCountByQuery(query)
    }
}
