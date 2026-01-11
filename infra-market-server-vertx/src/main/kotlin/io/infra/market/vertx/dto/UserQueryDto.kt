package io.infra.market.vertx.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 用户查询DTO
 * 
 * 用于用户列表查询的请求参数。
 * 支持按用户名和状态进行筛选，并包含分页参数。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class UserQueryDto(
    /**
     * 用户名
     * 用于模糊查询用户名，长度不能超过50个字符，可以为null
     */
    @field:Size(max = 50, message = "用户名长度不能超过50个字符")
    val username: String? = null,
    
    /**
     * 用户状态
     * 用于按状态筛选用户，只能是active或inactive，可以为null
     */
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    /**
     * 页码
     * 分页查询的页码，从1开始，默认为1
     */
    @field:Min(value = 1, message = "页码不能小于1")
    val page: Int = 1,
    
    /**
     * 每页大小
     * 每页显示的记录数，范围1-1000，默认为10
     */
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    val size: Int = 10
)

