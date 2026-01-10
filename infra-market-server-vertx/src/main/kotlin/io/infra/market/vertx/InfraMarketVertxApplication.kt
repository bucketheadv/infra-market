package io.infra.market.vertx

import io.infra.market.vertx.config.AppConfig
import io.infra.market.vertx.config.ConfigLoader
import io.infra.market.vertx.config.DatabaseConfig
import io.infra.market.vertx.config.RedisConfig
import io.infra.market.vertx.config.ServerConfig
import io.infra.market.vertx.router.MainRouter
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
            val config = loadConfig()
            logger.info("配置加载完成，耗时: {} ms", System.currentTimeMillis() - configStartTime)
            
            // 初始化应用配置管理器
            val appConfigStartTime = System.currentTimeMillis()
            logger.info("正在初始化应用配置...")
            AppConfig.init(config)
            logger.info("应用配置初始化完成，耗时: {} ms", System.currentTimeMillis() - appConfigStartTime)
            
            // 初始化数据库连接池
            val dbPoolStartTime = System.currentTimeMillis()
            logger.info("正在初始化数据库连接池...")
            val dbPool = createDatabasePool(config)
            logger.info("数据库连接池初始化完成，耗时: {} ms", System.currentTimeMillis() - dbPoolStartTime)
            
            // 初始化 Redis 客户端
            val redisStartTime = System.currentTimeMillis()
            logger.info("正在初始化 Redis 客户端...")
            val redis = createRedisClient(config)
            val redisAPI = RedisAPI.api(redis)
            logger.info("Redis 客户端初始化完成，耗时: {} ms", System.currentTimeMillis() - redisStartTime)
            
            // 创建主路由器
            val routerStartTime = System.currentTimeMillis()
            logger.info("正在创建主路由器...")
            val router = MainRouter.create(vertx, dbPool, redisAPI)
            logger.info("主路由器创建完成，耗时: {} ms", System.currentTimeMillis() - routerStartTime)
            
            // 启动 HTTP 服务器
            val serverConfig = ServerConfig.fromJson(config.getJsonObject("server", JsonObject()))
            logger.info("正在启动 HTTP 服务器，端口: {}", serverConfig.port)
            vertx.createHttpServer()
                .requestHandler(router)
                .listen(serverConfig.port)
                .awaitForResult()
            
            val elapsedTime = System.currentTimeMillis() - startTime
            logger.info("========================================")
            logger.info("Infra Market Vert.x 服务器启动成功！")
            logger.info("端口: {}", serverConfig.port)
            logger.info("启动耗时: {} ms ({} 秒)", elapsedTime, String.format("%.2f", elapsedTime / 1000.0))
            logger.info("========================================")
        } catch (e: Exception) {
            logger.error("应用启动失败", e)
            throw e
        }
    }
    
    private fun loadConfig(): JsonObject {
        // 优先使用 Vert.x 部署配置（如果通过部署配置传入）
        val vertxConfig = config
        
        // 从配置文件加载
        val fileConfig = ConfigLoader.loadConfig(vertxConfig)
        
        // 如果 Vert.x 配置存在，则合并（Vert.x 配置优先级更高）
        return if (vertxConfig.size() > 0) {
            logger.info("使用 Vert.x 部署配置，并合并配置文件")
            mergeConfig(fileConfig, vertxConfig)
        } else {
            logger.info("使用配置文件")
            fileConfig
        }
    }
    
    /**
     * 合并配置，Vert.x 配置优先级更高
     */
    private fun mergeConfig(fileConfig: JsonObject, vertxConfig: JsonObject): JsonObject {
        val merged = JsonObject(fileConfig.encode())
        
        // 合并各个配置段
        if (vertxConfig.containsKey("server")) {
            merged.put("server", vertxConfig.getJsonObject("server"))
        }
        if (vertxConfig.containsKey("database")) {
            merged.put("database", vertxConfig.getJsonObject("database"))
        }
        if (vertxConfig.containsKey("redis")) {
            merged.put("redis", vertxConfig.getJsonObject("redis"))
        }
        if (vertxConfig.containsKey("jwt")) {
            merged.put("jwt", vertxConfig.getJsonObject("jwt"))
        }
        if (vertxConfig.containsKey("aes")) {
            merged.put("aes", vertxConfig.getJsonObject("aes"))
        }
        
        return merged
    }
    
    private fun createDatabasePool(config: JsonObject): Pool {
        val dbConfig = DatabaseConfig.fromJson(config.getJsonObject("database"))
        
        val connectOptions = MySQLConnectOptions()
            .setHost(dbConfig.host)
            .setPort(dbConfig.port)
            .setDatabase(dbConfig.database)
            .setUser(dbConfig.username)
            .setPassword(dbConfig.password)
            .setCharset(dbConfig.charset)
            .setCollation(dbConfig.collation)
        
        val poolOptions = PoolOptions()
            .setMaxSize(dbConfig.maxPoolSize)
        
        return MySQLBuilder.pool()
            .with(poolOptions)
            .connectingTo(connectOptions)
            .using(vertx)
            .build()
    }
    
    private fun createRedisClient(config: JsonObject): Redis {
        val redisConfig = RedisConfig.fromJson(config.getJsonObject("redis"))
        
        return Redis.createClient(vertx, "redis://${redisConfig.host}:${redisConfig.port}")
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(InfraMarketVertxApplication::class.java)
    val mainStartTime = System.currentTimeMillis()
    logger.info("正在启动 Infra Market Vert.x 应用...")
    
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

