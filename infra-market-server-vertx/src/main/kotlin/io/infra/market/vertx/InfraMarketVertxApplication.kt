package io.infra.market.vertx

import io.infra.market.vertx.config.ApplicationConfig
import io.infra.market.vertx.config.AppConfig
import io.infra.market.vertx.config.ConfigLoader
import io.infra.market.vertx.config.DatabaseConfig
import io.infra.market.vertx.config.RedisConfig
import io.infra.market.vertx.config.JacksonConfig
import io.infra.market.vertx.router.MainRouter
import io.infra.market.vertx.extensions.SqlLoggingPool
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.mysqlclient.MySQLBuilder
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * Infra Market Vert.x 主应用类
 * 
 * 规则3：Verticle 必须继承 CoroutineVerticle，而不是 AbstractVerticle
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
class InfraMarketVertxApplication : CoroutineVerticle() {
    
    private val logger = LoggerFactory.getLogger(InfraMarketVertxApplication::class.java)
    private val startTime = System.currentTimeMillis()
    
    override suspend fun start() {
        logger.info("开始启动 Verticle...")
        try {
            // 加载配置
            val configStartTime = System.currentTimeMillis()
            logger.info("正在加载配置...")
            val appConfig = loadConfig()
            logger.info("配置加载完成，耗时: {} ms", System.currentTimeMillis() - configStartTime)
            
            // 初始化应用配置管理器
            val appConfigStartTime = System.currentTimeMillis()
            logger.info("正在初始化应用配置...")
            AppConfig.init(appConfig)
            logger.info("应用配置初始化完成，耗时: {} ms", System.currentTimeMillis() - appConfigStartTime)
            
            // 初始化数据库连接池
            val dbPoolStartTime = System.currentTimeMillis()
            logger.info("正在初始化数据库连接池...")
            val dbPool = createDatabasePool(appConfig.database)
            logger.info("数据库连接池初始化完成，耗时: {} ms", System.currentTimeMillis() - dbPoolStartTime)
            
            // 初始化 Redis 客户端
            val redisStartTime = System.currentTimeMillis()
            logger.info("正在初始化 Redis 客户端...")
            val redis = createRedisClient(appConfig.redis)
            val redisAPI = RedisAPI.api(redis)
            logger.info("Redis 客户端初始化完成，耗时: {} ms", System.currentTimeMillis() - redisStartTime)
            
            // 创建主路由器
            val routerStartTime = System.currentTimeMillis()
            logger.info("正在创建主路由器...")
            val router = MainRouter.create(vertx, dbPool, redisAPI)
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
    
    private fun loadConfig(): ApplicationConfig {
        // 优先使用 Vert.x 部署配置（如果通过部署配置传入）
        val vertxConfigJson = config
        
        // 从配置文件加载
        val fileConfig = ConfigLoader.loadConfig()
        
        // 如果 Vert.x 配置存在，则合并（Vert.x 配置优先级更高）
        return if (vertxConfigJson.size() > 0) {
            logger.info("使用 Vert.x 部署配置，并合并配置文件")
            mergeConfig(fileConfig, vertxConfigJson)
        } else {
            logger.info("使用配置文件")
            fileConfig
        }
    }
    
    /**
     * 合并配置，Vert.x 配置优先级更高
     */
    private fun mergeConfig(fileConfig: ApplicationConfig, vertxConfigJson: JsonObject): ApplicationConfig {
        val mergedJson = JsonObject.mapFrom(fileConfig)
        
        // 合并各个配置段
        if (vertxConfigJson.containsKey("server")) {
            mergedJson.put("server", vertxConfigJson.getJsonObject("server"))
        }
        if (vertxConfigJson.containsKey("database")) {
            mergedJson.put("database", vertxConfigJson.getJsonObject("database"))
        }
        if (vertxConfigJson.containsKey("redis")) {
            mergedJson.put("redis", vertxConfigJson.getJsonObject("redis"))
        }
        if (vertxConfigJson.containsKey("jwt")) {
            mergedJson.put("jwt", vertxConfigJson.getJsonObject("jwt"))
        }
        if (vertxConfigJson.containsKey("aes")) {
            mergedJson.put("aes", vertxConfigJson.getJsonObject("aes"))
        }
        
        return ApplicationConfig.fromJson(mergedJson)
    }
    
    private fun createDatabasePool(dbConfig: DatabaseConfig): Pool {
        val database = dbConfig.database ?: throw IllegalStateException("数据库名称未配置，请在配置文件中设置 database.database")
        val username = dbConfig.username ?: throw IllegalStateException("数据库用户名未配置，请在配置文件中设置 database.username")
        val password = dbConfig.password ?: throw IllegalStateException("数据库密码未配置，请在配置文件中设置 database.password")
        
        val connectOptions = MySQLConnectOptions()
            .setHost(dbConfig.host)
            .setPort(dbConfig.port)
            .setDatabase(database)
            .setUser(username)
            .setPassword(password)
            .setCharset(dbConfig.charset)
            .setCollation(dbConfig.collation)
        
        val poolOptions = PoolOptions()
            .setMaxSize(dbConfig.maxPoolSize)
        
        val pool = MySQLBuilder.pool()
            .with(poolOptions)
            .connectingTo(connectOptions)
            .using(vertx)
            .build()
        
        // 包装 Pool 以支持 SQL 日志记录
        return SqlLoggingPool(pool)
    }
    
    private fun createRedisClient(redisConfig: RedisConfig): Redis {
        return Redis.createClient(vertx, "redis://${redisConfig.host}:${redisConfig.port}")
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(InfraMarketVertxApplication::class.java)
    val mainStartTime = System.currentTimeMillis()
    logger.info("正在启动 Infra Market Vert.x 应用...")
    
    // 初始化 Jackson 配置（必须在创建 Vertx 实例之前）
    JacksonConfig.init()
    
    val vertx = Vertx.vertx()
    vertx.deployVerticle(InfraMarketVertxApplication())
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

