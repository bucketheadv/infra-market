package io.infra.market.vertx.dto

/**
 * 角色查询参数DTO
 */
data class RoleQueryDto(
    val name: String? = null,
    val code: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val size: Int = 10
)

