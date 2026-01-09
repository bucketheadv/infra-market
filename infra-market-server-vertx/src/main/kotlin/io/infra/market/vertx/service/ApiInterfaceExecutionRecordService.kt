package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.repository.ApiInterfaceExecutionRecordDao
import io.vertx.core.Future
import io.vertx.core.json.JsonObject

/**
 * 接口执行记录服务
 */
class ApiInterfaceExecutionRecordService(
    private val apiInterfaceExecutionRecordDao: ApiInterfaceExecutionRecordDao
) {
    
    fun list(body: JsonObject): Future<ApiData<PageResultDto<JsonObject>>> {
        val interfaceId = body.getLong("interfaceId")
        val executorId = body.getLong("executorId")
        val keyword = body.getString("keyword")
        val page = body.getInteger("page") ?: 1
        val size = body.getInteger("size") ?: 10
        
        return apiInterfaceExecutionRecordDao.page(interfaceId, executorId, keyword, page, size)
            .map { (records, total) ->
                val recordDtos = records.map { it.toJsonObject() }
                ApiData.success(PageResultDto(recordDtos, total, page.toLong(), size.toLong()))
            }
    }
    
    fun detail(id: Long): Future<ApiData<JsonObject?>> {
        return apiInterfaceExecutionRecordDao.findById(id)
            .map { record ->
                if (record == null) {
                    ApiData.error<JsonObject?>("执行记录不存在")
                } else {
                    ApiData.success(record.toJsonObject())
                }
            }
    }
    
    fun getByExecutorId(executorId: Long, limit: Int): Future<ApiData<List<JsonObject>>> {
        return apiInterfaceExecutionRecordDao.findByExecutorId(executorId, limit)
            .map { records ->
                val recordDtos = records.map { it.toJsonObject() }
                ApiData.success(recordDtos)
            }
    }
    
    fun getExecutionStats(interfaceId: Long): Future<ApiData<JsonObject?>> {
        return apiInterfaceExecutionRecordDao.getExecutionStats(interfaceId)
            .map { stats ->
                if (stats == null) {
                    ApiData.error<JsonObject?>("未找到执行统计信息")
                } else {
                    val statsJson = JsonObject()
                    stats.forEach { (key, value) ->
                        statsJson.put(key, value)
                    }
                    ApiData.success(statsJson)
                }
            }
    }
    
    fun getExecutionCount(startTime: Long, endTime: Long): Future<ApiData<Long>> {
        return apiInterfaceExecutionRecordDao.countByTimeRange(startTime, endTime)
            .map { count ->
                ApiData.success(count)
            }
    }
    
    fun cleanup(beforeTime: Long): Future<ApiData<Int>> {
        return apiInterfaceExecutionRecordDao.deleteByTimeBefore(beforeTime)
            .map { deletedCount ->
                ApiData.success(deletedCount)
            }
    }
    
    private fun io.infra.market.vertx.entity.ApiInterfaceExecutionRecord.toJsonObject(): JsonObject {
        return JsonObject()
            .put("id", id)
            .put("interfaceId", interfaceId)
            .put("executorId", executorId)
            .put("executorName", executorName)
            .put("requestParams", requestParams)
            .put("requestHeaders", requestHeaders)
            .put("requestBody", requestBody)
            .put("responseStatus", responseStatus)
            .put("responseHeaders", responseHeaders)
            .put("responseBody", responseBody)
            .put("executionTime", executionTime)
            .put("success", success)
            .put("errorMessage", errorMessage)
            .put("remark", remark)
            .put("clientIp", clientIp)
            .put("userAgent", userAgent)
            .put("createTime", createTime)
            .put("updateTime", updateTime)
    }
}
