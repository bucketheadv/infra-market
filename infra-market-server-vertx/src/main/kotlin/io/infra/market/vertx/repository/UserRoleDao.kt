package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.UserRole
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 用户角色关联数据访问对象
 */
class UserRoleDao(private val pool: Pool) {
    
    fun findByUid(uid: Long): Future<List<UserRole>> {
        return pool.preparedQuery("SELECT * FROM user_role WHERE uid = ?")
            .execute(Tuple.of(uid))
            .map { rows ->
                rows.map { rowToUserRole(it) }
            }
    }
    
    fun findByUids(uids: List<Long>): Future<List<UserRole>> {
        if (uids.isEmpty()) {
            return Future.succeededFuture(emptyList())
        }
        
        val placeholders = uids.joinToString(",") { "?" }
        return pool.preparedQuery("SELECT * FROM user_role WHERE uid IN ($placeholders)")
            .execute(Tuple.from(uids))
            .map { rows ->
                rows.map { rowToUserRole(it) }
            }
    }
    
    fun findByRoleId(roleId: Long): Future<List<UserRole>> {
        return pool.preparedQuery("SELECT * FROM user_role WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .map { rows ->
                rows.map { rowToUserRole(it) }
            }
    }
    
    fun save(userRole: UserRole): Future<Long> {
        val now = System.currentTimeMillis()
        userRole.createTime = now
        userRole.updateTime = now
        
        return pool.preparedQuery(
            "INSERT INTO user_role (uid, role_id, create_time, update_time) VALUES (?, ?, ?, ?)"
        )
            .execute(Tuple.of(userRole.uid, userRole.roleId, userRole.createTime, userRole.updateTime))
            .map { rows ->
                rows.iterator().next().getLong(0)
            }
    }
    
    fun deleteByUid(uid: Long): Future<Void> {
        return pool.preparedQuery("DELETE FROM user_role WHERE uid = ?")
            .execute(Tuple.of(uid))
            .map { null }
    }
    
    fun deleteByRoleId(roleId: Long): Future<Void> {
        return pool.preparedQuery("DELETE FROM user_role WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .map { null }
    }
    
    fun countByRoleId(roleId: Long): Future<Long> {
        return pool.preparedQuery("SELECT COUNT(*) as total FROM user_role WHERE role_id = ?")
            .execute(Tuple.of(roleId))
            .map { rows -> rows.first().getLong("total") }
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

