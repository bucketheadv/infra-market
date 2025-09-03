package io.infra.market.config

import io.infra.market.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.stream.Collectors

/**
 * 全局异常处理器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 处理参数校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors.stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ))
        
        val errorMessage = errors.values.joinToString("; ")
        logger.warn("参数校验失败: {}", errorMessage)
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("参数校验失败", errorMessage, 400))
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        val errorMessage = "参数 '${ex.name}' 类型错误，期望类型: ${ex.requiredType?.simpleName}"
        logger.warn("参数类型错误: {}", errorMessage)
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("参数类型错误", errorMessage, 400))
    }
    
    /**
     * 处理其他运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("运行时异常", ex)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("系统内部错误", ex.message ?: "未知错误"))
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("系统异常", ex)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("系统异常", ex.message ?: "未知错误"))
    }
}
