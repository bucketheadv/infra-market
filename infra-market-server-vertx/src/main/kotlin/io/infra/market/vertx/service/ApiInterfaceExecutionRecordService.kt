package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.repository.ApiInterfaceExecutionRecordDao
import io.vertx.core.json.JsonObject

/**
 * 接口执行记录服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class ApiInterfaceExecutionRecordService(
    private val apiInterfaceExecutionRecordDao: ApiInterfaceExecutionRecordDao
) {
    
    suspend fun list(body: JsonObject): ApiData<PageResultDto<JsonObject>> {
        val interfaceId = body.getLong("interfaceId")
        val executorId = body.getLong("executorId")
        val keyword = body.getString("keyword")
        val page = body.getInteger("page") ?: 1
        val size = body.getInteger("size") ?: 10
        
        val (records, total) = apiInterfaceExecutionRecordDao.page(interfaceId, executorId, keyword, page, size)
        val recordDtos = records.map { it.toJsonObject() }
        return ApiData.success(PageResultDto(recordDtos, total, page.toLong(), size.toLong()))
    }
    
    suspend fun detail(id: Long): ApiData<JsonObject?> {
        val record = apiInterfaceExecutionRecordDao.findById(id) ?: return ApiData.error("执行记录不存在")

        return ApiData.success(record.toJsonObject())
    }
    
    suspend fun getByExecutorId(executorId: Long, limit: Int): ApiData<List<JsonObject>> {
        val records = apiInterfaceExecutionRecordDao.findByExecutorId(executorId, limit)
        val recordDtos = records.map { it.toJsonObject() }
        return ApiData.success(recordDtos)
    }
    
    suspend fun getExecutionStats(interfaceId: Long): ApiData<JsonObject?> {
        val stats = apiInterfaceExecutionRecordDao.getExecutionStats(interfaceId)
            ?: return ApiData.error("未找到执行统计信息")

        val statsJson = JsonObject()
        stats.forEach { (key, value) ->
            statsJson.put(key, value)
        }
        return ApiData.success(statsJson)
    }
    
    suspend fun getExecutionCount(startTime: Long, endTime: Long): ApiData<Long> {
        val count = apiInterfaceExecutionRecordDao.countByTimeRange(startTime, endTime)
        return ApiData.success(count)
    }
    
    suspend fun cleanup(beforeTime: Long): ApiData<Int> {
        val deletedCount = apiInterfaceExecutionRecordDao.deleteByTimeBefore(beforeTime)
        return ApiData.success(deletedCount)
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
