package io.infra.market.controller

import io.infra.market.annotation.RequiresPermission
import io.infra.market.dto.BatchRequest
import io.infra.market.dto.PermissionFormDto
import io.infra.market.dto.PermissionQueryDto
import io.infra.market.dto.StatusUpdateDto
import io.infra.market.service.PermissionService
import jakarta.validation.Valid
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
    
    @RequiresPermission("permission:list", "权限列表查看")
    @GetMapping
    fun getPermissions(@Valid query: PermissionQueryDto) = permissionService.getPermissions(query)
    
    @RequiresPermission("permission:list", "权限树查看")
    @GetMapping("/tree")
    fun getPermissionTree() = permissionService.getPermissionTree()
    
    @RequiresPermission("permission:list", "权限详情查看")
    @GetMapping("/{id}")
    fun getPermission(@PathVariable id: Long) = permissionService.getPermission(id)
    
    @RequiresPermission("permission:create", "权限创建")
    @PostMapping
    fun createPermission(@Valid @RequestBody form: PermissionFormDto) = permissionService.createPermission(form)
    
    @RequiresPermission("permission:update", "权限编辑")
    @PutMapping("/{id}")
    fun updatePermission(@PathVariable id: Long, @Valid @RequestBody form: PermissionFormDto) = permissionService.updatePermission(id, form)
    
    @RequiresPermission("permission:delete", "权限删除")
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Long) = permissionService.deletePermission(id)
    
    @RequiresPermission("permission:status", "权限状态管理")
    @PatchMapping("/{id}/status")
    fun updatePermissionStatus(@PathVariable id: Long, @Valid @RequestBody request: StatusUpdateDto) = 
        permissionService.updatePermissionStatus(id, request.status)
    
    @RequiresPermission("permission:delete", "权限批量删除")
    @DeleteMapping("/batch")
    fun batchDeletePermissions(@Valid @RequestBody request: BatchRequest) = 
        permissionService.batchDeletePermissions(request.ids)
}
