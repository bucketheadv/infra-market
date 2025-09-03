package io.infra.market.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 权限创建/更新DTO
 */
data class PermissionFormDto(
    @field:NotBlank(message = "权限名称不能为空")
    @field:Size(min = 2, max = 50, message = "权限名称长度必须在2-50个字符之间")
    val name: String,
    
    @field:NotBlank(message = "权限编码不能为空")
    @field:Size(min = 2, max = 100, message = "权限编码长度必须在2-100个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9:]+$", message = "权限编码只能包含字母、数字和冒号")
    val code: String,
    
    @field:NotBlank(message = "权限类型不能为空")
    @field:Pattern(regexp = "^(menu|button|api)$", message = "权限类型只能是menu、button或api")
    val type: String,
    
    val parentId: Long?,
    
    @field:Size(max = 200, message = "路径长度不能超过200个字符")
    val path: String?,
    
    @field:Size(max = 100, message = "图标长度不能超过100个字符")
    val icon: String?,
    
    @field:NotNull(message = "排序值不能为空")
    @field:Min(value = 0, message = "排序值不能小于0")
    val sort: Int
)

/**
 * 权限查询DTO
 */
data class PermissionQueryDto(
    @field:Size(max = 50, message = "权限名称长度不能超过50个字符")
    val name: String? = null,
    
    @field:Size(max = 100, message = "权限编码长度不能超过100个字符")
    val code: String? = null,
    
    @field:Pattern(regexp = "^(menu|button|api)$", message = "权限类型只能是menu、button或api")
    val type: String? = null,
    
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    @field:Min(value = 1, message = "当前页不能小于1")
    val current: Int = 1,
    
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
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
