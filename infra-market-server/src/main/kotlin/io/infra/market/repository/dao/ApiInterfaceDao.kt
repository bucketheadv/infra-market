package io.infra.market.repository.dao

import io.infra.market.repository.entity.ApiInterface
import io.infra.market.repository.mapper.ApiInterfaceMapper
import io.infra.market.dto.ApiInterfaceQueryDto
import com.mybatisflex.core.query.QueryWrapper
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.le
import com.mybatisflex.kotlin.extensions.kproperty.isNotNull
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.joda.time.DateTime
import org.springframework.stereotype.Repository

/**
 * 接口管理DAO
 */
@Repository
class ApiInterfaceDao : ServiceImpl<ApiInterfaceMapper, ApiInterface>() {

    /**
     * 分页查询接口
     * 
     * @param query 查询条件
     * @return 分页结果
     */
    fun page(query: ApiInterfaceQueryDto): Page<ApiInterface> {
        val queryBuilder = query()
        
        // 构建查询条件
        queryBuilder.whereWith {
            var condition = ApiInterface::id.isNotNull
            
            // 添加查询条件
            if (!query.name.isNullOrBlank()) {
                condition = condition and ApiInterface::name.like("%${query.name}%")
            }
            
            val method = query.method
            if (method != null) {
                condition = condition and ApiInterface::method.eq(method.code)
            }
            
            if (query.status != null) {
                condition = condition and ApiInterface::status.eq(query.status)
            }
            
            val environment = query.environment
            if (environment != null) {
                condition = condition and ApiInterface::environment.eq(environment.code)
            }
            
            condition
        }
        
        // 按创建时间倒序排序
        queryBuilder.orderBy("create_time DESC")
        
        val page = Page<ApiInterface>(query.page ?: 1, query.size ?: 10)
        return page(page, queryBuilder)
    }
    
    /**
     * 获取指定时间之前的接口总数
     */
    fun countBeforeDate(dateTime: DateTime): Long {
        val queryWrapper = QueryWrapper.create()
            .select()
            .from(ApiInterface::class.java)
            .where(ApiInterface::createTime.le(dateTime.millis))
        
        return mapper.selectCountByQuery(queryWrapper)
    }

    /**
     * 根据接口ID列表查询接口
     * 
     * @param ids 接口ID列表
     * @return 接口列表
     */
    fun findByIds(ids: List<Long>): List<ApiInterface> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        
        val queryWrapper = QueryWrapper.create()
            .select()
            .from(ApiInterface::class.java)
            .where("id IN (${ids.joinToString(",")})")
            .and(ApiInterface::status.eq(1))
        
        return mapper.selectListByQuery(queryWrapper)
    }
}
