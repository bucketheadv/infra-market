package io.infra.market.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

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
        fun <T> error(message: String, detail: String, code: Int = 500) = ApiResponse(code = code, message = message, data = detail as T?)
    }
}

/**
 * 批量操作DTO
 */
data class BatchRequest(
    @field:NotEmpty(message = "ID列表不能为空")
    val ids: List<Long>
)

/**
 * 修改密码DTO
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "原密码不能为空")
    @field:Size(min = 6, max = 20, message = "原密码长度必须在6-20个字符之间")
    val oldPassword: String,
    
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    val newPassword: String
)
