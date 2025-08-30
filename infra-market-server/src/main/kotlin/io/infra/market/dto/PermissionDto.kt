package io.infra.market.dto

/**
 * 权限创建/更新DTO
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
    val current: Int = 1,
    val size: Int = 10
)

/**
 * 权限DTO
 */
data class PermissionDto(
    val id: Long,
    val name: String,
    val code: String,
    val type: String,
    val parentId: Long?,
    val path: String?,
    val icon: String?,
    val sort: Int,
    val status: String,
    val createTime: String,
    val updateTime: String,
    val children: List<PermissionDto>? = null
)
