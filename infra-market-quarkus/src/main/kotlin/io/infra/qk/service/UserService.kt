package io.infra.qk.service

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.UserFormDto
import io.infra.qk.dto.UserQueryDto
import io.infra.qk.dto.PageResultDto
import io.infra.qk.dto.UserDto
import io.infra.qk.entity.User
import io.infra.qk.entity.UserRole
import io.infra.qk.enums.StatusEnum
import io.infra.qk.repository.UserRepository
import io.infra.qk.repository.UserRoleRepository
import io.infra.qk.util.AesUtil
import io.infra.qk.util.DateTimeUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.*

/**
 * 用户服务
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class UserService @Inject constructor(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository
) {
    
    fun getUserList(query: UserQueryDto): ApiResponse<PageResultDto<UserDto>> {
        val (users, total) = userRepository.findByCondition(
            query.username, query.status, query.current, query.size
        )
        
        val userDtos = users.map { user ->
            val roleIds = getUserRoleIds(user.id ?: 0)
            UserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                phone = user.phone,
                status = user.status,
                lastLoginTime = DateTimeUtil.formatDateTime(user.lastLoginTime),
                roleIds = roleIds,
                createTime = DateTimeUtil.formatDateTime(user.createTime),
                updateTime = DateTimeUtil.formatDateTime(user.updateTime)
            )
        }
        
        val pageResult = PageResultDto(
            records = userDtos,
            total = total,
            current = query.current,
            size = query.size
        )
        
        return ApiResponse.success(pageResult)
    }
    
    fun getUserById(id: Long): ApiResponse<UserDto> {
        val user = userRepository.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        val roleIds = getUserRoleIds(user.id ?: 0)
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status,
            lastLoginTime = DateTimeUtil.formatDateTime(user.lastLoginTime),
            roleIds = roleIds,
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        return ApiResponse.success(userDto)
    }
    
    @Transactional
    fun createUser(userForm: UserFormDto): ApiResponse<Unit> {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userForm.username)) {
            return ApiResponse.error("用户名已存在")
        }
        
        // 检查邮箱是否已存在
        if (!userForm.email.isNullOrBlank() && userRepository.existsByEmail(userForm.email)) {
            return ApiResponse.error("邮箱已存在")
        }
        
        val user = User(
            username = userForm.username,
            password = userForm.password?.let { AesUtil.encrypt(it) } ?: AesUtil.encrypt("123456"),
            email = userForm.email,
            phone = userForm.phone,
            status = StatusEnum.ACTIVE.code
        )
        
        userRepository.persist(user)
        
        // 保存用户角色关联
        saveUserRoles(user.id ?: 0, userForm.roleIds)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun updateUser(id: Long, userForm: UserFormDto): ApiResponse<Unit> {
        val user = userRepository.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户名是否已被其他用户使用
        val existingUser = userRepository.findByUsername(userForm.username)
        if (existingUser != null && existingUser.id != id) {
            return ApiResponse.error("用户名已存在")
        }
        
        // 检查邮箱是否已被其他用户使用
        if (!userForm.email.isNullOrBlank()) {
            val existingUserByEmail = userRepository.find("email", userForm.email).firstResult()
            if (existingUserByEmail != null && existingUserByEmail.id != id) {
                return ApiResponse.error("邮箱已存在")
            }
        }
        
        user.username = userForm.username
        user.email = userForm.email
        user.phone = userForm.phone
        user.updateTime = Date()
        
        // 如果提供了新密码，则更新密码
        if (!userForm.password.isNullOrBlank()) {
            user.password = AesUtil.encrypt(userForm.password)
        }
        
        userRepository.persist(user)
        
        // 更新用户角色关联
        userRoleRepository.deleteByUserId(id)
        saveUserRoles(id, userForm.roleIds)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun deleteUser(id: Long): ApiResponse<Unit> {
        val user = userRepository.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 删除用户角色关联
        userRoleRepository.deleteByUserId(id)
        
        // 删除用户
        userRepository.deleteById(id)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun updateUserStatus(id: Long, status: String): ApiResponse<Unit> {
        val user = userRepository.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        user.status = status
        user.updateTime = Date()
        userRepository.persist(user)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun batchDeleteUsers(ids: List<Long>): ApiResponse<Unit> {
        ids.forEach { id ->
            deleteUser(id)
        }
        return ApiResponse.success()
    }
    
    @Transactional
    fun resetPassword(id: Long): ApiResponse<Map<String, String>> {
        val user = userRepository.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        val newPassword = generateRandomPassword()
        user.password = AesUtil.encrypt(newPassword)
        user.updateTime = Date()
        userRepository.persist(user)
        
        return ApiResponse.success(mapOf("password" to newPassword))
    }
    
    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
    
    private fun getUserRoleIds(userId: Long): List<Long> {
        val userRoles = userRoleRepository.findByUserId(userId)
        return userRoles.map { it.roleId ?: 0 }
    }
    
    private fun saveUserRoles(userId: Long, roleIds: List<Long>) {
        roleIds.forEach { roleId ->
            val userRole = UserRole(userId = userId, roleId = roleId)
            userRoleRepository.persist(userRole)
        }
    }
}
