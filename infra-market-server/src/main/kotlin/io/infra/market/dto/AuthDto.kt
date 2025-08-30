package io.infra.market.dto

/**
 * 登录请求DTO
 */
data class LoginRequest(
    val username: String,
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
