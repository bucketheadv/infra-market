package io.infra.market.vertx.repository

import com.google.inject.Inject
import io.infra.market.vertx.entity.Role
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 角色数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class RoleDao @Inject constructor(pool: Pool) : BaseDao(pool) {
    
    suspend fun findById(id: Long): Role? {
        val rows = pool.preparedQuery("SELECT * FROM role_info WHERE id = ? AND status != 'deleted'")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToRole(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByCode(code: String): Role? {
        val rows = pool.preparedQuery("SELECT * FROM role_info WHERE code = ? AND status != 'deleted'")
            .execute(Tuple.of(code))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToRole(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByStatus(status: String): List<Role> {
        val rows = pool.preparedQuery("SELECT * FROM role_info WHERE status = ? ORDER BY id ASC")
            .execute(Tuple.of(status))
            .awaitForResult()
        return rows.map { rowToRole(it) }
    }
    
    suspend fun findByIds(ids: List<Long>): List<Role> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val placeholders = ids.joinToString(",") { "?" }
        val rows = pool.preparedQuery("SELECT * FROM role_info WHERE id IN ($placeholders) AND status != 'deleted' ORDER BY id ASC")
            .execute(Tuple.from(ids))
            .awaitForResult()
        return rows.map { rowToRole(it) }
    }
    
    suspend fun page(name: String?, code: String?, status: String?, page: Int, size: Int): PageWrapper<Role> {
        return page(
            tableName = "role_info",
            page = page,
            size = size,
            fixedConditions = listOf("status != 'deleted'"),
            dynamicConditions = listOf(
                "name LIKE ?" to name?.let { "%$it%" },
                "code LIKE ?" to code?.let { "%$it%" },
                "status = ?" to status
            ),
            orderBy = "id ASC",
            rowMapper = { rowToRole(it) }
        )
    }
    
    suspend fun save(role: Role): Long {
        setCreateTime(role)
        return execute(
            "INSERT INTO role_info (name, code, description, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
            role.name,
            role.code,
            role.description,
            role.status,
            role.createTime,
            role.updateTime
        )
    }
    
    suspend fun save(role: Role, connection: SqlConnection): Long {
        setCreateTime(role)
        return execute(
            connection,
            "INSERT INTO role_info (name, code, description, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
            role.name,
            role.code,
            role.description,
            role.status,
            role.createTime,
            role.updateTime
        )
    }
    
    suspend fun updateById(role: Role) {
        setUpdateTime(role)
        execute(
            "UPDATE role_info SET name = ?, code = ?, description = ?, status = ?, update_time = ? WHERE id = ?",
            role.name,
            role.code,
            role.description,
            role.status,
            role.updateTime,
            role.id
        )
    }
    
    suspend fun updateById(role: Role, connection: SqlConnection) {
        setUpdateTime(role)
        execute(
            connection,
            "UPDATE role_info SET name = ?, code = ?, description = ?, status = ?, update_time = ? WHERE id = ?",
            role.name,
            role.code,
            role.description,
            role.status,
            role.updateTime,
            role.id
        )
    }
    
    suspend fun count(): Long {
        val rows = pool.preparedQuery("SELECT COUNT(*) as total FROM role_info WHERE status != 'deleted'")
            .execute()
            .awaitForResult()
        return rows.first().getLong("total")
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
