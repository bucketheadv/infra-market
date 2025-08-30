package io.infra.market.controller

import io.infra.market.annotation.RequiresPermission
import io.infra.market.dto.BatchRequest
import io.infra.market.dto.RoleFormDto
import io.infra.market.dto.RoleQueryDto
import io.infra.market.service.RoleService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 角色管理控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) {
    
    @RequiresPermission("role:list", "角色列表查看")
    @GetMapping
    fun getRoles(query: RoleQueryDto) = roleService.getRoles(query)
    
    @RequiresPermission("role:list", "角色列表查看")
    @GetMapping("/all")
    fun getAllRoles() = roleService.getAllRoles()
    
    @RequiresPermission("role:list", "角色详情查看")
    @GetMapping("/{id}")
    fun getRole(@PathVariable id: Long) = roleService.getRole(id)
    
    @RequiresPermission("role:create", "角色创建")
    @PostMapping
    fun createRole(@RequestBody form: RoleFormDto) = roleService.createRole(form)
    
    @RequiresPermission("role:update", "角色编辑")
    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Long, @RequestBody form: RoleFormDto) = roleService.updateRole(id, form)
    
    @RequiresPermission("role:delete", "角色删除")
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long) = roleService.deleteRole(id)
    
    @RequiresPermission("role:status", "角色状态管理")
    @PatchMapping("/{id}/status")
    fun updateRoleStatus(@PathVariable id: Long, @RequestBody request: Map<String, String>) = 
        roleService.updateRoleStatus(id, request["status"] ?: "")
    
    @RequiresPermission("role:delete", "角色批量删除")
    @DeleteMapping("/batch")
    fun batchDeleteRoles(@RequestBody request: BatchRequest) = 
        roleService.batchDeleteRoles(request.ids)
}
