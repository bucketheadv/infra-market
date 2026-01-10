package io.infra.market.vertx.dto

/**
 * 通用API响应DTO
 */
data class ApiData<T>(
    val code: Int = 200,
    val message: String = "success",
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
 */
data class PageResultDto<T>(
    val records: List<T>,
    val total: Long,
    val page: Int,
    val size: Int
)

/**
 * 批量操作请求DTO
 */
data class BatchRequest(
    val ids: List<Long>
)

/**
 * 状态更新DTO
 */
data class StatusUpdateDto(
    val status: String
)

