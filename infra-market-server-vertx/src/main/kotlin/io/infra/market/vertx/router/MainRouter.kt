package io.infra.market.vertx.router

import com.google.inject.Inject
import io.infra.market.vertx.service.ApiInterfaceExecutionRecordService
import io.infra.market.vertx.service.ApiInterfaceService
import io.infra.market.vertx.service.AuthService
import io.infra.market.vertx.service.DashboardService
import io.infra.market.vertx.service.PermissionService
import io.infra.market.vertx.service.RoleService
import io.infra.market.vertx.service.UserService
import io.infra.market.vertx.exception.GlobalExceptionHandler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

/**
 * 主路由器
 * 
 * 使用 Guice 进行依赖注入，所有 Service 通过构造函数注入。
 */
class MainRouter @Inject constructor(
    private val vertx: Vertx,
    private val authService: AuthService,
    private val userService: UserService,
    private val roleService: RoleService,
    private val permissionService: PermissionService,
    private val apiInterfaceService: ApiInterfaceService,
    private val apiInterfaceExecutionRecordService: ApiInterfaceExecutionRecordService,
    private val dashboardService: DashboardService
) {
    
    fun create(): Router {
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
        
        // 注册全局异常处理器（必须在路由注册之前）
        GlobalExceptionHandler.register(router)
        
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

