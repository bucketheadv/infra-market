package dto

// ApiData 通用API响应结构
type ApiData[T any] struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Data    T      `json:"data,omitempty"`
}

// Success 创建成功响应
func Success[T any](data T) ApiData[T] {
	return ApiData[T]{
		Code:    200,
		Message: "success",
		Data:    data,
	}
}

// Error 创建错误响应
func Error[T any](message string, code int) ApiData[T] {
	return ApiData[T]{
		Code:    code,
		Message: message,
	}
}

// ErrorWithDetail 创建带详细信息的错误响应
func ErrorWithDetail[T any](message string, detail string, code int) ApiData[T] {
	return ApiData[T]{
		Code:    code,
		Message: message,
		Data:    any(detail).(T),
	}
}

// PageResult 分页结果
type PageResult[T any] struct {
	Records []T   `json:"records"`
	Total   int64 `json:"total"`
	Current int   `json:"current"`
	Size    int   `json:"size"`
}

// BatchRequest 批量操作请求
type BatchRequest struct {
	IDs []uint64 `json:"ids" binding:"required,min=1"`
}

// ChangePasswordRequest 修改密码请求
type ChangePasswordRequest struct {
	OldPassword string `json:"oldPassword" binding:"required,min=6,max=20"`
	NewPassword string `json:"newPassword" binding:"required,min=6,max=20"`
}

// StatusUpdateDto 状态更新请求
type StatusUpdateDto struct {
	Status string `json:"status" binding:"required"`
}

// IDUriParam ID路径参数
type IDUriParam struct {
	ID uint64 `uri:"id" binding:"required,min=1"`
}

// ExecutorIDUriParam 执行人ID路径参数
type ExecutorIDUriParam struct {
	ExecutorID uint64 `uri:"executorId" binding:"required,min=1"`
}

// InterfaceIDUriParam 接口ID路径参数
type InterfaceIDUriParam struct {
	InterfaceID uint64 `uri:"interfaceId" binding:"required,min=1"`
}
