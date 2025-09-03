package io.infra.qk.dto

/**
 * API 响应 DTO
 * @author liuqinglin
 * Date: 2025/8/30
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(200, "success", data)
        }
        
        fun <T> error(message: String, code: Int = 500): ApiResponse<T> {
            return ApiResponse(code, message, null)
        }
        
        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(code, message, null)
        }
    }
}
