package io.infra.market.vertx.router

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.exception.GlobalExceptionHandler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

/**
 * 主路由器
 * 
 * 使用 Guice 进行依赖注入，所有 Router 和 Service 通过构造函数注入。
 */
@Singleton
class MainRouter @Inject constructor(
    private val vertx: Vertx,
    private val authRouter: AuthRouter,
    private val userRouter: UserRouter,
    private val roleRouter: RoleRouter,
    private val permissionRouter: PermissionRouter,
    private val apiInterfaceRouter: ApiInterfaceRouter,
    private val apiInterfaceExecutionRecordRouter: ApiInterfaceExecutionRecordRouter,
    private val dashboardRouter: DashboardRouter
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
        authRouter.mount(router, vertx)
        userRouter.mount(router, vertx)
        roleRouter.mount(router, vertx)
        permissionRouter.mount(router, vertx)
        apiInterfaceRouter.mount(router, vertx)
        apiInterfaceExecutionRecordRouter.mount(router, vertx)
        dashboardRouter.mount(router, vertx)
        
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

