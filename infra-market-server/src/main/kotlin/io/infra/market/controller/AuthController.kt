package io.infra.market.controller

import io.infra.market.dto.LoginRequest
import io.infra.market.dto.ChangePasswordRequest
import io.infra.market.service.AuthService
import io.infra.market.util.AuthThreadLocal
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 认证控制器
 * 
 * 处理用户认证相关的HTTP请求，包括登录、登出、获取当前用户信息、
 * 获取用户菜单、刷新令牌和修改密码等功能。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    
    /**
     * 用户登录
     * 
     * 验证用户凭据并返回访问令牌和用户信息。
     * 
     * @param request 登录请求参数，包含用户名和密码
     * @return 登录响应，包含访问令牌、用户信息和权限列表
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest) = authService.login(request)
    
    /**
     * 获取当前用户信息
     * 
     * 根据当前登录用户的ID获取用户详细信息。
     * 
     * @return 当前用户的详细信息
     */
    @GetMapping("/current/user")
    fun getCurrentUser() = authService.getCurrentUser(AuthThreadLocal.getCurrentUserId()!!)
    
    /**
     * 获取用户菜单
     * 
     * 根据当前用户的权限获取可访问的菜单列表。
     * 
     * @return 用户可访问的菜单树结构
     */
    @GetMapping("/user/menus")
    fun getUserMenus() = authService.getUserMenus(AuthThreadLocal.getCurrentUserId()!!)
    
    /**
     * 刷新访问令牌
     * 
     * 为当前用户生成新的访问令牌，延长会话时间。
     * 
     * @return 新的访问令牌
     */
    @PostMapping("/refresh/token")
    fun refreshToken() = authService.refreshToken(AuthThreadLocal.getCurrentUserId()!!)
    
    /**
     * 用户登出
     * 
     * 使当前用户的访问令牌失效，结束会话。
     * 
     * @return 登出操作结果
     */
    @PostMapping("/logout")
    fun logout() = authService.logout(AuthThreadLocal.getCurrentUserId()!!)
    
    /**
     * 修改密码
     * 
     * 验证原密码并更新为新密码。
     * 
     * @param request 修改密码请求参数，包含原密码和新密码
     * @return 修改密码操作结果
     */
    @PostMapping("/change/password")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest) = 
        authService.changePassword(AuthThreadLocal.getCurrentUserId()!!, request)
}
