package io.infra.market.repository.mapper

import com.mybatisflex.core.BaseMapper
import io.infra.market.repository.entity.RolePermission
import org.apache.ibatis.annotations.Mapper

/**
 * 角色权限关联Mapper接口
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Mapper
interface RolePermissionMapper : BaseMapper<RolePermission>
