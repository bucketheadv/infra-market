package io.infra.market.dto

import io.infra.market.repository.entity.Role
import io.infra.market.util.DateTimeUtil
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 角色创建/更新表单DTO
 * 
 * 用于创建或更新角色的表单数据。
 * 包含角色的基本信息和权限分配。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class RoleFormDto(
    /**
     * 角色名称
     * 角色的显示名称，长度必须在2-50个字符之间
     */
    @field:NotBlank(message = "角色名称不能为空")
    @field:Size(min = 2, max = 50, message = "角色名称长度必须在2-50个字符之间")
    val name: String,
    
    /**
     * 角色编码
     * 角色的唯一标识码，只能包含字母、数字和下划线，长度2-100个字符
     */
    @field:NotBlank(message = "角色编码不能为空")
    @field:Size(min = 2, max = 100, message = "角色编码长度必须在2-100个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "角色编码只能包含字母、数字和下划线")
    val code: String,
    
    /**
     * 角色描述
     * 对角色功能的详细说明，长度不能超过200个字符，可以为null
     */
    @field:Size(max = 200, message = "角色描述长度不能超过200个字符")
    val description: String?,
    
    /**
     * 权限ID列表
     * 分配给角色的权限ID列表，不能为空
     */
    @field:NotEmpty(message = "权限ID列表不能为空")
    val permissionIds: List<Long>
)

/**
 * 角色查询DTO
 * 
 * 用于角色列表查询的请求参数。
 * 支持按角色名称、编码和状态进行筛选，并包含分页参数。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class RoleQueryDto(
    /**
     * 角色名称
     * 用于模糊查询角色名称，长度不能超过50个字符，可以为null
     */
    @field:Size(max = 50, message = "角色名称长度不能超过50个字符")
    val name: String? = null,
    
    /**
     * 角色编码
     * 用于模糊查询角色编码，长度不能超过100个字符，可以为null
     */
    @field:Size(max = 100, message = "角色编码长度不能超过100个字符")
    val code: String? = null,
    
    /**
     * 角色状态
     * 用于按状态筛选角色，只能是active或inactive，可以为null
     */
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    /**
     * 当前页码
     * 分页查询的当前页码，从1开始，默认为1
     */
    @field:Min(value = 1, message = "当前页不能小于1")
    val current: Int = 1,
    
    /**
     * 每页大小
     * 每页显示的记录数，范围1-1000，默认为10
     */
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    val size: Int = 10
)

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
        /**
         * 从Role实体转换为RoleDto
         * 
         * @param role 角色实体
         * @param permissionIds 角色权限ID列表，默认为空列表
         * @return RoleDto
         */
        fun fromEntity(role: Role, permissionIds: List<Long> = emptyList()): RoleDto {
            return RoleDto(
                id = role.id ?: 0,
                name = role.name ?: "",
                code = role.code ?: "",
                description = role.description,
                status = role.status,
                permissionIds = permissionIds,
                createTime = DateTimeUtil.formatDateTime(role.createTime),
                updateTime = DateTimeUtil.formatDateTime(role.updateTime)
            )
        }
        
        /**
         * 批量从Role实体列表转换为RoleDto列表
         * 
         * @param roles 角色实体列表
         * @param rolePermissionMap 角色ID到权限ID列表的映射，默认为空映射
         * @return RoleDto列表
         */
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
