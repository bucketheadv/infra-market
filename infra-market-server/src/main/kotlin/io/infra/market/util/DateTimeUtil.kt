package io.infra.market.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Date

/**
 * 日期时间工具类
 * @author liuqinglin
 * Date: 2025/8/30
 */
object DateTimeUtil {
    
    /**
     * 格式化日期时间为字符串
     * @param timestamp 毫秒时间戳
     * @return 格式化后的字符串，如果为null则返回空字符串
     */
    fun formatDateTime(timestamp: Long?): String {
        return if (timestamp != null) {
            val dateTime = DateTime(timestamp)
            dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        } else ""
    }
    
    /**
     * 格式化日期时间为字符串（兼容旧版本）
     * @param dateTime 日期时间
     * @return 格式化后的字符串，如果为null则返回空字符串
     * @deprecated 请使用 formatDateTime(Long?) 方法
     */
    @Deprecated("请使用 formatDateTime(Long?) 方法", ReplaceWith("formatDateTime(dateTime?.time)"))
    fun formatDateTime(dateTime: Date?): String {
        return formatDateTime(dateTime?.time)
    }
}
