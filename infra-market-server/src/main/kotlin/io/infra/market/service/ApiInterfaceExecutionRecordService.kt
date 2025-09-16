package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.ApiInterfaceExecutionRecordDao
import io.infra.market.repository.entity.ApiInterfaceExecutionRecord
import io.infra.market.util.DateTimeUtil
import org.joda.time.DateTime
import org.springframework.stereotype.Service

/**
 * 接口执行记录服务
 * 
 * 提供接口执行记录的业务逻辑处理。
 * 包括执行记录的查询、统计、删除等功能。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@Service
class ApiInterfaceExecutionRecordService(
    private val apiInterfaceExecutionRecordDao: ApiInterfaceExecutionRecordDao
) {

    fun findPage(queryDto: ApiInterfaceExecutionRecordQueryDto): PageResultDto<ApiInterfaceExecutionRecordDto> {
        val page = apiInterfaceExecutionRecordDao.page(queryDto)
        val recordDtos = page.records.map { convertToDto(it) }
        
        return PageResultDto(
            records = recordDtos,
            total = page.totalRow,
            current = page.pageNumber,
            size = page.pageSize
        )
    }

    /**
     * 根据ID查询执行记录
     * 
     * @param id 执行记录ID
     * @return 执行记录详情
     */
    fun getById(id: Long): ApiInterfaceExecutionRecordDto? {
        val record = apiInterfaceExecutionRecordDao.getById(id)
        return record?.let { convertToDto(it) }
    }

    /**
     * 根据接口ID查询执行记录
     * 
     * @param interfaceId 接口ID
     * @param limit 限制返回数量
     * @return 执行记录列表
     */
    fun findByInterfaceId(interfaceId: Long, limit: Int = 10): List<ApiInterfaceExecutionRecordDto> {
        val records = apiInterfaceExecutionRecordDao.findByInterfaceId(interfaceId, limit)
        return records.map { convertToDto(it) }
    }

    /**
     * 根据执行人ID查询执行记录
     * 
     * @param executorId 执行人ID
     * @param limit 限制返回数量
     * @return 执行记录列表
     */
    fun findByExecutorId(executorId: Long, limit: Int = 10): List<ApiInterfaceExecutionRecordDto> {
        val records = apiInterfaceExecutionRecordDao.findByExecutorId(executorId, limit)
        return records.map { convertToDto(it) }
    }

    /**
     * 获取接口执行统计信息
     * 
     * @param interfaceId 接口ID
     * @return 执行统计信息
     */
    fun getExecutionStats(interfaceId: Long): ApiInterfaceExecutionRecordStatsDto? {
        val stats = apiInterfaceExecutionRecordDao.getExecutionStats(interfaceId)
        return stats
    }

    /**
     * 根据时间范围统计执行记录数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行记录数量
     */
    fun countByTimeRange(startTime: Long, endTime: Long): Long {
        val startDateTime = DateTime(startTime)
        val endDateTime = DateTime(endTime)
        return apiInterfaceExecutionRecordDao.countByTimeRange(startDateTime, endDateTime)
    }

    /**
     * 删除指定时间之前的执行记录
     * 
     * @param beforeTime 指定时间
     * @return 删除的记录数量
     */
    fun deleteByTimeBefore(beforeTime: Long): Int {
        val beforeDateTime = DateTime(beforeTime)
        return apiInterfaceExecutionRecordDao.deleteByTimeBefore(beforeDateTime)
    }

    /**
     * 将实体转换为DTO
     * 
     * @param record 执行记录实体
     * @return 执行记录DTO
     */
    private fun convertToDto(record: ApiInterfaceExecutionRecord): ApiInterfaceExecutionRecordDto {
        return ApiInterfaceExecutionRecordDto(
            id = record.id,
            interfaceId = record.interfaceId,
            executorId = record.executorId,
            executorName = record.executorName,
            requestParams = record.requestParams,
            requestHeaders = record.requestHeaders,
            requestBody = record.requestBody,
            responseStatus = record.responseStatus,
            responseHeaders = record.responseHeaders,
            responseBody = record.responseBody,
            executionTime = record.executionTime,
            success = record.success,
            errorMessage = record.errorMessage,
            clientIp = record.clientIp,
            userAgent = record.userAgent,
            createTime = DateTimeUtil.formatDateTime(record.createTime),
            updateTime = DateTimeUtil.formatDateTime(record.updateTime)
        )
    }
}
