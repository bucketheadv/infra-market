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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 用户服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class UserService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao
) {
    
    suspend fun getUsers(username: String?, status: String?, page: Int, size: Int): ApiData<PageResultDto<UserDto>> {
        val (users, total) = userDao.findPage(username, status, page, size)
        val userRoles = userRoleDao.findByUids(users.mapNotNull { it.id })
        
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
        return ApiData.success(result)
    }
    
    suspend fun getUser(id: Long): ApiData<UserDto> {
        val user = userDao.findByUid(id) ?: return ApiData.error("用户不存在")

        val userRoles = userRoleDao.findByUid(id)
        val roleIds = userRoles.mapNotNull { it.roleId }
        val userDto = UserDto.fromEntity(user, roleIds)
        return ApiData.success(userDto)
    }
    
    suspend fun createUser(username: String, email: String?, phone: String?, password: String?, roleIds: List<Long>): ApiData<UserDto> {
        val existingUser = userDao.findByUsername(username)
        if (existingUser != null) {
            return ApiData.error("用户名已存在")
        }
        
        if (!email.isNullOrBlank()) {
            val existingEmail = userDao.findByEmail(email)
            if (existingEmail != null) {
                return ApiData.error("邮箱已存在")
            }
        }
        
        if (!phone.isNullOrBlank()) {
            val existingPhone = userDao.findByPhone(phone)
            if (existingPhone != null) {
                return ApiData.error("手机号已存在")
            }
        }
        
        return createUserInternal(username, email, phone, password, roleIds)
    }
    
    private suspend fun createUserInternal(username: String, email: String?, phone: String?, password: String?, roleIds: List<Long>): ApiData<UserDto> {
        val encodedPassword = AesUtil.encrypt(password ?: "123456")
        val user = User(
            username = username,
            password = encodedPassword,
            email = email,
            phone = phone,
            status = StatusEnum.ACTIVE.code
        )
        
        val userId = userDao.save(user)
        user.id = userId
        
        coroutineScope {
            roleIds.map { roleId ->
                async {
                    val userRole = UserRole(
                        uid = userId,
                        roleId = roleId
                    )
                    userRoleDao.save(userRole)
                }
            }.awaitAll()
        }
        
        val userDto = UserDto.fromEntity(user, roleIds)
        return ApiData.success(userDto)
    }
    
    suspend fun updateUser(id: Long, username: String, email: String?, phone: String?, password: String?, roleIds: List<Long>): ApiData<UserDto> {
        val user = userDao.findByUid(id) ?: return ApiData.error("用户不存在")

        user.username = username
        user.email = email
        user.phone = phone
        if (!password.isNullOrBlank()) {
            user.password = AesUtil.encrypt(password)
        }
        
        userDao.updateById(user)
        userRoleDao.deleteByUid(id)
        
        coroutineScope {
            roleIds.map { roleId ->
                async {
                    val userRole = UserRole(
                        uid = id,
                        roleId = roleId
                    )
                    userRoleDao.save(userRole)
                }
            }.awaitAll()
        }
        
        val userDto = UserDto.fromEntity(user, roleIds)
        return ApiData.success(userDto)
    }
    
    suspend fun deleteUser(id: Long): ApiData<Unit> {
        val user = userDao.findByUid(id) ?: return ApiData.error("用户不存在")

        userRoleDao.deleteByUid(id)
        userDao.deleteById(id)
        
        return ApiData.success()
    }
    
    suspend fun updateStatus(id: Long, status: String): ApiData<Unit> {
        val user = userDao.findByUid(id) ?: return ApiData.error("用户不存在")

        user.status = status
        userDao.updateById(user)
        
        return ApiData.success()
    }
    
    suspend fun resetPassword(id: Long): ApiData<Unit> {
        val user = userDao.findByUid(id) ?: return ApiData.error("用户不存在")

        val defaultPassword = "123456"
        user.password = AesUtil.encrypt(defaultPassword)
        userDao.updateById(user)
        
        return ApiData.success()
    }
    
    suspend fun batchDeleteUsers(ids: List<Long>): ApiData<Unit> {
        if (ids.isEmpty()) {
            return ApiData.error("请选择要删除的用户")
        }
        
        coroutineScope {
            ids.map { id ->
                async {
                    val user = userDao.findByUid(id)
                    if (user != null) {
                        userRoleDao.deleteByUid(id)
                        userDao.deleteById(id)
                    }
                }
            }.awaitAll()
        }
        
        return ApiData.success()
    }
}

