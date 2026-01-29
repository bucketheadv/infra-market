package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.Activity
import org.apache.ibatis.annotations.Mapper

/**
 * 活动Mapper
 */
@Mapper
interface ActivityMapper : BaseMapper<Activity>
