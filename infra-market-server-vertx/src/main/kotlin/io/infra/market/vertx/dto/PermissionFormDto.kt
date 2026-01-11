package io.infra.market.vertx.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 权限创建/更新表单DTO
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
    
    val parentId: Long? = null,
    
    @field:Size(max = 200, message = "路径长度不能超过200个字符")
    val path: String? = null,
    
    @field:Size(max = 100, message = "图标长度不能超过100个字符")
    val icon: String? = null,
    
    @field:NotNull(message = "排序值不能为空")
    @field:Min(value = 0, message = "排序值不能小于0")
    val sort: Int = 0
)
