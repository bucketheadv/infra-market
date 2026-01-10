package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.dto.UserDto
import io.infra.market.vertx.dto.UserFormDto
import io.infra.market.vertx.dto.UserQueryDto
import io.infra.market.vertx.dto.UserUpdateDto
import io.infra.market.vertx.entity.User
import io.infra.market.vertx.entity.UserRole
import io.infra.market.vertx.enums.StatusEnum
import io.infra.market.vertx.repository.UserDao
import io.infra.market.vertx.repository.UserRoleDao
import io.infra.market.vertx.util.AesUtil

/**
 * 用户服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class UserService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao
) {
    
    suspend fun getUsers(query: UserQueryDto): ApiData<PageResultDto<UserDto>> {
        val page = userDao.findPage(query.username, query.status, query.page, query.size)
        val userRoles = userRoleDao.findByUids(page.records.mapNotNull { it.id })
        
        val userRoleMap = userRoles.groupBy { it.uid }
            .mapValues { (_, roles) -> roles.mapNotNull { it.roleId } }
            .mapKeys { (key, _) -> key ?: 0L }
        
        val userDtos = UserDto.fromEntityList(page.records, userRoleMap)
        val result = PageResultDto(
            records = userDtos,
            total = page.total,
            page = page.page,
            size = page.size
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
    
    suspend fun createUser(form: UserFormDto): ApiData<UserDto> {
        val existingUser = userDao.findByUsername(form.username)
        if (existingUser != null) {
            return ApiData.error("用户名已存在")
        }
        
        if (!form.email.isNullOrBlank()) {
            val existingEmail = userDao.findByEmail(form.email)
            if (existingEmail != null) {
                return ApiData.error("邮箱已存在")
            }
        }
        
        if (!form.phone.isNullOrBlank()) {
            val existingPhone = userDao.findByPhone(form.phone)
            if (existingPhone != null) {
                return ApiData.error("手机号已存在")
            }
        }
        
        // 使用事务
        val result = userDao.withTransaction { conn ->
            val encodedPassword = AesUtil.encrypt(form.password ?: "123456")
            val user = User(
                username = form.username,
                password = encodedPassword,
                email = form.email,
                phone = form.phone,
                status = StatusEnum.ACTIVE.code
            )
            
            val userId = userDao.save(user, conn)
            user.id = userId
            
            form.roleIds.forEach { roleId ->
                val userRole = UserRole(
                    uid = userId,
                    roleId = roleId
                )
                userRoleDao.save(userRole, conn)
            }
            
            UserDto.fromEntity(user, form.roleIds)
        }
        
        return ApiData.success(result)
    }
    
    suspend fun updateUser(id: Long, form: UserUpdateDto): ApiData<UserDto> {
        val user = userDao.findByUid(id) ?: return ApiData.error("用户不存在")

        // 检查用户名是否已被其他用户使用
        val existingUser = userDao.findByUsername(form.username)
        if (existingUser != null && existingUser.id != user.id) {
            return ApiData.error("用户名已存在")
        }
        
        // 检查邮箱是否已被其他用户使用
        if (!form.email.isNullOrBlank()) {
            val existingEmail = userDao.findByEmail(form.email)
            if (existingEmail != null && existingEmail.id != user.id) {
                return ApiData.error("邮箱已存在")
            }
        }
        
        // 检查手机号是否已被其他用户使用
        if (!form.phone.isNullOrBlank()) {
            val existingPhone = userDao.findByPhone(form.phone)
            if (existingPhone != null && existingPhone.id != user.id) {
                return ApiData.error("手机号已存在")
            }
        }
        
        // 使用事务
        val result = userDao.withTransaction { conn ->
            user.username = form.username
            user.email = form.email
            user.phone = form.phone
            if (!form.password.isNullOrBlank()) {
                user.password = AesUtil.encrypt(form.password)
            }
            
            userDao.updateById(user, conn)
            userRoleDao.deleteByUid(id, conn)
            
            form.roleIds.forEach { roleId ->
                val userRole = UserRole(
                    uid = id,
                    roleId = roleId
                )
                userRoleDao.save(userRole, conn)
            }
            
            UserDto.fromEntity(user, form.roleIds)
        }
        
        return ApiData.success(result)
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
        
        // 使用事务
        userDao.withTransaction { conn ->
            ids.forEach { id ->
                userRoleDao.deleteByUid(id, conn)
                userDao.deleteById(id, conn)
            }
        }
        
        return ApiData.success()
    }
}

