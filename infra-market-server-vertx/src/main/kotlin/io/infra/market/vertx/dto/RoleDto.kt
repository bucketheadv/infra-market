package io.infra.market.vertx.dto

import io.infra.market.vertx.entity.Role
import io.infra.market.vertx.util.TimeUtil

/**
 * 角色信息DTO
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
) {
    companion object {
        fun fromEntity(role: Role, permissionIds: List<Long> = emptyList()): RoleDto {
            return RoleDto(
                id = role.id ?: 0,
                name = role.name ?: "",
                code = role.code ?: "",
                description = role.description,
                status = role.status,
                permissionIds = permissionIds,
                createTime = TimeUtil.format(role.createTime),
                updateTime = TimeUtil.format(role.updateTime)
            )
        }
        
        fun fromEntityList(
            roles: List<Role>,
            rolePermissionMap: Map<Long, List<Long>> = emptyMap()
        ): List<RoleDto> {
            return roles.map { role ->
                val permissionIds = rolePermissionMap[role.id] ?: emptyList()
                fromEntity(role, permissionIds)
            }
        }
    }
}

/**
 * 角色创建/更新表单DTO
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
    val page: Int = 1,
    val size: Int = 10
)

