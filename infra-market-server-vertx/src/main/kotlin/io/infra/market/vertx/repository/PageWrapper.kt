package io.infra.market.vertx.repository

/**
 * 分页查询结果
 * 
 * 用于封装分页查询的结果数据。
 * 包含记录列表、总数、当前页和每页大小等信息。
 * 
 * @param T 记录数据的类型
 */
data class PageWrapper<T>(
    /**
     * 记录列表
     * 当前页的数据记录列表
     */
    val records: List<T>,
    
    /**
     * 总记录数
     * 符合查询条件的总记录数
     */
    val total: Long,
    
    /**
     * 页码
     * 当前查询的页码（从1开始）
     */
    val page: Int,
    
    /**
     * 每页大小
     * 每页显示的记录数
     */
    val size: Int
)

