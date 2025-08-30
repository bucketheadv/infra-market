package io.infra.market.dao.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.dao.entity.User
import org.apache.ibatis.annotations.Mapper

/**
 * @author liuqinglin
 * Date: 2025/8/15 19:06
 */
@Mapper
interface UserMapper : BaseMapper<User>