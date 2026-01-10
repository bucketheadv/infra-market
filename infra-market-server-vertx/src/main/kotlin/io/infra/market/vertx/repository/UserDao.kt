package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.User
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 用户数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class UserDao(pool: Pool) : BaseDao(pool) {
    
    suspend fun findByUsername(username: String): User? {
        val rows = pool.preparedQuery("SELECT * FROM user_info WHERE username = ?")
            .execute(Tuple.of(username))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToUser(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByUid(uid: Long): User? {
        val rows = pool.preparedQuery("SELECT * FROM user_info WHERE id = ?")
            .execute(Tuple.of(uid))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToUser(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByEmail(email: String): User? {
        val rows = pool.preparedQuery("SELECT * FROM user_info WHERE email = ?")
            .execute(Tuple.of(email))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToUser(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByPhone(phone: String): User? {
        val rows = pool.preparedQuery("SELECT * FROM user_info WHERE phone = ?")
            .execute(Tuple.of(phone))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToUser(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findPage(username: String?, status: String?, page: Int, size: Int): PageWrapper<User> {
        return page(
            tableName = "user_info",
            page = page,
            size = size,
            dynamicConditions = listOf(
                "username LIKE ?" to username?.let { "%$it%" },
                "status = ?" to status
            ),
            orderBy = "create_time DESC",
            rowMapper = { rowToUser(it) }
        )
    }
    
    suspend fun save(user: User): Long {
        setCreateTime(user)
        return execute(
            "INSERT INTO user_info (username, password, email, phone, status, last_login_time, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            user.username,
            user.password,
            user.email,
            user.phone,
            user.status,
            user.lastLoginTime,
            user.createTime,
            user.updateTime
        )
    }
    
    suspend fun save(user: User, connection: SqlConnection): Long {
        setCreateTime(user)
        return execute(
            connection,
            "INSERT INTO user_info (username, password, email, phone, status, last_login_time, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            user.username,
            user.password,
            user.email,
            user.phone,
            user.status,
            user.lastLoginTime,
            user.createTime,
            user.updateTime
        )
    }
    
    suspend fun updateById(user: User) {
        setUpdateTime(user)
        execute(
            "UPDATE user_info SET username = ?, password = ?, email = ?, phone = ?, status = ?, last_login_time = ?, update_time = ? WHERE id = ?",
            user.username,
            user.password,
            user.email,
            user.phone,
            user.status,
            user.lastLoginTime,
            user.updateTime,
            user.id
        )
    }
    
    suspend fun updateById(user: User, connection: SqlConnection) {
        setUpdateTime(user)
        execute(
            connection,
            "UPDATE user_info SET username = ?, password = ?, email = ?, phone = ?, status = ?, last_login_time = ?, update_time = ? WHERE id = ?",
            user.username,
            user.password,
            user.email,
            user.phone,
            user.status,
            user.lastLoginTime,
            user.updateTime,
            user.id
        )
    }
    
    suspend fun deleteById(id: Long) {
        execute("DELETE FROM user_info WHERE id = ?", id)
    }
    
    suspend fun deleteById(id: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM user_info WHERE id = ?", id)
    }
    
    suspend fun findByIds(ids: List<Long>): List<User> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        
        val placeholders = ids.joinToString(",") { "?" }
        val rows = pool.preparedQuery("SELECT * FROM user_info WHERE id IN ($placeholders)")
            .execute(Tuple.from(ids))
            .awaitForResult()
        return rows.map { rowToUser(it) }
    }
    
    suspend fun countByStatus(status: String?): Long {
        val rows = if (status != null) {
            pool.preparedQuery("SELECT COUNT(*) as total FROM user_info WHERE status = ?")
                .execute(Tuple.of(status))
                .awaitForResult()
        } else {
            pool.preparedQuery("SELECT COUNT(*) as total FROM user_info")
                .execute()
                .awaitForResult()
        }
        return rows.first().getLong("total")
    }
    
    suspend fun findRecentUsers(limit: Int): List<User> {
        val rows = pool.preparedQuery(
            "SELECT * FROM user_info WHERE last_login_time IS NOT NULL " +
            "ORDER BY last_login_time DESC LIMIT ?"
        )
            .execute(Tuple.of(limit))
            .awaitForResult()
        return rows.map { rowToUser(it) }
    }
    
    private fun rowToUser(row: Row): User {
        return User(
            id = row.getLong("id"),
            username = row.getString("username"),
            password = row.getString("password"),
            email = row.getString("email"),
            phone = row.getString("phone"),
            status = row.getString("status") ?: "active",
            lastLoginTime = row.getLong("last_login_time"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}

