package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.Permission
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 权限数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class PermissionDao(pool: Pool) : BaseDao(pool) {
    
    suspend fun findById(id: Long): Permission? {
        val rows = pool.preparedQuery("SELECT * FROM permission_info WHERE id = ? AND status != 'deleted'")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToPermission(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByCode(code: String): Permission? {
        val rows = pool.preparedQuery("SELECT * FROM permission_info WHERE code = ? AND status != 'deleted'")
            .execute(Tuple.of(code))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToPermission(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByStatus(status: String): List<Permission> {
        val rows = pool.preparedQuery("SELECT * FROM permission_info WHERE status = ? ORDER BY id ASC")
            .execute(Tuple.of(status))
            .awaitForResult()
        return rows.map { rowToPermission(it) }
    }
    
    suspend fun findByIds(ids: List<Long>): List<Permission> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        
        val placeholders = ids.joinToString(",") { "?" }
        val rows = pool.preparedQuery("SELECT * FROM permission_info WHERE id IN ($placeholders) ORDER BY id ASC")
            .execute(Tuple.from(ids))
            .awaitForResult()
        return rows.map { rowToPermission(it) }
    }
    
    suspend fun findByParentId(parentId: Long): List<Permission> {
        val rows = pool.preparedQuery("SELECT * FROM permission_info WHERE parent_id = ? AND status != 'deleted' ORDER BY id ASC")
            .execute(Tuple.of(parentId))
            .awaitForResult()
        return rows.map { rowToPermission(it) }
    }
    
    suspend fun page(name: String?, code: String?, type: String?, status: String?, page: Int, size: Int): PageWrapper<Permission> {
        return page(
            tableName = "permission_info",
            page = page,
            size = size,
            fixedConditions = listOf("status != 'deleted'"),
            dynamicConditions = listOf(
                "name LIKE ?" to name?.let { "%$it%" },
                "code LIKE ?" to code?.let { "%$it%" },
                "type = ?" to type,
                "status = ?" to status
            ),
            orderBy = "id ASC",
            rowMapper = { rowToPermission(it) }
        )
    }
    
    suspend fun save(permission: Permission): Long {
        setCreateTime(permission)
        return execute(
            "INSERT INTO permission_info (name, code, type, parent_id, path, icon, sort, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            permission.name,
            permission.code,
            permission.type,
            permission.parentId,
            permission.path,
            permission.icon,
            permission.sort,
            permission.status,
            permission.createTime,
            permission.updateTime
        )
    }
    
    suspend fun updateById(permission: Permission) {
        setUpdateTime(permission)
        execute(
            "UPDATE permission_info SET name = ?, code = ?, type = ?, parent_id = ?, path = ?, icon = ?, sort = ?, status = ?, update_time = ? WHERE id = ?",
            permission.name,
            permission.code,
            permission.type,
            permission.parentId,
            permission.path,
            permission.icon,
            permission.sort,
            permission.status,
            permission.updateTime,
            permission.id
        )
    }
    
    suspend fun updateById(permission: Permission, connection: SqlConnection) {
        setUpdateTime(permission)
        execute(
            connection,
            "UPDATE permission_info SET name = ?, code = ?, type = ?, parent_id = ?, path = ?, icon = ?, sort = ?, status = ?, update_time = ? WHERE id = ?",
            permission.name,
            permission.code,
            permission.type,
            permission.parentId,
            permission.path,
            permission.icon,
            permission.sort,
            permission.status,
            permission.updateTime,
            permission.id
        )
    }
    
    suspend fun count(): Long {
        val rows = pool.preparedQuery("SELECT COUNT(*) as total FROM permission_info WHERE status != 'deleted'")
            .execute()
            .awaitForResult()
        return rows.first().getLong("total")
    }
    
    private fun rowToPermission(row: Row): Permission {
        return Permission(
            id = row.getLong("id"),
            name = row.getString("name"),
            code = row.getString("code"),
            type = row.getString("type") ?: "menu",
            parentId = row.getLong("parent_id"),
            path = row.getString("path"),
            icon = row.getString("icon"),
            sort = row.getInteger("sort") ?: 0,
            status = row.getString("status") ?: "active",
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}

