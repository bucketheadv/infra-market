package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.ActivityTemplate
import org.apache.ibatis.annotations.Mapper

/**
 * 活动模板Mapper
 */
@Mapper
interface ActivityTemplateMapper : BaseMapper<ActivityTemplate>
