package io.infra.market.vertx.dto

/**
 * 权限创建/更新表单DTO
 */
data class PermissionFormDto(
    val name: String,
    val code: String,
    val type: String,
    val parentId: Long? = null,
    val path: String? = null,
    val icon: String? = null,
    val sort: Int = 0
)
