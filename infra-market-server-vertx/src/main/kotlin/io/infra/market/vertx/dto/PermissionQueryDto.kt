package io.infra.market.vertx.dto

/**
 * 权限查询参数DTO
 */
data class PermissionQueryDto(
    val name: String? = null,
    val code: String? = null,
    val type: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val size: Int = 10
)

