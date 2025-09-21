package io.infra.market.dto

import io.infra.market.repository.entity.User
import io.infra.market.util.DateTimeUtil
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 登录请求DTO
 * 
 * 用于用户登录认证的请求参数。
 * 包含用户名和密码，用于验证用户身份。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class LoginRequest(
    /**
     * 用户名
     * 用于登录的用户名，长度必须在2-20个字符之间
     */
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    val username: String,
    
    /**
     * 密码
     * 用户登录密码，长度必须在6-20个字符之间
     */
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    val password: String
)

/**
 * 登录响应DTO
 * 
 * 用户登录成功后的响应数据。
 * 包含访问令牌、用户信息和权限列表。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class LoginResponse(
    /**
     * 访问令牌
     * JWT格式的访问令牌，用于后续API调用的身份验证
     */
    val token: String,
    
    /**
     * 用户信息
     * 登录用户的基本信息
     */
    val user: UserDto,
    
    /**
     * 权限列表
     * 用户拥有的权限编码列表
     */
    val permissions: List<String>
)

/**
 * 用户信息DTO
 * 
 * 用于传输用户基本信息的DTO。
 * 包含用户的完整信息，用于前端显示和业务处理。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
data class UserDto(
    /**
     * 用户ID
     * 用户的唯一标识
     */
    val id: Long,
    
    /**
     * 用户名
     * 用户的登录用户名
     */
    val username: String,
    
    /**
     * 邮箱地址
     * 用户的邮箱地址，可以为null
     */
    val email: String?,
    
    /**
     * 手机号码
     * 用户的手机号码，可以为null
     */
    val phone: String?,
    
    /**
     * 用户状态
     * 用户的状态，如active、inactive等
     */
    val status: String,
    
    /**
     * 最后登录时间
     * 用户最后一次登录的时间，格式化的字符串
     */
    val lastLoginTime: String?,
    
    /**
     * 角色ID列表
     * 用户拥有的角色ID列表，默认为空列表
     */
    val roleIds: List<Long> = emptyList(),
    
    /**
     * 创建时间
     * 用户账户的创建时间，格式化的字符串
     */
    val createTime: String,
    
    /**
     * 更新时间
     * 用户信息最后更新的时间，格式化的字符串
     */
    val updateTime: String
) {
    companion object {
        /**
         * 从User实体转换为UserDto
         * 
         * @param user 用户实体
         * @param roleIds 用户角色ID列表，默认为空列表
         * @return UserDto
         */
        fun fromEntity(user: User, roleIds: List<Long> = emptyList()): UserDto {
            return UserDto(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email,
                phone = user.phone,
                status = user.status,
                lastLoginTime = DateTimeUtil.formatDateTime(user.lastLoginTime),
                roleIds = roleIds,
                createTime = DateTimeUtil.formatDateTime(user.createTime),
                updateTime = DateTimeUtil.formatDateTime(user.updateTime)
            )
        }
        
        /**
         * 批量从User实体列表转换为UserDto列表
         * 
         * @param users 用户实体列表
         * @param userRoleMap 用户ID到角色ID列表的映射，默认为空映射
         * @return UserDto列表
         */
        fun fromEntityList(
            users: List<User>,
            userRoleMap: Map<Long, List<Long>> = emptyMap()
        ): List<UserDto> {
            return users.map { user ->
                val roleIds = userRoleMap[user.id] ?: emptyList()
                fromEntity(user, roleIds)
            }
        }
    }
}
