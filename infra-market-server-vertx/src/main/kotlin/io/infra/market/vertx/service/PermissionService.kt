package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.dto.PermissionDto
import io.infra.market.vertx.entity.Permission
import io.infra.market.vertx.repository.PermissionDao
import io.infra.market.vertx.repository.RolePermissionDao
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 权限服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class PermissionService(
    private val permissionDao: PermissionDao,
    private val rolePermissionDao: RolePermissionDao
) {
    
    suspend fun getPermissions(name: String?, code: String?, type: String?, status: String?, page: Int, size: Int): ApiData<PageResultDto<PermissionDto>> {
        val (permissions, total) = permissionDao.page(name, code, type, status, page, size)
        val permissionDtos = PermissionDto.fromEntityList(permissions)
        return ApiData.success(PageResultDto(permissionDtos, total, page.toLong(), size.toLong()))
    }
    
    suspend fun getPermissionTree(): ApiData<List<PermissionDto>> {
        val permissions = permissionDao.findByStatus("active")
        val tree = PermissionDto.buildTree(permissions)
        return ApiData.success(tree)
    }
    
    suspend fun getPermission(id: Long): ApiData<PermissionDto> {
        val permission = permissionDao.findById(id) ?: return ApiData.error("权限不存在")

        val permissionDto = PermissionDto.fromEntity(permission)
        return ApiData.success(permissionDto)
    }
    
    suspend fun createPermission(name: String, code: String, type: String, parentId: Long?, path: String?, icon: String?, sort: Int): ApiData<PermissionDto> {
        val existingPermission = permissionDao.findByCode(code)
        if (existingPermission != null) {
            return ApiData.error("权限编码已存在")
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
        
        val permissionId = permissionDao.save(permission)
        permission.id = permissionId
        
        val permissionDto = PermissionDto.fromEntity(permission)
        return ApiData.success(permissionDto)
    }
    
    suspend fun updatePermission(id: Long, name: String, code: String, type: String, parentId: Long?, path: String?, icon: String?, sort: Int): ApiData<PermissionDto> {
        val permission = permissionDao.findById(id)
        
        if (permission == null) {
            return ApiData.error("权限不存在")
        }
        
        val existingPermission = permissionDao.findByCode(code)
        if (existingPermission != null && existingPermission.id != permission.id) {
            return ApiData.error("权限编码已存在")
        }
        
        permission.name = name
        permission.type = type
        permission.parentId = parentId
        permission.path = path
        permission.icon = icon
        permission.sort = sort
        permission.updateTime = System.currentTimeMillis()
        
        permissionDao.updateById(permission)
        
        val permissionDto = PermissionDto.fromEntity(permission)
        return ApiData.success(permissionDto)
    }
    
    suspend fun deletePermission(id: Long): ApiData<Unit> {
        val permission = permissionDao.findById(id)
        
        if (permission == null) {
            return ApiData.error("权限不存在")
        }
        
        if (permission.code == "system") {
            return ApiData.error("不能删除系统权限")
        }
        
        if (permission.status == "deleted") {
            return ApiData.error("权限已被删除")
        }
        
        val childPermissions = permissionDao.findByParentId(id)
        if (childPermissions.isNotEmpty()) {
            return ApiData.error("该权限下还有子权限，无法删除")
        }
        
        val roleCount = rolePermissionDao.countByPermissionId(id)
        if (roleCount > 0) {
            return ApiData.error("该权限下还有角色，无法删除")
        }
        
        permission.status = "deleted"
        permissionDao.updateById(permission)
        
        return ApiData.success(Unit)
    }
    
    suspend fun updatePermissionStatus(id: Long, status: String): ApiData<Unit> {
        if (status !in listOf("active", "inactive", "deleted")) {
            return ApiData.error("无效的状态值")
        }
        
        val permission = permissionDao.findById(id)
        
        if (permission == null) {
            return ApiData.error("权限不存在")
        }
        
        if (permission.code == "system" && status == "deleted") {
            return ApiData.error("不能删除系统权限")
        }
        
        if (permission.status == "deleted" && status != "deleted") {
            return ApiData.error("已删除的权限不能重新启用")
        }
        
        if (status == "deleted") {
            return deletePermission(id)
        }
        
        permission.status = status
        permissionDao.updateById(permission)
        
        return ApiData.success(Unit)
    }
    
    suspend fun batchDeletePermissions(ids: List<Long>): ApiData<Unit> {
        if (ids.isEmpty()) {
            return ApiData.error("请选择要删除的权限")
        }
        
        val permissions = permissionDao.findByIds(ids)
        if (permissions.size != ids.size) {
            return ApiData.error("部分权限不存在")
        }
        
        val systemPermission = permissions.find { it.code == "system" }
        if (systemPermission != null) {
            return ApiData.error("不能删除系统权限")
        }
        
        val results = coroutineScope {
            permissions.map { permission ->
                async {
                    val childPermissions = permissionDao.findByParentId(permission.id ?: 0)
                    val roleCount = rolePermissionDao.countByPermissionId(permission.id ?: 0)
                    Triple(permission, childPermissions, roleCount)
                }
            }.awaitAll()
        }
        
        val permissionWithChildren = results.find { it.second.isNotEmpty() }
        if (permissionWithChildren != null) {
            return ApiData.error("权限 ${permissionWithChildren.first.name} 下还有子权限，无法删除")
        }
        
        val permissionWithRoles = results.find { it.third > 0 }
        if (permissionWithRoles != null) {
            return ApiData.error("权限 ${permissionWithRoles.first.name} 下还有角色，无法删除")
        }
        
        coroutineScope {
            permissions.map { permission ->
                async {
                    permission.status = "deleted"
                    permissionDao.updateById(permission)
                }
            }.awaitAll()
        }
        
        return ApiData.success(Unit)
    }
}
