package io.infra.qk.dto

/**
 * 用户创建/更新 DTO
 */
data class UserFormDto(
    val username: String,
    val email: String?,
    val phone: String?,
    val password: String?,
    val roleIds: List<Long>
)

/**
 * 用户查询 DTO
 */
data class UserQueryDto(
    val username: String? = null,
    val status: String? = null,
    val current: Int = 1,
    val size: Int = 10
)

/**
 * 分页结果 DTO
 */
data class PageResultDto<T>(
    val records: List<T>,
    val total: Long,
    val current: Int,
    val size: Int
)
