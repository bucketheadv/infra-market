package io.infra.market.controller

import io.infra.market.annotation.RequiresPermission
import io.infra.market.dto.BatchRequest
import io.infra.market.dto.StatusUpdateDto
import io.infra.market.dto.UserFormDto
import io.infra.market.dto.UserQueryDto
import io.infra.market.dto.UserUpdateDto
import io.infra.market.service.UserService
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
 * 用户管理控制器
 * 
 * 处理用户管理相关的HTTP请求，包括用户的增删改查、状态管理、
 * 密码重置和批量操作等功能。所有操作都需要相应的权限验证。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    
    /**
     * 获取用户列表
     * 
     * 根据查询条件获取用户列表，支持分页和筛选。
     * 
     * @param query 查询条件，包含用户名、状态和分页参数
     * @return 分页的用户列表
     */
    @RequiresPermission("user:list", "用户列表查看")
    @GetMapping
    fun getUsers(@Valid query: UserQueryDto) = userService.getUsers(query)
    
    /**
     * 获取用户详情
     * 
     * 根据用户ID获取用户的详细信息。
     * 
     * @param id 用户ID
     * @return 用户详细信息
     */
    @RequiresPermission("user:list", "用户详情查看")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) = userService.getUser(id)
    
    /**
     * 创建用户
     * 
     * 创建新用户并分配角色。
     * 
     * @param form 用户创建表单数据
     * @return 创建的用户信息
     */
    @RequiresPermission("user:create", "用户创建")
    @PostMapping
    fun createUser(@Valid @RequestBody form: UserFormDto) = userService.createUser(form)
    
    /**
     * 更新用户信息
     * 
     * 更新指定用户的基本信息和角色分配。
     * 
     * @param id 用户ID
     * @param form 用户更新表单数据
     * @return 更新后的用户信息
     */
    @RequiresPermission("user:update", "用户编辑")
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @Valid @RequestBody form: UserUpdateDto) = userService.updateUser(id, form)
    
    /**
     * 删除用户
     * 
     * 删除指定的用户。
     * 
     * @param id 用户ID
     * @return 删除操作结果
     */
    @RequiresPermission("user:delete", "用户删除")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
    
    /**
     * 更新用户状态
     * 
     * 更新指定用户的状态（激活/未激活）。
     * 
     * @param id 用户ID
     * @param request 状态更新请求，包含新的状态值
     * @return 状态更新操作结果
     */
    @RequiresPermission("user:status", "用户状态管理")
    @PatchMapping("/{id}/status")
    fun updateUserStatus(@PathVariable id: Long, @Valid @RequestBody request: StatusUpdateDto) = 
        userService.updateUserStatus(id, request.status)
    
    /**
     * 重置用户密码
     * 
     * 为指定用户重置密码为默认密码。
     * 
     * @param id 用户ID
     * @return 密码重置操作结果
     */
    @RequiresPermission("user:update", "用户密码重置")
    @PostMapping("/{id}/reset/password")
    fun resetPassword(@PathVariable id: Long) = userService.resetPassword(id)
    
    /**
     * 批量删除用户
     * 
     * 批量删除多个用户。
     * 
     * @param request 批量删除请求，包含要删除的用户ID列表
     * @return 批量删除操作结果
     */
    @RequiresPermission("user:delete", "用户批量删除")
    @DeleteMapping("/batch")
    fun batchDeleteUsers(@Valid @RequestBody request: BatchRequest) = 
        userService.batchDeleteUsers(request.ids)
}