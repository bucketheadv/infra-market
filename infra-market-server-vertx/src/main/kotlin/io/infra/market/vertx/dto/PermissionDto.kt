package io.infra.market.vertx.dto

import io.infra.market.vertx.entity.Permission

/**
 * 权限信息DTO
 */
data class PermissionDto(
    val id: Long,
    val name: String,
    val code: String,
    val type: String,
    val parentId: Long?,
    val path: String?,
    val icon: String?,
    val sort: Int,
    val status: String,
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

