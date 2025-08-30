package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.RoleDto
import io.infra.market.dto.RoleFormDto
import io.infra.market.dto.RoleQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.RoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.entity.Role
import io.infra.market.repository.entity.RolePermission
import io.infra.market.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleDao: RoleDao,
    private val rolePermissionDao: RolePermissionDao
) {
    
    fun getRoles(query: RoleQueryDto): ApiResponse<PageResultDto<RoleDto>> {
        // 使用DAO的page方法进行分页查询
        val page = roleDao.page(query)
        
        val roleDtos = page.records.map { role ->
            RoleDto(
                id = role.id ?: 0,
                name = role.name ?: "",
                code = role.code ?: "",
                description = role.description,
                status = role.status,
                createTime = DateTimeUtil.formatDateTime(role.createTime),
                updateTime = DateTimeUtil.formatDateTime(role.updateTime)
            )
        }
        
        val result = PageResultDto(
            records = roleDtos,
            total = page.totalRow,
            current = page.pageNumber.toInt(),
            size = page.pageSize.toInt()
        )
        
        return ApiResponse.success(result)
    }
    
    fun getAllRoles(): ApiResponse<List<RoleDto>> {
        val roles = roleDao.findByStatus(StatusEnum.ACTIVE.code)
        
        val roleDtos = roles.map { role ->
            RoleDto(
                id = role.id ?: 0,
                name = role.name ?: "",
                code = role.code ?: "",
                description = role.description,
                status = role.status,
                createTime = DateTimeUtil.formatDateTime(role.createTime),
                updateTime = DateTimeUtil.formatDateTime(role.updateTime)
            )
        }
        
        return ApiResponse.success(roleDtos)
    }
    
    fun getRole(id: Long): ApiResponse<RoleDto> {
        val role = roleDao.getById(id) ?: return ApiResponse.error("角色不存在")
        
        val roleDto = RoleDto(
            id = role.id ?: 0,
            name = role.name ?: "",
            code = role.code ?: "",
            description = role.description,
            status = role.status,
            createTime = DateTimeUtil.formatDateTime(role.createTime),
            updateTime = DateTimeUtil.formatDateTime(role.updateTime)
        )
        
        return ApiResponse.success(roleDto)
    }
    
    @Transactional
    fun createRole(form: RoleFormDto): ApiResponse<RoleDto> {
        // 检查角色编码是否已存在
        if (roleDao.findByCode(form.code) != null) {
            return ApiResponse.error("角色编码已存在")
        }
        
        val role = Role(
            name = form.name,
            code = form.code,
            description = form.description,
            status = StatusEnum.ACTIVE.code
        )
        
        roleDao.save(role)
        
        // 保存角色权限关联
        for (permissionId in form.permissionIds) {
            val rolePermission = RolePermission(
                roleId = role.id,
                permissionId = permissionId
            )
            rolePermissionDao.save(rolePermission)
        }
        
        val roleDto = RoleDto(
            id = role.id ?: 0,
            name = role.name ?: "",
            code = role.code ?: "",
            description = role.description,
            status = role.status,
            createTime = DateTimeUtil.formatDateTime(role.createTime),
            updateTime = DateTimeUtil.formatDateTime(role.updateTime)
        )
        
        return ApiResponse.success(roleDto)
    }
    
    @Transactional
    fun updateRole(id: Long, form: RoleFormDto): ApiResponse<RoleDto> {
        val role = roleDao.getById(id) ?: return ApiResponse.error("角色不存在")
        
        // 检查角色编码是否已被其他角色使用
        val existingRole = roleDao.findByCode(form.code)
        if (existingRole != null && existingRole.id != role.id) {
            return ApiResponse.error("角色编码已存在")
        }
        
        role.name = form.name
        role.description = form.description
        
        roleDao.updateById(role)
        
        // 更新角色权限关联
        rolePermissionDao.deleteByRoleId(id)
        for (permissionId in form.permissionIds) {
            val rolePermission = RolePermission(
                roleId = id,
                permissionId = permissionId
            )
            rolePermissionDao.save(rolePermission)
        }
        
        val roleDto = RoleDto(
            id = role.id ?: 0,
            name = role.name ?: "",
            code = role.code ?: "",
            description = role.description,
            status = role.status,
            createTime = DateTimeUtil.formatDateTime(role.createTime),
            updateTime = DateTimeUtil.formatDateTime(role.updateTime)
        )
        
        return ApiResponse.success(roleDto)
    }
    
    fun deleteRole(id: Long): ApiResponse<Unit> {
        val role = roleDao.getById(id) ?: return ApiResponse.error("角色不存在")
        
        // 软删除：将状态设置为已删除
        role.status = StatusEnum.DELETED.code
        roleDao.updateById(role)
        
        return ApiResponse.success()
    }
    
    fun updateRoleStatus(id: Long, status: String): ApiResponse<Unit> {
        val role = roleDao.getById(id) ?: return ApiResponse.error("角色不存在")
        
        role.status = status
        roleDao.updateById(role)
        
        return ApiResponse.success()
    }
}
