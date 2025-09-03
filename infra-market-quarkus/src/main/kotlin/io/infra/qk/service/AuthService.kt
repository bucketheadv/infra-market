package io.infra.qk.service

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.LoginRequest
import io.infra.qk.dto.LoginResponse
import io.infra.qk.dto.UserDto
import io.infra.qk.dto.PermissionDto
import io.infra.qk.dto.ChangePasswordRequest
import io.infra.qk.entity.Permission
import io.infra.qk.enums.StatusEnum
import io.infra.qk.repository.UserRepository
import io.infra.qk.repository.UserRoleRepository
import io.infra.qk.repository.RolePermissionRepository
import io.infra.qk.repository.PermissionRepository
import io.infra.qk.util.DateTimeUtil
import io.infra.qk.util.AesUtil
import io.infra.qk.util.JwtUtil
import io.quarkus.redis.client.RedisClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.*

/**
 * 认证服务
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class AuthService @Inject constructor(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val permissionRepository: PermissionRepository,
    private val redisClient: RedisClient
) {
    
    @Transactional
    fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        val user = userRepository.findByUsername(request.username) ?: return ApiResponse.error("用户名或密码错误")
        
        // 验证密码
        if (!AesUtil.matches(request.password, user.password ?: "")) {
            return ApiResponse.error("用户名或密码错误")
        }
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 更新登录时间
        user.lastLoginTime = Date()
        userRepository.persist(user)
        
        // 获取用户权限
        val permissions = getUserPermissions(user.id ?: 0)
        
        // 生成JWT token
        val token = JwtUtil.generateToken(user.id ?: 0, user.username ?: "")
        
        // 保存token到Redis
        saveToken(user.id ?: 0, token)
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status,
            lastLoginTime = DateTimeUtil.formatDateTime(user.lastLoginTime),
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
    
    fun logout(userId: Long): ApiResponse<Unit> {
        // 从Redis中删除token
        deleteToken(userId)
        
        return ApiResponse.success()
    }
    
    fun getCurrentUser(userId: Long): ApiResponse<UserDto> {
        val user = userRepository.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status,
            lastLoginTime = DateTimeUtil.formatDateTime(user.lastLoginTime),
            createTime = DateTimeUtil.formatDateTime(user.createTime),
            updateTime = DateTimeUtil.formatDateTime(user.updateTime)
        )
        
        return ApiResponse.success(userDto)
    }
    
    fun getUserMenus(userId: Long): ApiResponse<List<PermissionDto>> {
        val permissions = getUserPermissions(userId)
        val menuPermissions = permissionRepository.find("type", "menu").list()
        
        // 过滤用户有权限的菜单
        val userMenuPermissions = menuPermissions.filter { it.code in permissions }
        
        // 构建菜单树
        val menuTree = buildMenuTree(userMenuPermissions)
        
        return ApiResponse.success(menuTree)
    }
    
    fun refreshToken(userId: Long): ApiResponse<String> {
        val user = userRepository.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 生成新的JWT token
        val token = JwtUtil.generateToken(user.id ?: 0, user.username ?: "")
        
        // 保存新token到Redis
        saveToken(user.id ?: 0, token)
        
        return ApiResponse.success(token)
    }
    
    @Transactional
    fun changePassword(userId: Long, request: ChangePasswordRequest): ApiResponse<Unit> {
        val user = userRepository.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 验证旧密码
        if (!AesUtil.matches(request.oldPassword, user.password ?: "")) {
            return ApiResponse.error("旧密码错误")
        }
        
        // 更新密码
        user.password = AesUtil.encrypt(request.newPassword)
        user.updateTime = Date()
        userRepository.persist(user)
        
        return ApiResponse.success()
    }
    
    private fun getUserPermissions(userId: Long): List<String> {
        val userRoles = userRoleRepository.find("userId", userId).list()
        val roleIds = userRoles.mapNotNull { it.roleId }
        
        if (roleIds.isEmpty()) return emptyList()
        
        val rolePermissions = rolePermissionRepository.find("roleId IN (?1)", roleIds).list()
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }
        
        if (permissionIds.isEmpty()) return emptyList()
        
        val permissions = permissionRepository.find("id IN (?1)", permissionIds).list()
        return permissions.mapNotNull { it.code }.filter { it.isNotBlank() }
    }
    
    private fun buildMenuTree(permissions: List<Permission>): List<PermissionDto> {
        val permissionMap = permissions.associateBy { it.id ?: 0 }
        val rootMenus = mutableListOf<PermissionDto>()
        
        for (permission in permissions) {
            if (permission.parentId == null) {
                // 根节点，构建菜单树
                val menuWithChildren = buildMenuWithChildren(permission, permissionMap)
                rootMenus.add(menuWithChildren)
            }
        }
        
        return rootMenus.sortedBy { it.sortOrder }
    }
    
    private fun buildMenuWithChildren(permission: Permission, permissionMap: Map<Long, Permission>): PermissionDto {
        val children = mutableListOf<PermissionDto>()
        
        // 查找所有以当前权限为父级的权限
        for (childPermission in permissionMap.values) {
            if (childPermission.parentId != null && childPermission.parentId == permission.id) {
                children.add(buildMenuWithChildren(childPermission, permissionMap))
            }
        }
        
        // 按sort字段排序子权限
        val sortedChildren = children.sortedBy { it.sortOrder }
        
        // 返回带有子权限的新PermissionDto
        return PermissionDto(
            id = permission.id ?: 0,
            permissionName = permission.name ?: "",
            permissionCode = permission.code ?: "",
            permissionType = permission.type,
            parentId = permission.parentId ?: 0,
            path = permission.path,
            component = null,
            icon = permission.icon,
            sortOrder = permission.sort,
            status = permission.status,
            createTime = DateTimeUtil.formatDateTime(permission.createTime),
            updateTime = DateTimeUtil.formatDateTime(permission.updateTime),
            children = sortedChildren
        )
    }
    
    private fun saveToken(userId: Long, token: String) {
        redisClient.setex("token:$userId", "${24 * 60 * 60}", token)
    }
    
    private fun deleteToken(userId: Long) {
        redisClient.del(listOf("token:$userId"))
    }
}
