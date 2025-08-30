package io.infra.market.dto

/**
 * 角色创建/更新DTO
 */
data class RoleFormDto(
    val name: String,
    val code: String,
    val description: String?,
    val permissionIds: List<Long>
)

/**
 * 角色查询DTO
 */
data class RoleQueryDto(
    val name: String? = null,
    val code: String? = null,
    val status: String? = null,
    val current: Int = 1,
    val size: Int = 10
)

/**
 * 角色DTO
 */
data class RoleDto(
    val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val status: String,
    val permissionIds: List<Long> = emptyList(),
    val createTime: String,
    val updateTime: String
)
