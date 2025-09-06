package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.le
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.kotlin.extensions.kproperty.inList
import com.mybatisflex.kotlin.extensions.kproperty.ne
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.dto.RoleQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.entity.Role
import io.infra.market.repository.mapper.RoleMapper
import org.joda.time.DateTime
import org.springframework.stereotype.Repository

/**
 * 角色DAO
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Repository
class RoleDao : ServiceImpl<RoleMapper, Role>() {
    
    fun findByName(name: String): Role? {
        val query = query().whereWith {
            Role::name.eq(name) and  Role::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByCode(code: String): Role? {
        val query = query().whereWith {
            Role::code.eq(code) and Role::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByStatus(status: String): List<Role> {
        val query = query().whereWith {
            Role::status.eq(status)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun findByIds(ids: List<Long>): List<Role> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val query = query().whereWith {
            Role::id.inList(ids)
            Role::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun page(query: RoleQueryDto): Page<Role> {
        val queryBuilder = query()
        
        // 默认排除已删除的角色
        queryBuilder.whereWith {
            Role::status.ne(StatusEnum.DELETED.code)
        }
        
        // 添加查询条件
        if (!query.name.isNullOrBlank()) {
            queryBuilder.and { Role::name.like("%${query.name}%") }
        }
        
        if (!query.code.isNullOrBlank()) {
            queryBuilder.and { Role::code.like("%${query.code}%") }
        }
        
        if (!query.status.isNullOrBlank()) {
            queryBuilder.and { Role::status.eq(query.status) }
        }
        
        // 按id排序
        queryBuilder.orderBy("id ASC")
        
        val page = Page<Role>(query.current, query.size)
        return page(page, queryBuilder)
    }
    
    /**
     * 获取角色总数（排除已删除的角色）
     */
    override fun count(): Long {
        val query = query().whereWith {
            Role::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectCountByQuery(query)
    }
    
    /**
     * 获取指定时间之前的角色总数（排除已删除的角色）
     */
    fun countBeforeDate(dateTime: DateTime): Long {
        val query = query().whereWith {
            Role::status.ne(StatusEnum.DELETED.code) and Role::createTime.le(dateTime.toDate())
        }
        return mapper.selectCountByQuery(query)
    }
}
