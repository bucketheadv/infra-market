package io.infra.market.repository.dao

import io.infra.market.repository.entity.ApiInterface
import io.infra.market.repository.mapper.ApiInterfaceMapper
import io.infra.market.dto.ApiInterfaceQueryDto
import com.mybatisflex.core.query.QueryWrapper
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.le
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.joda.time.DateTime
import org.springframework.stereotype.Repository

/**
 * 接口管理DAO
 */
@Repository
class ApiInterfaceDao : ServiceImpl<ApiInterfaceMapper, ApiInterface>() {

    fun findByCondition(query: ApiInterfaceQueryDto): List<ApiInterface> {
        val queryWrapper = QueryWrapper.create()
            .select()
            .from(ApiInterface::class.java)

        // 构建查询条件
        if (!query.name.isNullOrBlank()) {
            queryWrapper.where(ApiInterface::name.like("%${query.name}%"))
        }

        val method = query.method
        if (method != null) {
            queryWrapper.where(ApiInterface::method.eq(method.code))
        }

        if (query.status != null) {
            queryWrapper.where(ApiInterface::status.eq(query.status))
        }

        val environment = query.environment
        if (environment != null) {
            queryWrapper.where(ApiInterface::environment.eq(environment.code))
        }

        queryWrapper.orderBy("create_time DESC")
        return mapper.selectListByQuery(queryWrapper)
    }
    
    /**
     * 获取指定时间之前的接口总数
     */
    fun countBeforeDate(dateTime: DateTime): Long {
        val queryWrapper = QueryWrapper.create()
            .select()
            .from(ApiInterface::class.java)
            .where(ApiInterface::createTime.le(dateTime.toDate()))
        
        return mapper.selectCountByQuery(queryWrapper)
    }
}
