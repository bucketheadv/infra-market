package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.RolePermission
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 角色权限关联数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class RolePermissionDao(pool: Pool) : BaseDao(pool) {
    
    suspend fun findByRoleIds(roleIds: List<Long>): List<RolePermission> {
        if (roleIds.isEmpty()) {
            return emptyList()
        }
        
        val placeholders = roleIds.joinToString(",") { "?" }
        val rows = pool.preparedQuery("SELECT * FROM role_permission WHERE role_id IN ($placeholders)")
            .execute(Tuple.from(roleIds))
            .awaitForResult()
        return rows.map { rowToRolePermission(it) }
    }
    
    suspend fun findByRoleId(roleId: Long): List<RolePermission> {
        val rows = pool.preparedQuery("SELECT * FROM role_permission WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .awaitForResult()
        return rows.map { rowToRolePermission(it) }
    }
    
    suspend fun findByPermissionId(permissionId: Long): List<RolePermission> {
        val rows = pool.preparedQuery("SELECT * FROM role_permission WHERE permission_id = ?")
            .execute(Tuple.of(permissionId))
            .awaitForResult()
        return rows.map { rowToRolePermission(it) }
    }
    
    suspend fun countByPermissionId(permissionId: Long): Long {
        val rows = pool.preparedQuery("SELECT COUNT(*) as total FROM role_permission WHERE permission_id = ?")
            .execute(Tuple.of(permissionId))
            .awaitForResult()
        return rows.first().getLong("total")
    }
    
    suspend fun save(rolePermission: RolePermission): Long {
        setCreateTime(rolePermission)
        return execute(
            "INSERT INTO role_permission (role_id, permission_id, create_time, update_time) VALUES (?, ?, ?, ?)",
            rolePermission.roleId,
            rolePermission.permissionId,
            rolePermission.createTime,
            rolePermission.updateTime
        )
    }
    
    suspend fun save(rolePermission: RolePermission, connection: SqlConnection): Long {
        setCreateTime(rolePermission)
        return execute(
            connection,
            "INSERT INTO role_permission (role_id, permission_id, create_time, update_time) VALUES (?, ?, ?, ?)",
            rolePermission.roleId,
            rolePermission.permissionId,
            rolePermission.createTime,
            rolePermission.updateTime
        )
    }
    
    suspend fun deleteByRoleId(roleId: Long) {
        execute("DELETE FROM role_permission WHERE role_id = ?", roleId)
    }
    
    suspend fun deleteByRoleId(roleId: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM role_permission WHERE role_id = ?", roleId)
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
