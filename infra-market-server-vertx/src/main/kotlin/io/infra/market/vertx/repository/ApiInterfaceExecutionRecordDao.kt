package io.infra.market.vertx.repository

import com.google.inject.Inject
import io.infra.market.vertx.dto.ApiInterfaceExecutionRecordStatsDto
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
class ApiInterfaceExecutionRecordDao @Inject constructor(pool: Pool) : BaseDao(pool) {
    
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
    
    suspend fun page(interfaceId: Long?, executorId: Long?, keyword: String?, page: Int, size: Int): PageWrapper<ApiInterfaceExecutionRecord> {
        val dynamicConditions = mutableListOf<Pair<String, Any?>>().apply {
            interfaceId?.let { add("interface_id = ?" to it) }
            executorId?.let { add("executor_id = ?" to it) }
        }
        
        val multiParamConditions = if (!keyword.isNullOrBlank()) {
            val pattern = "%$keyword%"
            listOf("(executor_name LIKE ? OR error_message LIKE ? OR remark LIKE ?)" to listOf(pattern, pattern, pattern))
        } else {
            emptyList()
        }
        
        return page(
            tableName = "api_interface_execution_record",
            page = page,
            size = size,
            dynamicConditions = dynamicConditions,
            multiParamConditions = multiParamConditions,
            orderBy = "id DESC",
            rowMapper = { rowToRecord(it) }
        )
    }
    
    suspend fun getExecutionStats(interfaceId: Long): ApiInterfaceExecutionRecordStatsDto? {
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
            
            return ApiInterfaceExecutionRecordStatsDto(
                totalExecutions = totalExecutions,
                successCount = successExecutions,
                failureCount = failedExecutions,
                successRate = successRate,
                averageExecutionTime = avgExecutionTime,
                minExecutionTime = minExecutionTime,
                maxExecutionTime = maxExecutionTime
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
        setCreateTime(record)
        return execute(
            "INSERT INTO api_interface_execution_record (interface_id, executor_id, executor_name, request_params, request_headers, request_body, response_status, response_headers, response_body, execution_time, success, error_message, remark, client_ip, user_agent, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            record.interfaceId,
            record.executorId,
            record.executorName,
            record.requestParams,
            record.requestHeaders,
            record.requestBody,
            record.responseStatus,
            record.responseHeaders,
            record.responseBody,
            record.executionTime,
            record.success,
            record.errorMessage,
            record.remark,
            record.clientIp,
            record.userAgent,
            record.createTime,
            record.updateTime
        )
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
