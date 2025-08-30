package io.infra.market.dto

/**
 * 通用响应DTO
 */
data class ApiResponse<T>(
    val code: Int = 200,
    val message: String = "success",
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(data = data)
        fun <T> success(): ApiResponse<T> = ApiResponse()
        fun <T> error(message: String, code: Int = 500): ApiResponse<T> = ApiResponse(code = code, message = message)
    }
}

/**
 * 批量操作DTO
 */
data class BatchRequest(
    val ids: List<Long>
)
