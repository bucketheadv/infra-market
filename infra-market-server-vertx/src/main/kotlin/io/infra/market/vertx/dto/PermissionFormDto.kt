package io.infra.market.vertx.dto

/**
 * 权限创建/更新表单DTO
 */
data class PermissionFormDto(
    val name: String,
    val code: String,
    val type: String,
    val parentId: Long?,
    val path: String?,
    val icon: String?,
    val sort: Int
)

/**
 * 权限查询DTO
 */
data class PermissionQueryDto(
    val name: String? = null,
    val code: String? = null,
    val type: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val size: Int = 10
)

