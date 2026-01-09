package io.infra.market.vertx.dto

import io.infra.market.vertx.entity.User
import io.infra.market.vertx.util.TimeUtil

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
 * 用户信息DTO
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
) {
    companion object {
        fun fromEntity(user: User, roleIds: List<Long> = emptyList()): UserDto {
            return UserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                phone = user.phone,
                status = user.status,
                lastLoginTime = TimeUtil.format(user.lastLoginTime),
                roleIds = roleIds,
                createTime = TimeUtil.format(user.createTime),
                updateTime = TimeUtil.format(user.updateTime)
            )
        }
        
        fun fromEntityList(
            users: List<User>,
            userRoleMap: Map<Long, List<Long>> = emptyMap()
        ): List<UserDto> {
            return users.map { user ->
                val roleIds = userRoleMap[user.id] ?: emptyList()
                fromEntity(user, roleIds)
            }
        }
    }
}

/**
 * 修改密码请求DTO
 */
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

