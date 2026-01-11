package io.infra.market.vertx.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 权限创建/更新表单DTO
 * 
 * 用于创建或更新权限的表单数据。
 * 包含权限的基本信息，支持树形结构的权限管理。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class PermissionFormDto(
    /**
     * 权限名称
     * 权限的显示名称，长度必须在2-50个字符之间
     */
    @field:NotBlank(message = "权限名称不能为空")
    @field:Size(min = 2, max = 50, message = "权限名称长度必须在2-50个字符之间")
    val name: String,
    
    /**
     * 权限编码
     * 权限的唯一标识码，只能包含字母、数字和冒号，长度2-100个字符
     */
    @field:NotBlank(message = "权限编码不能为空")
    @field:Size(min = 2, max = 100, message = "权限编码长度必须在2-100个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9:]+$", message = "权限编码只能包含字母、数字和冒号")
    val code: String,
    
    /**
     * 权限类型
     * 权限的类型，只能是menu（菜单）、button（按钮）或api（接口）
     */
    @field:NotBlank(message = "权限类型不能为空")
    @field:Pattern(regexp = "^(menu|button|api)$", message = "权限类型只能是menu、button或api")
    val type: String,
    
    /**
     * 父权限ID
     * 父权限的ID，用于构建权限树，顶级权限可以为null
     */
    val parentId: Long? = null,
    
    /**
     * 权限路径
     * 权限对应的路径，菜单权限为前端路由，接口权限为API路径，长度不能超过200个字符
     */
    @field:Size(max = 200, message = "路径长度不能超过200个字符")
    val path: String? = null,
    
    /**
     * 权限图标
     * 权限的图标，用于前端菜单显示，长度不能超过100个字符
     */
    @field:Size(max = 100, message = "图标长度不能超过100个字符")
    val icon: String? = null,
    
    /**
     * 排序值
     * 权限的排序值，用于控制显示顺序，不能小于0
     */
    @field:NotNull(message = "排序值不能为空")
    @field:Min(value = 0, message = "排序值不能小于0")
    val sort: Int = 0
)
