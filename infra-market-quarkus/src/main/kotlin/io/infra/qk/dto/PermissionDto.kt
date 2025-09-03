package io.infra.qk.dto

/**
 * 权限 DTO
 */
data class PermissionDto(
    val id: Long,
    val permissionName: String,
    val permissionCode: String,
    val permissionType: String,
    val parentId: Long?,
    val path: String?,
    val component: String?,
    val icon: String?,
    val sortOrder: Int,
    val status: String,
    val createTime: String,
    val updateTime: String,
    var children: List<PermissionDto> = emptyList()
)

/**
 * 权限创建/更新 DTO
 */
data class PermissionFormDto(
    val permissionName: String,
    val permissionCode: String,
    val permissionType: String,
    val parentId: Long,
    val path: String?,
    val component: String?,
    val icon: String?,
    val sortOrder: Int,
    val status: String
)

/**
 * 权限查询 DTO
 */
data class PermissionQueryDto(
    val permissionName: String? = null,
    val permissionCode: String? = null,
    val permissionType: String? = null,
    val parentId: Long? = null,
    val status: String? = null,
    val current: Int = 1,
    val size: Int = 10
)
