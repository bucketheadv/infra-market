package io.infra.market.vertx.repository

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.entity.ActivityComponent
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 活动组件数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ActivityComponentDao @Inject constructor(pool: Pool) : BaseDao(pool) {
    
    suspend fun findById(id: Long): ActivityComponent? {
        val rows = pool.preparedQuery("SELECT * FROM activity_component WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToActivityComponent(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findAll(): List<ActivityComponent> {
        val rows = pool.preparedQuery("SELECT * FROM activity_component ORDER BY create_time DESC")
            .execute()
            .awaitForResult()
        return rows.map { rowToActivityComponent(it) }
    }
    
    suspend fun findPage(name: String?, status: Int?, page: Int, size: Int): PageWrapper<ActivityComponent> {
        return page(
            tableName = "activity_component",
            page = page,
            size = size,
            dynamicConditions = listOf(
                "name LIKE ?" to name?.let { "%$it%" },
                "status = ?" to status
            ),
            orderBy = "create_time DESC",
            rowMapper = { rowToActivityComponent(it) }
        )
    }
    
    suspend fun save(component: ActivityComponent): Long {
        setCreateTime(component)
        return execute(
            "INSERT INTO activity_component (name, description, fields, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
            component.name,
            component.description,
            component.fields,
            component.status,
            component.createTime,
            component.updateTime
        )
    }
    
    suspend fun save(component: ActivityComponent, connection: SqlConnection): Long {
        setCreateTime(component)
        return execute(
            connection,
            "INSERT INTO activity_component (name, description, fields, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
            component.name,
            component.description,
            component.fields,
            component.status,
            component.createTime,
            component.updateTime
        )
    }
    
    suspend fun updateById(component: ActivityComponent) {
        setUpdateTime(component)
        execute(
            "UPDATE activity_component SET name = ?, description = ?, fields = ?, status = ?, update_time = ? WHERE id = ?",
            component.name,
            component.description,
            component.fields,
            component.status,
            component.updateTime,
            component.id
        )
    }
    
    suspend fun updateById(component: ActivityComponent, connection: SqlConnection) {
        setUpdateTime(component)
        execute(
            connection,
            "UPDATE activity_component SET name = ?, description = ?, fields = ?, status = ?, update_time = ? WHERE id = ?",
            component.name,
            component.description,
            component.fields,
            component.status,
            component.updateTime,
            component.id
        )
    }
    
    suspend fun deleteById(id: Long) {
        execute("DELETE FROM activity_component WHERE id = ?", id)
    }
    
    suspend fun deleteById(id: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM activity_component WHERE id = ?", id)
    }
    
    private fun rowToActivityComponent(row: Row): ActivityComponent {
        return ActivityComponent(
            id = row.getLong("id"),
            name = row.getString("name"),
            description = row.getString("description"),
            fields = row.getString("fields"),
            status = row.getInteger("status"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}
