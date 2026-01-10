package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.ApiInterface
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 接口数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class ApiInterfaceDao(private val pool: Pool) {
    
    suspend fun findById(id: Long): ApiInterface? {
        val rows = pool.preparedQuery("SELECT * FROM api_interface WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToApiInterface(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByIds(ids: List<Long>): List<ApiInterface> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val placeholders = ids.joinToString(",") { "?" }
        val rows = pool.preparedQuery("SELECT * FROM api_interface WHERE id IN ($placeholders)")
            .execute(Tuple.from(ids))
            .awaitForResult()
        return rows.map { rowToApiInterface(it) }
    }
    
    suspend fun page(name: String?, method: String?, status: Int?, environment: String?, page: Int, size: Int): Pair<List<ApiInterface>, Long> {
        val offset = (page - 1) * size
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()
        
        if (!name.isNullOrBlank()) {
            conditions.add("name LIKE ?")
            params.add("%$name%")
        }
        
        if (!method.isNullOrBlank()) {
            conditions.add("method = ?")
            params.add(method)
        }
        
        if (status != null) {
            conditions.add("status = ?")
            params.add(status)
        }
        
        if (!environment.isNullOrBlank()) {
            conditions.add("environment = ?")
            params.add(environment)
        }
        
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else {
            ""
        }
        
        val countQuery = "SELECT COUNT(*) as total FROM api_interface $whereClause"
        val dataQuery = "SELECT * FROM api_interface $whereClause ORDER BY id DESC LIMIT ? OFFSET ?"
        
        val countRows = pool.preparedQuery(countQuery)
            .execute(if (params.isNotEmpty()) Tuple.from(params) else Tuple.tuple())
            .awaitForResult()
        val total = countRows.first().getLong("total")
        
        val dataRows = pool.preparedQuery(dataQuery)
            .execute(Tuple.from(params + size + offset))
            .awaitForResult()
        
        return Pair(dataRows.map { rowToApiInterface(it) }, total)
    }
    
    suspend fun save(apiInterface: ApiInterface): Long {
        val now = System.currentTimeMillis()
        apiInterface.createTime = now
        apiInterface.updateTime = now
        
        val rows = pool.preparedQuery(
            "INSERT INTO api_interface (name, method, url, description, post_type, params, status, environment, timeout, value_path, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )
            .execute(Tuple.of(
                apiInterface.name, apiInterface.method, apiInterface.url, apiInterface.description,
                apiInterface.postType, apiInterface.params, apiInterface.status, apiInterface.environment,
                apiInterface.timeout, apiInterface.valuePath, apiInterface.createTime, apiInterface.updateTime
            ))
            .awaitForResult()
        return rows.iterator().next().getLong(0)
    }
    
    suspend fun updateById(apiInterface: ApiInterface) {
        apiInterface.updateTime = System.currentTimeMillis()
        pool.preparedQuery(
            "UPDATE api_interface SET name = ?, method = ?, url = ?, description = ?, post_type = ?, params = ?, status = ?, environment = ?, timeout = ?, value_path = ?, update_time = ? WHERE id = ?"
        )
            .execute(Tuple.of(
                apiInterface.name, apiInterface.method, apiInterface.url, apiInterface.description,
                apiInterface.postType, apiInterface.params, apiInterface.status, apiInterface.environment,
                apiInterface.timeout, apiInterface.valuePath, apiInterface.updateTime, apiInterface.id
            ))
            .awaitForResult()
    }
    
    suspend fun deleteById(id: Long) {
        pool.preparedQuery("DELETE FROM api_interface WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
    }
    
    suspend fun findMostUsedInterfaceIds(days: Int, limit: Int): List<Long> {
        val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val rows = pool.preparedQuery(
            "SELECT interface_id, COUNT(*) as count FROM api_interface_execution_record WHERE create_time >= ? GROUP BY interface_id ORDER BY count DESC LIMIT ?"
        )
            .execute(Tuple.of(startTime, limit))
            .awaitForResult()
        return rows.map { it.getLong("interface_id") }
    }
    
    suspend fun count(): Long {
        val rows = pool.preparedQuery("SELECT COUNT(*) as total FROM api_interface")
            .execute()
            .awaitForResult()
        return rows.first().getLong("total")
    }
    
    private fun rowToApiInterface(row: Row): ApiInterface {
        return ApiInterface(
            id = row.getLong("id"),
            name = row.getString("name"),
            method = row.getString("method"),
            url = row.getString("url"),
            description = row.getString("description"),
            postType = row.getString("post_type"),
            params = row.getString("params"),
            status = row.getInteger("status"),
            environment = row.getString("environment"),
            timeout = row.getLong("timeout"),
            valuePath = row.getString("value_path"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}
