package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.Role
import org.apache.ibatis.annotations.Mapper

/**
 * 角色Mapper接口
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Mapper
interface RoleMapper : BaseMapper<Role>
