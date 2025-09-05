package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.ApiInterfaceExecutionRecord
import org.apache.ibatis.annotations.Mapper

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
interface ApiInterfaceExecutionRecordMapper : BaseMapper<ApiInterfaceExecutionRecord>
