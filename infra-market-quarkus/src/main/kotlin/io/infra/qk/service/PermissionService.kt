package io.infra.qk.service

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.PermissionFormDto
import io.infra.qk.dto.PermissionQueryDto
import io.infra.qk.dto.PageResultDto
import io.infra.qk.dto.PermissionDto
import io.infra.qk.entity.Permission
import io.infra.qk.enums.StatusEnum
import io.infra.qk.repository.PermissionRepository
import io.infra.qk.util.DateTimeUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.*

/**
 * 权限服务
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class PermissionService @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    
    fun getPermissionList(query: PermissionQueryDto): ApiResponse<PageResultDto<PermissionDto>> {
        val (permissions, total) = permissionRepository.findByCondition(
            query.permissionName, query.permissionCode, query.permissionType, 
            query.parentId, query.status, query.current, query.size
        )
        
        val permissionDtos = permissions.map { permission ->
            PermissionDto(
                id = permission.id ?: 0,
                permissionName = permission.name ?: "",
                permissionCode = permission.code ?: "",
                permissionType = permission.type,
                parentId = permission.parentId,
                path = permission.path,
                component = "",
                icon = permission.icon,
                sortOrder = permission.sort,
                status = permission.status,
                createTime = DateTimeUtil.formatDateTime(permission.createTime),
                updateTime = DateTimeUtil.formatDateTime(permission.updateTime)
            )
        }
        
        val pageResult = PageResultDto(
            records = permissionDtos,
            total = total,
            current = query.current,
            size = query.size
        )
        
        return ApiResponse.success(pageResult)
    }
    
    fun getPermissionById(id: Long): ApiResponse<PermissionDto> {
        val permission = permissionRepository.findById(id) ?: return ApiResponse.error("权限不存在")
        
        val permissionDto = PermissionDto(
            id = permission.id ?: 0,
            permissionName = permission.name ?: "",
            permissionCode = permission.code ?: "",
            permissionType = permission.type,
            parentId = permission.parentId,
            path = permission.path,
            component = "",
            icon = permission.icon,
            sortOrder = permission.sort,
            status = permission.status,
            createTime = DateTimeUtil.formatDateTime(permission.createTime),
            updateTime = DateTimeUtil.formatDateTime(permission.updateTime)
        )
        
        return ApiResponse.success(permissionDto)
    }
    
    fun getPermissionTree(): ApiResponse<List<PermissionDto>> {
        val allPermissions = permissionRepository.listAll()
        val rootPermissions = allPermissions.filter { it.parentId == 0L || it.parentId == null }
        
        val permissionTree = rootPermissions.map { permission ->
            buildPermissionWithChildren(permission, allPermissions)
        }.sortedBy { it.sortOrder }
        
        return ApiResponse.success(permissionTree)
    }
    
    @Transactional
    fun createPermission(permissionForm: PermissionFormDto): ApiResponse<Unit> {
        // 检查权限编码是否已存在
        if (permissionRepository.existsByPermissionCode(permissionForm.permissionCode)) {
            return ApiResponse.error("权限编码已存在")
        }
        
        val permission = Permission(
            name = permissionForm.permissionName,
            code = permissionForm.permissionCode,
            type = permissionForm.permissionType,
            parentId = permissionForm.parentId,
            path = permissionForm.path,
            icon = permissionForm.icon,
            sort = permissionForm.sortOrder,
            status = permissionForm.status
        )
        
        permissionRepository.persist(permission)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun updatePermission(id: Long, permissionForm: PermissionFormDto): ApiResponse<Unit> {
        val permission = permissionRepository.findById(id) ?: return ApiResponse.error("权限不存在")
        
        // 检查权限编码是否已被其他权限使用
        val existingPermission = permissionRepository.findByPermissionCode(permissionForm.permissionCode)
        if (existingPermission != null && existingPermission.id != id) {
            return ApiResponse.error("权限编码已存在")
        }
        
        permission.name = permissionForm.permissionName
        permission.code = permissionForm.permissionCode
        permission.type = permissionForm.permissionType
        permission.parentId = permissionForm.parentId
        permission.path = permissionForm.path
        permission.icon = permissionForm.icon
        permission.sort = permissionForm.sortOrder
        permission.status = permissionForm.status
        permission.updateTime = Date()
        
        permissionRepository.persist(permission)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun deletePermission(id: Long): ApiResponse<Unit> {
        val permission = permissionRepository.findById(id) ?: return ApiResponse.error("权限不存在")
        
        // 检查是否有子权限
        val childPermissions = permissionRepository.findByParentId(id)
        if (childPermissions.isNotEmpty()) {
            return ApiResponse.error("存在子权限，无法删除")
        }
        
        // 删除权限
        permissionRepository.deleteById(id)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun updatePermissionStatus(id: Long, status: String): ApiResponse<Unit> {
        val permission = permissionRepository.findById(id) ?: return ApiResponse.error("权限不存在")
        
        permission.status = status
        permission.updateTime = Date()
        permissionRepository.persist(permission)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun batchDeletePermissions(ids: List<Long>): ApiResponse<Unit> {
        ids.forEach { id ->
            deletePermission(id)
        }
        return ApiResponse.success()
    }
    
    private fun buildPermissionWithChildren(permission: Permission, allPermissions: List<Permission>): PermissionDto {
        val children = allPermissions.filter { it.parentId == permission.id }
        
        val permissionDto = PermissionDto(
            id = permission.id ?: 0,
            permissionName = permission.name ?: "",
            permissionCode = permission.code ?: "",
            permissionType = permission.type,
            parentId = permission.parentId,
            path = permission.path,
            component = "",
            icon = permission.icon,
            sortOrder = permission.sort,
            status = permission.status,
            createTime = DateTimeUtil.formatDateTime(permission.createTime),
            updateTime = DateTimeUtil.formatDateTime(permission.updateTime)
        )
        
        if (children.isNotEmpty()) {
            permissionDto.children = children.map { buildPermissionWithChildren(it, allPermissions) }
                .sortedBy { it.sortOrder }
        }
        
        return permissionDto
    }
}
