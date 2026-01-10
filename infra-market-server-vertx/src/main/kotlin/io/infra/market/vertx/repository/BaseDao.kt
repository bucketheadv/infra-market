package io.infra.market.vertx.repository

import io.infra.market.vertx.entity.TimestampedEntity
import io.infra.market.vertx.extensions.awaitForResult
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language

/**
 * DAO 基类
 * 提供公共的 Pool 访问方法和时间戳管理
 */
abstract class BaseDao(protected val pool: Pool) {
    
    /**
     * 设置实体的创建时间和更新时间
     */
    protected fun setCreateTime(entity: TimestampedEntity) {
        val now = System.currentTimeMillis()
        entity.createTime = now
        entity.updateTime = now
    }
    
    /**
     * 设置实体的更新时间
     */
    protected fun setUpdateTime(entity: TimestampedEntity) {
        entity.updateTime = System.currentTimeMillis()
    }
    
    /**
     * 执行 SQL 语句
     * - INSERT: 返回生成的主键 ID
     * - UPDATE/DELETE: 返回受影响的行数（通常可以忽略）
     * 
     * @param connection 数据库连接（用于事务）
     * @param sql SQL 语句（使用 @Language("SQL") 注解以支持 IDEA SQL 高亮）
     * @param params SQL 参数
     * @return INSERT 返回主键 ID，UPDATE/DELETE 返回受影响的行数
     */
    protected suspend fun execute(
        connection: SqlConnection,
        @Language("SQL") sql: String,
        vararg params: Any?
    ): Long {
        val rows = connection.preparedQuery(sql)
            .execute(Tuple.from(params.toList()))
            .awaitForResult()
        return extractResult(rows)
    }
    
    /**
     * 执行 SQL 语句
     * - INSERT: 返回生成的主键 ID
     * - UPDATE/DELETE: 返回受影响的行数（通常可以忽略）
     * 
     * @param sql SQL 语句（使用 @Language("SQL") 注解以支持 IDEA SQL 高亮）
     * @param params SQL 参数
     * @return INSERT 返回主键 ID，UPDATE/DELETE 返回受影响的行数
     */
    protected suspend fun execute(
        @Language("SQL") sql: String,
        vararg params: Any?
    ): Long {
        val rows = pool.preparedQuery(sql)
            .execute(Tuple.from(params.toList()))
            .awaitForResult()
        return extractResult(rows)
    }
    
    /**
     * 从查询结果中提取返回值
     * INSERT 语句返回生成的主键 ID，UPDATE/DELETE 返回受影响的行数
     */
    private fun extractResult(rows: RowSet<Row>): Long {
        return if (rows.size() > 0) {
            try {
                rows.iterator().next().getLong(0)
            } catch (e: Exception) {
                rows.rowCount().toLong()
            }
        } else {
            rows.rowCount().toLong()
        }
    }
    
    /**
     * 在事务中执行 suspend 函数
     * 
     * @param block 事务块
     * @return 事务块的返回值
     */
    suspend fun <T> withTransaction(
        block: suspend (SqlConnection) -> T
    ): T {
        return pool.withTransaction { connection: SqlConnection? ->
            executeInTransaction(connection, block)
        }.awaitForResult()
    }
    
    /**
     * 在已有事务上下文中执行 suspend 函数
     * 
     * @param connection 数据库连接
     * @param block 执行块
     * @return Future 结果
     */
    fun <T> executeInTransaction(
        connection: SqlConnection?,
        block: suspend (SqlConnection) -> T
    ): Future<T> {
        return Future.future { promise ->
            try {
                val result = runBlocking {
                    block(connection!!)
                }
                promise.complete(result)
            } catch (e: Exception) {
                promise.fail(e)
            }
        }
    }
    
    /**
     * 通用分页查询方法
     * 
     * @param tableName 表名
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param fixedConditions 固定的 WHERE 条件（如 "status != 'deleted'"），可以为空
     * @param dynamicConditions 动态条件列表，每个元素是一个 Pair<String, Any?>，格式为 ("field = ?", value)
     * @param multiParamConditions 多参数条件列表，每个元素是一个 Pair<String, List<Any?>>，格式为 ("field1 LIKE ? OR field2 LIKE ?", [value1, value2])
     * @param orderBy 排序字段（如 "id ASC" 或 "create_time DESC"），默认为 "id ASC"
     * @param rowMapper 行转换函数，将 Row 转换为实体对象
     * @return PageWrapper<T> 分页结果对象
     */
    protected suspend fun <T> page(
        tableName: String,
        page: Int,
        size: Int,
        fixedConditions: List<String> = emptyList(),
        dynamicConditions: List<Pair<String, Any?>> = emptyList(),
        multiParamConditions: List<Pair<String, List<Any?>>> = emptyList(),
        orderBy: String = "id ASC",
        rowMapper: (Row) -> T
    ): PageWrapper<T> {
        // 构建条件和参数
        val validDynamic = dynamicConditions.filter { (_, v) -> v != null && v.toString().isNotBlank() }
        val validMulti = multiParamConditions.filter { (_, vs) -> vs.isNotEmpty() && vs.all { it != null && it.toString().isNotBlank() } }
        
        val conditions = (fixedConditions + validDynamic.map { it.first } + validMulti.map { it.first })
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" AND ")
            ?.let { "WHERE $it" }
            ?: ""
        
        val params = mutableListOf<Any>().apply {
            addAll(validDynamic.map { it.second!! })
            validMulti.forEach { addAll(it.second.filterNotNull()) }
        }
        
        // 查询总数和数据
        val total = pool.preparedQuery("SELECT COUNT(*) as total FROM $tableName $conditions")
            .execute(if (params.isNotEmpty()) Tuple.from(params) else Tuple.tuple())
            .awaitForResult()
            .first()
            .getLong("total")
        
        // 查询数据
        val offset = (page - 1) * size
        val dataParams = mutableListOf<Any>().apply {
            addAll(params)
            add(size)
            add(offset)
        }
        val records = pool.preparedQuery("SELECT * FROM $tableName $conditions ORDER BY $orderBy LIMIT ? OFFSET ?")
            .execute(Tuple.from(dataParams))
            .awaitForResult()
            .map(rowMapper)
        
        return PageWrapper(records, total, page, size)
    }
}

