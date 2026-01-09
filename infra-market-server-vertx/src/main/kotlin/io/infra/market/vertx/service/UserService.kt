package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.dto.UserDto
import io.infra.market.vertx.entity.User
import io.infra.market.vertx.entity.UserRole
import io.infra.market.vertx.enums.StatusEnum
import io.infra.market.vertx.repository.UserDao
import io.infra.market.vertx.repository.UserRoleDao
import io.infra.market.vertx.util.AesUtil
import io.infra.market.vertx.util.FutureUtil
import io.vertx.core.Future

/**
 * 用户服务
 */
class UserService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao
) {
    
    fun getUsers(username: String?, status: String?, page: Int, size: Int): Future<ApiData<PageResultDto<UserDto>>> {
        return userDao.findPage(username, status, page, size)
            .map { (users, total) ->
                userRoleDao.findByUids(users.mapNotNull { it.id })
                    .map { userRoles ->
                        val userRoleMap = userRoles.groupBy { it.uid }
                            .mapValues { (_, roles) -> roles.mapNotNull { it.roleId } }
                            .mapKeys { (key, _) -> key ?: 0L }
                        
                        val userDtos = UserDto.fromEntityList(users, userRoleMap)
                        val result = PageResultDto(
                            records = userDtos,
                            total = total,
                            page = page.toLong(),
                            size = size.toLong()
                        )
                        ApiData.success(result)
                    }
            }
            .compose { it }
    }
    
    fun getUser(id: Long): Future<ApiData<UserDto>> {
        return userDao.findByUid(id)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<UserDto>("用户不存在"))
                }
                
                userRoleDao.findByUid(id)
                    .map { userRoles ->
                        val roleIds = userRoles.mapNotNull { it.roleId }
                        val userDto = UserDto.fromEntity(user, roleIds)
                        ApiData.success(userDto)
                    }
            }
    }
    
    fun createUser(username: String, email: String?, phone: String?, password: String?, roleIds: List<Long>): Future<ApiData<UserDto>> {
        return userDao.findByUsername(username)
            .compose { existingUser ->
                if (existingUser != null) {
                    return@compose Future.succeededFuture(ApiData.error<UserDto>("用户名已存在"))
                }
                
                if (!email.isNullOrBlank()) {
                    userDao.findByEmail(email)
                        .compose { existingEmail ->
                            if (existingEmail != null) {
                                return@compose Future.succeededFuture(ApiData.error<UserDto>("邮箱已存在"))
                            }
                            
                            if (!phone.isNullOrBlank()) {
                                userDao.findByPhone(phone)
                                    .compose { existingPhone ->
                                        if (existingPhone != null) {
                                            return@compose Future.succeededFuture(ApiData.error<UserDto>("手机号已存在"))
                                        }
                                        createUserInternal(username, email, phone, password, roleIds)
                                    }
                            } else {
                                createUserInternal(username, email, phone, password, roleIds)
                            }
                        }
                } else {
                    if (!phone.isNullOrBlank()) {
                        userDao.findByPhone(phone)
                            .compose { existingPhone ->
                                if (existingPhone != null) {
                                    return@compose Future.succeededFuture(ApiData.error<UserDto>("手机号已存在"))
                                }
                                createUserInternal(username, email, phone, password, roleIds)
                            }
                    } else {
                        createUserInternal(username, email, phone, password, roleIds)
                    }
                }
            }
    }
    
    private fun createUserInternal(username: String, email: String?, phone: String?, password: String?, roleIds: List<Long>): Future<ApiData<UserDto>> {
        val encodedPassword = AesUtil.encrypt(password ?: "123456")
        val user = User(
            username = username,
            password = encodedPassword,
            email = email,
            phone = phone,
            status = StatusEnum.ACTIVE.code
        )
        
        return userDao.save(user)
            .compose { userId ->
                user.id = userId
                val now = System.currentTimeMillis()
                val futures = roleIds.map { roleId ->
                    val userRole = UserRole(
                        uid = userId,
                        roleId = roleId
                    )
                    userRoleDao.save(userRole)
                }
                
                FutureUtil.all(futures)
                    .map {
                        val userDto = UserDto.fromEntity(user, roleIds)
                        ApiData.success(userDto)
                    }
            }
    }
    
    fun updateUser(id: Long, username: String, email: String?, phone: String?, password: String?, roleIds: List<Long>): Future<ApiData<UserDto>> {
        return userDao.findByUid(id)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<UserDto>("用户不存在"))
                }
                
                user.username = username
                user.email = email
                user.phone = phone
                if (!password.isNullOrBlank()) {
                    user.password = AesUtil.encrypt(password)
                }
                
                userDao.updateById(user)
                    .compose {
                        userRoleDao.deleteByUid(id)
                            .compose {
                                val now = System.currentTimeMillis()
                                val futures = roleIds.map { roleId ->
                                    val userRole = UserRole(
                                        uid = id,
                                        roleId = roleId
                                    )
                                    userRoleDao.save(userRole)
                                }
                                
                                FutureUtil.all(futures)
                                    .map {
                                        val userDto = UserDto.fromEntity(user, roleIds)
                                        ApiData.success(userDto)
                                    }
                            }
                    }
            }
    }
    
    fun deleteUser(id: Long): Future<ApiData<Unit>> {
        return userDao.findByUid(id)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("用户不存在"))
                }
                
                userRoleDao.deleteByUid(id)
                    .compose {
                        userDao.deleteById(id)
                            .map { ApiData.success<Unit>() }
                    }
            }
    }
    
    fun updateStatus(id: Long, status: String): Future<ApiData<Unit>> {
        return userDao.findByUid(id)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("用户不存在"))
                }
                
                user.status = status
                userDao.updateById(user)
                    .map { ApiData.success<Unit>() }
            }
    }
    
    fun resetPassword(id: Long): Future<ApiData<Unit>> {
        return userDao.findByUid(id)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("用户不存在"))
                }
                
                val defaultPassword = "123456"
                user.password = AesUtil.encrypt(defaultPassword)
                userDao.updateById(user)
                    .map { ApiData.success<Unit>() }
            }
    }
    
    fun batchDeleteUsers(ids: List<Long>): Future<ApiData<Unit>> {
        if (ids.isEmpty()) {
            return Future.succeededFuture(ApiData.error<Unit>("请选择要删除的用户"))
        }
        
        val futures: List<Future<Unit>> = ids.map { id ->
            userDao.findByUid(id)
                .compose { user ->
                    if (user == null) {
                        Future.succeededFuture<Unit>()
                    } else {
                        userRoleDao.deleteByUid(id)
                            .compose {
                                userDao.deleteById(id)
                                    .map { null }
                            }
                    }
                }
        }
        
        return FutureUtil.all<Unit>(futures)
            .map { ApiData.success() }
    }
}

