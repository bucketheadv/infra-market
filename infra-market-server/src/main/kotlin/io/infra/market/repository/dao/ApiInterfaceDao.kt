package io.infra.market.repository.dao

import io.infra.market.repository.entity.ApiInterface
import io.infra.market.repository.mapper.ApiInterfaceMapper
import io.infra.market.dto.ApiInterfaceQueryDto
import com.mybatisflex.core.query.QueryWrapper
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.spring.service.impl.ServiceImpl
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

        query.name?.let {
            queryWrapper.where(ApiInterface::name.like("%$it%"))
        }

        query.method?.let {
            queryWrapper.where(ApiInterface::method.eq(it.code))
        }

        query.status?.let {
            queryWrapper.where(ApiInterface::status.eq(it))
        }

        query.environment?.let {
            queryWrapper.where(ApiInterface::environment.eq(it.code))
        }

        queryWrapper.orderBy("create_time DESC")

        return mapper.selectListByQuery(queryWrapper)
    }
}
