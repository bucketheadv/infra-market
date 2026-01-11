package io.infra.market.vertx.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 角色创建/更新表单DTO
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
    val description: String? = null,
    
    @field:NotEmpty(message = "权限ID列表不能为空")
    val permissionIds: List<Long> = emptyList()
)

