package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.inList
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.kotlin.extensions.kproperty.ne
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
            Permission::code.eq(code) and  Permission::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByStatus(status: String): List<Permission> {
        val query = query().whereWith {
            Permission::status.eq(status)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun findByIds(ids: List<Long>): List<Permission> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val query = query().whereWith {
            Permission::id.inList(ids)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun findMenusByIds(ids: List<Long>): List<Permission> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val query = query().whereWith {
            Permission::id.inList(ids)
        }
        val permissions = mapper.selectListByQuery(query)
        // 在内存中过滤菜单权限
        return permissions.filter { 
            it.status == StatusEnum.ACTIVE.code && it.type == PermissionTypeEnum.MENU.code 
        }
    }
    
    fun findByParentId(parentId: Long): List<Permission> {
        val query = query().whereWith {
            Permission::parentId.eq(parentId) and Permission::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectListByQuery(query)
    }
    
    fun page(query: PermissionQueryDto): Page<Permission> {
        val queryBuilder = query()
        
        // 默认排除已删除的权限
        queryBuilder.whereWith {
            Permission::status.ne(StatusEnum.DELETED.code)
        }
        
        // 添加查询条件
        if (!query.name.isNullOrBlank()) {
            queryBuilder.and { Permission::name.like("%${query.name}%") }
        }
        
        if (!query.code.isNullOrBlank()) {
            queryBuilder.and { Permission::code.like("%${query.code}%") }
        }
        
        if (!query.type.isNullOrBlank()) {
            queryBuilder.and { Permission::type.eq(query.type) }
        }
        
        if (!query.status.isNullOrBlank()) {
            queryBuilder.and { Permission::status.eq(query.status) }
        }
        
        // 按id排序
        queryBuilder.orderBy("id ASC")
        
        val page = Page<Permission>(query.current, query.size)
        return page(page, queryBuilder)
    }
    
    /**
     * 获取权限总数（排除已删除的权限）
     */
    override fun count(): Long {
        val query = query().whereWith {
            Permission::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectCountByQuery(query)
    }
}
