package io.infra.market.vertx.config

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.infra.market.vertx.extensions.SqlLoggingPool
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
import io.infra.market.vertx.router.MainRouter
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
        
        // 绑定 DAO（单例）- Guice 会自动识别 @Inject 构造函数，这里只需要指定作用域
        bind(UserDao::class.java).`in`(Singleton::class.java)
        bind(UserRoleDao::class.java).`in`(Singleton::class.java)
        bind(RoleDao::class.java).`in`(Singleton::class.java)
        bind(RolePermissionDao::class.java).`in`(Singleton::class.java)
        bind(PermissionDao::class.java).`in`(Singleton::class.java)
        bind(ApiInterfaceDao::class.java).`in`(Singleton::class.java)
        bind(ApiInterfaceExecutionRecordDao::class.java).`in`(Singleton::class.java)
        
        // 绑定 Service（单例）- Guice 会自动识别 @Inject 构造函数，这里只需要指定作用域
        bind(TokenService::class.java).`in`(Singleton::class.java)
        bind(AuthService::class.java).`in`(Singleton::class.java)
        bind(UserService::class.java).`in`(Singleton::class.java)
        bind(RoleService::class.java).`in`(Singleton::class.java)
        bind(PermissionService::class.java).`in`(Singleton::class.java)
        bind(ApiInterfaceService::class.java).`in`(Singleton::class.java)
        bind(ApiInterfaceExecutionRecordService::class.java).`in`(Singleton::class.java)
        bind(DashboardService::class.java).`in`(Singleton::class.java)
        
        // 绑定 MainRouter（单例）
        bind(MainRouter::class.java).`in`(Singleton::class.java)
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

