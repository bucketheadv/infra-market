package io.infra.market.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 登录请求DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    val username: String,
    
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    val password: String
)

/**
 * 登录响应DTO
 */
data class LoginResponse(
    val token: String,
    val user: UserDto,
    val permissions: List<String>
)

/**
 * 用户DTO
 */
data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val phone: String?,
    val status: String,
    val lastLoginTime: String?,
    val roleIds: List<Long> = emptyList(),
    val createTime: String,
    val updateTime: String
)
