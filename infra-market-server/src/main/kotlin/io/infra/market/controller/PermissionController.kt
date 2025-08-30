package io.infra.market.controller

import io.infra.market.dto.ApiResponse
import io.infra.market.dto.BatchRequest
import io.infra.market.dto.PageResultDto
import io.infra.market.dto.PermissionDto
import io.infra.market.dto.PermissionFormDto
import io.infra.market.dto.PermissionQueryDto
import io.infra.market.service.PermissionService
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
 * 权限管理控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@RestController
@RequestMapping("/permissions")
class PermissionController(
    private val permissionService: PermissionService
) {
    
    @GetMapping
    fun getPermissions(query: PermissionQueryDto) = permissionService.getPermissions(query)
    
    @GetMapping("/tree")
    fun getPermissionTree() = permissionService.getPermissionTree()
    
    @GetMapping("/{id}")
    fun getPermission(@PathVariable id: Long) = permissionService.getPermission(id)
    
    @PostMapping
    fun createPermission(@RequestBody form: PermissionFormDto) = permissionService.createPermission(form)
    
    @PutMapping("/{id}")
    fun updatePermission(@PathVariable id: Long, @RequestBody form: PermissionFormDto) = permissionService.updatePermission(id, form)
    
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Long) = permissionService.deletePermission(id)
    
    @PatchMapping("/{id}/status")
    fun updatePermissionStatus(@PathVariable id: Long, @RequestParam status: String) = permissionService.updatePermissionStatus(id, status)
    
    @DeleteMapping("/batch")
    fun batchDeletePermissions(@RequestBody request: BatchRequest) = 
        request.ids.forEach { permissionService.deletePermission(it) }
}
