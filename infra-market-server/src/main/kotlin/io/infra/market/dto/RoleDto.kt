package io.infra.market.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 角色创建/更新DTO
 */
data class RoleFormDto(
    @field:NotBlank(message = "角色名称不能为空")
    @field:Size(min = 2, max = 50, message = "角色名称长度必须在2-50个字符之间")
    val name: String,
    
    @field:NotBlank(message = "角色编码不能为空")
    @field:Size(min = 2, max = 100, message = "角色编码长度必须在2-100个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "角色编码只能包含字母、数字和下划线")
    val code: String,
    
    @field:Size(max = 200, message = "角色描述长度不能超过200个字符")
    val description: String?,
    
    @field:NotEmpty(message = "权限ID列表不能为空")
    val permissionIds: List<Long>
)

/**
 * 角色查询DTO
 */
data class RoleQueryDto(
    @field:Size(max = 50, message = "角色名称长度不能超过50个字符")
    val name: String? = null,
    
    @field:Size(max = 100, message = "角色编码长度不能超过100个字符")
    val code: String? = null,
    
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    @field:Min(value = 1, message = "当前页不能小于1")
    val current: Int = 1,
    
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
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
