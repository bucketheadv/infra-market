package io.infra.market.enums

/**
 * 错误消息枚举
 * 
 * 用于统一管理错误消息，避免硬编码
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
enum class ErrorMessageEnum(val message: String) {
    // 参数校验相关
    VALIDATION_FAILED("参数校验失败"),
    PARAM_TYPE_ERROR("参数类型错误"),
    
    // 系统错误相关
    SYSTEM_INTERNAL_ERROR("系统内部错误"),
    SYSTEM_ERROR("系统异常"),
    UNKNOWN_ERROR("未知错误"),
    
    // 权限相关
    PERMISSION_DENIED("权限不足，无法访问此功能"),
    
    // 认证相关
    LOGIN_EXPIRED("登录已过期，请重新登录"),
    
    // 资源相关
    RESOURCE_NOT_FOUND("请求的资源不存在"),
    
    // 网络相关
    NETWORK_ERROR("网络错误，请检查网络连接"),
    REQUEST_TIMEOUT("请求超时，请检查网络连接或增加超时时间");
    
    companion object {
        /**
         * 根据参数名和期望类型生成参数类型错误消息
         */
        fun getParamTypeErrorMessage(paramName: String, expectedType: String?): String {
            return "${PARAM_TYPE_ERROR.message}，参数 '$paramName' 类型错误，期望类型: $expectedType"
        }
    }
}
