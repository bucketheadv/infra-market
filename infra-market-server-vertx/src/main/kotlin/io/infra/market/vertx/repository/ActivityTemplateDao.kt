package io.infra.market.vertx.repository

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.entity.ActivityTemplate
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 活动模板数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ActivityTemplateDao @Inject constructor(pool: Pool) : BaseDao(pool) {
    
    suspend fun findById(id: Long): ActivityTemplate? {
        val rows = pool.preparedQuery("SELECT * FROM activity_template WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToActivityTemplate(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findAll(): List<ActivityTemplate> {
        val rows = pool.preparedQuery("SELECT * FROM activity_template ORDER BY create_time DESC")
            .execute()
            .awaitForResult()
        return rows.map { rowToActivityTemplate(it) }
    }
    
    suspend fun findPage(name: String?, status: Int?, page: Int, size: Int): PageWrapper<ActivityTemplate> {
        return page(
            tableName = "activity_template",
            page = page,
            size = size,
            dynamicConditions = listOf(
                "name LIKE ?" to name?.let { "%$it%" },
                "status = ?" to status
            ),
            orderBy = "create_time DESC",
            rowMapper = { rowToActivityTemplate(it) }
        )
    }
    
    suspend fun save(template: ActivityTemplate): Long {
        setCreateTime(template)
        return execute(
            "INSERT INTO activity_template (name, description, fields, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
            template.name,
            template.description,
            template.fields,
            template.status,
            template.createTime,
            template.updateTime
        )
    }
    
    suspend fun save(template: ActivityTemplate, connection: SqlConnection): Long {
        setCreateTime(template)
        return execute(
            connection,
            "INSERT INTO activity_template (name, description, fields, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
            template.name,
            template.description,
            template.fields,
            template.status,
            template.createTime,
            template.updateTime
        )
    }
    
    suspend fun updateById(template: ActivityTemplate) {
        setUpdateTime(template)
        execute(
            "UPDATE activity_template SET name = ?, description = ?, fields = ?, status = ?, update_time = ? WHERE id = ?",
            template.name,
            template.description,
            template.fields,
            template.status,
            template.updateTime,
            template.id
        )
    }
    
    suspend fun updateById(template: ActivityTemplate, connection: SqlConnection) {
        setUpdateTime(template)
        execute(
            connection,
            "UPDATE activity_template SET name = ?, description = ?, fields = ?, status = ?, update_time = ? WHERE id = ?",
            template.name,
            template.description,
            template.fields,
            template.status,
            template.updateTime,
            template.id
        )
    }
    
    suspend fun deleteById(id: Long) {
        execute("DELETE FROM activity_template WHERE id = ?", id)
    }
    
    suspend fun deleteById(id: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM activity_template WHERE id = ?", id)
    }
    
    private fun rowToActivityTemplate(row: Row): ActivityTemplate {
        return ActivityTemplate(
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
