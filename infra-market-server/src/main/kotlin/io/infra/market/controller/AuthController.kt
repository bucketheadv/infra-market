package io.infra.market.controller

import io.infra.market.dto.LoginRequest
import io.infra.market.service.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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
    
    @GetMapping("/current-user")
    fun getCurrentUser(@RequestHeader("Authorization") token: String) = 
        authService.getCurrentUser(token.replace("Bearer ", ""))
    
    @PostMapping("/logout")
    fun logout() = authService.logout()
}
