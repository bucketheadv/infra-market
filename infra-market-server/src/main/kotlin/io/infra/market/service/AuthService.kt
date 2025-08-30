package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.LoginRequest
import io.infra.market.dto.LoginResponse
import io.infra.market.dto.UserDto
import io.infra.market.dto.PermissionDto
import io.infra.market.enums.StatusEnum
import io.infra.market.enums.PermissionTypeEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.util.DateTimeUtil
import io.infra.market.util.AesUtil
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val permissionDao: PermissionDao
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
        
        // 获取用户权限
        val permissions = getUserPermissions(user.id ?: 0)
        
        // 生成token（简化处理，使用用户ID作为token）
        val token = user.id.toString()
        
        val userDto = UserDto(
            id = user.id ?: 0,
            username = user.username ?: "",
            email = user.email,
            phone = user.phone,
            status = user.status,
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
        // 验证token是否为空
        if (token.isBlank()) {
            return ApiResponse.error("无效的token")
        }
        
        // 这里简化处理，实际应该验证token
        // 从token中解析用户ID（这里假设token就是用户ID）
        val userId = token.toLongOrNull() ?: return ApiResponse.error("无效的token")
        
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
    
    fun getUserMenus(token: String): ApiResponse<List<PermissionDto>> {
        // 验证token是否为空
        if (token.isBlank()) {
            return ApiResponse.error("无效的token")
        }
        
        // 从token中解析用户ID
        val userId = token.toLongOrNull() ?: return ApiResponse.error("无效的token")
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 获取用户角色
        val userRoles = userRoleDao.findByUserId(user.id ?: 0)
        val roleIds = userRoles.map { it.roleId }.filterNotNull()
        
        if (roleIds.isEmpty()) {
            return ApiResponse.success(emptyList())
        }
        
        // 批量获取角色权限 - 避免N+1查询
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.map { it.permissionId }.filterNotNull().toSet()
        
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
    
    fun refreshToken(token: String): ApiResponse<Map<String, String>> {
        // 验证token是否为空
        if (token.isBlank()) {
            return ApiResponse.error("无效的token")
        }
        
        // 从token中解析用户ID
        val userId = token.toLongOrNull() ?: return ApiResponse.error("无效的token")
        
        val user = userDao.findByUid(userId) ?: return ApiResponse.error("用户不存在")
        
        // 检查用户状态
        if (user.status != StatusEnum.ACTIVE.code) {
            return ApiResponse.error("用户已被禁用")
        }
        
        // 生成新的token（这里简化处理，实际应该使用JWT）
        val newToken = user.id.toString()
        
        return ApiResponse.success(mapOf("token" to newToken))
    }
    
    fun logout(): ApiResponse<Unit> {
        // 这里简化处理，实际应该清除token
        return ApiResponse.success()
    }
    
    private fun getUserPermissions(userId: Long): List<String> {
        val userRoles = userRoleDao.findByUserId(userId)
        val roleIds = userRoles.map { it.roleId }.filterNotNull()
        
        if (roleIds.isEmpty()) {
            return emptyList()
        }
        
        // 批量获取角色权限 - 避免N+1查询
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val permissionIds = rolePermissions.map { it.permissionId }.filterNotNull().toSet()
        
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
