package io.infra.market.controller

import io.infra.market.dto.LoginRequest
import io.infra.market.service.AuthService
import io.infra.market.util.AuthThreadLocal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 认证控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) = authService.login(request)
    
    @GetMapping("/current/user")
    fun getCurrentUser() = authService.getCurrentUser(AuthThreadLocal.getCurrentUserId())
    
    @GetMapping("/user/menus")
    fun getUserMenus() = authService.getUserMenus(AuthThreadLocal.getCurrentUserId())
    
    @PostMapping("/refresh/token")
    fun refreshToken() = authService.refreshToken(AuthThreadLocal.getCurrentUserId())
    
    @PostMapping("/logout")
    fun logout() = authService.logout(AuthThreadLocal.getCurrentUserId())
}
