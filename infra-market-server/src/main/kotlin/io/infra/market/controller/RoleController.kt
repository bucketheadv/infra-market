package io.infra.market.controller

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.BatchRequest
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.RoleDto
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
    
    @GetMapping
    fun getRoles(query: RoleQueryDto) = roleService.getRoles(query)
    
    @GetMapping("/all")
    fun getAllRoles() = roleService.getAllRoles()
    
    @GetMapping("/{id}")
    fun getRole(@PathVariable id: Long) = roleService.getRole(id)
    
    @PostMapping
    fun createRole(@RequestBody form: RoleFormDto) = roleService.createRole(form)
    
    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Long, @RequestBody form: RoleFormDto) = roleService.updateRole(id, form)
    
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long) = roleService.deleteRole(id)
    
    @PatchMapping("/{id}/status")
    fun updateRoleStatus(@PathVariable id: Long, @RequestParam status: String) = roleService.updateRoleStatus(id, status)
    
    @DeleteMapping("/batch")
    fun batchDeleteRoles(@RequestBody request: BatchRequest) = 
        request.ids.forEach { roleService.deleteRole(it) }
}
