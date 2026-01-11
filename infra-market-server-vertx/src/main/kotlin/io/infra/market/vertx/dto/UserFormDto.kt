package io.infra.market.vertx.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 用户创建表单DTO
 * 
 * 用于创建新用户的表单数据。
 * 包含用户的基本信息和角色分配。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class UserFormDto(
    /**
     * 用户名
     * 用户的登录用户名，只能包含字母、数字和下划线，长度2-20个字符
     */
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    /**
     * 邮箱地址
     * 用户的邮箱地址，必须符合邮箱格式，可以为null
     */
    @field:Email(message = "邮箱格式不正确")
    val email: String? = null,
    
    /**
     * 手机号码
     * 用户的手机号码，必须符合中国大陆手机号格式，可以为null
     */
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String? = null,
    
    /**
     * 密码
     * 用户登录密码，长度必须在6-20个字符之间，可以为null（系统自动生成）
     */
    @field:Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    val password: String? = null,
    
    /**
     * 角色ID列表
     * 分配给用户的角色ID列表，不能为空
     */
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long> = emptyList()
)

/**
 * 用户更新表单DTO
 * 
 * 用于更新现有用户信息的表单数据。
 * 包含用户的基本信息和角色分配，密码为可选字段。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class UserUpdateDto(
    /**
     * 用户名
     * 用户的登录用户名，只能包含字母、数字和下划线，长度2-20个字符
     */
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    /**
     * 邮箱地址
     * 用户的邮箱地址，必须符合邮箱格式，可以为null
     */
    @field:Email(message = "邮箱格式不正确")
    val email: String? = null,
    
    /**
     * 手机号码
     * 用户的手机号码，必须符合中国大陆手机号格式，可以为null
     */
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String? = null,
    
    /**
     * 密码
     * 用户登录密码，更新时完全可选，不进行校验
     * 如果提供则更新密码，如果不提供则保持原密码不变
     */
    val password: String? = null,
    
    /**
     * 角色ID列表
     * 分配给用户的角色ID列表，不能为空
     */
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long> = emptyList()
)

