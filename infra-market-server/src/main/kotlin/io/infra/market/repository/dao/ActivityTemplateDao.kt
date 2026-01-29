package io.infra.market.repository.dao

import io.infra.market.repository.entity.ActivityTemplate
import io.infra.market.repository.mapper.ActivityTemplateMapper
import io.infra.market.dto.ActivityTemplateQueryDto
import com.mybatisflex.core.query.QueryWrapper
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.isNotNull
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.springframework.stereotype.Repository

/**
 * 活动模板DAO
 */
@Repository
class ActivityTemplateDao : ServiceImpl<ActivityTemplateMapper, ActivityTemplate>() {

    /**
     * 分页查询活动模板
     * 
     * @param query 查询条件
     * @return 分页结果
     */
    fun page(query: ActivityTemplateQueryDto): Page<ActivityTemplate> {
        val queryBuilder = query()
        
        // 构建查询条件
        queryBuilder.whereWith {
            var condition = ActivityTemplate::id.isNotNull
            
            // 添加查询条件
            if (!query.name.isNullOrBlank()) {
                condition = condition and ActivityTemplate::name.like("%${query.name}%")
            }
            
            if (query.status != null) {
                condition = condition and ActivityTemplate::status.eq(query.status)
            }
            
            condition
        }
        
        // 按创建时间倒序排序
        queryBuilder.orderBy("create_time DESC")
        
        val page = Page<ActivityTemplate>(query.page ?: 1, query.size ?: 10)
        return page(page, queryBuilder)
    }
}
