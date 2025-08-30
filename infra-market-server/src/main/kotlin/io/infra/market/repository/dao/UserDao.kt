package io.infra.market.repository.dao

import com.mybatisflex.kotlin.extensions.kproperty.inList
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.spring.service.impl.ServiceImpl
import io.infra.market.repository.entity.User
import io.infra.market.repository.mapper.UserMapper
import org.springframework.stereotype.Repository

/**
 * @author liuqinglin
 * Date: 2025/8/15 19:14
 */
@Repository
class UserDao : ServiceImpl<UserMapper, User>() {
    fun findByUid(id: Long) : User? {
        return mapper.selectOneById(id)
    }

    fun findByUids(uids: List<Long>) : List<User> {
        val condition = query().whereWith {
            User::id.inList(uids)
        }
        return mapper.selectListByQuery(condition)
    }
}