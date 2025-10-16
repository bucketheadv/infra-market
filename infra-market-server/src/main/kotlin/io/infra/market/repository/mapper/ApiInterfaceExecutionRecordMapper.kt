package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.ApiInterfaceExecutionRecord
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

/**
 * 接口执行记录Mapper接口
 * 
 * 提供接口执行记录的数据访问方法。
 * 继承MyBatis-Flex的BaseMapper，提供基础的CRUD操作。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@Mapper
interface ApiInterfaceExecutionRecordMapper : BaseMapper<ApiInterfaceExecutionRecord> {
    
    /**
     * 查询最近最热门的接口ID列表
     * 
     * @param startTime 开始时间（毫秒时间戳）
     * @param limit 返回的接口数量
     * @return 接口ID列表
     */
    @Select("""
        SELECT interface_id
        FROM api_interface_execution_record
        WHERE create_time >= #{startTime}
        AND interface_id IS NOT NULL
        GROUP BY interface_id
        ORDER BY COUNT(*) DESC
        LIMIT #{limit}
    """)
    fun findMostUsedInterfaceIds(
        @Param("startTime") startTime: Long,
        @Param("limit") limit: Int
    ): List<Long>
}
