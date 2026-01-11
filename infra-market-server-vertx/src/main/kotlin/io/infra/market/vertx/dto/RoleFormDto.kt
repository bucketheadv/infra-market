package io.infra.market.vertx.dto

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
    val description: String? = null,
    
    /**
     * 权限ID列表
     * 分配给角色的权限ID列表，不能为空
     */
    @field:NotEmpty(message = "权限ID列表不能为空")
    val permissionIds: List<Long> = emptyList()
)

