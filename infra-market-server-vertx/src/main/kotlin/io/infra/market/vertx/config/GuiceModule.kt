package io.infra.market.vertx.config

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.infra.market.vertx.extensions.SqlLoggingPool
import io.vertx.core.Vertx
import io.vertx.mysqlclient.MySQLBuilder
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import org.slf4j.LoggerFactory

/**
 * Guice 模块配置
 * 
 * 配置所有依赖注入的绑定关系，包括数据库连接池、Redis 客户端、DAO 和 Service。
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
class GuiceModule(
    private val vertx: Vertx,
    private val appConfig: ApplicationConfig
) : AbstractModule() {

    private val logger = LoggerFactory.getLogger(GuiceModule::class.java)

    override fun configure() {
        // 绑定 Vertx 实例
        bind(Vertx::class.java).toInstance(vertx)
        
        // 绑定应用配置
        bind(ApplicationConfig::class.java).toInstance(appConfig)
        
        // DAO 和 Service 类已使用 @Singleton 注解，Guice 会自动识别并创建单例
        // 无需在此处显式绑定
    }

    /**
     * 提供数据库连接池实例
     */
    @Provides
    @Singleton
    fun provideDatabasePool(): Pool {
        logger.info("正在创建数据库连接池...")
        val dbConfig = appConfig.database
        
        val database = dbConfig.database ?: throw IllegalStateException("数据库名称未配置")
        val username = dbConfig.username ?: throw IllegalStateException("数据库用户名未配置")
        val password = dbConfig.password ?: throw IllegalStateException("数据库密码未配置")
        
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

    /**
     * 提供 Redis 客户端实例
     */
    @Provides
    @Singleton
    fun provideRedis(): Redis {
        logger.info("正在创建 Redis 客户端...")
        val redisConfig = appConfig.redis
        return Redis.createClient(vertx, "redis://${redisConfig.host}:${redisConfig.port}")
    }

    /**
     * 提供 RedisAPI 实例
     */
    @Provides
    @Singleton
    fun provideRedisAPI(redis: Redis): RedisAPI {
        return RedisAPI.api(redis)
    }
}

