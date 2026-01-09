package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.Role
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

/**
 * 角色数据访问对象
 */
class RoleDao(private val pool: Pool) {
    
    fun findById(id: Long): Future<Role?> {
        return pool.preparedQuery("SELECT * FROM role_info WHERE id = ? AND status != 'deleted'")
            .execute(Tuple.of(id))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToRole(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findByCode(code: String): Future<Role?> {
        return pool.preparedQuery("SELECT * FROM role_info WHERE code = ? AND status != 'deleted'")
            .execute(Tuple.of(code))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToRole(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findByStatus(status: String): Future<List<Role>> {
        return pool.preparedQuery("SELECT * FROM role_info WHERE status = ? ORDER BY id ASC")
            .execute(Tuple.of(status))
            .map { rows ->
                rows.map { rowToRole(it) }
            }
    }
    
    fun findByIds(ids: List<Long>): Future<List<Role>> {
        if (ids.isEmpty()) {
            return Future.succeededFuture(emptyList())
        }
        val placeholders = ids.joinToString(",") { "?" }
        return pool.preparedQuery("SELECT * FROM role_info WHERE id IN ($placeholders) AND status != 'deleted' ORDER BY id ASC")
            .execute(Tuple.from(ids))
            .map { rows ->
                rows.map { rowToRole(it) }
            }
    }
    
    fun page(name: String?, code: String?, status: String?, page: Int, size: Int): Future<Pair<List<Role>, Long>> {
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
        
        if (!status.isNullOrBlank()) {
            conditions.add("status = ?")
            params.add(status)
        }
        
        val whereClause = conditions.joinToString(" AND ")
        
        val countQuery = "SELECT COUNT(*) as total FROM role_info WHERE $whereClause"
        val dataQuery = "SELECT * FROM role_info WHERE $whereClause ORDER BY id ASC LIMIT ? OFFSET ?"
        
        return pool.preparedQuery(countQuery)
            .execute(Tuple.from(params))
            .compose { countRows ->
                val total = countRows.first().getLong("total")
                pool.preparedQuery(dataQuery)
                    .execute(Tuple.from(params + size + offset))
                    .map { dataRows ->
                        Pair(dataRows.map { rowToRole(it) }, total)
                    }
            }
    }
    
    fun save(role: Role): Future<Long> {
        val now = System.currentTimeMillis()
        role.createTime = now
        role.updateTime = now
        
        return pool.preparedQuery(
            "INSERT INTO role_info (name, code, description, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)"
        )
            .execute(Tuple.of(role.name, role.code, role.description, role.status, role.createTime, role.updateTime))
            .map { rows ->
                rows.iterator().next().getLong(0)
            }
    }
    
    fun updateById(role: Role): Future<Void> {
        role.updateTime = System.currentTimeMillis()
        return pool.preparedQuery(
            "UPDATE role_info SET name = ?, code = ?, description = ?, status = ?, update_time = ? WHERE id = ?"
        )
            .execute(Tuple.of(role.name, role.code, role.description, role.status, role.updateTime, role.id))
            .map { null }
    }
    
    fun count(): Future<Long> {
        return pool.preparedQuery("SELECT COUNT(*) as total FROM role_info WHERE status != 'deleted'")
            .execute()
            .map { rows -> rows.first().getLong("total") }
    }
    
    private fun rowToRole(row: Row): Role {
        return Role(
            id = row.getLong("id"),
            name = row.getString("name"),
            code = row.getString("code"),
            description = row.getString("description"),
            status = row.getString("status") ?: "active",
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}

