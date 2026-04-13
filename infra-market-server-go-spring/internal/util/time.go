package util

import (
	"time"
)

const pattern = "2006-01-02 15:04:05"

// Format 格式化时间戳为字符串
func Format(timestamp *int64) string {
	if timestamp == nil || *timestamp == 0 {
		return ""
	}
	t := time.Unix(*timestamp/1000, (*timestamp%1000)*1000000)
	return t.Format(pattern)
}

// FormatDateTime 格式化时间戳为字符串（兼容方法）
func FormatDateTime(timestamp *int64) string {
	return Format(timestamp)
}
