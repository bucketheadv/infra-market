package io.infra.market.repository.dao

import io.infra.market.repository.entity.ActivityComponent
import io.infra.market.repository.mapper.ActivityComponentMapper
import io.infra.market.dto.ActivityComponentQueryDto
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.isNotNull
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.springframework.stereotype.Repository

/**
 * 活动组件DAO
 */
@Repository
class ActivityComponentDao : ServiceImpl<ActivityComponentMapper, ActivityComponent>() {

    /**
     * 分页查询活动组件
     * 
     * @param query 查询条件
     * @return 分页结果
     */
    fun page(query: ActivityComponentQueryDto): Page<ActivityComponent> {
        val queryBuilder = query()
        
        // 构建查询条件
        queryBuilder.whereWith {
            var condition = ActivityComponent::id.isNotNull
            
            // 添加查询条件
            if (!query.name.isNullOrBlank()) {
                condition = condition and ActivityComponent::name.like("%${query.name}%")
            }
            
            if (query.status != null) {
                condition = condition and ActivityComponent::status.eq(query.status)
            }
            
            condition
        }
        
        // 按创建时间倒序排序
        queryBuilder.orderBy("create_time DESC")
        
        val page = Page<ActivityComponent>(query.page ?: 1, query.size ?: 10)
        return page(page, queryBuilder)
    }
    
    override fun list(): List<ActivityComponent> {
        val queryBuilder = query()
        queryBuilder.orderBy("create_time DESC")
        return mapper.selectListByQuery(queryBuilder)
    }
}
