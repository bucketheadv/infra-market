package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.dto.RoleDto
import io.infra.market.vertx.dto.RoleFormDto
import io.infra.market.vertx.dto.RoleQueryDto
import io.infra.market.vertx.entity.Role
import io.infra.market.vertx.entity.RolePermission
import io.infra.market.vertx.repository.RoleDao
import io.infra.market.vertx.repository.RolePermissionDao
import io.infra.market.vertx.repository.UserRoleDao
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 角色服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class RoleService(
    private val roleDao: RoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val userRoleDao: UserRoleDao
) {
    
    suspend fun getRoles(query: RoleQueryDto): ApiData<PageResultDto<RoleDto>> {
        val (roles, total) = roleDao.page(query.name, query.code, query.status, query.page, query.size)
        val roleIds = roles.mapNotNull { it.id }
        
        if (roleIds.isEmpty()) {
            return ApiData.success(PageResultDto(emptyList(), total, query.page.toLong(), query.size.toLong()))
        }
        
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val rolePermissionsMap = rolePermissions.groupBy { it.roleId ?: 0L }
            .mapValues { (_, permissions) -> permissions.mapNotNull { it.permissionId } }
        
        val roleDtos = RoleDto.fromEntityList(roles, rolePermissionsMap)
        return ApiData.success(PageResultDto(roleDtos, total, query.page.toLong(), query.size.toLong()))
    }
    
    suspend fun getAllRoles(): ApiData<List<RoleDto>> {
        val roles = roleDao.findByStatus("active")
        val roleIds = roles.mapNotNull { it.id }
        
        if (roleIds.isEmpty()) {
            return ApiData.success(emptyList())
        }
        
        val rolePermissions = rolePermissionDao.findByRoleIds(roleIds)
        val rolePermissionsMap = rolePermissions.groupBy { it.roleId ?: 0L }
            .mapValues { (_, permissions) -> permissions.mapNotNull { it.permissionId } }
        
        val roleDtos = RoleDto.fromEntityList(roles, rolePermissionsMap)
        return ApiData.success(roleDtos)
    }
    
    suspend fun getRole(id: Long): ApiData<RoleDto> {
        val role = roleDao.findById(id) ?: return ApiData.error("角色不存在")

        val rolePermissions = rolePermissionDao.findByRoleId(id)
        val permissionIds = rolePermissions.mapNotNull { it.permissionId }
        val roleDto = RoleDto.fromEntity(role, permissionIds)
        return ApiData.success(roleDto)
    }
    
    suspend fun createRole(form: RoleFormDto): ApiData<RoleDto> {
        val existingRole = roleDao.findByCode(form.code)
        if (existingRole != null) {
            return ApiData.error("角色编码已存在")
        }
        
        val now = System.currentTimeMillis()
        val role = Role(
            name = form.name,
            code = form.code,
            description = form.description,
            status = "active"
        )
        role.createTime = now
        role.updateTime = now
        
        val roleId = roleDao.save(role)
        role.id = roleId
        
        coroutineScope {
            form.permissionIds.map { permissionId ->
                async {
                    val rolePermission = RolePermission(
                        roleId = roleId,
                        permissionId = permissionId
                    )
                    rolePermissionDao.save(rolePermission)
                }
            }.awaitAll()
        }
        
        val roleDto = RoleDto.fromEntity(role, form.permissionIds)
        return ApiData.success(roleDto)
    }
    
    suspend fun updateRole(id: Long, form: RoleFormDto): ApiData<RoleDto> {
        val role = roleDao.findById(id) ?: return ApiData.error("角色不存在")

        val existingRole = roleDao.findByCode(form.code)
        if (existingRole != null && existingRole.id != role.id) {
            return ApiData.error("角色编码已存在")
        }
        
        role.name = form.name
        role.code = form.code
        role.description = form.description
        role.updateTime = System.currentTimeMillis()
        
        roleDao.updateById(role)
        rolePermissionDao.deleteByRoleId(id)
        
        coroutineScope {
            form.permissionIds.map { permissionId ->
                async {
                    val rolePermission = RolePermission(
                        roleId = id,
                        permissionId = permissionId
                    )
                    rolePermissionDao.save(rolePermission)
                }
            }.awaitAll()
        }
        
        val roleDto = RoleDto.fromEntity(role, form.permissionIds)
        return ApiData.success(roleDto)
    }
    
    suspend fun deleteRole(id: Long): ApiData<Unit> {
        val role = roleDao.findById(id) ?: return ApiData.error("角色不存在")

        if (role.code == "admin") {
            return ApiData.error("不能删除系统角色")
        }
        
        if (role.status == "deleted") {
            return ApiData.error("角色已被删除")
        }
        
        val userCount = userRoleDao.countByRoleId(id)
        if (userCount > 0) {
            return ApiData.error("该角色下还有用户，无法删除")
        }
        
        role.status = "deleted"
        roleDao.updateById(role)
        
        return ApiData.success(Unit)
    }
    
    suspend fun updateRoleStatus(id: Long, status: String): ApiData<Unit> {
        if (status !in listOf("active", "inactive", "deleted")) {
            return ApiData.error("无效的状态值")
        }

        val role = roleDao.findById(id) ?: return ApiData.error("角色不存在")

        if (role.code == "admin" && status == "deleted") {
            return ApiData.error("不能删除系统角色")
        }
        
        if (role.status == "deleted" && status != "deleted") {
            return ApiData.error("已删除的角色不能重新启用")
        }
        
        if (status == "deleted") {
            return deleteRole(id)
        }
        
        role.status = status
        roleDao.updateById(role)
        
        return ApiData.success(Unit)
    }
    
    suspend fun batchDeleteRoles(ids: List<Long>): ApiData<Unit> {
        if (ids.isEmpty()) {
            return ApiData.error("请选择要删除的角色")
        }
        
        val roles = roleDao.findByIds(ids)
        if (roles.size != ids.size) {
            return ApiData.error("部分角色不存在")
        }
        
        val systemRole = roles.find { it.code == "admin" }
        if (systemRole != null) {
            return ApiData.error("不能删除系统角色")
        }
        
        val roleCounts = coroutineScope {
            roles.map { role ->
                async {
                    val count = userRoleDao.countByRoleId(role.id ?: 0)
                    Pair(role, count)
                }
            }.awaitAll()
        }
        
        val roleWithUsers = roleCounts.find { it.second > 0 }
        if (roleWithUsers != null) {
            return ApiData.error("角色 ${roleWithUsers.first.name} 下还有用户，无法删除")
        }
        
        coroutineScope {
            roles.map { role ->
                async {
                    role.status = "deleted"
                    roleDao.updateById(role)
                }
            }.awaitAll()
        }
        
        return ApiData.success(Unit)
    }
}
