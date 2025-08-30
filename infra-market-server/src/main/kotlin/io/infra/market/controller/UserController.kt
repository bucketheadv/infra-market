package io.infra.market.controller

import io.infra.market.dto.BatchRequest
import io.infra.market.dto.UserFormDto
import io.infra.market.dto.UserQueryDto
import io.infra.market.service.UserService
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
    
    @GetMapping
    fun getUsers(query: UserQueryDto) = userService.getUsers(query)
    
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) = userService.getUser(id)
    
    @PostMapping
    fun createUser(@RequestBody form: UserFormDto) = userService.createUser(form)
    
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody form: UserFormDto) = userService.updateUser(id, form)
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
    
    @PatchMapping("/{id}/status")
    fun updateUserStatus(@PathVariable id: Long, @RequestParam status: String) = userService.updateUserStatus(id, status)
    
    @PostMapping("/{id}/reset-password")
    fun resetPassword(@PathVariable id: Long) = userService.resetPassword(id)
    
    @DeleteMapping("/batch")
    fun batchDeleteUsers(@RequestBody request: BatchRequest) = 
        request.ids.forEach { userService.deleteUser(it) }
}