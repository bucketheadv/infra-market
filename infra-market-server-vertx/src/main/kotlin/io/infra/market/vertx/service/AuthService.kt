package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.ChangePasswordRequest
import io.infra.market.vertx.dto.LoginRequest
import io.infra.market.vertx.dto.LoginResponse
import io.infra.market.vertx.dto.PermissionDto
import io.infra.market.vertx.dto.UserDto
import io.infra.market.vertx.entity.Permission
import io.infra.market.vertx.enums.PermissionTypeEnum
import io.infra.market.vertx.enums.StatusEnum
import io.infra.market.vertx.repository.PermissionDao
import io.infra.market.vertx.repository.RolePermissionDao
import io.infra.market.vertx.repository.UserDao
import io.infra.market.vertx.repository.UserRoleDao
import io.infra.market.vertx.util.AesUtil
import io.infra.market.vertx.util.JwtUtil

/**
 * 认证服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class AuthService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val permissionDao: PermissionDao,
    private val tokenService: TokenService
) {
    
    suspend fun login(request: LoginRequest): ApiData<LoginResponse> {
        val user = userDao.findByUsername(request.username) ?: return ApiData.error("用户名或密码错误")

        if (!AesUtil.matches(request.password, user.password ?: "")) {
            return ApiData.error("用户名或密码错误")
        }
        
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiData.error("用户已被禁用")
        }
        
        user.lastLoginTime = System.currentTimeMillis()
        userDao.updateById(user)
        
        val permissions = getUserPermissions(user.id ?: 0)
        val token = JwtUtil.generateToken(user.id ?: 0, user.username ?: "")
        tokenService.saveToken(user.id ?: 0, token)
        
        val userDto = UserDto.fromEntity(user)
        val response = LoginResponse(
            token = token,
            user = userDto,
            permissions = permissions
        )
        return ApiData.success(response)
    }
    
    suspend fun logout(uid: Long): ApiData<Unit> {
        tokenService.deleteToken(uid)
        return ApiData.success()
    }
    
    suspend fun getCurrentUser(uid: Long): ApiData<LoginResponse> {
        val user = userDao.findByUid(uid) ?: return ApiData.error("用户不存在")

        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiData.error("用户已被禁用")
        }
        
        val permissions = getUserPermissions(user.id ?: 0)
        val userDto = UserDto.fromEntity(user)
        val response = LoginResponse(
            token = "",
            user = userDto,
            permissions = permissions
        )
        return ApiData.success(response)
    }
    
    suspend fun getUserMenus(uid: Long): ApiData<List<PermissionDto>> {
        val user = userDao.findByUid(uid) ?: return ApiData.error("用户不存在")

        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiData.error("用户已被禁用")
        }
        
        val userRoles = userRoleDao.findByUid(user.id ?: 0)
        val roleIds = userRoles.mapNotNull { it.roleId }
        
        if (roleIds.isEmpty()) {
            return ApiData.success(emptyList<PermissionDto>())
        }
        
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
        
        if (permissionIds.isEmpty()) {
            return ApiData.success(emptyList<PermissionDto>())
        }
        
        // 获取用户拥有的所有权限（包括按钮权限）
        val allUserPermissions = permissionDao.findByIds(permissionIds.toList())
        val activePermissions = allUserPermissions.filter { it.status == StatusEnum.ACTIVE.code }
        
        // 收集所有需要查询的父级权限ID
        val parentIdsToQuery = mutableSetOf<Long>()
        
        // 收集所有按钮权限的父级权限ID
        for (permission in activePermissions) {
            if (permission.type == PermissionTypeEnum.BUTTON.code) {
                permission.parentId?.let { parentIdsToQuery.add(it) }
            }
        }
        
        // 收集所有菜单权限的父级权限ID
        for (permission in activePermissions) {
            if (permission.type == PermissionTypeEnum.MENU.code) {
                permission.parentId?.let { parentIdsToQuery.add(it) }
            }
        }
        
        // 递归收集所有父级权限ID
        val allParentIds = collectAllParentIds(parentIdsToQuery)
        
        // 批量获取所有父级权限
        val allParentPermissions = if (allParentIds.isNotEmpty()) {
            permissionDao.findByIds(allParentIds.toList())
        } else {
            emptyList()
        }
        
        // 构建权限ID到权限对象的映射
        val permissionMap = allParentPermissions.associateBy { it.id ?: 0 }
        
        // 收集所有需要的菜单权限ID（包括父级链）
        val allMenuPermissionIds = mutableSetOf<Long>()
        
        // 从按钮权限向上收集父级菜单
        for (permission in activePermissions) {
            if (permission.type == PermissionTypeEnum.BUTTON.code) {
                var currentParentId = permission.parentId
                while (currentParentId != null) {
                    allMenuPermissionIds.add(currentParentId)
                    val parentPermission = permissionMap[currentParentId]
                    currentParentId = parentPermission?.parentId
                }
            }
        }
        
        // 从菜单权限向上收集父级菜单
        for (permission in activePermissions) {
            if (permission.type == PermissionTypeEnum.MENU.code) {
                var currentParentId = permission.parentId
                while (currentParentId != null) {
                    allMenuPermissionIds.add(currentParentId)
                    val parentPermission = permissionMap[currentParentId]
                    currentParentId = parentPermission?.parentId
                }
            }
        }
        
        // 添加用户直接拥有的菜单权限
        allMenuPermissionIds.addAll(
            activePermissions
                .filter { it.type == PermissionTypeEnum.MENU.code }
                .mapNotNull { it.id }
        )
        
        // 合并所有需要的权限ID
        val allNeededPermissionIds = (allMenuPermissionIds + activePermissions.mapNotNull { it.id }).toSet()
        
        // 批量获取所有需要的权限详情
        val permissions = permissionDao.findByIds(allNeededPermissionIds.toList())
        
        // 只返回菜单类型的权限
        val menuPermissions = permissions.filter { 
            it.type == PermissionTypeEnum.MENU.code && 
            it.status == StatusEnum.ACTIVE.code 
        }
        
        // 构建菜单树
        val menuTree = PermissionDto.buildTree(menuPermissions)
        return ApiData.success(menuTree)
    }
    
    /**
     * 递归收集所有父级权限ID
     */
    private suspend fun collectAllParentIds(initialParentIds: Set<Long>): Set<Long> {
        val allParentIds = mutableSetOf<Long>()
        val processedIds = mutableSetOf<Long>()
        val idsToProcess = initialParentIds.toMutableSet()
        
        return collectParentIdsRecursive(idsToProcess, allParentIds, processedIds)
    }
    
    /**
     * 递归收集父级权限ID
     */
    private suspend fun collectParentIdsRecursive(
        idsToProcess: MutableSet<Long>,
        allParentIds: MutableSet<Long>,
        processedIds: MutableSet<Long>
    ): Set<Long> {
        if (idsToProcess.isEmpty()) {
            return allParentIds
        }
        
        val currentBatch = idsToProcess.toList()
        idsToProcess.clear()
        
        val permissions = permissionDao.findByIds(currentBatch)
        
        for (permission in permissions) {
            val permissionId = permission.id ?: 0
            if (permissionId !in processedIds) {
                processedIds.add(permissionId)
                allParentIds.add(permissionId)
                
                // 如果这个权限还有父级，添加到下一批处理
                permission.parentId?.let { parentId ->
                    if (parentId !in processedIds) {
                        idsToProcess.add(parentId)
                    }
                }
            }
        }
        
        return collectParentIdsRecursive(idsToProcess, allParentIds, processedIds)
    }
    
    suspend fun refreshToken(uid: Long): ApiData<Map<String, String>> {
        val user = userDao.findByUid(uid) ?: return ApiData.error("用户不存在")

        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiData.error("用户已被禁用")
        }
        
        val token = tokenService.refreshToken(uid, user.username ?: "")
        return ApiData.success(mapOf("token" to token))
    }
    
    suspend fun changePassword(uid: Long, request: ChangePasswordRequest): ApiData<Unit> {
        val user = userDao.findByUid(uid) ?: return ApiData.error("用户不存在")

        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiData.error("用户已被禁用")
        }
        
        if (!AesUtil.matches(request.oldPassword, user.password ?: "")) {
            return ApiData.error("原密码错误")
        }
        
        if (request.oldPassword == request.newPassword) {
            return ApiData.error("新密码不能与原密码相同")
        }
        
        if (request.newPassword.length < 6) {
            return ApiData.error("新密码长度不能少于6位")
        }
        
        val encryptedNewPassword = AesUtil.encrypt(request.newPassword)
        user.password = encryptedNewPassword
        userDao.updateById(user)
        
        return ApiData.success()
    }
    
    private suspend fun getUserPermissions(uid: Long): List<String> {
        val userRoles = userRoleDao.findByUid(uid)
        val roleIds = userRoles.mapNotNull { it.roleId }
        
        if (roleIds.isEmpty()) {
            return emptyList()
        }
        
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
        
        if (permissionIds.isEmpty()) {
            return emptyList()
        }
        
        val permissions = permissionDao.findByIds(permissionIds.toList())
        return permissions.filter { it.status == StatusEnum.ACTIVE.code }
            .map { it.code ?: "" }
            .distinct()
    }
}

