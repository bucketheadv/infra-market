package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.LoginRequest
import io.infra.market.dto.LoginResponse
import io.infra.market.dto.UserDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.util.DateTimeUtil
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val permissionDao: PermissionDao
) {
    
    private val passwordEncoder = BCryptPasswordEncoder()
    
    fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        val user = userDao.findByUsername(request.username) ?: return ApiResponse.error("用户名或密码错误")
        
        // 验证密码
        if (!passwordEncoder.matches(request.password, user.password ?: "")) {
            return ApiResponse.error("用户名或密码错误")
        }
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 获取用户权限
        val permissions = getUserPermissions(user.id ?: 0)
        
        // 生成token（简化处理，实际应该使用JWT）
        val token = generateToken()
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status.code,
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        val response = LoginResponse(
            token = token,
            user = userDto,
            permissions = permissions
        )
        
        return ApiResponse.success(response)
    }
    
    fun getCurrentUser(token: String): ApiResponse<LoginResponse> {
        // 这里简化处理，实际应该验证token
        // 从token中解析用户ID（这里假设token就是用户ID）
        val userId = token.toLongOrNull() ?: return ApiResponse.error("无效的token")
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE) {
            return ApiResponse.error("用户已被禁用")
        }
        
        val permissions = getUserPermissions(user.id ?: 0)
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status.code,
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        val response = LoginResponse(
            token = token,
            user = userDto,
            permissions = permissions
        )
        
        return ApiResponse.success(response)
    }
    
    fun logout(): ApiResponse<Unit> {
        // 这里简化处理，实际应该清除token
        return ApiResponse.success()
    }
    
    private fun getUserPermissions(userId: Long): List<String> {
        val userRoles = userRoleDao.findByUserId(userId)
        val roleIds = userRoles.map { it.roleId }.filterNotNull()
        
        val permissions = mutableListOf<String>()
        for (roleId in roleIds) {
            val rolePermissions = rolePermissionDao.findByRoleId(roleId)
            val permissionIds = rolePermissions.map { it.permissionId }.filterNotNull()
            
            for (permissionId in permissionIds) {
                val permission = permissionDao.getById(permissionId)
                if (permission != null && permission.status == StatusEnum.ACTIVE) {
                    permissions.add(permission.code ?: "")
                }
            }
        }
        
        return permissions.distinct()
    }
    
    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }
}
