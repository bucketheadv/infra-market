package io.infra.qk.annotation

/**
 * 权限验证注解
 * 用于标记需要特定权限才能访问的方法
 * 
 * @author liuqinglin
 * Date: 2025/1/27
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresPermission(
    /**
     * 所需权限编码
     */
    val value: String,
    
    /**
     * 权限描述
     */
    val description: String = "",
    
    /**
     * 是否允许多个权限（OR关系）
     */
    val allowMultiple: Boolean = false
)
