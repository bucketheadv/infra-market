package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.market.enums.PermissionTypeEnum
import io.infra.market.enums.StatusEnum
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 权限实体类
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Table("permission_info")
data class Permission(
    var name: String? = null,
    var code: String? = null,
    var type: PermissionTypeEnum = PermissionTypeEnum.MENU,
    var parentId: Long? = null,
    var path: String? = null,
    var icon: String? = null,
    var sort: Int = 0,
    var status: StatusEnum = StatusEnum.ACTIVE
) : BaseActiveRecordEntity<Permission, Long>()
