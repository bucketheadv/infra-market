package io.infra.market.vertx

import com.google.inject.Guice
import com.google.inject.Inject
import io.infra.market.vertx.config.ApplicationConfig
import io.infra.market.vertx.config.AppConfig
import io.infra.market.vertx.config.ConfigLoader
import io.infra.market.vertx.config.GuiceModule
import io.infra.market.vertx.config.JacksonConfig
import io.infra.market.vertx.router.MainRouter
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * Infra Market Vert.x 主应用类
 * 
 * 规则3：Verticle 必须继承 CoroutineVerticle，而不是 AbstractVerticle
 * 使用 Guice 进行依赖注入。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
class InfraMarketVertxApplication @Inject constructor(
    private val appConfig: ApplicationConfig,
    private val mainRouter: MainRouter
) : CoroutineVerticle() {
    
    private val logger = LoggerFactory.getLogger(InfraMarketVertxApplication::class.java)
    private val startTime = System.currentTimeMillis()
    
    override suspend fun start() {
        logger.info("开始启动 Verticle...")
        try {
            // 初始化应用配置管理器
            val appConfigStartTime = System.currentTimeMillis()
            logger.info("正在初始化应用配置...")
            AppConfig.init(appConfig)
            logger.info("应用配置初始化完成，耗时: {} ms", System.currentTimeMillis() - appConfigStartTime)
            
            // 创建主路由器
            val routerStartTime = System.currentTimeMillis()
            logger.info("正在创建主路由器...")
            val router = mainRouter.create()
            logger.info("主路由器创建完成，耗时: {} ms", System.currentTimeMillis() - routerStartTime)
            
            // 启动 HTTP 服务器
            logger.info("正在启动 HTTP 服务器，端口: {}", appConfig.server.port)
            vertx.createHttpServer()
                .requestHandler(router)
                .listen(appConfig.server.port)
                .awaitForResult()
            
            val elapsedTime = System.currentTimeMillis() - startTime
            logger.info("========================================")
            logger.info("Infra Market Vert.x 服务器启动成功！")
            logger.info("端口: {}", appConfig.server.port)
            logger.info("启动耗时: {} ms ({} 秒)", elapsedTime, String.format("%.2f", elapsedTime / 1000.0))
            logger.info("========================================")
        } catch (e: Exception) {
            logger.error("应用启动失败", e)
            throw e
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(InfraMarketVertxApplication::class.java)
    val mainStartTime = System.currentTimeMillis()
    logger.info("正在启动 Infra Market Vert.x 应用...")
    
    // 初始化 Jackson 配置（必须在创建 Vertx 实例之前）
    JacksonConfig.init()
    
    // 加载配置
    val configStartTime = System.currentTimeMillis()
    logger.info("正在加载配置...")
    val appConfig = ConfigLoader.loadConfig()
    logger.info("配置加载完成，耗时: {} ms", System.currentTimeMillis() - configStartTime)
    
    val vertx = Vertx.vertx()
    
    // 创建 Guice 模块
    val guiceModule = GuiceModule(vertx, appConfig)
    
    // 创建 Guice Injector
    val injector = Guice.createInjector(guiceModule)
    
    // 使用 Guice 创建 Verticle 实例
    val verticle = injector.getInstance(InfraMarketVertxApplication::class.java)
    
    // 部署 Verticle
    vertx.deployVerticle(verticle)
        .onSuccess {
            val totalElapsedTime = System.currentTimeMillis() - mainStartTime
            logger.info("Infra Market Vert.x 应用部署成功，总耗时: {} ms ({} 秒)", 
                totalElapsedTime, String.format("%.2f", totalElapsedTime / 1000.0))
        }
        .onFailure { error ->
            val totalElapsedTime = System.currentTimeMillis() - mainStartTime
            logger.error("Infra Market Vert.x 应用部署失败，耗时: {} ms ({} 秒)", 
                totalElapsedTime, String.format("%.2f", totalElapsedTime / 1000.0), error)
            exitProcess(1)
        }
}

