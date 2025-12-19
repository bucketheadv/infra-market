package enums

// ParamType 参数类型枚举
type ParamType string

const (
	ParamTypeURL    ParamType = "URL_PARAM"
	ParamTypeBody   ParamType = "BODY_PARAM"
	ParamTypeHeader ParamType = "HEADER_PARAM"
)

func (p ParamType) Code() string {
	return string(p)
}

func ParamTypeFromCode(code string) *ParamType {
	types := map[string]ParamType{
		"URL_PARAM":    ParamTypeURL,
		"BODY_PARAM":   ParamTypeBody,
		"HEADER_PARAM": ParamTypeHeader,
	}
	if paramType, ok := types[code]; ok {
		return &paramType
	}
	return nil
}
