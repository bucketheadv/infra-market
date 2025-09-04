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
import org.springframework.web.bind.annotation.RestController

/**
 * 权限管理控制器
 * 
 * 处理权限管理相关的HTTP请求，包括权限的增删改查、状态管理、
 * 树形结构展示和批量操作等功能。所有操作都需要相应的权限验证。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/permissions")
class PermissionController(
    private val permissionService: PermissionService
) {
    
    /**
     * 获取权限列表
     * 
     * 根据查询条件获取权限列表，支持分页和筛选。
     * 
     * @param query 查询条件，包含权限名称、编码、类型、状态和分页参数
     * @return 分页的权限列表
     */
    @RequiresPermission("permission:list", "权限列表查看")
    @GetMapping
    fun getPermissions(@Valid query: PermissionQueryDto) = permissionService.getPermissions(query)
    
    /**
     * 获取权限树
     * 
     * 获取完整的权限树结构，用于权限管理和角色分配。
     * 
     * @return 权限树结构
     */
    @RequiresPermission("permission:list", "权限树查看")
    @GetMapping("/tree")
    fun getPermissionTree() = permissionService.getPermissionTree()
    
    /**
     * 获取权限详情
     * 
     * 根据权限ID获取权限的详细信息。
     * 
     * @param id 权限ID
     * @return 权限详细信息
     */
    @RequiresPermission("permission:list", "权限详情查看")
    @GetMapping("/{id}")
    fun getPermission(@PathVariable id: Long) = permissionService.getPermission(id)
    
    /**
     * 创建权限
     * 
     * 创建新权限，支持树形结构。
     * 
     * @param form 权限创建表单数据
     * @return 创建的权限信息
     */
    @RequiresPermission("permission:create", "权限创建")
    @PostMapping
    fun createPermission(@Valid @RequestBody form: PermissionFormDto) = permissionService.createPermission(form)
    
    /**
     * 更新权限信息
     * 
     * 更新指定权限的基本信息。
     * 
     * @param id 权限ID
     * @param form 权限更新表单数据
     * @return 更新后的权限信息
     */
    @RequiresPermission("permission:update", "权限编辑")
    @PutMapping("/{id}")
    fun updatePermission(@PathVariable id: Long, @Valid @RequestBody form: PermissionFormDto) = permissionService.updatePermission(id, form)
    
    /**
     * 删除权限
     * 
     * 删除指定的权限。
     * 
     * @param id 权限ID
     * @return 删除操作结果
     */
    @RequiresPermission("permission:delete", "权限删除")
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Long) = permissionService.deletePermission(id)
    
    /**
     * 更新权限状态
     * 
     * 更新指定权限的状态（激活/未激活）。
     * 
     * @param id 权限ID
     * @param request 状态更新请求，包含新的状态值
     * @return 状态更新操作结果
     */
    @RequiresPermission("permission:status", "权限状态管理")
    @PatchMapping("/{id}/status")
    fun updatePermissionStatus(@PathVariable id: Long, @Valid @RequestBody request: StatusUpdateDto) = 
        permissionService.updatePermissionStatus(id, request.status)
    
    /**
     * 批量删除权限
     * 
     * 批量删除多个权限。
     * 
     * @param request 批量删除请求，包含要删除的权限ID列表
     * @return 批量删除操作结果
     */
    @RequiresPermission("permission:delete", "权限批量删除")
    @DeleteMapping("/batch")
    fun batchDeletePermissions(@Valid @RequestBody request: BatchRequest) = 
        permissionService.batchDeletePermissions(request.ids)
}
