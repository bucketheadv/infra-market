package io.infra.market.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * 状态更新DTO
 * 
 * 用于批量更新实体状态的请求参数。
 * 支持将实体的状态更新为active或inactive。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class StatusUpdateDto(
    /**
     * 状态值
     * 要更新的状态，只能是active（激活）或inactive（未激活）
     */
    @field:NotBlank(message = "状态值不能为空")
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String
)
