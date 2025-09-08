package io.infra.market.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 通用API响应DTO
 * 
 * 用于统一API接口的响应格式，包含状态码、消息和数据。
 * 支持泛型，可以携带任意类型的数据。
 * 
 * @param T 响应数据的类型
 * @author liuqinglin
 * @since 1.0.0
 */
data class ApiResponse<T>(
    /**
     * 响应状态码
     * 200表示成功，其他值表示各种错误状态
     * 默认为200
     */
    val code: Int = 200,
    
    /**
     * 响应消息
     * 用于描述操作结果或错误信息
     * 默认为"success"
     */
    val message: String = "success",
    
    /**
     * 响应数据
     * 具体的业务数据，可以为null
     */
    val data: T? = null
) {
    companion object {
        /**
         * 创建成功响应，携带数据
         * @param data 响应数据
         * @return 成功响应对象
         */
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(data = data)
        
        /**
         * 创建成功响应，不携带数据
         * @return 成功响应对象
         */
        fun <T> success(): ApiResponse<T> = ApiResponse()
        
        /**
         * 创建错误响应
         * @param message 错误消息
         * @param code 错误状态码，默认为500
         * @return 错误响应对象
         */
        fun <T> error(message: String, code: Int = 500): ApiResponse<T> = ApiResponse(code = code, message = message)
        
        /**
         * 创建错误响应，携带详细信息
         * @param message 错误消息
         * @param detail 错误详细信息
         * @param code 错误状态码，默认为500
         * @return 错误响应对象
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> error(message: String, detail: String, code: Int = 500) = ApiResponse(code = code, message = message, data = detail as T?)
    }
}

/**
 * 批量操作请求DTO
 * 
 * 用于批量删除、批量更新等操作的请求参数。
 * 包含需要操作的实体ID列表。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class BatchRequest(
    /**
     * 实体ID列表
     * 包含需要批量操作的实体ID，不能为空
     */
    @field:NotEmpty(message = "ID列表不能为空")
    val ids: List<Long>
)

/**
 * 修改密码请求DTO
 * 
 * 用于用户修改密码的请求参数。
 * 包含原密码和新密码，用于验证身份和设置新密码。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class ChangePasswordRequest(
    /**
     * 原密码
     * 用于验证用户身份，长度必须在6-20个字符之间
     */
    @field:NotBlank(message = "原密码不能为空")
    @field:Size(min = 6, max = 20, message = "原密码长度必须在6-20个字符之间")
    val oldPassword: String,
    
    /**
     * 新密码
     * 用户设置的新密码，长度必须在6-20个字符之间
     */
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    val newPassword: String
)
