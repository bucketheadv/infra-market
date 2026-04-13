package util

// AuthHolder 认证相关的Context工具
type AuthHolder struct{}

var uidKey = "uid"

// GetUID 从Context获取当前用户ID
func GetUID(ctx any) (uint64, bool) {
	// 这里需要根据实际的Context类型来实现
	// 在Gin中，应该从gin.Context获取
	return 0, false
}

// SetUID 设置当前用户ID到Context
func SetUID(ctx any, uid uint64) {
	// 在Gin中，应该设置到gin.Context
}

// ClearUID 清理当前用户ID
func ClearUID(ctx any) {
	// 在Gin中，不需要特别清理
}
