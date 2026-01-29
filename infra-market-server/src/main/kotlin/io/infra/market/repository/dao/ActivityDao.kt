package io.infra.market.repository.dao

import io.infra.market.repository.entity.Activity
import io.infra.market.repository.mapper.ActivityMapper
import io.infra.market.dto.ActivityQueryDto
import com.mybatisflex.kotlin.extensions.kproperty.eq
import com.mybatisflex.kotlin.extensions.kproperty.like
import com.mybatisflex.kotlin.extensions.kproperty.isNotNull
import com.mybatisflex.kotlin.extensions.wrapper.whereWith
import com.mybatisflex.kotlin.extensions.condition.and
import com.mybatisflex.core.paginate.Page
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.springframework.stereotype.Repository

/**
 * 活动DAO
 */
@Repository
class ActivityDao : ServiceImpl<ActivityMapper, Activity>() {

    /**
     * 分页查询活动
     * 
     * @param query 查询条件
     * @return 分页结果
     */
    fun page(query: ActivityQueryDto): Page<Activity> {
        val queryBuilder = query()
        
        // 构建查询条件
        queryBuilder.whereWith {
            var condition = Activity::id.isNotNull
            
            // 添加查询条件
            if (!query.name.isNullOrBlank()) {
                condition = condition and Activity::name.like("%${query.name}%")
            }
            
            if (query.templateId != null) {
                condition = condition and Activity::templateId.eq(query.templateId)
            }
            
            if (query.status != null) {
                condition = condition and Activity::status.eq(query.status)
            }
            
            condition
        }
        
        // 按创建时间倒序排序
        queryBuilder.orderBy("create_time DESC")
        
        val page = Page<Activity>(query.page ?: 1, query.size ?: 10)
        return page(page, queryBuilder)
    }
}
