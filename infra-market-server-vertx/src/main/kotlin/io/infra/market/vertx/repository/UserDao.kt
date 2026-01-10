package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.User
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 用户数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class UserDao(private val pool: Pool) {
    
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
    
    suspend fun findPage(username: String?, status: String?, page: Int, size: Int): Pair<List<User>, Long> {
        val offset = (page - 1) * size
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()
        
        if (!username.isNullOrBlank()) {
            conditions.add("username LIKE ?")
            params.add("%$username%")
        }
        
        if (!status.isNullOrBlank()) {
            conditions.add("status = ?")
            params.add(status)
        }
        
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else {
            ""
        }
        
        val countQuery = "SELECT COUNT(*) as total FROM user_info $whereClause"
        val dataQuery = "SELECT * FROM user_info $whereClause ORDER BY create_time DESC LIMIT ? OFFSET ?"
        
        val countRows = pool.preparedQuery(countQuery)
            .execute(if (params.isNotEmpty()) Tuple.from(params) else Tuple.tuple())
            .awaitForResult()
        val total = countRows.first().getLong("total")
        
        val dataParams = params.toMutableList()
        dataParams.add(size)
        dataParams.add(offset)
        
        val dataRows = pool.preparedQuery(dataQuery)
            .execute(Tuple.from(dataParams))
            .awaitForResult()
        val users = dataRows.map { rowToUser(it) }
        
        return Pair(users, total)
    }
    
    suspend fun save(user: User): Long {
        val now = System.currentTimeMillis()
        user.createTime = now
        user.updateTime = now
        
        val rows = pool.preparedQuery(
            "INSERT INTO user_info (username, password, email, phone, status, last_login_time, create_time, update_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )
            .execute(Tuple.of(
                user.username,
                user.password,
                user.email,
                user.phone,
                user.status,
                user.lastLoginTime,
                user.createTime,
                user.updateTime
            ))
            .awaitForResult()
        return rows.iterator().next().getLong(0)
    }
    
    suspend fun updateById(user: User) {
        user.updateTime = System.currentTimeMillis()
        
        pool.preparedQuery(
            "UPDATE user_info SET username = ?, password = ?, email = ?, phone = ?, status = ?, " +
            "last_login_time = ?, update_time = ? WHERE id = ?"
        )
            .execute(Tuple.of(
                user.username,
                user.password,
                user.email,
                user.phone,
                user.status,
                user.lastLoginTime,
                user.updateTime,
                user.id
            ))
            .awaitForResult()
    }
    
    suspend fun deleteById(id: Long) {
        pool.preparedQuery("DELETE FROM user_info WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
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

