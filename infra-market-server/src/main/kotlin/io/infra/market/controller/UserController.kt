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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 用户管理控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    
    @RequiresPermission("user:list", "用户列表查看")
    @GetMapping
    fun getUsers(@Valid query: UserQueryDto) = userService.getUsers(query)
    
    @RequiresPermission("user:list", "用户详情查看")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) = userService.getUser(id)
    
    @RequiresPermission("user:create", "用户创建")
    @PostMapping
    fun createUser(@Valid @RequestBody form: UserFormDto) = userService.createUser(form)
    
    @RequiresPermission("user:update", "用户编辑")
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @Valid @RequestBody form: UserUpdateDto) = userService.updateUser(id, form)
    
    @RequiresPermission("user:delete", "用户删除")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
    
    @RequiresPermission("user:status", "用户状态管理")
    @PatchMapping("/{id}/status")
    fun updateUserStatus(@PathVariable id: Long, @Valid @RequestBody request: StatusUpdateDto) = 
        userService.updateUserStatus(id, request.status)
    
    @RequiresPermission("user:update", "用户密码重置")
    @PostMapping("/{id}/reset/password")
    fun resetPassword(@PathVariable id: Long) = userService.resetPassword(id)
    
    @RequiresPermission("user:delete", "用户批量删除")
    @DeleteMapping("/batch")
    fun batchDeleteUsers(@Valid @RequestBody request: BatchRequest) = 
        userService.batchDeleteUsers(request.ids)
}