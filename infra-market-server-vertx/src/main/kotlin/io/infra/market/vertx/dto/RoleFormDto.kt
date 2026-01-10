package io.infra.market.vertx.dto

/**
 * 角色创建/更新表单DTO
 */
data class RoleFormDto(
    val name: String,
    val code: String,
    val description: String? = null,
    val permissionIds: List<Long> = emptyList()
)

