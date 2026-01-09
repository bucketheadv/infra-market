package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.dto.RoleDto
import io.infra.market.vertx.entity.Role
import io.infra.market.vertx.entity.RolePermission
import io.infra.market.vertx.repository.RoleDao
import io.infra.market.vertx.repository.RolePermissionDao
import io.infra.market.vertx.repository.UserRoleDao
import io.infra.market.vertx.util.FutureUtil
import io.vertx.core.Future

/**
 * 角色服务
 */
class RoleService(
    private val roleDao: RoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val userRoleDao: UserRoleDao
) {
    
    fun getRoles(name: String?, code: String?, status: String?, page: Int, size: Int): Future<ApiData<PageResultDto<RoleDto>>> {
        return roleDao.page(name, code, status, page, size)
            .compose { (roles, total) ->
                val roleIds = roles.mapNotNull { it.id }
                if (roleIds.isEmpty()) {
                    return@compose Future.succeededFuture(
                        ApiData.success(PageResultDto(emptyList(), total, page.toLong(), size.toLong()))
                    )
                }
                
                rolePermissionDao.findByRoleIds(roleIds)
                    .map { rolePermissions ->
                        val rolePermissionsMap = rolePermissions.groupBy { it.roleId ?: 0L }
                            .mapValues { (_, permissions) -> permissions.mapNotNull { it.permissionId } }
                        
                        val roleDtos = RoleDto.fromEntityList(roles, rolePermissionsMap)
                        ApiData.success(PageResultDto(roleDtos, total, page.toLong(), size.toLong()))
                    }
            }
    }
    
    fun getAllRoles(): Future<ApiData<List<RoleDto>>> {
        return roleDao.findByStatus("active")
            .compose { roles ->
                val roleIds = roles.mapNotNull { it.id }
                if (roleIds.isEmpty()) {
                    return@compose Future.succeededFuture(ApiData.success(emptyList()))
                }
                
                rolePermissionDao.findByRoleIds(roleIds)
                    .map { rolePermissions ->
                        val rolePermissionsMap = rolePermissions.groupBy { it.roleId ?: 0L }
                            .mapValues { (_, permissions) -> permissions.mapNotNull { it.permissionId } }
                        
                        val roleDtos = RoleDto.fromEntityList(roles, rolePermissionsMap)
                        ApiData.success(roleDtos)
                    }
            }
    }
    
    fun getRole(id: Long): Future<ApiData<RoleDto>> {
        return roleDao.findById(id)
            .compose { role ->
                if (role == null) {
                    return@compose Future.succeededFuture(ApiData.error<RoleDto>("角色不存在"))
                }
                
                rolePermissionDao.findByRoleId(id)
                    .map { rolePermissions ->
                        val permissionIds = rolePermissions.mapNotNull { it.permissionId }
                        val roleDto = RoleDto.fromEntity(role, permissionIds)
                        ApiData.success(roleDto)
                    }
            }
    }
    
    fun createRole(name: String, code: String, description: String?, permissionIds: List<Long>): Future<ApiData<RoleDto>> {
        return roleDao.findByCode(code)
            .compose { existingRole ->
                if (existingRole != null) {
                    return@compose Future.succeededFuture(ApiData.error<RoleDto>("角色编码已存在"))
                }
                
                val now = System.currentTimeMillis()
                val role = Role(
                    name = name,
                    code = code,
                    description = description,
                    status = "active"
                )
                role.createTime = now
                role.updateTime = now
                
                roleDao.save(role)
                    .compose { roleId ->
                        role.id = roleId
                        val saveFutures = permissionIds.map { permissionId ->
                            val rolePermission = RolePermission(
                                roleId = roleId,
                                permissionId = permissionId
                            )
                            rolePermissionDao.save(rolePermission).map { null }
                        }
                        
                        FutureUtil.all(saveFutures)
                            .map {
                                val roleDto = RoleDto.fromEntity(role, permissionIds)
                                ApiData.success(roleDto)
                            }
                    }
            }
    }
    
    fun updateRole(id: Long, name: String, code: String, description: String?, permissionIds: List<Long>): Future<ApiData<RoleDto>> {
        return roleDao.findById(id)
            .compose { role ->
                if (role == null) {
                    return@compose Future.succeededFuture(ApiData.error<RoleDto>("角色不存在"))
                }
                
                roleDao.findByCode(code)
                    .compose { existingRole ->
                        if (existingRole != null && existingRole.id != role.id) {
                            return@compose Future.succeededFuture(ApiData.error<RoleDto>("角色编码已存在"))
                        }
                        
                        role.name = name
                        role.description = description
                        role.updateTime = System.currentTimeMillis()
                        
                        roleDao.updateById(role)
                            .compose {
                                rolePermissionDao.deleteByRoleId(id)
                                    .compose {
                                        val saveFutures = permissionIds.map { permissionId ->
                                            val rolePermission = RolePermission(
                                                roleId = id,
                                                permissionId = permissionId
                                            )
                                            rolePermissionDao.save(rolePermission).map { null }
                                        }
                                        
                                        FutureUtil.all(saveFutures)
                                            .map {
                                                val roleDto = RoleDto.fromEntity(role, permissionIds)
                                                ApiData.success(roleDto)
                                            }
                                    }
                            }
                    }
            }
    }
    
    fun deleteRole(id: Long): Future<ApiData<Unit>> {
        return roleDao.findById(id)
            .compose { role ->
                if (role == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("角色不存在"))
                }
                
                if (role.code == "admin") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("不能删除系统角色"))
                }
                
                if (role.status == "deleted") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("角色已被删除"))
                }
                
                userRoleDao.countByRoleId(id)
                    .compose { userCount ->
                        if (userCount > 0) {
                            return@compose Future.succeededFuture(ApiData.error<Unit>("该角色下还有用户，无法删除"))
                        }
                        
                        role.status = "deleted"
                        roleDao.updateById(role)
                            .map { ApiData.success(Unit) }
                    }
            }
    }
    
    fun updateRoleStatus(id: Long, status: String): Future<ApiData<Unit>> {
        if (status !in listOf("active", "inactive", "deleted")) {
            return Future.succeededFuture(ApiData.error<Unit>("无效的状态值"))
        }
        
        return roleDao.findById(id)
            .compose { role ->
                if (role == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("角色不存在"))
                }
                
                if (role.code == "admin" && status == "deleted") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("不能删除系统角色"))
                }
                
                if (role.status == "deleted" && status != "deleted") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("已删除的角色不能重新启用"))
                }
                
                if (status == "deleted") {
                    return@compose deleteRole(id)
                }
                
                role.status = status
                roleDao.updateById(role)
                    .map { ApiData.success(Unit) }
            }
    }
    
    fun batchDeleteRoles(ids: List<Long>): Future<ApiData<Unit>> {
        if (ids.isEmpty()) {
            return Future.succeededFuture(ApiData.error<Unit>("请选择要删除的角色"))
        }
        
        return roleDao.findByIds(ids)
            .compose { roles ->
                if (roles.size != ids.size) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("部分角色不存在"))
                }
                
                val systemRole = roles.find { it.code == "admin" }
                if (systemRole != null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("不能删除系统角色"))
                }
                
                val checkFutures = roles.map { role ->
                    userRoleDao.countByRoleId(role.id ?: 0)
                        .map { count -> Pair(role, count) }
                }
                
                FutureUtil.all(checkFutures)
                    .compose { roleCounts ->
                        val roleWithUsers = roleCounts.find { it.second > 0 }
                        if (roleWithUsers != null) {
                            return@compose Future.succeededFuture(
                                ApiData.error<Unit>("角色 ${roleWithUsers.first.name} 下还有用户，无法删除")
                            )
                        }
                        
                        val updateFutures = roles.map { role ->
                            role.status = "deleted"
                            roleDao.updateById(role).map { null }
                        }
                        
                        FutureUtil.all(updateFutures)
                            .map { ApiData.success(Unit) }
                    }
            }
    }
}
