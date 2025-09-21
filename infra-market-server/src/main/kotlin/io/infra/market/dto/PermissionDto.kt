package io.infra.market.dto

import io.infra.market.repository.entity.Permission
import io.infra.market.util.DateTimeUtil
import jakarta.validation.constraints.Max
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
    val parentId: Long?,
    
    /**
     * 权限路径
     * 权限对应的路径，菜单权限为前端路由，接口权限为API路径，长度不能超过200个字符
     */
    @field:Size(max = 200, message = "路径长度不能超过200个字符")
    val path: String?,
    
    /**
     * 权限图标
     * 权限的图标，用于前端菜单显示，长度不能超过100个字符
     */
    @field:Size(max = 100, message = "图标长度不能超过100个字符")
    val icon: String?,
    
    /**
     * 排序值
     * 权限的排序值，用于控制显示顺序，不能小于0
     */
    @field:NotNull(message = "排序值不能为空")
    @field:Min(value = 0, message = "排序值不能小于0")
    val sort: Int
)

/**
 * 权限查询DTO
 * 
 * 用于权限列表查询的请求参数。
 * 支持按权限名称、编码、类型和状态进行筛选，并包含分页参数。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class PermissionQueryDto(
    /**
     * 权限名称
     * 用于模糊查询权限名称，长度不能超过50个字符，可以为null
     */
    @field:Size(max = 50, message = "权限名称长度不能超过50个字符")
    val name: String? = null,
    
    /**
     * 权限编码
     * 用于模糊查询权限编码，长度不能超过100个字符，可以为null
     */
    @field:Size(max = 100, message = "权限编码长度不能超过100个字符")
    val code: String? = null,
    
    /**
     * 权限类型
     * 用于按类型筛选权限，只能是menu、button或api，可以为null
     */
    @field:Pattern(regexp = "^(menu|button|api)$", message = "权限类型只能是menu、button或api")
    val type: String? = null,
    
    /**
     * 权限状态
     * 用于按状态筛选权限，只能是active或inactive，可以为null
     */
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    /**
     * 当前页码
     * 分页查询的当前页码，从1开始，默认为1
     */
    @field:Min(value = 1, message = "当前页不能小于1")
    val current: Int = 1,
    
    /**
     * 每页大小
     * 每页显示的记录数，范围1-1000，默认为10
     */
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    val size: Int = 10
)

/**
 * 权限信息DTO
 * 
 * 用于传输权限信息的DTO。
 * 包含权限的完整信息，支持树形结构。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class PermissionDto(
    /**
     * 权限ID
     * 权限的唯一标识
     */
    val id: Long,
    
    /**
     * 权限名称
     * 权限的显示名称
     */
    val name: String,
    
    /**
     * 权限编码
     * 权限的唯一标识码
     */
    val code: String,
    
    /**
     * 权限类型
     * 权限的类型，如menu、button、api等
     */
    val type: String,
    
    /**
     * 父权限ID
     * 父权限的ID，用于构建权限树，顶级权限为null
     */
    val parentId: Long?,
    
    /**
     * 权限路径
     * 权限对应的路径，菜单权限为前端路由，接口权限为API路径
     */
    val path: String?,
    
    /**
     * 权限图标
     * 权限的图标，用于前端菜单显示
     */
    val icon: String?,
    
    /**
     * 排序值
     * 权限的排序值，用于控制显示顺序
     */
    val sort: Int,
    
    /**
     * 权限状态
     * 权限的状态，如active、inactive等
     */
    val status: String,
    
    /**
     * 创建时间
     * 权限创建的时间，格式化的字符串
     */
    val createTime: String,
    
    /**
     * 更新时间
     * 权限信息最后更新的时间，格式化的字符串
     */
    val updateTime: String,
    
    /**
     * 子权限列表
     * 当前权限的子权限列表，用于构建权限树，默认为null
     */
    val children: List<PermissionDto>? = null
) {
    companion object {
        /**
         * 从Permission实体转换为PermissionDto
         * 
         * @param permission 权限实体
         * @param children 子权限列表，默认为空列表
         * @return PermissionDto
         */
        fun fromEntity(permission: Permission, children: List<PermissionDto> = emptyList()): PermissionDto {
            return PermissionDto(
                id = permission.id ?: 0,
                name = permission.name ?: "",
                code = permission.code ?: "",
                type = permission.type,
                parentId = permission.parentId,
                path = permission.path,
                icon = permission.icon,
                sort = permission.sort,
                status = permission.status,
                children = children,
                createTime = DateTimeUtil.formatDateTime(permission.createTime),
                updateTime = DateTimeUtil.formatDateTime(permission.updateTime)
            )
        }
        
        /**
         * 批量从Permission实体列表转换为PermissionDto列表
         * 
         * @param permissions 权限实体列表
         * @return PermissionDto列表
         */
        fun fromEntityList(permissions: List<Permission>): List<PermissionDto> {
            return permissions.map { fromEntity(it) }
        }
        
        /**
         * 构建权限树形结构
         * 
         * @param permissions 权限实体列表
         * @return 树形结构的PermissionDto列表
         */
        fun buildTree(permissions: List<Permission>): List<PermissionDto> {
            val permissionDtos = fromEntityList(permissions)
            return buildPermissionTree(permissionDtos)
        }
        
        /**
         * 构建权限树形结构（内部方法）
         */
        private fun buildPermissionTree(permissions: List<PermissionDto>): List<PermissionDto> {
            val permissionMap = permissions.associateBy { it.id }
            val rootPermissions = mutableListOf<PermissionDto>()
            
            for (permission in permissions) {
                if (permission.parentId == null) {
                    // 根节点，直接添加到结果列表
                    rootPermissions.add(buildPermissionWithChildren(permission, permissionMap))
                }
            }
            
            return rootPermissions.sortedBy { it.sort }
        }
        
        /**
         * 递归构建带子权限的权限对象
         */
        private fun buildPermissionWithChildren(
            permission: PermissionDto,
            permissionMap: Map<Long, PermissionDto>
        ): PermissionDto {
            val children = mutableListOf<PermissionDto>()
            
            // 查找所有以当前权限为父级的权限
            for (childPermission in permissionMap.values) {
                if (childPermission.parentId == permission.id) {
                    children.add(buildPermissionWithChildren(childPermission, permissionMap))
                }
            }
            
            // 按sort字段排序子权限
            val sortedChildren = children.sortedBy { it.sort }
            
            // 返回带有子权限的新PermissionDto
            return permission.copy(children = sortedChildren)
        }
    }
}
