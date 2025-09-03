package io.infra.qk.dto

/**
 * 角色 DTO
 */
data class RoleDto(
    val id: Long,
    val roleName: String,
    val roleCode: String,
    val description: String?,
    val status: String,
    val createTime: String,
    val updateTime: String
)

/**
 * 角色创建/更新 DTO
 */
data class RoleFormDto(
    val roleName: String,
    val roleCode: String,
    val description: String?,
    val status: String,
    val permissionIds: List<Long>
)

/**
 * 角色查询 DTO
 */
data class RoleQueryDto(
    val roleName: String? = null,
    val roleCode: String? = null,
    val status: String? = null,
    val current: Int = 1,
    val size: Int = 10
)
