package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.LoginRequest
import io.infra.market.dto.LoginResponse
import io.infra.market.dto.UserDto
import io.infra.market.dto.PermissionDto
import io.infra.market.dto.ChangePasswordRequest
import io.infra.market.enums.PermissionTypeEnum
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.util.DateTimeUtil
import io.infra.market.util.AesUtil
import io.infra.market.util.JwtUtil
import org.springframework.stereotype.Service

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
        user.lastLoginTime = System.currentTimeMillis()
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
        
        // 获取用户拥有的所有权限（包括按钮权限）
        val allUserPermissions = permissionDao.findByIds(permissionIds.toList())
        
        // 收集所有需要的菜单权限ID
        val allMenuPermissionIds = mutableSetOf<Long>()
        
        // 收集所有需要查询的父级权限ID
        val parentIdsToQuery = mutableSetOf<Long>()
        
        // 收集所有按钮权限的父级权限ID
        for (permission in allUserPermissions) {
            if (permission.type == PermissionTypeEnum.BUTTON.code) {
                val parentId = permission.parentId
                if (parentId != null) {
                    parentIdsToQuery.add(parentId)
                }
            }
        }
        
        // 收集所有菜单权限的父级权限ID
        for (permission in allUserPermissions) {
            if (permission.type == PermissionTypeEnum.MENU.code) {
                val parentId = permission.parentId
                if (parentId != null) {
                    parentIdsToQuery.add(parentId)
                }
            }
        }
        
        // 递归收集所有父级权限ID
        val allParentIds = collectAllParentIds(parentIdsToQuery)
        
        // 批量获取所有父级权限，避免N+1查询
        val allParentPermissions = if (allParentIds.isNotEmpty()) {
            permissionDao.findByIds(allParentIds.toList())
        } else {
            emptyList()
        }
        
        // 构建权限ID到权限对象的映射
        val permissionMap = allParentPermissions.associateBy { it.id ?: 0 }
        
        // 收集所有需要的菜单权限ID（包括父级链）
        for (permission in allUserPermissions) {
            if (permission.type == PermissionTypeEnum.BUTTON.code) {
                var currentParentId = permission.parentId
                while (currentParentId != null) {
                    allMenuPermissionIds.add(currentParentId)
                    val parentPermission = permissionMap[currentParentId]
                    currentParentId = parentPermission?.parentId
                }
            }
        }
        
        for (permission in allUserPermissions) {
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
        allMenuPermissionIds.addAll(allUserPermissions.filter { it.type == PermissionTypeEnum.MENU.code }.map { it.id ?: 0 })
        
        // 合并所有需要的权限ID
        val allNeededPermissionIds = (allMenuPermissionIds + allUserPermissions.mapNotNull { it.id }).toSet()
        
        // 批量获取所有需要的权限详情
        val permissions = permissionDao.findByIds(allNeededPermissionIds.toList()).map { permission ->
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
                // 根节点，构建菜单树
                val menuWithChildren = buildMenuWithChildren(permission, permissionMap)
                rootMenus.add(menuWithChildren)
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
    
    /**
     * 递归收集所有父级权限ID
     */
    private fun collectAllParentIds(initialParentIds: Set<Long>): Set<Long> {
        val allParentIds = mutableSetOf<Long>()
        val processedIds = mutableSetOf<Long>()
        val idsToProcess = initialParentIds.toMutableSet()
        
        while (idsToProcess.isNotEmpty()) {
            val currentBatch = idsToProcess.toList()
            idsToProcess.clear()
            
            // 批量查询当前批次的权限
            val permissions = permissionDao.findByIds(currentBatch)
            
            for (permission in permissions) {
                val permissionId = permission.id ?: 0
                if (permissionId !in processedIds) {
                    processedIds.add(permissionId)
                    allParentIds.add(permissionId)
                    
                    // 如果这个权限还有父级，添加到下一批处理
                    val parentId = permission.parentId
                    if (parentId != null && parentId !in processedIds) {
                        idsToProcess.add(parentId)
                    }
                }
            }
        }
        
        return allParentIds
    }
    

}
