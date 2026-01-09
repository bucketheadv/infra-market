package io.infra.market.vertx.util

import io.vertx.core.CompositeFuture
import io.vertx.core.Future

/**
 * Future 工具类
 * 
 * 用于处理多个 Future 的组合操作
 */
object FutureUtil {
    
    /**
     * 组合多个 Future（适用于 List）
     * 
     * 使用递归方式组合多个 Future
     */
    fun <T> all(futures: List<Future<T>>): Future<List<T>> {
        if (futures.isEmpty()) {
            return Future.succeededFuture(emptyList())
        }
        
        if (futures.size == 1) {
            return futures[0].map { listOf(it) }
        }
        
        // 使用递归方式组合 Future
        return combineFutures(futures, 0, emptyList())
    }
    
    /**
     * 递归组合 Future
     */
    private fun <T> combineFutures(
        futures: List<Future<T>>,
        index: Int,
        accumulated: List<T>
    ): Future<List<T>> {
        if (index >= futures.size) {
            return Future.succeededFuture(accumulated)
        }
        
        return futures[index]
            .compose { result ->
                combineFutures(futures, index + 1, accumulated + result)
            }
    }
    
    /**
     * 组合多个 Future（适用于固定数量的 Future）
     * 
     * 使用 Future.all 来组合多个 Future
     */
    fun all(
        f1: Future<*>,
        f2: Future<*>,
        f3: Future<*>,
        f4: Future<*>,
        f5: Future<*>
    ): Future<CompositeFuture> {
        return Future.all(f1, f2, f3, f4, f5)
    }
}

