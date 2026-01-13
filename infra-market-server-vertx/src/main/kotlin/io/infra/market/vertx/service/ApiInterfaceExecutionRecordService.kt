package io.infra.market.vertx.service

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.ApiInterfaceExecutionRecordDto
import io.infra.market.vertx.dto.ApiInterfaceExecutionRecordQueryDto
import io.infra.market.vertx.dto.ApiInterfaceExecutionRecordStatsDto
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.entity.ApiInterfaceExecutionRecord
import io.infra.market.vertx.repository.ApiInterfaceExecutionRecordDao
import io.infra.market.vertx.util.TimeUtil

/**
 * 接口执行记录服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ApiInterfaceExecutionRecordService @Inject constructor(
    private val apiInterfaceExecutionRecordDao: ApiInterfaceExecutionRecordDao
) {
    
    suspend fun list(query: ApiInterfaceExecutionRecordQueryDto): ApiData<PageResultDto<ApiInterfaceExecutionRecordDto>> {
        val page = query.page ?: 1
        val size = query.size ?: 10
        
        val pageResult = apiInterfaceExecutionRecordDao.page(query.interfaceId, query.executorId, query.keyword, page, size)
        val recordDtos = pageResult.records.map { convertToDto(it) }
        return ApiData.success(PageResultDto(recordDtos, pageResult.total, pageResult.page, pageResult.size))
    }
    
    suspend fun detail(id: Long): ApiData<ApiInterfaceExecutionRecordDto?> {
        val record = apiInterfaceExecutionRecordDao.findById(id) ?: return ApiData.error("执行记录不存在")

        return ApiData.success(convertToDto(record))
    }
    
    suspend fun getByExecutorId(executorId: Long, limit: Int): ApiData<List<ApiInterfaceExecutionRecordDto>> {
        val records = apiInterfaceExecutionRecordDao.findByExecutorId(executorId, limit)
        val recordDtos = records.map { convertToDto(it) }
        return ApiData.success(recordDtos)
    }
    
    suspend fun getExecutionStats(interfaceId: Long): ApiData<ApiInterfaceExecutionRecordStatsDto?> {
        val stats = apiInterfaceExecutionRecordDao.getExecutionStats(interfaceId)
            ?: return ApiData.error("未找到执行统计信息")

        return ApiData.success(stats)
    }
    
    suspend fun getExecutionCount(startTime: Long, endTime: Long): ApiData<Long> {
        val count = apiInterfaceExecutionRecordDao.countByTimeRange(startTime, endTime)
        return ApiData.success(count)
    }
    
    suspend fun cleanup(beforeTime: Long): ApiData<Int> {
        val deletedCount = apiInterfaceExecutionRecordDao.deleteByTimeBefore(beforeTime)
        return ApiData.success(deletedCount)
    }
    
    private fun convertToDto(entity: ApiInterfaceExecutionRecord): ApiInterfaceExecutionRecordDto {
        return ApiInterfaceExecutionRecordDto(
            id = entity.id,
            interfaceId = entity.interfaceId,
            executorId = entity.executorId,
            executorName = entity.executorName,
            requestParams = entity.requestParams,
            requestHeaders = entity.requestHeaders,
            requestBody = entity.requestBody,
            responseStatus = entity.responseStatus,
            responseHeaders = entity.responseHeaders,
            responseBody = entity.responseBody,
            executionTime = entity.executionTime,
            success = entity.success,
            errorMessage = entity.errorMessage,
            remark = entity.remark,
            clientIp = entity.clientIp,
            userAgent = entity.userAgent,
            createTime = TimeUtil.format(entity.createTime),
            updateTime = TimeUtil.format(entity.updateTime)
        )
    }
}
