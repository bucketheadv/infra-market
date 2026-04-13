package util

// IsNotBlank 判断字符串指针不为nil且不为空字符串
func IsNotBlank(s *string) bool {
	return s != nil && *s != ""
}

