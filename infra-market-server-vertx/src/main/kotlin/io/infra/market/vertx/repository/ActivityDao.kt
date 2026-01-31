package io.infra.market.vertx.repository

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.entity.Activity
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 活动数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ActivityDao @Inject constructor(pool: Pool) : BaseDao(pool) {
    
    suspend fun findById(id: Long): Activity? {
        val rows = pool.preparedQuery("SELECT * FROM activity WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToActivity(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findPage(name: String?, templateId: Long?, status: Int?, page: Int, size: Int): PageWrapper<Activity> {
        return page(
            tableName = "activity",
            page = page,
            size = size,
            dynamicConditions = listOf(
                "name LIKE ?" to name?.let { "%$it%" },
                "template_id = ?" to templateId,
                "status = ?" to status
            ),
            orderBy = "create_time DESC",
            rowMapper = { rowToActivity(it) }
        )
    }
    
    suspend fun save(activity: Activity): Long {
        setCreateTime(activity)
        return execute(
            "INSERT INTO activity (name, description, template_id, config_data, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
            activity.name,
            activity.description,
            activity.templateId,
            activity.configData,
            activity.status,
            activity.createTime,
            activity.updateTime
        )
    }
    
    suspend fun save(activity: Activity, connection: SqlConnection): Long {
        setCreateTime(activity)
        return execute(
            connection,
            "INSERT INTO activity (name, description, template_id, config_data, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
            activity.name,
            activity.description,
            activity.templateId,
            activity.configData,
            activity.status,
            activity.createTime,
            activity.updateTime
        )
    }
    
    suspend fun updateById(activity: Activity) {
        setUpdateTime(activity)
        execute(
            "UPDATE activity SET name = ?, description = ?, template_id = ?, config_data = ?, status = ?, update_time = ? WHERE id = ?",
            activity.name,
            activity.description,
            activity.templateId,
            activity.configData,
            activity.status,
            activity.updateTime,
            activity.id
        )
    }
    
    suspend fun updateById(activity: Activity, connection: SqlConnection) {
        setUpdateTime(activity)
        execute(
            connection,
            "UPDATE activity SET name = ?, description = ?, template_id = ?, config_data = ?, status = ?, update_time = ? WHERE id = ?",
            activity.name,
            activity.description,
            activity.templateId,
            activity.configData,
            activity.status,
            activity.updateTime,
            activity.id
        )
    }
    
    suspend fun deleteById(id: Long) {
        execute("DELETE FROM activity WHERE id = ?", id)
    }
    
    suspend fun deleteById(id: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM activity WHERE id = ?", id)
    }
    
    private fun rowToActivity(row: Row): Activity {
        return Activity(
            id = row.getLong("id"),
            name = row.getString("name"),
            description = row.getString("description"),
            templateId = row.getLong("template_id"),
            configData = row.getString("config_data"),
            status = row.getInteger("status"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}
