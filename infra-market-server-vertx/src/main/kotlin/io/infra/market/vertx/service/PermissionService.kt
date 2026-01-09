package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.dto.PermissionDto
import io.infra.market.vertx.entity.Permission
import io.infra.market.vertx.repository.PermissionDao
import io.infra.market.vertx.repository.RolePermissionDao
import io.vertx.core.Future

/**
 * 权限服务
 */
class PermissionService(
    private val permissionDao: PermissionDao,
    private val rolePermissionDao: RolePermissionDao
) {
    
    fun getPermissions(name: String?, code: String?, type: String?, status: String?, page: Int, size: Int): Future<ApiData<PageResultDto<PermissionDto>>> {
        return permissionDao.page(name, code, type, status, page, size)
            .map { (permissions, total) ->
                val permissionDtos = PermissionDto.fromEntityList(permissions)
                ApiData.success(PageResultDto(permissionDtos, total, page.toLong(), size.toLong()))
            }
    }
    
    fun getPermissionTree(): Future<ApiData<List<PermissionDto>>> {
        return permissionDao.findByStatus("active")
            .map { permissions ->
                val tree = PermissionDto.buildTree(permissions)
                ApiData.success(tree)
            }
    }
    
    fun getPermission(id: Long): Future<ApiData<PermissionDto>> {
        return permissionDao.findById(id)
            .map { permission ->
                if (permission == null) {
                    ApiData.error<PermissionDto>("权限不存在")
                } else {
                    val permissionDto = PermissionDto.fromEntity(permission)
                    ApiData.success(permissionDto)
                }
            }
    }
    
    fun createPermission(name: String, code: String, type: String, parentId: Long?, path: String?, icon: String?, sort: Int): Future<ApiData<PermissionDto>> {
        return permissionDao.findByCode(code)
            .compose { existingPermission ->
                if (existingPermission != null) {
                    return@compose Future.succeededFuture(ApiData.error<PermissionDto>("权限编码已存在"))
                }
                
                val now = System.currentTimeMillis()
                val permission = Permission(
                    name = name,
                    code = code,
                    type = type,
                    parentId = parentId,
                    path = path,
                    icon = icon,
                    sort = sort,
                    status = "active"
                )
                permission.createTime = now
                permission.updateTime = now
                
                permissionDao.save(permission)
                    .map { permissionId ->
                        permission.id = permissionId
                        val permissionDto = PermissionDto.fromEntity(permission)
                        ApiData.success(permissionDto)
                    }
            }
    }
    
    fun updatePermission(id: Long, name: String, code: String, type: String, parentId: Long?, path: String?, icon: String?, sort: Int): Future<ApiData<PermissionDto>> {
        return permissionDao.findById(id)
            .compose { permission ->
                if (permission == null) {
                    return@compose Future.succeededFuture(ApiData.error<PermissionDto>("权限不存在"))
                }
                
                permissionDao.findByCode(code)
                    .compose { existingPermission ->
                        if (existingPermission != null && existingPermission.id != permission.id) {
                            return@compose Future.succeededFuture(ApiData.error<PermissionDto>("权限编码已存在"))
                        }
                        
                        permission.name = name
                        permission.type = type
                        permission.parentId = parentId
                        permission.path = path
                        permission.icon = icon
                        permission.sort = sort
                        permission.updateTime = System.currentTimeMillis()
                        
                        permissionDao.updateById(permission)
                            .map {
                                val permissionDto = PermissionDto.fromEntity(permission)
                                ApiData.success(permissionDto)
                            }
                    }
            }
    }
    
    fun deletePermission(id: Long): Future<ApiData<Unit>> {
        return permissionDao.findById(id)
            .compose { permission ->
                if (permission == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("权限不存在"))
                }
                
                if (permission.code == "system") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("不能删除系统权限"))
                }
                
                if (permission.status == "deleted") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("权限已被删除"))
                }
                
                permissionDao.findByParentId(id)
                    .compose { childPermissions ->
                        if (childPermissions.isNotEmpty()) {
                            return@compose Future.succeededFuture(ApiData.error<Unit>("该权限下还有子权限，无法删除"))
                        }
                        
                        rolePermissionDao.countByPermissionId(id)
                            .compose { roleCount ->
                                if (roleCount > 0) {
                                    return@compose Future.succeededFuture(ApiData.error<Unit>("该权限下还有角色，无法删除"))
                                }
                                
                                permission.status = "deleted"
                                permissionDao.updateById(permission)
                                    .map { ApiData.success(Unit) }
                            }
                    }
            }
    }
    
    fun updatePermissionStatus(id: Long, status: String): Future<ApiData<Unit>> {
        if (status !in listOf("active", "inactive", "deleted")) {
            return Future.succeededFuture(ApiData.error<Unit>("无效的状态值"))
        }
        
        return permissionDao.findById(id)
            .compose { permission ->
                if (permission == null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("权限不存在"))
                }
                
                if (permission.code == "system" && status == "deleted") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("不能删除系统权限"))
                }
                
                if (permission.status == "deleted" && status != "deleted") {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("已删除的权限不能重新启用"))
                }
                
                if (status == "deleted") {
                    return@compose deletePermission(id)
                }
                
                permission.status = status
                permissionDao.updateById(permission)
                    .map { ApiData.success(Unit) }
            }
    }
    
    fun batchDeletePermissions(ids: List<Long>): Future<ApiData<Unit>> {
        if (ids.isEmpty()) {
            return Future.succeededFuture(ApiData.error<Unit>("请选择要删除的权限"))
        }
        
        return permissionDao.findByIds(ids)
            .compose { permissions ->
                if (permissions.size != ids.size) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("部分权限不存在"))
                }
                
                val systemPermission = permissions.find { it.code == "system" }
                if (systemPermission != null) {
                    return@compose Future.succeededFuture(ApiData.error<Unit>("不能删除系统权限"))
                }
                
                val checkFutures = permissions.map { permission ->
                    permissionDao.findByParentId(permission.id ?: 0)
                        .compose { childPermissions ->
                            rolePermissionDao.countByPermissionId(permission.id ?: 0)
                                .map { roleCount -> Triple(permission, childPermissions, roleCount) }
                        }
                }
                
                io.infra.market.vertx.util.FutureUtil.all(checkFutures)
                    .compose { results ->
                        val permissionWithChildren = results.find { it.second.isNotEmpty() }
                        if (permissionWithChildren != null) {
                            return@compose Future.succeededFuture(
                                ApiData.error<Unit>("权限 ${permissionWithChildren.first.name} 下还有子权限，无法删除")
                            )
                        }
                        
                        val permissionWithRoles = results.find { it.third > 0 }
                        if (permissionWithRoles != null) {
                            return@compose Future.succeededFuture(
                                ApiData.error<Unit>("权限 ${permissionWithRoles.first.name} 下还有角色，无法删除")
                            )
                        }
                        
                        val updateFutures = permissions.map { permission ->
                            permission.status = "deleted"
                            permissionDao.updateById(permission).map { null }
                        }
                        
                        io.infra.market.vertx.util.FutureUtil.all(updateFutures)
                            .map { ApiData.success(Unit) }
                    }
            }
    }
}
