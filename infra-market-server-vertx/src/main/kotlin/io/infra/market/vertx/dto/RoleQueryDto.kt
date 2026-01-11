package io.infra.market.vertx.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 角色查询参数DTO
 */
data class RoleQueryDto(
    @field:Size(max = 50, message = "角色名称长度不能超过50个字符")
    val name: String? = null,
    
    @field:Size(max = 100, message = "角色编码长度不能超过100个字符")
    val code: String? = null,
    
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    @field:Min(value = 1, message = "页码不能小于1")
    val page: Int = 1,
    
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    val size: Int = 10
)

