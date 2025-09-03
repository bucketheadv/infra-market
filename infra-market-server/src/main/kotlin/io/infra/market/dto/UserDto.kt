package io.infra.market.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 用户创建DTO
 */
data class UserFormDto(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    @field:Email(message = "邮箱格式不正确")
    val email: String?,
    
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String?,
    
    @field:Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    val password: String? = null,
    
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long>
)

/**
 * 用户更新DTO
 */
data class UserUpdateDto(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    @field:Email(message = "邮箱格式不正确")
    val email: String?,
    
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String?,
    
    // 更新时密码完全可选，不进行校验
    val password: String? = null,
    
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long>
)

/**
 * 用户查询DTO
 */
data class UserQueryDto(
    @field:Size(max = 50, message = "用户名长度不能超过50个字符")
    val username: String? = null,
    
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    @field:Min(value = 1, message = "当前页不能小于1")
    val current: Int = 1,
    
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    val size: Int = 10
)

/**
 * 分页结果DTO
 */
data class PageResultDto<T>(
    val records: List<T>,
    val total: Long,
    val current: Int,
    val size: Int
)
