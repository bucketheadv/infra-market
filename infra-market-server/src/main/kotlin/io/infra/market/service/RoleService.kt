package io.infra.market.service

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.RoleDto
import io.infra.market.dto.RoleFormDto
import io.infra.market.dto.RoleQueryDto
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.RoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.entity.Role
import io.infra.market.repository.entity.RolePermission
import io.infra.market.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleDao: RoleDao,
    private val rolePermissionDao: RolePermissionDao,
    private val userRoleDao: UserRoleDao
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
        
        // 检查是否为系统角色（假设角色编码为admin的角色为系统角色）
        if (role.code == "admin") {
            return ApiResponse.error("不能删除系统角色")
        }
        
        // 检查角色当前状态
        if (role.status == StatusEnum.DELETED.code) {
            return ApiResponse.error("角色已被删除")
        }
        
        // 检查是否有用户正在使用此角色
        val userCount = userRoleDao.countByRoleId(id)
        if (userCount > 0) {
            return ApiResponse.error("该角色下还有用户，无法删除")
        }
        
        // 软删除：将状态设置为已删除
        role.status = StatusEnum.DELETED.code
        roleDao.updateById(role)
        
        return ApiResponse.success()
    }
    
    fun updateRoleStatus(id: Long, status: String): ApiResponse<Unit> {
        val role = roleDao.getById(id) ?: return ApiResponse.error("角色不存在")
        
        // 验证状态值是否有效
        val statusEnum = StatusEnum.fromCode(status)
        if (statusEnum == null) {
            return ApiResponse.error("无效的状态值")
        }
        
        // 检查是否为系统角色
        if (role.code == "admin" && status == StatusEnum.DELETED.code) {
            return ApiResponse.error("不能删除系统角色")
        }
        
        // 检查状态转换是否合理
        if (role.status == StatusEnum.DELETED.code && status != StatusEnum.DELETED.code) {
            return ApiResponse.error("已删除的角色不能重新启用")
        }
        
        // 如果是要删除角色，调用删除方法
        if (status == StatusEnum.DELETED.code) {
            return deleteRole(id)
        }
        
        role.status = status
        roleDao.updateById(role)
        
        return ApiResponse.success()
    }
    
    fun batchDeleteRoles(ids: List<Long>): ApiResponse<Unit> {
        if (ids.isEmpty()) {
            return ApiResponse.error("请选择要删除的角色")
        }
        
        val roles = roleDao.findByIds(ids)
        if (roles.size != ids.size) {
            return ApiResponse.error("部分角色不存在")
        }
        
        // 检查是否包含系统角色
        val systemRole = roles.find { it.code == "admin" }
        if (systemRole != null) {
            return ApiResponse.error("不能删除系统角色")
        }
        
        // 检查是否有用户正在使用这些角色
        for (role in roles) {
            val userCount = userRoleDao.countByRoleId(role.id ?: 0)
            if (userCount > 0) {
                return ApiResponse.error("角色 ${role.name} 下还有用户，无法删除")
            }
        }
        
        // 批量删除
        for (role in roles) {
            role.status = StatusEnum.DELETED.code
            roleDao.updateById(role)
        }
        
        return ApiResponse.success()
    }
}
