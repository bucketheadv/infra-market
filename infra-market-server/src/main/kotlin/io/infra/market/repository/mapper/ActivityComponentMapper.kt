package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.ActivityComponent
import org.apache.ibatis.annotations.Mapper

/**
 * 活动组件Mapper
 */
@Mapper
interface ActivityComponentMapper : BaseMapper<ActivityComponent>
