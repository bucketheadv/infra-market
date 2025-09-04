package io.infra.market.dto

import io.infra.market.enums.HttpMethodEnum
import io.infra.market.enums.ParamTypeEnum
import io.infra.market.enums.DataTypeEnum
import io.infra.market.enums.InputTypeEnum
import io.infra.market.enums.PostTypeEnum
import java.util.Date

/**
 * 接口管理DTO
 */
data class ApiInterfaceDto(
    var id: Long? = null,
    var name: String? = null,
    var method: HttpMethodEnum? = null,
    var url: String? = null,
    var description: String? = null,
    var status: Int? = null,
    var createTime: Date? = null,
    var updateTime: Date? = null,
    var postType: PostTypeEnum? = null,
    var urlParams: List<ApiParamDto>? = null,
    var headerParams: List<ApiParamDto>? = null,
    var bodyParams: List<ApiParamDto>? = null
)

/**
 * 下拉选项DTO
 */
data class SelectOptionDto(
    var value: String? = null,
    var label: String? = null
)

/**
 * 接口参数DTO
 */
data class ApiParamDto(
    var name: String? = null,
    var chineseName: String? = null,
    var paramType: ParamTypeEnum? = null,
    var inputType: InputTypeEnum? = null,
    var dataType: DataTypeEnum? = null,
    var required: Boolean? = null,
    var defaultValue: Any? = null,
    var changeable: Boolean? = null,
    var options: List<SelectOptionDto>? = null,
    var description: String? = null,
    var sort: Int? = null
)

/**
 * 接口表单DTO
 */
data class ApiInterfaceFormDto(
    var id: Long? = null,
    var name: String? = null,
    var method: HttpMethodEnum? = null,
    var url: String? = null,
    var description: String? = null,
    var postType: PostTypeEnum? = null,
    var urlParams: List<ApiParamDto>? = null,
    var headerParams: List<ApiParamDto>? = null,
    var bodyParams: List<ApiParamDto>? = null
)

/**
 * 接口查询DTO
 */
data class ApiInterfaceQueryDto(
    var name: String? = null,
    var method: HttpMethodEnum? = null,
    var status: Int? = null,
    var page: Int? = null,
    var size: Int? = null
)

/**
 * 接口执行请求DTO
 */
data class ApiExecuteRequestDto(
    var interfaceId: Long? = null,
    var headers: Map<String, String>? = null,
    var urlParams: Map<String, Any>? = null,
    var bodyParams: Map<String, Any>? = null
)

/**
 * 接口执行响应DTO
 */
data class ApiExecuteResponseDto(
    var status: Int? = null,
    var headers: Map<String, String>? = null,
    var body: String? = null,
    var responseTime: Long? = null,
    var success: Boolean? = null,
    var error: String? = null
)
