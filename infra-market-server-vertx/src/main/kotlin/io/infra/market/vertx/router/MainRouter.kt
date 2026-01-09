package io.infra.market.vertx.router

import io.infra.market.vertx.repository.ApiInterfaceDao
import io.infra.market.vertx.repository.ApiInterfaceExecutionRecordDao
import io.infra.market.vertx.repository.PermissionDao
import io.infra.market.vertx.repository.RoleDao
import io.infra.market.vertx.repository.RolePermissionDao
import io.infra.market.vertx.repository.UserDao
import io.infra.market.vertx.repository.UserRoleDao
import io.infra.market.vertx.service.ApiInterfaceExecutionRecordService
import io.infra.market.vertx.service.ApiInterfaceService
import io.infra.market.vertx.service.AuthService
import io.infra.market.vertx.service.DashboardService
import io.infra.market.vertx.service.PermissionService
import io.infra.market.vertx.service.RoleService
import io.infra.market.vertx.service.TokenService
import io.infra.market.vertx.service.UserService
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.sqlclient.Pool
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI

/**
 * 主路由器
 */
object MainRouter {
    
    fun create(vertx: Vertx, dbPool: Pool, redis: Redis, redisAPI: RedisAPI): Router {
        val router = Router.router(vertx)
        
        // CORS 配置
        router.route().handler(
            CorsHandler.create()
                .addOrigin("*")
                .allowedMethods(setOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH))
                .allowedHeaders(setOf("Content-Type", "Authorization"))
        )
        
        // Body 处理
        router.route().handler(BodyHandler.create())
        
        // 初始化 DAO
        val userDao = UserDao(dbPool)
        val userRoleDao = UserRoleDao(dbPool)
        val roleDao = RoleDao(dbPool)
        val rolePermissionDao = RolePermissionDao(dbPool)
        val permissionDao = PermissionDao(dbPool)
        val apiInterfaceDao = ApiInterfaceDao(dbPool)
        val apiInterfaceExecutionRecordDao = ApiInterfaceExecutionRecordDao(dbPool)
        
        // 初始化 Service
        val tokenService = TokenService(redis, redisAPI)
        val authService = AuthService(userDao, userRoleDao, rolePermissionDao, permissionDao, tokenService)
        val userService = UserService(userDao, userRoleDao)
        val dashboardService = DashboardService(userDao, roleDao, permissionDao, apiInterfaceDao)
        
        // 初始化更多 Service
        val roleService = RoleService(roleDao, rolePermissionDao, userRoleDao)
        val permissionService = PermissionService(permissionDao, rolePermissionDao)
        val apiInterfaceService = ApiInterfaceService(apiInterfaceDao, userDao, apiInterfaceExecutionRecordDao, vertx)
        val apiInterfaceExecutionRecordService = ApiInterfaceExecutionRecordService(apiInterfaceExecutionRecordDao)
        
        // 直接注册路由到主路由（路由内部已包含完整路径）
        AuthRouter(authService).mount(router, vertx)
        UserRouter(userService).mount(router, vertx)
        RoleRouter(roleService).mount(router, vertx)
        PermissionRouter(permissionService).mount(router, vertx)
        ApiInterfaceRouter(apiInterfaceService).mount(router, vertx)
        ApiInterfaceExecutionRecordRouter(apiInterfaceExecutionRecordService).mount(router, vertx)
        DashboardRouter(dashboardService).mount(router, vertx)
        
        // 健康检查
        router.get("/health").handler { ctx ->
            ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end("""{"status":"ok"}""")
        }
        
        return router
    }
}

