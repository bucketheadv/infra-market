package io.infra.market.vertx.dto

/**
 * 用户创建表单DTO
 */
data class UserFormDto(
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val roleIds: List<Long> = emptyList()
)

/**
 * 用户更新表单DTO
 */
data class UserUpdateDto(
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val roleIds: List<Long> = emptyList()
)

