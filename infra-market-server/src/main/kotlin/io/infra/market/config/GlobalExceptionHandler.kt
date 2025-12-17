package io.infra.market.config

import io.infra.market.dto.ApiData
import io.infra.market.enums.ErrorMessageEnum
import io.infra.structure.core.utils.Loggable
import org.springframework.http.HttpStatus
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
class GlobalExceptionHandler : Loggable {
    
    /**
     * 处理参数校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ApiData<Nothing> {
        val errors = ex.bindingResult.fieldErrors.stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ))
        
        val errorMessage = errors.values.joinToString("; ")
        log.warn("参数校验失败: {}", errorMessage)
        
        return ApiData.error(
            ErrorMessageEnum.VALIDATION_FAILED.message, 
            errorMessage, 
            HttpStatus.BAD_REQUEST.value()
        )
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ApiData<Nothing> {
        val errorMessage = ErrorMessageEnum.getParamTypeErrorMessage(
            ex.name, 
            ex.requiredType?.simpleName
        )
        log.warn("参数类型错误: {}", errorMessage)
        
        return ApiData.error(
            ErrorMessageEnum.PARAM_TYPE_ERROR.message, 
            errorMessage, 
            HttpStatus.BAD_REQUEST.value()
        )
    }
    
    /**
     * 处理其他运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ApiData<Nothing> {
        log.error("运行时异常", ex)
        
        return ApiData.error(
            ErrorMessageEnum.SYSTEM_INTERNAL_ERROR.message, 
            ex.message ?: ErrorMessageEnum.UNKNOWN_ERROR.message, 
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ApiData<Nothing> {
        log.error("系统异常", ex)
        
        return ApiData.error(
            ErrorMessageEnum.SYSTEM_ERROR.message, 
            ex.message ?: ErrorMessageEnum.UNKNOWN_ERROR.message, 
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
    }
}
