package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.ApiInterface
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 接口数据访问对象
 */
class ApiInterfaceDao(private val pool: Pool) {
    
    fun findById(id: Long): Future<ApiInterface?> {
        return pool.preparedQuery("SELECT * FROM api_interface WHERE id = ?")
            .execute(Tuple.of(id))
            .map { rows ->
                if (rows.size() > 0) {
                    rowToApiInterface(rows.first())
                } else {
                    null
                }
            }
    }
    
    fun findByIds(ids: List<Long>): Future<List<ApiInterface>> {
        if (ids.isEmpty()) {
            return Future.succeededFuture(emptyList())
        }
        val placeholders = ids.joinToString(",") { "?" }
        return pool.preparedQuery("SELECT * FROM api_interface WHERE id IN ($placeholders)")
            .execute(Tuple.from(ids))
            .map { rows ->
                rows.map { rowToApiInterface(it) }
            }
    }
    
    fun page(name: String?, method: String?, status: Int?, environment: String?, page: Int, size: Int): Future<Pair<List<ApiInterface>, Long>> {
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
        
        return pool.preparedQuery(countQuery)
            .execute(if (params.isNotEmpty()) Tuple.from(params) else Tuple.tuple())
            .compose { countRows ->
                val total = countRows.first().getLong("total")
                pool.preparedQuery(dataQuery)
                    .execute(Tuple.from(params + size + offset))
                    .map { dataRows ->
                        Pair(dataRows.map { rowToApiInterface(it) }, total)
                    }
            }
    }
    
    fun save(apiInterface: ApiInterface): Future<Long> {
        val now = System.currentTimeMillis()
        apiInterface.createTime = now
        apiInterface.updateTime = now
        
        return pool.preparedQuery(
            "INSERT INTO api_interface (name, method, url, description, post_type, params, status, environment, timeout, value_path, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )
            .execute(Tuple.of(
                apiInterface.name, apiInterface.method, apiInterface.url, apiInterface.description,
                apiInterface.postType, apiInterface.params, apiInterface.status, apiInterface.environment,
                apiInterface.timeout, apiInterface.valuePath, apiInterface.createTime, apiInterface.updateTime
            ))
            .map { rows ->
                rows.iterator().next().getLong(0)
            }
    }
    
    fun updateById(apiInterface: ApiInterface): Future<Void> {
        apiInterface.updateTime = System.currentTimeMillis()
        return pool.preparedQuery(
            "UPDATE api_interface SET name = ?, method = ?, url = ?, description = ?, post_type = ?, params = ?, status = ?, environment = ?, timeout = ?, value_path = ?, update_time = ? WHERE id = ?"
        )
            .execute(Tuple.of(
                apiInterface.name, apiInterface.method, apiInterface.url, apiInterface.description,
                apiInterface.postType, apiInterface.params, apiInterface.status, apiInterface.environment,
                apiInterface.timeout, apiInterface.valuePath, apiInterface.updateTime, apiInterface.id
            ))
            .map { null }
    }
    
    fun deleteById(id: Long): Future<Void> {
        return pool.preparedQuery("DELETE FROM api_interface WHERE id = ?")
            .execute(Tuple.of(id))
            .map { null }
    }
    
    fun findMostUsedInterfaceIds(days: Int, limit: Int): Future<List<Long>> {
        val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return pool.preparedQuery(
            "SELECT interface_id, COUNT(*) as count FROM api_interface_execution_record WHERE create_time >= ? GROUP BY interface_id ORDER BY count DESC LIMIT ?"
        )
            .execute(Tuple.of(startTime, limit))
            .map { rows ->
                rows.map { it.getLong("interface_id") }
            }
    }
    
    fun count(): Future<Long> {
        return pool.preparedQuery("SELECT COUNT(*) as total FROM api_interface")
            .execute()
            .map { rows -> rows.first().getLong("total") }
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

