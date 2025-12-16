package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.inList
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
    
    fun findByUid(uid: Long): List<UserRole> {
        val query = query().whereWith {
            UserRole::uid.eq(uid)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun findByUids(uids: List<Long>): List<UserRole> {
        if (uids.isEmpty()) {
            return emptyList()
        }
        val query = query().whereWith {
            UserRole::uid.inList(uids)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun deleteByUid(uid: Long): Int {
        val query = query().whereWith {
            UserRole::uid.eq(uid)
        }
        return mapper.deleteByQuery(query)
    }
    
    fun countByRoleId(roleId: Long): Long {
        val query = query().whereWith {
            UserRole::roleId.eq(roleId)
        }
        return mapper.selectCountByQuery(query)
    }
}
