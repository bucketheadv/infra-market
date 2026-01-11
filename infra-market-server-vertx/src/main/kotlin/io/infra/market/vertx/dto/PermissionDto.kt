package io.infra.market.vertx.dto

import io.infra.market.vertx.entity.Permission

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
     * 子权限列表
     * 当前权限的子权限列表，用于构建权限树，默认为null
     */
    val children: List<PermissionDto>? = null
) {
    companion object {
        /**
         * 从实体构建DTO
         */
        fun fromEntity(permission: Permission): PermissionDto {
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
                children = null
            )
        }
        
        /**
         * 从实体列表构建DTO列表
         */
        fun fromEntityList(permissions: List<Permission>): List<PermissionDto> {
            return permissions.map { permission ->
                PermissionDto(
                    id = permission.id ?: 0,
                    name = permission.name ?: "",
                    code = permission.code ?: "",
                    type = permission.type,
                    parentId = permission.parentId,
                    path = permission.path,
                    icon = permission.icon,
                    sort = permission.sort,
                    status = permission.status,
                    children = null
                )
            }
        }
        
        /**
         * 构建权限树形结构
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

