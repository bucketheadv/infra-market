package io.infra.market.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * 状态更新DTO
 */
data class StatusUpdateDto(
    @field:NotBlank(message = "状态值不能为空")
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String
)
