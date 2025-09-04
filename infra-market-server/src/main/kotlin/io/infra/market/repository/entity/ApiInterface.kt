package io.infra.market.repository.entity

import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.KeyType
import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 接口管理实体
 */
@Table("api_interface")
data class ApiInterface(
    @Id(keyType = KeyType.Auto)
    override var id: Long? = null,
    var name: String? = null,
    var method: String? = null,
    var url: String? = null,
    var description: String? = null,
    var postType: String? = null,
    var params: String? = null,
    var status: Int? = null,
    var tag: String? = null,
) : BaseActiveRecordEntity<ApiInterface, Long>()
