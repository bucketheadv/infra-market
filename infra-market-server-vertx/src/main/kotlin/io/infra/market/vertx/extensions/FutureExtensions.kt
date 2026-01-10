package io.infra.market.vertx.extensions

import io.vertx.core.Future
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Future 扩展函数
 * 用于在 Vert.x 中将 Future 转换为 suspend 函数
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
suspend fun <T> Future<T>.awaitForResult(): T {
    return suspendCancellableCoroutine { continuation ->
        this.onComplete { result ->
            if (result.succeeded()) {
                continuation.resume(result.result())
            } else {
                continuation.resumeWithException(result.cause())
            }
        }
    }
}

