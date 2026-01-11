package io.infra.market.vertx.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 用户创建表单DTO
 */
data class UserFormDto(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    @field:Email(message = "邮箱格式不正确")
    val email: String? = null,
    
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String? = null,
    
    @field:Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    val password: String? = null,
    
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long> = emptyList()
)

/**
 * 用户更新表单DTO
 */
data class UserUpdateDto(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    @field:Email(message = "邮箱格式不正确")
    val email: String? = null,
    
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String? = null,
    
    val password: String? = null,
    
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long> = emptyList()
)

