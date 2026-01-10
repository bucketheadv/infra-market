package io.infra.market.vertx.util

import io.infra.market.vertx.dto.PermissionQueryDto
import io.infra.market.vertx.dto.RoleQueryDto
import io.infra.market.vertx.dto.UserQueryDto
import io.vertx.ext.web.RoutingContext

/**
 * 查询参数工具类
 */
object QueryParamUtil {
    
    /**
     * 从 RoutingContext 获取字符串查询参数
     */
    fun getString(ctx: RoutingContext, key: String): String? {
        val value: String? = ctx.queryParams().get(key)
        return value?.takeIf { it.isNotBlank() }
    }
    
    /**
     * 从 RoutingContext 获取整数查询参数
     */
    fun getInt(ctx: RoutingContext, key: String, default: Int? = null): Int? {
        val value = ctx.queryParams().get(key)?.toIntOrNull()
        return value ?: default
    }
    
    /**
     * 从 RoutingContext 获取长整数查询参数
     */
    fun getLong(ctx: RoutingContext, key: String, default: Long? = null): Long? {
        val value = ctx.queryParams().get(key)?.toLongOrNull()
        return value ?: default
    }
    
    /**
     * 从 RoutingContext 获取分页参数
     */
    fun getPage(ctx: RoutingContext, default: Int = 1): Int {
        return getInt(ctx, "page", default) ?: default
    }
    
    /**
     * 从 RoutingContext 获取分页大小参数
     */
    fun getSize(ctx: RoutingContext, default: Int = 10): Int {
        return getInt(ctx, "size", default) ?: default
    }
    
    /**
     * 从 RoutingContext 构建 RoleQueryDto
     */
    fun buildRoleQuery(ctx: RoutingContext): RoleQueryDto {
        return RoleQueryDto(
            name = getString(ctx, "name"),
            code = getString(ctx, "code"),
            status = getString(ctx, "status"),
            page = getPage(ctx),
            size = getSize(ctx)
        )
    }
    
    /**
     * 从 RoutingContext 构建 UserQueryDto
     */
    fun buildUserQuery(ctx: RoutingContext): UserQueryDto {
        return UserQueryDto(
            username = getString(ctx, "username"),
            status = getString(ctx, "status"),
            page = getPage(ctx),
            size = getSize(ctx)
        )
    }
    
    /**
     * 从 RoutingContext 构建 PermissionQueryDto
     */
    fun buildPermissionQuery(ctx: RoutingContext): PermissionQueryDto {
        return PermissionQueryDto(
            name = getString(ctx, "name"),
            code = getString(ctx, "code"),
            type = getString(ctx, "type"),
            status = getString(ctx, "status"),
            page = getPage(ctx),
            size = getSize(ctx)
        )
    }
}

