package io.infra.market.vertx.util

import org.joda.time.DateTime

/**
 * 日期时间工具类
 */
object TimeUtil {

    private const val PATTERN = "yyyy-MM-dd HH:mm:ss"

    /**
     * 格式化日期时间为字符串
     */
    fun format(timestamp: Long?, pattern: String = PATTERN): String {
        return if (timestamp != null) DateTime(timestamp).toString(pattern) else ""
    }
}

