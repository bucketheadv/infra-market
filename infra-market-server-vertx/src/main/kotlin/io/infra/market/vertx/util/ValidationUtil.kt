package io.infra.market.vertx.util

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.slf4j.LoggerFactory

/**
 * 参数验证工具类
 * 
 * 提供类似 Spring @Valid 的参数验证功能
 */
object ValidationUtil {
    
    private val logger = LoggerFactory.getLogger(ValidationUtil::class.java)
    
    /**
     * Validator 实例（单例）
     */
    private val validator: Validator by lazy {
        val factory: ValidatorFactory = (jakarta.validation.Validation.byProvider(HibernateValidator::class.java)
            .configure() as HibernateValidatorConfiguration)
            .buildValidatorFactory()
        factory.validator
    }
    
    /**
     * 验证对象
     * 
     * @param obj 要验证的对象
     * @return 验证结果，如果验证通过返回 null，否则返回错误消息
     */
    fun <T> validate(obj: T): String? {
        val violations: Set<ConstraintViolation<T>> = validator.validate(obj)
        
        if (violations.isEmpty()) {
            return null
        }
        
        // 收集所有验证错误
        val errorMessages = violations.map { violation ->
            val fieldName = violation.propertyPath.toString()
            val message = violation.message
            if (fieldName.isNotBlank()) {
                "$fieldName: $message"
            } else {
                message
            }
        }
        
        val errorMessage = errorMessages.joinToString("; ")
        logger.debug("参数验证失败: {}", errorMessage)
        
        return errorMessage
    }
    
    /**
     * 验证对象，如果验证失败则抛出异常
     * 
     * @param obj 要验证的对象
     * @throws IllegalArgumentException 如果验证失败
     */
    fun <T> validateOrThrow(obj: T) {
        val errorMessage = validate(obj)
        if (errorMessage != null) {
            throw IllegalArgumentException(errorMessage)
        }
    }
}

