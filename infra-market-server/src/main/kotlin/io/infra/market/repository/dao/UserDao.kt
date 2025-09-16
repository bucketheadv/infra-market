package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.inList
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.le
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.kotlin.extensions.kproperty.isNotNull
import com.mybatisflex.kotlin.extensions.kproperty.ne
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.dto.UserQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.entity.User
import io.infra.market.repository.mapper.UserMapper
import org.joda.time.DateTime
import org.springframework.stereotype.Repository

/**
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Repository
class UserDao : ServiceImpl<UserMapper, User>() {
    
    fun findByUid(id: Long): User? {
        val query = query().whereWith {
            User::id.eq(id) and  User::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }

    fun findByUids(uids: List<Long>): List<User> {
        val condition = query().whereWith {
            User::id.inList(uids) and User::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectListByQuery(condition)
    }
    
    fun findByUsername(username: String): User? {
        val query = query().whereWith {
            User::username.eq(username) and User::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByEmail(email: String): User? {
        val query = query().whereWith {
            User::email.eq(email) and User::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun findByPhone(phone: String): User? {
        val query = query().whereWith {
            User::phone.eq(phone) and User::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectOneByQuery(query)
    }
    
    fun page(query: UserQueryDto): Page<User> {
        val queryBuilder = query()
        
        // 构建查询条件
        queryBuilder.whereWith {
            // 默认排除已删除的用户
            var condition = User::status.ne(StatusEnum.DELETED.code)
            
            // 添加查询条件
            if (!query.username.isNullOrBlank()) {
                condition = condition and User::username.like("%${query.username}%")
            }
            
            if (!query.status.isNullOrBlank()) {
                condition = condition and User::status.eq(query.status)
            }
            
            condition
        }
        
        // 按id排序
        queryBuilder.orderBy("id ASC")
        
        val page = Page<User>(query.current, query.size)
        return page(page, queryBuilder)
    }
    

    
    /**
     * 获取最近登录的用户
     */
    fun getRecentLoginUsers(limit: Int): List<User> {
        val query = query().whereWith {
            User::status.ne(StatusEnum.DELETED.code) and User::lastLoginTime.isNotNull
        }.orderBy("last_login_time DESC").limit(limit)
        
        return mapper.selectListByQuery(query)
    }
    
    /**
     * 获取用户总数（排除已删除的用户）
     */
    override fun count(): Long {
        val query = query().whereWith {
            User::status.ne(StatusEnum.DELETED.code)
        }
        return mapper.selectCountByQuery(query)
    }
    
    /**
     * 获取指定时间之前的用户总数（排除已删除的用户）
     */
    fun countBeforeDate(dateTime: DateTime): Long {
        val query = query().whereWith {
            User::status.ne(StatusEnum.DELETED.code) and User::createTime.le(dateTime.millis)
        }
        return mapper.selectCountByQuery(query)
    }
}