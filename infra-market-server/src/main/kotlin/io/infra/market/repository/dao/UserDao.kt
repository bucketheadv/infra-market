package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.inList
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.dto.UserQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.entity.User
import io.infra.market.repository.mapper.UserMapper
import org.springframework.stereotype.Repository

/**
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Repository
class UserDao : ServiceImpl<UserMapper, User>() {
    
    fun findByUid(id: Long): User? {
        return mapper.selectOneById(id)
    }

    fun findByUids(uids: List<Long>): List<User> {
        val condition = query().whereWith {
            User::id.inList(uids)
        }
        return mapper.selectListByQuery(condition)
    }
    
    fun findByUsername(username: String): User? {
        val query = query().whereWith {
            User::username.eq(username)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByEmail(email: String): User? {
        val query = query().whereWith {
            User::email.eq(email)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByPhone(phone: String): User? {
        val query = query().whereWith {
            User::phone.eq(phone)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun page(query: UserQueryDto): Page<User> {
        val queryBuilder = query()
        
        // 添加查询条件
        if (!query.username.isNullOrBlank()) {
            queryBuilder.whereWith {
                User::username.like("%${query.username}%")
            }
        }
        
        if (!query.status.isNullOrBlank()) {
            queryBuilder.whereWith {
                User::status.eq(query.status)
            }
        }
        
        val page = Page<User>(query.current, query.size)
        return page(page, queryBuilder)
    }
}