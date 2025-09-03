package io.infra.qk.service

import io.infra.qk.dto.ApiResponse
import io.infra.qk.dto.RoleFormDto
import io.infra.qk.dto.RoleQueryDto
import io.infra.qk.dto.PageResultDto
import io.infra.qk.dto.RoleDto
import io.infra.qk.entity.Role
import io.infra.qk.entity.RolePermission
import io.infra.qk.enums.StatusEnum
import io.infra.qk.repository.RoleRepository
import io.infra.qk.repository.RolePermissionRepository
import io.infra.qk.util.DateTimeUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.*

/**
 * 角色服务
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class RoleService @Inject constructor(
    private val roleRepository: RoleRepository,
    private val rolePermissionRepository: RolePermissionRepository
) {
    
    fun getRoleList(query: RoleQueryDto): ApiResponse<PageResultDto<RoleDto>> {
        val (roles, total) = roleRepository.findByCondition(
            query.roleName, query.roleCode, query.status, query.current, query.size
        )
        
        val roleDtos = roles.map { role ->
            RoleDto(
                id = role.id ?: 0,
                roleName = role.name ?: "",
                roleCode = role.code ?: "",
                description = role.description,
                status = role.status,
                createTime = DateTimeUtil.formatDateTime(role.createTime),
                updateTime = DateTimeUtil.formatDateTime(role.updateTime)
            )
        }
        
        val pageResult = PageResultDto(
            records = roleDtos,
            total = total,
            current = query.current,
            size = query.size
        )
        
        return ApiResponse.success(pageResult)
    }
    
    fun getAllRoles(): ApiResponse<List<RoleDto>> {
        val roles = roleRepository.findAllActive()
        
        val roleDtos = roles.map { role ->
            RoleDto(
                id = role.id ?: 0,
                roleName = role.name ?: "",
                roleCode = role.code ?: "",
                description = role.description,
                status = role.status,
                createTime = DateTimeUtil.formatDateTime(role.createTime),
                updateTime = DateTimeUtil.formatDateTime(role.updateTime)
            )
        }
        
        return ApiResponse.success(roleDtos)
    }
    
    fun getRoleById(id: Long): ApiResponse<RoleDto> {
        val role = roleRepository.findById(id) ?: return ApiResponse.error("角色不存在")
        
        val roleDto = RoleDto(
            id = role.id ?: 0,
            roleName = role.name ?: "",
            roleCode = role.code ?: "",
            description = role.description,
            status = role.status,
            createTime = DateTimeUtil.formatDateTime(role.createTime),
            updateTime = DateTimeUtil.formatDateTime(role.updateTime)
        )
        
        return ApiResponse.success(roleDto)
    }
    
    @Transactional
    fun createRole(roleForm: RoleFormDto): ApiResponse<Unit> {
        // 检查角色编码是否已存在
        if (roleRepository.existsByRoleCode(roleForm.roleCode)) {
            return ApiResponse.error("角色编码已存在")
        }
        
        val role = Role(
            name = roleForm.roleName,
            code = roleForm.roleCode,
            description = roleForm.description,
            status = roleForm.status
        )
        
        roleRepository.persist(role)
        
        // 保存角色权限关联
        saveRolePermissions(role.id ?: 0, roleForm.permissionIds)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun updateRole(id: Long, roleForm: RoleFormDto): ApiResponse<Unit> {
        val role = roleRepository.findById(id) ?: return ApiResponse.error("角色不存在")
        
        // 检查角色编码是否已被其他角色使用
        val existingRole = roleRepository.findByRoleCode(roleForm.roleCode)
        if (existingRole != null && existingRole.id != id) {
            return ApiResponse.error("角色编码已存在")
        }
        
        role.name = roleForm.roleName
        role.code = roleForm.roleCode
        role.description = roleForm.description
        role.status = roleForm.status
        role.updateTime = Date()
        
        roleRepository.persist(role)
        
        // 更新角色权限关联
        rolePermissionRepository.deleteByRoleId(id)
        saveRolePermissions(id, roleForm.permissionIds)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun deleteRole(id: Long): ApiResponse<Unit> {
        val role = roleRepository.findById(id) ?: return ApiResponse.error("角色不存在")
        
        // 删除角色权限关联
        rolePermissionRepository.deleteByRoleId(id)
        
        // 删除角色
        roleRepository.deleteById(id)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun updateRoleStatus(id: Long, status: String): ApiResponse<Unit> {
        val role = roleRepository.findById(id) ?: return ApiResponse.error("角色不存在")
        
        role.status = status
        role.updateTime = Date()
        roleRepository.persist(role)
        
        return ApiResponse.success()
    }
    
    @Transactional
    fun batchDeleteRoles(ids: List<Long>): ApiResponse<Unit> {
        ids.forEach { id ->
            deleteRole(id)
        }
        return ApiResponse.success()
    }
    
    private fun saveRolePermissions(roleId: Long, permissionIds: List<Long>) {
        permissionIds.forEach { permissionId ->
            val rolePermission = RolePermission(roleId = roleId, permissionId = permissionId)
            rolePermissionRepository.persist(rolePermission)
        }
    }
}
