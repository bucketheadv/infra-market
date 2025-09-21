package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.PermissionDto
import io.infra.market.dto.PermissionFormDto
import io.infra.market.dto.PermissionQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.entity.Permission
import io.infra.market.util.DateTimeUtil
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val permissionDao: PermissionDao,
    private val rolePermissionDao: RolePermissionDao
) {
    
    fun getPermissions(query: PermissionQueryDto): ApiResponse<PageResultDto<PermissionDto>> {
        // 使用DAO的page方法进行分页查询
        val page = permissionDao.page(query)
        
        val permissionDtos = page.records.map { permission ->
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
        
        val result = PageResultDto(
            records = permissionDtos,
            total = page.totalRow,
            current = page.pageNumber,
            size = page.pageSize
        )
        
        return ApiResponse.success(result)
    }
    
    fun getPermissionTree(): ApiResponse<List<PermissionDto>> {
        val permissions = permissionDao.findByStatus(StatusEnum.ACTIVE.code)
        
        val permissionDtos = permissions.map { permission ->
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
        
        // 构建树形结构
        val tree = buildPermissionTree(permissionDtos)
        
        return ApiResponse.success(tree)
    }
    
    fun getPermission(id: Long): ApiResponse<PermissionDto> {
        val permission = permissionDao.getById(id) ?: return ApiResponse.error("权限不存在")
        
        val permissionDto = PermissionDto(
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
        
        return ApiResponse.success(permissionDto)
    }
    
    fun createPermission(form: PermissionFormDto): ApiResponse<PermissionDto> {
        // 检查权限编码是否已存在
        if (permissionDao.findByCode(form.code) != null) {
            return ApiResponse.error("权限编码已存在")
        }
        
        val permission = Permission(
            name = form.name,
            code = form.code,
            type = form.type,
            parentId = form.parentId,
            path = form.path,
            icon = form.icon,
            sort = form.sort,
            status = StatusEnum.ACTIVE.code
        )
        
        permissionDao.save(permission)
        
        val permissionDto = PermissionDto(
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
        
        return ApiResponse.success(permissionDto)
    }
    
    fun updatePermission(id: Long, form: PermissionFormDto): ApiResponse<PermissionDto> {
        val permission = permissionDao.getById(id) ?: return ApiResponse.error("权限不存在")
        
        // 检查权限编码是否已被其他权限使用
        val existingPermission = permissionDao.findByCode(form.code)
        if (existingPermission != null && existingPermission.id != permission.id) {
            return ApiResponse.error("权限编码已存在")
        }
        
        permission.name = form.name
        permission.type = form.type
        permission.parentId = form.parentId
        permission.path = form.path
        permission.icon = form.icon
        permission.sort = form.sort
        
        permissionDao.updateById(permission)
        
        val permissionDto = PermissionDto(
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
        
        return ApiResponse.success(permissionDto)
    }
    
    fun deletePermission(id: Long): ApiResponse<Unit> {
        val permission = permissionDao.getById(id) ?: return ApiResponse.error("权限不存在")
        
        // 检查是否为系统权限（假设权限编码为system的权限为系统权限）
        if (permission.code == "system") {
            return ApiResponse.error("不能删除系统权限")
        }
        
        // 检查权限当前状态
        if (permission.status == StatusEnum.DELETED.code) {
            return ApiResponse.error("权限已被删除")
        }
        
        // 检查是否有子权限
        val childPermissions = permissionDao.findByParentId(id)
        if (childPermissions.isNotEmpty()) {
            return ApiResponse.error("该权限下还有子权限，无法删除")
        }
        
        // 检查是否有角色正在使用此权限
        val roleCount = rolePermissionDao.countByPermissionId(id)
        if (roleCount > 0) {
            return ApiResponse.error("该权限下还有角色，无法删除")
        }
        
        // 软删除：将状态设置为已删除
        permission.status = StatusEnum.DELETED.code
        permissionDao.updateById(permission)
        
        return ApiResponse.success()
    }
    
    fun updatePermissionStatus(id: Long, status: String): ApiResponse<Unit> {
        val permission = permissionDao.getById(id) ?: return ApiResponse.error("权限不存在")
        
        // 验证状态值是否有效
        StatusEnum.fromCode(status) ?: return ApiResponse.error("无效的状态值")

        // 检查是否为系统权限
        if (permission.code == "system" && status == StatusEnum.DELETED.code) {
            return ApiResponse.error("不能删除系统权限")
        }
        
        // 检查状态转换是否合理
        if (permission.status == StatusEnum.DELETED.code && status != StatusEnum.DELETED.code) {
            return ApiResponse.error("已删除的权限不能重新启用")
        }
        
        // 如果是要删除权限，调用删除方法
        if (status == StatusEnum.DELETED.code) {
            return deletePermission(id)
        }
        
        permission.status = status
        permissionDao.updateById(permission)
        
        return ApiResponse.success()
    }
    
    private fun buildPermissionTree(permissions: List<PermissionDto>): List<PermissionDto> {
        val permissionMap = permissions.associateBy { it.id }
        val rootPermissions = mutableListOf<PermissionDto>()
        
        for (permission in permissions) {
            if (permission.parentId == null) {
                // 根节点，直接添加到结果列表
                rootPermissions.add(buildPermissionWithChildren(permission, permissionMap))
            }
        }
        
        return rootPermissions.sortedBy { it.sort }
    }
    
    private fun buildPermissionWithChildren(permission: PermissionDto, permissionMap: Map<Long, PermissionDto>): PermissionDto {
        val children = mutableListOf<PermissionDto>()
        
        // 查找所有以当前权限为父级的权限
        for (childPermission in permissionMap.values) {
            if (childPermission.parentId == permission.id) {
                children.add(buildPermissionWithChildren(childPermission, permissionMap))
            }
        }
        
        // 按sort字段排序子权限
        val sortedChildren = children.sortedBy { it.sort }
        
        // 返回带有子权限的新PermissionDto
        return permission.copy(children = sortedChildren)
    }
    
    fun batchDeletePermissions(ids: List<Long>): ApiResponse<Unit> {
        if (ids.isEmpty()) {
            return ApiResponse.error("请选择要删除的权限")
        }
        
        val permissions = permissionDao.findByIds(ids)
        if (permissions.size != ids.size) {
            return ApiResponse.error("部分权限不存在")
        }
        
        // 检查是否包含系统权限
        val systemPermission = permissions.find { it.code == "system" }
        if (systemPermission != null) {
            return ApiResponse.error("不能删除系统权限")
        }
        
        // 检查是否有子权限或角色关联
        for (permission in permissions) {
            val childPermissions = permissionDao.findByParentId(permission.id ?: 0)
            if (childPermissions.isNotEmpty()) {
                return ApiResponse.error("权限 ${permission.name} 下还有子权限，无法删除")
            }
            
            val roleCount = rolePermissionDao.countByPermissionId(permission.id ?: 0)
            if (roleCount > 0) {
                return ApiResponse.error("权限 ${permission.name} 下还有角色，无法删除")
            }
        }
        
        // 批量删除
        for (permission in permissions) {
            permission.status = StatusEnum.DELETED.code
            permissionDao.updateById(permission)
        }
        
        return ApiResponse.success()
    }
}
