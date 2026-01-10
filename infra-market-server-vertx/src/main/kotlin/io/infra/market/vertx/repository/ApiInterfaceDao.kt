package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.ApiInterface
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * 接口数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class ApiInterfaceDao(pool: Pool) : BaseDao(pool) {
    
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
    
    suspend fun page(name: String?, method: String?, status: Int?, environment: String?, page: Int, size: Int): PageWrapper<ApiInterface> {
        return page(
            tableName = "api_interface",
            page = page,
            size = size,
            dynamicConditions = listOf(
                "name LIKE ?" to name?.let { "%$it%" },
                "method = ?" to method,
                "status = ?" to status,
                "environment = ?" to environment
            ),
            orderBy = "id DESC",
            rowMapper = { rowToApiInterface(it) }
        )
    }
    
    suspend fun save(apiInterface: ApiInterface): Long {
        setCreateTime(apiInterface)
        return execute(
            "INSERT INTO api_interface (name, method, url, description, post_type, params, status, environment, timeout, value_path, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            apiInterface.name,
            apiInterface.method,
            apiInterface.url,
            apiInterface.description,
            apiInterface.postType,
            apiInterface.params,
            apiInterface.status,
            apiInterface.environment,
            apiInterface.timeout,
            apiInterface.valuePath,
            apiInterface.createTime,
            apiInterface.updateTime
        )
    }
    
    suspend fun save(apiInterface: ApiInterface, connection: SqlConnection): Long {
        setCreateTime(apiInterface)
        return execute(
            connection,
            "INSERT INTO api_interface (name, method, url, description, post_type, params, status, environment, timeout, value_path, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            apiInterface.name,
            apiInterface.method,
            apiInterface.url,
            apiInterface.description,
            apiInterface.postType,
            apiInterface.params,
            apiInterface.status,
            apiInterface.environment,
            apiInterface.timeout,
            apiInterface.valuePath,
            apiInterface.createTime,
            apiInterface.updateTime
        )
    }
    
    suspend fun updateById(apiInterface: ApiInterface) {
        setUpdateTime(apiInterface)
        execute(
            "UPDATE api_interface SET name = ?, method = ?, url = ?, description = ?, post_type = ?, params = ?, status = ?, environment = ?, timeout = ?, value_path = ?, update_time = ? WHERE id = ?",
            apiInterface.name,
            apiInterface.method,
            apiInterface.url,
            apiInterface.description,
            apiInterface.postType,
            apiInterface.params,
            apiInterface.status,
            apiInterface.environment,
            apiInterface.timeout,
            apiInterface.valuePath,
            apiInterface.updateTime,
            apiInterface.id
        )
    }
    
    suspend fun updateById(apiInterface: ApiInterface, connection: SqlConnection) {
        setUpdateTime(apiInterface)
        execute(
            connection,
            "UPDATE api_interface SET name = ?, method = ?, url = ?, description = ?, post_type = ?, params = ?, status = ?, environment = ?, timeout = ?, value_path = ?, update_time = ? WHERE id = ?",
            apiInterface.name,
            apiInterface.method,
            apiInterface.url,
            apiInterface.description,
            apiInterface.postType,
            apiInterface.params,
            apiInterface.status,
            apiInterface.environment,
            apiInterface.timeout,
            apiInterface.valuePath,
            apiInterface.updateTime,
            apiInterface.id
        )
    }
    
    suspend fun deleteById(id: Long) {
        execute("DELETE FROM api_interface WHERE id = ?", id)
    }
    
    suspend fun deleteById(id: Long, connection: SqlConnection) {
        execute(connection, "DELETE FROM api_interface WHERE id = ?", id)
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
