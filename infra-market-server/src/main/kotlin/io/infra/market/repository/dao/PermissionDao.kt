package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.dto.PermissionQueryDto
import io.infra.market.enums.PermissionTypeEnum
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.entity.Permission
import io.infra.market.repository.mapper.PermissionMapper
import org.springframework.stereotype.Repository

/**
 * 权限DAO
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Repository
class PermissionDao : ServiceImpl<PermissionMapper, Permission>() {
    
    fun findByCode(code: String): Permission? {
        val query = query().whereWith {
            Permission::code.eq(code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByStatus(status: String): List<Permission> {
        val query = query().whereWith {
            Permission::status.eq(status)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun page(query: PermissionQueryDto): Page<Permission> {
        val queryBuilder = query()
        
        // 添加查询条件
        if (!query.name.isNullOrBlank()) {
            queryBuilder.whereWith {
                Permission::name.like("%${query.name}%")
            }
        }
        
        if (!query.code.isNullOrBlank()) {
            queryBuilder.whereWith {
                Permission::code.like("%${query.code}%")
            }
        }
        
        if (!query.type.isNullOrBlank()) {
            queryBuilder.whereWith {
                Permission::type.eq(query.type)
            }
        }
        
        if (!query.status.isNullOrBlank()) {
            queryBuilder.whereWith {
                Permission::status.eq(query.status)
            }
        }
        
        val page = Page<Permission>(query.current, query.size)
        return page(page, queryBuilder)
    }
}
