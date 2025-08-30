package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.dto.RoleQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.entity.Role
import io.infra.market.repository.mapper.RoleMapper
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
            Role::name.eq(name)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByCode(code: String): Role? {
        val query = query().whereWith {
            Role::code.eq(code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByStatus(status: StatusEnum): List<Role> {
        val query = query().whereWith {
            Role::status.eq(status)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun page(query: RoleQueryDto): Page<Role> {
        val queryBuilder = query()
        
        // 添加查询条件
        if (!query.name.isNullOrBlank()) {
            queryBuilder.whereWith {
                Role::name.like("%${query.name}%")
            }
        }
        
        if (!query.code.isNullOrBlank()) {
            queryBuilder.whereWith {
                Role::code.like("%${query.code}%")
            }
        }
        
        if (!query.status.isNullOrBlank()) {
            val status = StatusEnum.fromCode(query.status)
            if (status != null) {
                queryBuilder.whereWith {
                    Role::status.eq(status)
                }
            }
        }
        
        val page = Page<Role>(query.current, query.size)
        return page(page, queryBuilder)
    }
}
