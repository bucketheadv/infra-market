package io.infra.market.vertx.dto

import io.infra.market.vertx.enums.EnvironmentEnum
import io.infra.market.vertx.enums.HttpMethodEnum

/**
 * 接口管理DTO
 */
data class ApiInterfaceDto(
    var id: Long? = null,
    var name: String? = null,
    var method: String? = null,
    var url: String? = null,
    var description: String? = null,
    var status: Int? = null,
    var createTime: Long? = null,
    var updateTime: Long? = null,
    var postType: String? = null,
    var environment: String? = null,
    var urlParams: List<ApiParamDto>? = null,
    var headerParams: List<ApiParamDto>? = null,
    var bodyParams: List<ApiParamDto>? = null,
    var timeout: Long? = null,
    var valuePath: String? = null
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
    var paramType: String? = null,
    var inputType: String? = null,
    var dataType: String? = null,
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
    var method: String? = null,
    var url: String? = null,
    var description: String? = null,
    var postType: String? = null,
    var environment: String? = null,
    var urlParams: List<ApiParamDto>? = null,
    var headerParams: List<ApiParamDto>? = null,
    var bodyParams: List<ApiParamDto>? = null,
    var timeout: Long? = null,
    var valuePath: String? = null
)

/**
 * 接口执行请求DTO
 */
data class ApiExecuteRequestDto(
    var interfaceId: Long? = null,
    var headers: Map<String, String>? = null,
    var urlParams: Map<String, Any>? = null,
    var bodyParams: Map<String, Any>? = null,
    var timeout: Long? = null,
    var remark: String? = null
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
    var error: String? = null,
    var extractedValue: String? = null
)

