package io.infra.qk.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

/**
 * 日期时间工具类
 * @author liuqinglin
 * Date: 2025/8/30
 */
object DateTimeUtil {
    
    private val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss"
    private val DATE_PATTERN = "yyyy-MM-dd"
    private val TIME_PATTERN = "HH:mm:ss"
    
    /**
     * 格式化日期时间
     */
    fun formatDateTime(date: Date?): String {
        return if (date != null) {
            val dateTime = DateTime(date)
            dateTime.toString(DateTimeFormat.forPattern(DEFAULT_PATTERN))
        } else ""
    }
    
    /**
     * 格式化日期
     */
    fun formatDate(date: Date?): String {
        return if (date != null) {
            val dateTime = DateTime(date)
            dateTime.toString(DateTimeFormat.forPattern(DATE_PATTERN))
        } else ""
    }
    
    /**
     * 格式化时间
     */
    fun formatTime(date: Date?): String {
        return if (date != null) {
            val dateTime = DateTime(date)
            dateTime.toString(DateTimeFormat.forPattern(TIME_PATTERN))
        } else ""
    }
    
    /**
     * 解析日期时间字符串
     */
    fun parseDateTime(dateTimeStr: String): Date? {
        return try {
            val dateTime = DateTimeFormat.forPattern(DEFAULT_PATTERN).parseDateTime(dateTimeStr)
            dateTime.toDate()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取当前时间
     */
    fun now(): Date {
        return DateTime.now().toDate()
    }
    
    /**
     * 获取当前时间字符串
     */
    fun nowString(): String {
        return DateTime.now().toString(DateTimeFormat.forPattern(DEFAULT_PATTERN))
    }
}
