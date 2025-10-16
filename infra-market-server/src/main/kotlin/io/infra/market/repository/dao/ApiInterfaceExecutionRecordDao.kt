package io.infra.market.repository.dao

import com.mybatisflex.spring.service.impl.ServiceImpl
import com.mybatisflex.core.query.QueryWrapper
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.ge
import com.mybatisflex.kotlin.extensions.kproperty.le
import com.mybatisflex.kotlin.extensions.kproperty.lt
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.isNotNull
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.core.paginate.Page
import io.infra.market.dto.ApiInterfaceExecutionRecordQueryDto
import io.infra.market.dto.ApiInterfaceExecutionRecordStatsDto
import io.infra.market.repository.entity.ApiInterfaceExecutionRecord
import io.infra.market.repository.mapper.ApiInterfaceExecutionRecordMapper
import io.infra.market.util.TimeUtil
import org.springframework.stereotype.Repository
import org.joda.time.DateTime

/**
 * 接口执行记录DAO类
 * 
 * 提供接口执行记录的数据访问方法。
 * 继承ServiceImpl，提供基础的CRUD操作和自定义查询方法。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@Repository
class ApiInterfaceExecutionRecordDao : ServiceImpl<ApiInterfaceExecutionRecordMapper, ApiInterfaceExecutionRecord>() {

    /**
     * 分页查询执行记录
     * 
     * @param queryDto 查询条件
     * @return 分页结果
     */
    fun page(queryDto: ApiInterfaceExecutionRecordQueryDto): Page<ApiInterfaceExecutionRecord> {
        val queryBuilder = query()
        
        // 构建查询条件
        queryBuilder.whereWith {
            var condition = ApiInterfaceExecutionRecord::id.isNotNull
            
            // 添加查询条件
            if (queryDto.interfaceId != null) {
                condition = condition and ApiInterfaceExecutionRecord::interfaceId.eq(queryDto.interfaceId)
            }
            
            if (queryDto.executorId != null) {
                condition = condition and ApiInterfaceExecutionRecord::executorId.eq(queryDto.executorId)
            }
            
            if (!queryDto.executorName.isNullOrBlank()) {
                condition = condition and ApiInterfaceExecutionRecord::executorName.like("%${queryDto.executorName}%")
            }
            
            if (queryDto.success != null) {
                condition = condition and ApiInterfaceExecutionRecord::success.eq(queryDto.success)
            }
            
            val minExecutionTime = queryDto.minExecutionTime
            if (minExecutionTime != null) {
                condition = condition and ApiInterfaceExecutionRecord::executionTime.ge(minExecutionTime)
            }
            
            val maxExecutionTime = queryDto.maxExecutionTime
            if (maxExecutionTime != null) {
                condition = condition and ApiInterfaceExecutionRecord::executionTime.le(maxExecutionTime)
            }
            
            val startTime = queryDto.startTime
            if (startTime != null) {
                condition = condition and ApiInterfaceExecutionRecord::createTime.ge(startTime.millis)
            }
            
            val endTime = queryDto.endTime
            if (endTime != null) {
                condition = condition and ApiInterfaceExecutionRecord::createTime.le(endTime.millis)
            }
            
            condition
        }
        
        // 按创建时间倒序排序
        queryBuilder.orderBy("create_time DESC")
        
        val page = Page<ApiInterfaceExecutionRecord>(queryDto.page ?: 1, queryDto.size ?: 10)
        return page(page, queryBuilder)
    }

    /**
     * 根据执行人ID查询执行记录
     * 
     * @param executorId 执行人ID
     * @param limit 限制返回数量
     * @return 执行记录列表
     */
    fun findByExecutorId(executorId: Long, limit: Int = 10): List<ApiInterfaceExecutionRecord> {
        val query = QueryWrapper.create()
            .select()
            .from(ApiInterfaceExecutionRecord::class.java)
            .where(ApiInterfaceExecutionRecord::executorId.eq(executorId))
            .orderBy("create_time DESC")
            .limit(limit)

        return mapper.selectListByQuery(query)
    }

    /**
     * 统计接口执行记录
     * 
     * @param interfaceId 接口ID
     * @return 统计信息
     */
    fun getExecutionStats(interfaceId: Long): ApiInterfaceExecutionRecordStatsDto? {
        val query = QueryWrapper.create()
            .select()
            .from(ApiInterfaceExecutionRecord::class.java)
            .where(ApiInterfaceExecutionRecord::interfaceId.eq(interfaceId))

        val records = mapper.selectListByQuery(query)
        
        if (records.isEmpty()) {
            return null
        }

        val totalExecutions = records.size.toLong()
        val successExecutions = records.count { it.success == true }.toLong()
        val failedExecutions = totalExecutions - successExecutions
        val successRate = if (totalExecutions > 0) (successExecutions.toDouble() / totalExecutions) * 100 else 0.0

        val executionTimes = records.mapNotNull { it.executionTime }
        val avgExecutionTime = if (executionTimes.isNotEmpty()) {
            executionTimes.average()
        } else 0.0

        val minExecutionTime = executionTimes.minOrNull() ?: 0L
        val maxExecutionTime = executionTimes.maxOrNull() ?: 0L
        val lastExecutionTime = records.maxByOrNull { it.createTime ?: 0L }?.createTime?.let { 
            TimeUtil.format(it)
        }

        return ApiInterfaceExecutionRecordStatsDto(
            interfaceId = interfaceId,
            totalExecutions = totalExecutions,
            successExecutions = successExecutions,
            failedExecutions = failedExecutions,
            successRate = successRate,
            avgExecutionTime = avgExecutionTime,
            minExecutionTime = minExecutionTime,
            maxExecutionTime = maxExecutionTime,
            lastExecutionTime = lastExecutionTime
        )
    }

    /**
     * 根据时间范围统计执行记录数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行记录数量
     */
    fun countByTimeRange(startTime: DateTime, endTime: DateTime): Long {
        val query = QueryWrapper.create()
            .select()
            .from(ApiInterfaceExecutionRecord::class.java)
            .where(ApiInterfaceExecutionRecord::createTime.ge(startTime.millis))
            .where(ApiInterfaceExecutionRecord::createTime.le(endTime.millis))

        return mapper.selectCountByQuery(query)
    }

    /**
     * 删除指定时间之前的执行记录
     * 
     * @param beforeTime 指定时间
     * @return 删除的记录数量
     */
    fun deleteByTimeBefore(beforeTime: DateTime): Int {
        val query = QueryWrapper.create()
            .from(ApiInterfaceExecutionRecord::class.java)
            .where(ApiInterfaceExecutionRecord::createTime.lt(beforeTime.millis))

        return mapper.deleteByQuery(query)
    }

    /**
     * 查询最近最热门的接口ID列表
     * 
     * 根据最近一定时间内的执行次数，统计使用最频繁的接口。
     * 使用Mapper接口中定义的自定义SQL查询方法。
     * 
     * @param days 统计最近多少天的数据，默认30天
     * @param limit 返回的接口数量，默认5个
     * @return 接口ID列表，按执行次数倒序排列
     */
    fun findMostUsedInterfaceIds(days: Int = 30, limit: Int = 5): List<Long> {
        val startTime = DateTime.now().minusDays(days).millis
        return mapper.findMostUsedInterfaceIds(startTime, limit)
    }
}
