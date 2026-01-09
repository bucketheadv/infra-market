package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.RolePermission
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 角色权限关联数据访问对象
 */
class RolePermissionDao(private val pool: Pool) {
    
    fun findByRoleIds(roleIds: List<Long>): Future<List<RolePermission>> {
        if (roleIds.isEmpty()) {
            return Future.succeededFuture(emptyList())
        }
        
        val placeholders = roleIds.joinToString(",") { "?" }
        return pool.preparedQuery("SELECT * FROM role_permission WHERE role_id IN ($placeholders)")
            .execute(Tuple.from(roleIds))
            .map { rows ->
                rows.map { rowToRolePermission(it) }
            }
    }
    
    fun findByRoleId(roleId: Long): Future<List<RolePermission>> {
        return pool.preparedQuery("SELECT * FROM role_permission WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .map { rows ->
                rows.map { rowToRolePermission(it) }
            }
    }
    
    fun findByPermissionId(permissionId: Long): Future<List<RolePermission>> {
        return pool.preparedQuery("SELECT * FROM role_permission WHERE permission_id = ?")
            .execute(Tuple.of(permissionId))
            .map { rows ->
                rows.map { rowToRolePermission(it) }
            }
    }
    
    fun countByPermissionId(permissionId: Long): Future<Long> {
        return pool.preparedQuery("SELECT COUNT(*) as total FROM role_permission WHERE permission_id = ?")
            .execute(Tuple.of(permissionId))
            .map { rows -> rows.first().getLong("total") }
    }
    
    fun save(rolePermission: RolePermission): Future<Long> {
        val now = System.currentTimeMillis()
        rolePermission.createTime = now
        rolePermission.updateTime = now
        
        return pool.preparedQuery(
            "INSERT INTO role_permission (role_id, permission_id, create_time, update_time) VALUES (?, ?, ?, ?)"
        )
            .execute(Tuple.of(rolePermission.roleId, rolePermission.permissionId, rolePermission.createTime, rolePermission.updateTime))
            .map { rows ->
                rows.iterator().next().getLong(0)
            }
    }
    
    fun deleteByRoleId(roleId: Long): Future<Void> {
        return pool.preparedQuery("DELETE FROM role_permission WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .map { null }
    }
    
    private fun rowToRolePermission(row: Row): RolePermission {
        return RolePermission(
            id = row.getLong("id"),
            roleId = row.getLong("role_id"),
            permissionId = row.getLong("permission_id"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}

