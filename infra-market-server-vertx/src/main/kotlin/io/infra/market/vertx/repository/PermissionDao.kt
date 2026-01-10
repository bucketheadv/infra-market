package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.Permission
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 权限数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class PermissionDao(private val pool: Pool) {
    
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
    
    suspend fun page(name: String?, code: String?, type: String?, status: String?, page: Int, size: Int): Pair<List<Permission>, Long> {
        val offset = (page - 1) * size
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()
        
        conditions.add("status != 'deleted'")
        
        if (!name.isNullOrBlank()) {
            conditions.add("name LIKE ?")
            params.add("%$name%")
        }
        
        if (!code.isNullOrBlank()) {
            conditions.add("code LIKE ?")
            params.add("%$code%")
        }
        
        if (!type.isNullOrBlank()) {
            conditions.add("type = ?")
            params.add(type)
        }
        
        if (!status.isNullOrBlank()) {
            conditions.add("status = ?")
            params.add(status)
        }
        
        val whereClause = conditions.joinToString(" AND ")
        
        val countQuery = "SELECT COUNT(*) as total FROM permission_info WHERE $whereClause"
        val dataQuery = "SELECT * FROM permission_info WHERE $whereClause ORDER BY id ASC LIMIT ? OFFSET ?"
        
        val countRows = pool.preparedQuery(countQuery)
            .execute(Tuple.from(params))
            .awaitForResult()
        val total = countRows.first().getLong("total")
        
        val dataRows = pool.preparedQuery(dataQuery)
            .execute(Tuple.from(params + size + offset))
            .awaitForResult()
        
        return Pair(dataRows.map { rowToPermission(it) }, total)
    }
    
    suspend fun save(permission: Permission): Long {
        val now = System.currentTimeMillis()
        permission.createTime = now
        permission.updateTime = now
        
        val rows = pool.preparedQuery(
            "INSERT INTO permission_info (name, code, type, parent_id, path, icon, sort, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )
            .execute(Tuple.of(
                permission.name, permission.code, permission.type, permission.parentId,
                permission.path, permission.icon, permission.sort, permission.status,
                permission.createTime, permission.updateTime
            ))
            .awaitForResult()
        return rows.iterator().next().getLong(0)
    }
    
    suspend fun updateById(permission: Permission) {
        permission.updateTime = System.currentTimeMillis()
        pool.preparedQuery(
            "UPDATE permission_info SET name = ?, code = ?, type = ?, parent_id = ?, path = ?, icon = ?, sort = ?, status = ?, update_time = ? WHERE id = ?"
        )
            .execute(Tuple.of(
                permission.name, permission.code, permission.type, permission.parentId,
                permission.path, permission.icon, permission.sort, permission.status,
                permission.updateTime, permission.id
            ))
            .awaitForResult()
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

