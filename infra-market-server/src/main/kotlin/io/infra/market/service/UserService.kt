package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.UserDto
import io.infra.market.dto.UserFormDto
import io.infra.market.dto.UserQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.entity.User
import io.infra.market.repository.entity.UserRole
import io.infra.market.util.DateTimeUtil
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao
) {
    
    private val passwordEncoder = BCryptPasswordEncoder()
    
    fun getUsers(query: UserQueryDto): ApiResponse<PageResultDto<UserDto>> {
        // 使用DAO的page方法进行分页查询
        val page = userDao.page(query)
        
        val userDtos = page.records.map { user ->
            UserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                phone = user.phone,
                status = user.status.code,
                createTime = DateTimeUtil.formatDateTime(user.createTime),
                updateTime = DateTimeUtil.formatDateTime(user.updateTime)
            )
        }
        
        val result = PageResultDto(
            records = userDtos,
            total = page.totalRow,
            current = page.pageNumber.toInt(),
            size = page.pageSize.toInt()
        )
        
        return ApiResponse.success(result)
    }
    
    fun getUser(id: Long): ApiResponse<UserDto> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status.code,
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        return ApiResponse.success(userDto)
    }
    
    @Transactional
    fun createUser(form: UserFormDto): ApiResponse<UserDto> {
        // 检查用户名是否已存在
        if (userDao.findByUsername(form.username) != null) {
            return ApiResponse.error("用户名已存在")
        }
        
        // 检查邮箱是否已存在
        if (!form.email.isNullOrBlank() && userDao.findByEmail(form.email) != null) {
            return ApiResponse.error("邮箱已存在")
        }
        
        // 检查手机号是否已存在
        if (!form.phone.isNullOrBlank() && userDao.findByPhone(form.phone) != null) {
            return ApiResponse.error("手机号已存在")
        }
        
        // 加密密码
        val encodedPassword = passwordEncoder.encode(form.password ?: "123456")
        
        val user = User(
            username = form.username,
            password = encodedPassword,
            email = form.email,
            phone = form.phone,
            status = StatusEnum.ACTIVE
        )
        
        userDao.save(user)
        
        // 保存用户角色关联
        for (roleId in form.roleIds) {
            val userRole = UserRole(
                userId = user.id,
                roleId = roleId
            )
            userRoleDao.save(userRole)
        }
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status.code,
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        return ApiResponse.success(userDto)
    }
    
    @Transactional
    fun updateUser(id: Long, form: UserFormDto): ApiResponse<UserDto> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户名是否已被其他用户使用
        val existingUser = userDao.findByUsername(form.username)
        if (existingUser != null && existingUser.id != user.id) {
            return ApiResponse.error("用户名已存在")
        }
        
        // 检查邮箱是否已被其他用户使用
        if (!form.email.isNullOrBlank()) {
            val existingEmailUser = userDao.findByEmail(form.email)
            if (existingEmailUser != null && existingEmailUser.id != user.id) {
                return ApiResponse.error("邮箱已存在")
            }
        }
        
        // 检查手机号是否已被其他用户使用
        if (!form.phone.isNullOrBlank()) {
            val existingPhoneUser = userDao.findByPhone(form.phone)
            if (existingPhoneUser != null && existingPhoneUser.id != user.id) {
                return ApiResponse.error("手机号已存在")
            }
        }
        
        user.username = form.username
        user.email = form.email
        user.phone = form.phone
        
        // 如果提供了新密码，则更新密码
        if (!form.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(form.password)
        }
        
        userDao.updateById(user)
        
        // 更新用户角色关联
        userRoleDao.deleteByUserId(id)
        for (roleId in form.roleIds) {
            val userRole = UserRole(
                userId = id,
                roleId = roleId
            )
            userRoleDao.save(userRole)
        }
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status.code,
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        return ApiResponse.success(userDto)
    }
    
    fun deleteUser(id: Long): ApiResponse<Unit> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 软删除：将状态设置为已删除
        user.status = StatusEnum.DELETED
        userDao.updateById(user)
        
        return ApiResponse.success()
    }
    
    fun updateUserStatus(id: Long, status: String): ApiResponse<Unit> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        val statusEnum = StatusEnum.fromCode(status) ?: return ApiResponse.error("无效的状态值")
        user.status = statusEnum
        userDao.updateById(user)
        
        return ApiResponse.success()
    }
    
    fun resetPassword(id: Long): ApiResponse<Map<String, String>> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 生成随机密码
        val newPassword = generateRandomPassword()
        user.password = passwordEncoder.encode(newPassword)
        userDao.updateById(user)
        
        return ApiResponse.success(mapOf("password" to newPassword))
    }
    
    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
