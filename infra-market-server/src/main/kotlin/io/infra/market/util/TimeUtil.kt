package io.infra.market.util

import org.joda.time.DateTime
import java.util.Date

/**
 * 日期时间工具类
 * @author liuqinglin
 * Date: 2025/8/30
 */
object TimeUtil {

    private const val PATTERN = "yyyy-MM-dd HH:mm:ss"

    /**
     * 格式化日期时间为字符串
     * @param timestamp 毫秒时间戳
     * @return 格式化后的字符串，如果为null则返回空字符串
     */
    fun format(timestamp: Long?, pattern: String = PATTERN): String {
        return if (timestamp != null) DateTime(timestamp).toString(pattern) else ""
    }
    
    /**
     * 格式化日期时间为字符串（兼容旧版本）
     * @param dateTime 日期时间
     * @return 格式化后的字符串，如果为null则返回空字符串
     * @deprecated 请使用 formatDateTime(Long?) 方法
     */
    @Deprecated("请使用 format(Long?, String) 方法", ReplaceWith("format(dateTime?.time, String)"))
    fun format(dateTime: Date?): String {
        return format(dateTime?.time)
    }
}
