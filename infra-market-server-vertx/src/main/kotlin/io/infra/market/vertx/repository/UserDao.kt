package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.User
import io.infra.market.vertx.util.TimeUtil
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 用户数据访问对象
 */
class UserDao(private val pool: Pool) {
    
    fun findByUsername(username: String): Future<User?> {
        return pool.preparedQuery("SELECT * FROM user_info WHERE username = ?")
            .execute(Tuple.of(username))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToUser(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findByUid(uid: Long): Future<User?> {
        return pool.preparedQuery("SELECT * FROM user_info WHERE id = ?")
            .execute(Tuple.of(uid))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToUser(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findByEmail(email: String): Future<User?> {
        return pool.preparedQuery("SELECT * FROM user_info WHERE email = ?")
            .execute(Tuple.of(email))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToUser(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findByPhone(phone: String): Future<User?> {
        return pool.preparedQuery("SELECT * FROM user_info WHERE phone = ?")
            .execute(Tuple.of(phone))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToUser(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findPage(username: String?, status: String?, page: Int, size: Int): Future<Pair<List<User>, Long>> {
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
        
        return pool.preparedQuery(countQuery)
            .execute(if (params.isNotEmpty()) Tuple.from(params) else Tuple.tuple())
            .compose { countRows ->
                val total = countRows.first().getLong("total")
                val dataParams = params.toMutableList()
                dataParams.add(size)
                dataParams.add(offset)
                
                pool.preparedQuery(dataQuery)
                    .execute(Tuple.from(dataParams))
                    .map { dataRows ->
                        val users = dataRows.map { rowToUser(it) }
                        Pair(users, total)
                    }
            }
    }
    
    fun save(user: User): Future<Long> {
        val now = System.currentTimeMillis()
        user.createTime = now
        user.updateTime = now
        
        return pool.preparedQuery(
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
            .map { rows ->
                rows.iterator().next().getLong(0)
            }
    }
    
    fun updateById(user: User): Future<Void> {
        user.updateTime = System.currentTimeMillis()
        
        return pool.preparedQuery(
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
            .map { null }
    }
    
    fun deleteById(id: Long): Future<Void> {
        return pool.preparedQuery("DELETE FROM user_info WHERE id = ?")
            .execute(Tuple.of(id))
            .map { null }
    }
    
    fun findByIds(ids: List<Long>): Future<List<User>> {
        if (ids.isEmpty()) {
            return Future.succeededFuture(emptyList())
        }
        
        val placeholders = ids.joinToString(",") { "?" }
        return pool.preparedQuery("SELECT * FROM user_info WHERE id IN ($placeholders)")
            .execute(Tuple.from(ids))
            .map { rows ->
                rows.map { rowToUser(it) }
            }
    }
    
    fun countByStatus(status: String?): Future<Long> {
        return if (status != null) {
            pool.preparedQuery("SELECT COUNT(*) as total FROM user_info WHERE status = ?")
                .execute(Tuple.of(status))
                .map { rows -> rows.first().getLong("total") }
        } else {
            pool.preparedQuery("SELECT COUNT(*) as total FROM user_info")
                .execute()
                .map { rows -> rows.first().getLong("total") }
        }
    }
    
    fun findRecentUsers(limit: Int): Future<List<User>> {
        return pool.preparedQuery(
            "SELECT * FROM user_info WHERE last_login_time IS NOT NULL " +
            "ORDER BY last_login_time DESC LIMIT ?"
        )
            .execute(Tuple.of(limit))
            .map { rows ->
                rows.map { rowToUser(it) }
            }
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

