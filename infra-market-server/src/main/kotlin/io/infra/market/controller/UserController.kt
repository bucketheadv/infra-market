package io.infra.market.controller

import com.mybatisflex.kotlin.extensions.db.query
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import io.infra.market.dao.dao.UserDao
import io.infra.market.dao.entity.User
import io.infra.structure.core.tool.JsonTool
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author liuqinglin
 * Date: 2025/8/15 19:09
 */
@RestController
@RequestMapping("/user")
class UserController(
    private val userDao: UserDao,
) {
    @GetMapping("/list")
    fun index() : List<User> {
//        val users = query<User> {
//            whereWith {
//                User::id.eq(1)
//            }
//        }
        val users = userDao.findByUids(listOf(1, 2))
        println(JsonTool.toJsonString(users))
        return users
    }
}