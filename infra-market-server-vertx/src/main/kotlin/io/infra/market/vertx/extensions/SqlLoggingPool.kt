package io.infra.market.vertx.extensions

import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.Query
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.TransactionPropagation
import io.vertx.sqlclient.Tuple
import java.util.function.Function
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * SQL 日志记录工具对象
 */
private object SqlLogger {
    private val logger: Logger = LoggerFactory.getLogger("io.infra.market.vertx.sql")
    
    /**
     * 记录 SQL 语句
     */
    fun logSql(sql: String, params: Tuple?) {
        if (logger.isDebugEnabled) {
            val sqlLog = if (params == null || params.size() == 0) {
                sql
            } else {
                val paramsStr = (0 until params.size()).joinToString(", ") { index ->
                    when (val param = params.getValue(index)) {
                        null -> "null"
                        is String -> "'$param'"
                        else -> param.toString()
                    }
                }
                "$sql, 参数: [$paramsStr]"
            }
            logger.debug("执行 SQL: {}", sqlLog)
        }
    }
}

/**
 * SQL 日志记录 Pool 包装器
 * 用于拦截并记录所有 SQL 查询
 */
class SqlLoggingPool(private val delegate: Pool) : Pool by delegate {
    
    override fun query(sql: String): Query<RowSet<Row>> {
        SqlLogger.logSql(sql, null)
        return delegate.query(sql)
    }
    
    override fun preparedQuery(sql: String): PreparedQuery<RowSet<Row>> {
        return SqlLoggingPreparedQuery(delegate.preparedQuery(sql), sql)
    }
    
    override fun getConnection(): Future<SqlConnection> {
        return delegate.connection.map { connection ->
            SqlLoggingConnection(connection)
        }
    }
    
    override fun <T> withTransaction(function: Function<SqlConnection?, Future<T?>>): Future<T?> {
        return delegate.withTransaction { connection ->
            function.apply(SqlLoggingConnection(connection))
        }
    }
    
    override fun <T> withTransaction(
        txPropagation: TransactionPropagation?,
        function: Function<SqlConnection?, Future<T?>>
    ): Future<T?> {
        return delegate.withTransaction(txPropagation) { connection ->
            function.apply(SqlLoggingConnection(connection))
        }
    }
    
    override fun <T> withConnection(function: Function<SqlConnection?, Future<T?>>): Future<T?> {
        return delegate.withConnection { connection ->
            function.apply(SqlLoggingConnection(connection))
        }
    }
}

/**
 * SQL 日志记录 PreparedQuery 包装器
 */
class SqlLoggingPreparedQuery<T>(
    private val delegate: PreparedQuery<T>,
    private val sql: String
) : PreparedQuery<T> by delegate {
    
    override fun execute(args: Tuple): Future<T> {
        SqlLogger.logSql(sql, args)
        return delegate.execute(args)
    }
    
    override fun execute(): Future<T> {
        SqlLogger.logSql(sql, null)
        return delegate.execute()
    }
}

/**
 * SQL 日志记录 Connection 包装器
 */
class SqlLoggingConnection(
    private val delegate: SqlConnection
) : SqlConnection by delegate {
    
    override fun query(sql: String): Query<RowSet<Row>> {
        SqlLogger.logSql(sql, null)
        return delegate.query(sql)
    }
    
    override fun preparedQuery(sql: String): PreparedQuery<RowSet<Row>> {
        return SqlLoggingPreparedQuery(delegate.preparedQuery(sql), sql)
    }
}

