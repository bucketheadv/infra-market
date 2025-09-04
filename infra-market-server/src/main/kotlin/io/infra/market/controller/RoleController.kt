package io.infra.market.controller

import io.infra.market.annotation.RequiresPermission
import io.infra.market.dto.BatchRequest
import io.infra.market.dto.RoleFormDto
import io.infra.market.dto.RoleQueryDto
import io.infra.market.dto.StatusUpdateDto
import io.infra.market.service.RoleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 角色管理控制器
 * 
 * 处理角色管理相关的HTTP请求，包括角色的增删改查、状态管理、
 * 权限分配和批量操作等功能。所有操作都需要相应的权限验证。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) {
    
    /**
     * 获取角色列表
     * 
     * 根据查询条件获取角色列表，支持分页和筛选。
     * 
     * @param query 查询条件，包含角色名称、编码、状态和分页参数
     * @return 分页的角色列表
     */
    @RequiresPermission("role:list", "角色列表查看")
    @GetMapping
    fun getRoles(@Valid query: RoleQueryDto) = roleService.getRoles(query)
    
    /**
     * 获取所有角色
     * 
     * 获取系统中所有激活的角色，用于下拉选择等场景。
     * 
     * @return 所有激活的角色列表
     */
    @RequiresPermission("role:list", "角色列表查看")
    @GetMapping("/all")
    fun getAllRoles() = roleService.getAllRoles()
    
    /**
     * 获取角色详情
     * 
     * 根据角色ID获取角色的详细信息。
     * 
     * @param id 角色ID
     * @return 角色详细信息
     */
    @RequiresPermission("role:list", "角色详情查看")
    @GetMapping("/{id}")
    fun getRole(@PathVariable id: Long) = roleService.getRole(id)
    
    /**
     * 创建角色
     * 
     * 创建新角色并分配权限。
     * 
     * @param form 角色创建表单数据
     * @return 创建的角色信息
     */
    @RequiresPermission("role:create", "角色创建")
    @PostMapping
    fun createRole(@Valid @RequestBody form: RoleFormDto) = roleService.createRole(form)
    
    /**
     * 更新角色信息
     * 
     * 更新指定角色的基本信息和权限分配。
     * 
     * @param id 角色ID
     * @param form 角色更新表单数据
     * @return 更新后的角色信息
     */
    @RequiresPermission("role:update", "角色编辑")
    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Long, @Valid @RequestBody form: RoleFormDto) = roleService.updateRole(id, form)
    
    /**
     * 删除角色
     * 
     * 删除指定的角色。
     * 
     * @param id 角色ID
     * @return 删除操作结果
     */
    @RequiresPermission("role:delete", "角色删除")
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long) = roleService.deleteRole(id)
    
    /**
     * 更新角色状态
     * 
     * 更新指定角色的状态（激活/未激活）。
     * 
     * @param id 角色ID
     * @param request 状态更新请求，包含新的状态值
     * @return 状态更新操作结果
     */
    @RequiresPermission("role:status", "角色状态管理")
    @PatchMapping("/{id}/status")
    fun updateRoleStatus(@PathVariable id: Long, @Valid @RequestBody request: StatusUpdateDto) = 
        roleService.updateRoleStatus(id, request.status)
    
    /**
     * 批量删除角色
     * 
     * 批量删除多个角色。
     * 
     * @param request 批量删除请求，包含要删除的角色ID列表
     * @return 批量删除操作结果
     */
    @RequiresPermission("role:delete", "角色批量删除")
    @DeleteMapping("/batch")
    fun batchDeleteRoles(@Valid @RequestBody request: BatchRequest) = 
        roleService.batchDeleteRoles(request.ids)
}
