package io.infra.qk.dto

/**
 * 登录请求 DTO
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * 登录响应 DTO
 */
data class LoginResponse(
    val token: String,
    val user: UserDto,
    val permissions: List<String>
)

/**
 * 用户 DTO
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

/**
 * 修改密码请求 DTO
 */
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
