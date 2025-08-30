package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.LoginRequest
import io.infra.market.dto.LoginResponse
import io.infra.market.dto.UserDto
import io.infra.market.dto.PermissionDto
import io.infra.market.dto.ChangePasswordRequest
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.util.DateTimeUtil
import io.infra.market.util.AesUtil
import io.infra.market.util.JwtUtil
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val permissionDao: PermissionDao,
    private val tokenService: TokenService
) {
    
    fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        val user = userDao.findByUsername(request.username) ?: return ApiResponse.error("用户名或密码错误")
        
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
        userDao.updateById(user)
        
        // 获取用户权限
        val permissions = getUserPermissions(user.id ?: 0)
        
        // 生成JWT token
        val token = JwtUtil.generateToken(user.id ?: 0, user.username ?: "")
        
        // 保存token到Redis
        tokenService.saveToken(user.id ?: 0, token)
        
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
        tokenService.deleteToken(userId)
        
        return ApiResponse.success()
    }
    
    fun getCurrentUser(userId: Long): ApiResponse<LoginResponse> {
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        val permissions = getUserPermissions(user.id ?: 0)
        
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
            token = "", // 不再返回token，因为前端已经有token了
            user = userDto,
            permissions = permissions
        )
        
        return ApiResponse.success(response)
    }
    
    fun getUserMenus(userId: Long): ApiResponse<List<PermissionDto>> {
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 获取用户角色
        val userRoles = userRoleDao.findByUserId(user.id ?: 0)
        val roleIds = userRoles.mapNotNull { it.roleId }

        if (roleIds.isEmpty()) {
            return ApiResponse.success(emptyList())
        }
        
        // 批量获取角色权限 - 避免N+1查询
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
        
        if (permissionIds.isEmpty()) {
            return ApiResponse.success(emptyList())
        }
        
        // 获取所有菜单权限（包括父级和子级）
        val allMenuPermissionIds = mutableSetOf<Long>()
        
        // 先获取用户直接拥有的菜单权限
        val directMenuPermissions = permissionDao.findMenusByIds(permissionIds.toList())
        allMenuPermissionIds.addAll(directMenuPermissions.map { it.id ?: 0 })
        
        // 对于每个直接拥有的菜单权限，找到其所有父级菜单
        for (permission in directMenuPermissions) {
            var currentParentId = permission.parentId
            while (currentParentId != null) {
                allMenuPermissionIds.add(currentParentId)
                val parentPermission = permissionDao.findByIds(listOf(currentParentId)).firstOrNull()
                currentParentId = parentPermission?.parentId
            }
        }
        
        // 批量获取所有需要的权限详情
        val permissions = permissionDao.findByIds(allMenuPermissionIds.toList()).map { permission ->
            PermissionDto(
                id = permission.id ?: 0,
                name = permission.name ?: "",
                code = permission.code ?: "",
                type = permission.type,
                parentId = permission.parentId,
                path = permission.path,
                icon = permission.icon,
                sort = permission.sort,
                status = permission.status,
                createTime = DateTimeUtil.formatDateTime(permission.createTime),
                updateTime = DateTimeUtil.formatDateTime(permission.updateTime)
            )
        }
        
        // 构建菜单树
        val menuTree = buildMenuTree(permissions)
        
        return ApiResponse.success(menuTree)
    }
    
    private fun buildMenuTree(permissions: List<PermissionDto>): List<PermissionDto> {
        val permissionMap = permissions.associateBy { it.id }
        val rootMenus = mutableListOf<PermissionDto>()
        
        for (permission in permissions) {
            if (permission.parentId == null) {
                // 根节点，直接添加到结果列表
                rootMenus.add(buildMenuWithChildren(permission, permissionMap))
            }
        }
        
        return rootMenus.sortedBy { it.sort }
    }
    
    private fun buildMenuWithChildren(permission: PermissionDto, permissionMap: Map<Long, PermissionDto>): PermissionDto {
        val children = mutableListOf<PermissionDto>()
        
        // 查找所有以当前权限为父级的权限
        for (childPermission in permissionMap.values) {
            if (childPermission.parentId == permission.id) {
                children.add(buildMenuWithChildren(childPermission, permissionMap))
            }
        }
        
        // 按sort字段排序子权限
        val sortedChildren = children.sortedBy { it.sort }
        
        // 返回带有子权限的新PermissionDto
        return permission.copy(children = sortedChildren)
    }
    
    fun refreshToken(userId: Long): ApiResponse<Map<String, String>> {
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 生成新的token
        val newToken = tokenService.refreshToken(userId, user.username ?: "")
        
        return ApiResponse.success(mapOf("token" to newToken))
    }
    
    fun changePassword(userId: Long, request: ChangePasswordRequest): ApiResponse<Unit> {
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 验证旧密码
        if (!AesUtil.matches(request.oldPassword, user.password ?: "")) {
            return ApiResponse.error("原密码错误")
        }
        
        // 验证新密码不能与旧密码相同
        if (request.oldPassword == request.newPassword) {
            return ApiResponse.error("新密码不能与原密码相同")
        }
        
        // 验证新密码长度
        if (request.newPassword.length < 6) {
            return ApiResponse.error("新密码长度不能少于6位")
        }
        
        // 加密新密码
        val encryptedNewPassword = AesUtil.encrypt(request.newPassword)
        
        // 更新密码
        user.password = encryptedNewPassword
        userDao.updateById(user)
        
        return ApiResponse.success()
    }
    
    private fun getUserPermissions(userId: Long): List<String> {
        val userRoles = userRoleDao.findByUserId(userId)
        val roleIds = userRoles.mapNotNull { it.roleId }

        if (roleIds.isEmpty()) {
            return emptyList()
        }
        
        // 批量获取角色权限 - 避免N+1查询
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
        
        if (permissionIds.isEmpty()) {
            return emptyList()
        }
        
        // 批量获取权限详情 - 避免N+1查询
        val permissions = permissionDao.findByIds(permissionIds.toList())
            .filter { it.status == StatusEnum.ACTIVE.code }
            .map { it.code ?: "" }
        
        return permissions.distinct()
    }
    

}
