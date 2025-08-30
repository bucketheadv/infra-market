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
     * @param dateTime 日期时间
     * @return 格式化后的字符串，如果为null则返回空字符串
     */
    fun formatDateTime(dateTime: Date?): String {
        return if (dateTime != null) {
            val dateTime = DateTime(dateTime)
            dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        } else ""
    }
}
