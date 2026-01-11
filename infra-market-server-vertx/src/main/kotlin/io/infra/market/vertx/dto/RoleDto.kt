package io.infra.market.vertx.dto

import io.infra.market.vertx.entity.Role
import io.infra.market.vertx.util.TimeUtil

/**
 * 角色信息DTO
 * 
 * 用于传输角色信息的DTO。
 * 包含角色的完整信息和权限列表。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class RoleDto(
    /**
     * 角色ID
     * 角色的唯一标识
     */
    val id: Long,
    
    /**
     * 角色名称
     * 角色的显示名称
     */
    val name: String,
    
    /**
     * 角色编码
     * 角色的唯一标识码
     */
    val code: String,
    
    /**
     * 角色描述
     * 对角色功能的详细说明，可以为null
     */
    val description: String?,
    
    /**
     * 角色状态
     * 角色的状态，如active、inactive等
     */
    val status: String,
    
    /**
     * 权限ID列表
     * 角色拥有的权限ID列表，默认为空列表
     */
    val permissionIds: List<Long> = emptyList(),
    
    /**
     * 创建时间
     * 角色创建的时间，格式化的字符串
     */
    val createTime: String,
    
    /**
     * 更新时间
     * 角色信息最后更新的时间，格式化的字符串
     */
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

