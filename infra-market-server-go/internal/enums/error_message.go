package enums

// ErrorMessage 错误消息枚举
type ErrorMessage string

const (
	// 参数校验相关
	ErrorMessageValidationFailed ErrorMessage = "参数校验失败"
	ErrorMessageParamTypeError   ErrorMessage = "参数类型错误"

	// 系统错误相关
	ErrorMessageSystemInternalError ErrorMessage = "系统内部错误"
	ErrorMessageSystemError         ErrorMessage = "系统异常"
	ErrorMessageUnknownError        ErrorMessage = "未知错误"

	// 权限相关
	ErrorMessagePermissionDenied ErrorMessage = "权限不足，无法访问此功能"

	// 认证相关
	ErrorMessageLoginExpired ErrorMessage = "登录已过期，请重新登录"

	// 资源相关
	ErrorMessageResourceNotFound ErrorMessage = "请求的资源不存在"

	// 网络相关
	ErrorMessageNetworkError   ErrorMessage = "网络错误，请检查网络连接"
	ErrorMessageRequestTimeout ErrorMessage = "请求超时，请检查网络连接或增加超时时间"
)

func (e ErrorMessage) Message() string {
	return string(e)
}

// GetParamTypeErrorMessage 根据参数名和期望类型生成参数类型错误消息
func GetParamTypeErrorMessage(paramName string, expectedType string) string {
	if expectedType == "" {
		return string(ErrorMessageParamTypeError) + "，参数 '" + paramName + "' 类型错误"
	}
	return string(ErrorMessageParamTypeError) + "，参数 '" + paramName + "' 类型错误，期望类型: " + expectedType
}
