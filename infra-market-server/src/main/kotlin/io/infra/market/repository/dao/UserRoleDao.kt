package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.repository.entity.UserRole
import io.infra.market.repository.mapper.UserRoleMapper
import org.springframework.stereotype.Repository

/**
 * 用户角色关联DAO
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Repository
class UserRoleDao : ServiceImpl<UserRoleMapper, UserRole>() {
    
    fun findByUserId(userId: Long): List<UserRole> {
        val query = query().whereWith {
            UserRole::userId.eq(userId)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun findByRoleId(roleId: Long): List<UserRole> {
        val query = query().whereWith {
            UserRole::roleId.eq(roleId)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun deleteByUserId(userId: Long): Int {
        val query = query().whereWith {
            UserRole::userId.eq(userId)
        }
        return mapper.deleteByQuery(query)
    }
}
