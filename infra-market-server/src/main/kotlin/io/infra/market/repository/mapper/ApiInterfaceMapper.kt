package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.ApiInterface
import org.apache.ibatis.annotations.Mapper

/**
 * 接口管理Mapper
 */
@Mapper
interface ApiInterfaceMapper : BaseMapper<ApiInterface>
