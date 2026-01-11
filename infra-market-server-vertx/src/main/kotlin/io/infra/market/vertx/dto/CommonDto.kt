package io.infra.market.vertx.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

/**
 * 通用API响应DTO
 * 
 * 用于封装所有API接口的统一响应格式。
 * 包含状态码、消息和数据三个字段。
 * 
 * @param T 响应数据的类型
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiData<T>(
    /**
     * 状态码
     * HTTP状态码，200表示成功，其他值表示失败
     */
    val code: Int = 200,
    
    /**
     * 消息
     * 响应消息，成功时通常为"success"，失败时为错误描述
     */
    val message: String = "success",
    
    /**
     * 数据
     * 响应数据，类型由泛型T决定，可以为null
     */
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): ApiData<T> = ApiData(data = data)
        fun <T> success(): ApiData<T> = ApiData()
        fun <T> error(message: String, code: Int = 500): ApiData<T> = ApiData(code = code, message = message)
    }
}

/**
 * 分页结果DTO
 * 
 * 用于封装分页查询的结果数据。
 * 包含记录列表、总数、当前页和每页大小等信息。
 * 
 * @param T 记录数据的类型
 * @author liuqinglin
 * @since 1.0.0
 */
data class PageResultDto<T>(
    /**
     * 记录列表
     * 当前页的数据记录列表
     */
    val records: List<T>,
    
    /**
     * 总记录数
     * 符合查询条件的总记录数
     */
    val total: Long,
    
    /**
     * 页码
     * 当前查询的页码
     */
    val page: Int,
    
    /**
     * 每页大小
     * 每页显示的记录数
     */
    val size: Int
)

/**
 * 批量操作请求DTO
 * 
 * 用于批量删除、批量更新等操作的请求参数。
 * 包含要操作的ID列表。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class BatchRequest(
    /**
     * ID列表
     * 要操作的记录ID列表，不能为空
     */
    @field:NotEmpty(message = "ID列表不能为空")
    val ids: List<Long>
)

/**
 * 状态更新DTO
 * 
 * 用于更新记录状态的请求参数。
 * 包含新的状态值。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class StatusUpdateDto(
    /**
     * 状态值
     * 新的状态值，只能是active或inactive
     */
    @field:NotBlank(message = "状态值不能为空")
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String
)
