package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.ApiInterfaceExecutionRecord
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

/**
 * 接口执行记录数据访问对象
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class ApiInterfaceExecutionRecordDao(private val pool: Pool) {
    
    suspend fun findById(id: Long): ApiInterfaceExecutionRecord? {
        val rows = pool.preparedQuery("SELECT * FROM api_interface_execution_record WHERE id = ?")
            .execute(Tuple.of(id))
            .awaitForResult()
        return if (rows.size() > 0) {
            rowToRecord(rows.first())
        } else {
            null
        }
    }
    
    suspend fun findByExecutorId(executorId: Long, limit: Int): List<ApiInterfaceExecutionRecord> {
        val rows = pool.preparedQuery("SELECT * FROM api_interface_execution_record WHERE executor_id = ? ORDER BY create_time DESC LIMIT ?")
            .execute(Tuple.of(executorId, limit))
            .awaitForResult()
        return rows.map { rowToRecord(it) }
    }
    
    suspend fun page(interfaceId: Long?, executorId: Long?, keyword: String?, page: Int, size: Int): Pair<List<ApiInterfaceExecutionRecord>, Long> {
        val offset = (page - 1) * size
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()
        
        if (interfaceId != null) {
            conditions.add("interface_id = ?")
            params.add(interfaceId)
        }
        
        if (executorId != null) {
            conditions.add("executor_id = ?")
            params.add(executorId)
        }
        
        if (!keyword.isNullOrBlank()) {
            conditions.add("(executor_name LIKE ? OR error_message LIKE ? OR remark LIKE ?)")
            params.add("%$keyword%")
            params.add("%$keyword%")
            params.add("%$keyword%")
        }
        
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else {
            ""
        }
        
        val countQuery = "SELECT COUNT(*) as total FROM api_interface_execution_record $whereClause"
        val dataQuery = "SELECT * FROM api_interface_execution_record $whereClause ORDER BY id DESC LIMIT ? OFFSET ?"
        
        val countRows = pool.preparedQuery(countQuery)
            .execute(if (params.isNotEmpty()) Tuple.from(params) else Tuple.tuple())
            .awaitForResult()
        val total = countRows.first().getLong("total")
        
        val dataRows = pool.preparedQuery(dataQuery)
            .execute(Tuple.from(params + size + offset))
            .awaitForResult()
        
        return Pair(dataRows.map { rowToRecord(it) }, total)
    }
    
    suspend fun getExecutionStats(interfaceId: Long): Map<String, Any>? {
        val rows = pool.preparedQuery("SELECT * FROM api_interface_execution_record WHERE interface_id = ?")
            .execute(Tuple.of(interfaceId))
            .awaitForResult()
        val records = rows.map { rowToRecord(it) }
        if (records.isEmpty()) {
            return null
        } else {
            val totalExecutions = records.size.toLong()
            val successExecutions = records.count { it.success == true }.toLong()
            val failedExecutions = totalExecutions - successExecutions
            val successRate = if (totalExecutions > 0) (successExecutions.toDouble() / totalExecutions) * 100 else 0.0
            val executionTimes = records.mapNotNull { it.executionTime }
            val avgExecutionTime = if (executionTimes.isNotEmpty()) {
                executionTimes.average()
            } else {
                0.0
            }
            val minExecutionTime = executionTimes.minOrNull() ?: 0L
            val maxExecutionTime = executionTimes.maxOrNull() ?: 0L
            val lastExecutionTime = records.maxByOrNull { it.createTime ?: 0L }?.createTime
            
            return mapOf(
                "interfaceId" to interfaceId,
                "totalExecutions" to totalExecutions,
                "successExecutions" to successExecutions,
                "failedExecutions" to failedExecutions,
                "successRate" to successRate,
                "avgExecutionTime" to avgExecutionTime,
                "minExecutionTime" to minExecutionTime,
                "maxExecutionTime" to maxExecutionTime,
                "lastExecutionTime" to (lastExecutionTime ?: 0L)
            )
        }
    }
    
    suspend fun countByTimeRange(startTime: Long, endTime: Long): Long {
        val rows = pool.preparedQuery("SELECT COUNT(*) as total FROM api_interface_execution_record WHERE create_time >= ? AND create_time <= ?")
            .execute(Tuple.of(startTime, endTime))
            .awaitForResult()
        return rows.first().getLong("total")
    }
    
    suspend fun deleteByTimeBefore(beforeTime: Long): Int {
        val rows = pool.preparedQuery("DELETE FROM api_interface_execution_record WHERE create_time < ?")
            .execute(Tuple.of(beforeTime))
            .awaitForResult()
        return rows.rowCount()
    }
    
    suspend fun save(record: ApiInterfaceExecutionRecord): Long {
        val now = System.currentTimeMillis()
        record.createTime = now
        record.updateTime = now
        
        val rows = pool.preparedQuery(
            "INSERT INTO api_interface_execution_record (interface_id, executor_id, executor_name, request_params, request_headers, request_body, response_status, response_headers, response_body, execution_time, success, error_message, remark, client_ip, user_agent, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )
            .execute(Tuple.of(
                record.interfaceId, record.executorId, record.executorName,
                record.requestParams, record.requestHeaders, record.requestBody,
                record.responseStatus, record.responseHeaders, record.responseBody,
                record.executionTime, record.success, record.errorMessage,
                record.remark, record.clientIp, record.userAgent,
                record.createTime, record.updateTime
            ))
            .awaitForResult()
        return rows.iterator().next().getLong(0)
    }
    
    private fun rowToRecord(row: Row): ApiInterfaceExecutionRecord {
        return ApiInterfaceExecutionRecord(
            id = row.getLong("id"),
            interfaceId = row.getLong("interface_id"),
            executorId = row.getLong("executor_id"),
            executorName = row.getString("executor_name"),
            requestParams = row.getString("request_params"),
            requestHeaders = row.getString("request_headers"),
            requestBody = row.getString("request_body"),
            responseStatus = row.getInteger("response_status"),
            responseHeaders = row.getString("response_headers"),
            responseBody = row.getString("response_body"),
            executionTime = row.getLong("execution_time"),
            success = row.getBoolean("success"),
            errorMessage = row.getString("error_message"),
            remark = row.getString("remark"),
            clientIp = row.getString("client_ip"),
            userAgent = row.getString("user_agent"),
            createTime = row.getLong("create_time"),
            updateTime = row.getLong("update_time")
        )
    }
}
