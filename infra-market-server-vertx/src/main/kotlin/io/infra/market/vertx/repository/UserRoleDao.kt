package io.infra.market.vertx.repository

import com.google.inject.Inject
import io.infra.market.vertx.entity.UserRole
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 用户角色关联数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class UserRoleDao @Inject constructor(pool: Pool) : BaseDao(pool) {
    
    suspend fun findByUid(uid: Long): List<UserRole> {
        val rows = pool.preparedQuery("SELECT * FROM user_role WHERE uid = ?")
            .execute(Tuple.of(uid))
            .awaitForResult()
        return rows.map { rowToUserRole(it) }
    }
    
    suspend fun findByUids(uids: List<Long>): List<UserRole> {
        if (uids.isEmpty()) {
            return emptyList()
        }
        
        val placeholders = uids.joinToString(",") { "?" }
        val rows = pool.preparedQuery("SELECT * FROM user_role WHERE uid IN ($placeholders)")
            .execute(Tuple.from(uids))
            .awaitForResult()
        return rows.map { rowToUserRole(it) }
    }
    
    suspend fun findByRoleId(roleId: Long): List<UserRole> {
        val rows = pool.preparedQuery("SELECT * FROM user_role WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .awaitForResult()
        return rows.map { rowToUserRole(it) }
    }
    
    suspend fun save(userRole: UserRole): Long {
        setCreateTime(userRole)
        return execute(
            "INSERT INTO user_role (uid, role_id, create_time, update_time) VALUES (?, ?, ?, ?)",
            userRole.uid,
            userRole.roleId,
            userRole.createTime,
            userRole.updateTime
        )
    }
    
    suspend fun save(userRole: UserRole, connection: SqlConnection): Long {
        setCreateTime(userRole)
        return execute(
            connection,
            "INSERT INTO user_role (uid, role_id, create_time, update_time) VALUES (?, ?, ?, ?)",
            userRole.uid,
            userRole.roleId,
            userRole.createTime,
            userRole.updateTime
        )
    }
    
    suspend fun deleteByUid(uid: Long) {
        execute("DELETE FROM user_role WHERE uid = ?", uid)
    }
    
    suspend fun deleteByUid(uid: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM user_role WHERE uid = ?", uid)
    }
    
    suspend fun deleteByRoleId(roleId: Long) {
        pool.preparedQuery("DELETE FROM user_role WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .awaitForResult()
    }
    
    suspend fun countByRoleId(roleId: Long): Long {
        val rows = pool.preparedQuery("SELECT COUNT(*) as total FROM user_role WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .awaitForResult()
        return rows.first().getLong("total")
    }
    
    private fun rowToUserRole(row: Row): UserRole {
        return UserRole(
            id = row.getLong("id"),
            uid = row.getLong("uid"),
            roleId = row.getLong("role_id"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}
