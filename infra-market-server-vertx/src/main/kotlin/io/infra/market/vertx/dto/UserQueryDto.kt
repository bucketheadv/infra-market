package io.infra.market.vertx.dto

/**
 * 用户查询参数DTO
 */
data class UserQueryDto(
    val username: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val size: Int = 10
)

