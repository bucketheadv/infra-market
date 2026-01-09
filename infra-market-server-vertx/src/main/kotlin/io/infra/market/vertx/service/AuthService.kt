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
import io.vertx.core.Future

/**
 * 认证服务
 */
class AuthService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val permissionDao: PermissionDao,
    private val tokenService: TokenService
) {
    
    fun login(request: LoginRequest): Future<ApiData<LoginResponse>> {
        return userDao.findByUsername(request.username)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<LoginResponse>("用户名或密码错误"))
                }
                
                if (!AesUtil.matches(request.password, user.password ?: "")) {
                    return@compose Future.succeededFuture(ApiData.error<LoginResponse>("用户名或密码错误"))
                }
                
                if (user.status != StatusEnum.ACTIVE.code) {
                    return@compose Future.succeededFuture(ApiData.error<LoginResponse>("用户已被禁用"))
                }
                
                user.lastLoginTime = System.currentTimeMillis()
                userDao.updateById(user)
                    .compose {
                        getUserPermissions(user.id ?: 0)
                    }
                    .compose { permissions ->
                        val token = JwtUtil.generateToken(user.id ?: 0, user.username ?: "")
                        tokenService.saveToken(user.id ?: 0, token)
                            .map {
                                val userDto = UserDto.fromEntity(user)
                                val response = LoginResponse(
                                    token = token,
                                    user = userDto,
                                    permissions = permissions
                                )
                                ApiData.success(response)
                            }
                    }
            }
    }
    
    fun logout(uid: Long): Future<ApiData<Unit>> {
        return tokenService.deleteToken(uid)
            .map { ApiData.success<Unit>() }
    }
    
    fun getCurrentUser(uid: Long): Future<ApiData<LoginResponse>> {
        return userDao.findByUid(uid)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<LoginResponse>("用户不存在"))
                }
                
                if (user.status != StatusEnum.ACTIVE.code) {
                    return@compose Future.succeededFuture(ApiData.error<LoginResponse>("用户已被禁用"))
                }
                
                getUserPermissions(user.id ?: 0)
                    .map { permissions ->
                        val userDto = UserDto.fromEntity(user)
                        val response = LoginResponse(
                            token = "",
                            user = userDto,
                            permissions = permissions
                        )
                        ApiData.success(response)
                    }
            }
    }
    
    fun getUserMenus(uid: Long): Future<ApiData<List<PermissionDto>>> {
        return userDao.findByUid(uid)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error("用户不存在"))
                }
                
                if (user.status != StatusEnum.ACTIVE.code) {
                    return@compose Future.succeededFuture(ApiData.error("用户已被禁用"))
                }
                
                userRoleDao.findByUid(user.id ?: 0)
                    .compose { userRoles ->
                        val roleIds = userRoles.mapNotNull { it.roleId }
                        if (roleIds.isEmpty()) {
                            return@compose Future.succeededFuture(ApiData.success<List<PermissionDto>>(emptyList()))
                        }
                        
                        rolePermissionDao.findByRoleIds(roleIds)
                            .compose { rolePermissions ->
                                val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
                                if (permissionIds.isEmpty()) {
                                    return@compose Future.succeededFuture(ApiData.success<List<PermissionDto>>(emptyList()))
                                }
                                
                                // 获取用户拥有的所有权限（包括按钮权限）
                                permissionDao.findByIds(permissionIds.toList())
                                    .compose { allUserPermissions ->
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
                                        collectAllParentIds(parentIdsToQuery)
                                            .compose { allParentIds: Set<Long> ->
                                                // 批量获取所有父级权限
                                                val allParentPermissionsFuture: Future<List<Permission>> = if (allParentIds.isNotEmpty()) {
                                                    permissionDao.findByIds(allParentIds.toList())
                                                } else {
                                                    Future.succeededFuture(emptyList<Permission>())
                                                }
                                                
                                                allParentPermissionsFuture.compose<ApiData<List<PermissionDto>>> { allParentPermissions: List<Permission> ->
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
                                                    permissionDao.findByIds(allNeededPermissionIds.toList())
                                                        .map { permissions: List<Permission> ->
                                                            // 只返回菜单类型的权限
                                                            val menuPermissions = permissions.filter { 
                                                                it.type == PermissionTypeEnum.MENU.code && 
                                                                it.status == StatusEnum.ACTIVE.code 
                                                            }
                                                            
                                                            // 构建菜单树
                                                            PermissionDto.buildTree(menuPermissions)
                                                        }
                                                        .map { menuTree: List<PermissionDto> ->
                                                            ApiData.success<List<PermissionDto>>(menuTree)
                                                        }
                                                }
                                            }
                                    }
                            }
                    }
            }
    }
    
    /**
     * 递归收集所有父级权限ID
     */
    private fun collectAllParentIds(initialParentIds: Set<Long>): Future<Set<Long>> {
        val allParentIds = mutableSetOf<Long>()
        val processedIds = mutableSetOf<Long>()
        val idsToProcess = initialParentIds.toMutableSet()
        
        return collectParentIdsRecursive(idsToProcess, allParentIds, processedIds)
    }
    
    /**
     * 递归收集父级权限ID（异步）
     */
    private fun collectParentIdsRecursive(
        idsToProcess: MutableSet<Long>,
        allParentIds: MutableSet<Long>,
        processedIds: MutableSet<Long>
    ): Future<Set<Long>> {
        if (idsToProcess.isEmpty()) {
            return Future.succeededFuture(allParentIds)
        }
        
        val currentBatch = idsToProcess.toList()
        idsToProcess.clear()
        
        return permissionDao.findByIds(currentBatch)
            .compose { permissions ->
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
                
                collectParentIdsRecursive(idsToProcess, allParentIds, processedIds)
            }
    }
    
    fun refreshToken(uid: Long): Future<ApiData<Map<String, String>>> {
        return userDao.findByUid(uid)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error("用户不存在"))
                }
                
                if (user.status != StatusEnum.ACTIVE.code) {
                    return@compose Future.succeededFuture(ApiData.error("用户已被禁用"))
                }
                
                val newToken = tokenService.refreshToken(uid, user.username ?: "")
                    .map { token ->
                        ApiData.success(mapOf("token" to token))
                    }
                newToken
            }
    }
    
    fun changePassword(uid: Long, request: ChangePasswordRequest): Future<ApiData<Unit>> {
        return userDao.findByUid(uid)
            .compose { user ->
                if (user == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("用户不存在"))
                }
                
                if (user.status != StatusEnum.ACTIVE.code) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("用户已被禁用"))
                }
                
                if (!AesUtil.matches(request.oldPassword, user.password ?: "")) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("原密码错误"))
                }
                
                if (request.oldPassword == request.newPassword) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("新密码不能与原密码相同"))
                }
                
                if (request.newPassword.length < 6) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("新密码长度不能少于6位"))
                }
                
                val encryptedNewPassword = AesUtil.encrypt(request.newPassword)
                user.password = encryptedNewPassword
                userDao.updateById(user)
                    .map { ApiData.success<Unit>() }
            }
    }
    
    private fun getUserPermissions(uid: Long): Future<List<String>> {
        return userRoleDao.findByUid(uid)
            .compose { userRoles ->
                val roleIds = userRoles.mapNotNull { it.roleId }
                if (roleIds.isEmpty()) {
                    return@compose Future.succeededFuture(emptyList())
                }
                
                rolePermissionDao.findByRoleIds(roleIds)
                    .compose { rolePermissions ->
                        val permissionIds = rolePermissions.mapNotNull { it.permissionId }.toSet()
                        if (permissionIds.isEmpty()) {
                            return@compose Future.succeededFuture(emptyList())
                        }
                        
                        permissionDao.findByIds(permissionIds.toList())
                            .map { permissions ->
                                permissions.filter { it.status == StatusEnum.ACTIVE.code }
                                    .map { it.code ?: "" }
                                    .distinct()
                            }
                    }
            }
    }
}

