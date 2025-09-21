package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.UserDto
import io.infra.market.dto.UserFormDto
import io.infra.market.dto.UserQueryDto
import io.infra.market.dto.UserUpdateDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.entity.User
import io.infra.market.repository.entity.UserRole
import io.infra.market.util.AesUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao
) {
    
    fun getUsers(query: UserQueryDto): ApiResponse<PageResultDto<UserDto>> {
        // 使用DAO的page方法进行分页查询
        val page = userDao.page(query)
        
        // 批量获取所有用户的角色ID，避免N+1查询
        val uids = page.records.mapNotNull { it.id }
        val userRoles = userRoleDao.findByUserIds(uids)
        val userRoleMap = userRoles.groupBy { it.userId }
            .mapValues { (_, roles) -> roles.mapNotNull { it.roleId } }
            .mapKeys { (key, _) -> key ?: 0L }
        
        val userDtos = UserDto.fromEntityList(page.records, userRoleMap)
        
        val result = PageResultDto(
            records = userDtos,
            total = page.totalRow,
            current = page.pageNumber,
            size = page.pageSize
        )
        
        return ApiResponse.success(result)
    }
    
    fun getUser(id: Long): ApiResponse<UserDto> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 获取用户的角色ID列表
        val roleIds = userRoleDao.findByUserId(id).mapNotNull { it.roleId }
        
        val userDto = UserDto.fromEntity(user, roleIds)
        
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
        val encodedPassword = AesUtil.encrypt(form.password ?: "123456")
        
        val user = User(
            username = form.username,
            password = encodedPassword,
            email = form.email,
            phone = form.phone,
            status = StatusEnum.ACTIVE.code
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
        
        val userDto = UserDto.fromEntity(user, form.roleIds)
        
        return ApiResponse.success(userDto)
    }
    
    @Transactional
    fun updateUser(id: Long, form: UserUpdateDto): ApiResponse<UserDto> {
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
            user.password = AesUtil.encrypt(form.password)
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
        
        val userDto = UserDto.fromEntity(user, form.roleIds)
        
        return ApiResponse.success(userDto)
    }
    
    fun deleteUser(id: Long): ApiResponse<Unit> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 检查是否为超级管理员（假设用户名为admin的用户为超级管理员）
        if (user.username == "admin") {
            return ApiResponse.error("不能删除超级管理员用户")
        }
        
        // 检查用户当前状态
        if (user.status == StatusEnum.DELETED.code) {
            return ApiResponse.error("用户已被删除")
        }
        
        // 软删除：将状态设置为已删除
        user.status = StatusEnum.DELETED.code
        userDao.updateById(user)
        
        return ApiResponse.success()
    }
    
    fun updateUserStatus(id: Long, status: String): ApiResponse<Unit> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 验证状态值是否有效
        StatusEnum.fromCode(status) ?: return ApiResponse.error("无效的状态值")

        // 检查是否为超级管理员
        if (user.username == "admin" && status == StatusEnum.DELETED.code) {
            return ApiResponse.error("不能删除超级管理员用户")
        }
        
        // 检查状态转换是否合理
        if (user.status == StatusEnum.DELETED.code && status != StatusEnum.DELETED.code) {
            return ApiResponse.error("已删除的用户不能重新启用")
        }
        
        // 如果是要删除用户，调用删除方法
        if (status == StatusEnum.DELETED.code) {
            return deleteUser(id)
        }
        
        user.status = status
        userDao.updateById(user)
        
        return ApiResponse.success()
    }
    
    fun resetPassword(id: Long): ApiResponse<Map<String, String>> {
        val user = userDao.findByUid(id) ?: return ApiResponse.error("用户不存在")
        
        // 生成随机密码
        val newPassword = generateRandomPassword()
        user.password = AesUtil.encrypt(newPassword)
        userDao.updateById(user)
        
        return ApiResponse.success(mapOf("password" to newPassword))
    }
    
    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
    
    fun batchDeleteUsers(ids: List<Long>): ApiResponse<Unit> {
        if (ids.isEmpty()) {
            return ApiResponse.error("请选择要删除的用户")
        }
        
        val users = userDao.findByUids(ids)
        if (users.size != ids.size) {
            return ApiResponse.error("部分用户不存在")
        }
        
        // 检查是否包含超级管理员
        val adminUser = users.find { it.username == "admin" }
        if (adminUser != null) {
            return ApiResponse.error("不能删除超级管理员用户")
        }
        
        // 批量删除
        for (user in users) {
            user.status = StatusEnum.DELETED.code
            userDao.updateById(user)
        }
        
        return ApiResponse.success()
    }
}
