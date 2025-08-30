package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.UserRole
import org.apache.ibatis.annotations.Mapper

/**
 * 用户角色关联Mapper接口
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Mapper
interface UserRoleMapper : BaseMapper<UserRole>
