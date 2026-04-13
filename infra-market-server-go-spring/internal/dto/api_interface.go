package dto

// ApiInterfaceDto 接口信息DTO
type ApiInterfaceDto struct {
	ID           *uint64       `json:"id"`
	Name         *string       `json:"name"`
	Method       *string       `json:"method"`
	URL          *string       `json:"url"`
	Description  *string       `json:"description"`
	Status       *int          `json:"status"`
	PostType     *string       `json:"postType"`
	Environment  *string       `json:"environment"`
	Timeout      *int64        `json:"timeout"`
	ValuePath    *string       `json:"valuePath"`
	URLParams    []ApiParamDto `json:"urlParams"`
	HeaderParams []ApiParamDto `json:"headerParams"`
	BodyParams   []ApiParamDto `json:"bodyParams"`
	CreateTime   *string       `json:"createTime"`
	UpdateTime   *string       `json:"updateTime"`
}

// ApiInterfaceFormDto 接口创建/更新表单
type ApiInterfaceFormDto struct {
	ID           *uint64       `json:"id"`
	Name         *string       `json:"name" binding:"required"`
	Method       *string       `json:"method" binding:"required"`
	URL          *string       `json:"url" binding:"required"`
	Description  *string       `json:"description"`
	PostType     *string       `json:"postType"`
	Environment  *string       `json:"environment"`
	Timeout      *int64        `json:"timeout"`
	ValuePath    *string       `json:"valuePath"`
	URLParams    []ApiParamDto `json:"urlParams"`
	HeaderParams []ApiParamDto `json:"headerParams"`
	BodyParams   []ApiParamDto `json:"bodyParams"`
}

// ApiInterfaceQueryDto 接口查询DTO
type ApiInterfaceQueryDto struct {
	Name        *string `form:"name"`
	Method      *string `form:"method"`
	Status      *int    `form:"status"`
	Environment *string `form:"environment"`
	Pagination
}

// ApiParamDto 接口参数DTO
type ApiParamDto struct {
	Name         *string           `json:"name"`
	ChineseName  *string           `json:"chineseName"`
	ParamType    *string           `json:"paramType"`
	InputType    *string           `json:"inputType"`
	DataType     *string           `json:"dataType"`
	Required     *bool             `json:"required"`
	DefaultValue any               `json:"defaultValue"`
	Changeable   *bool             `json:"changeable"`
	Options      []SelectOptionDto `json:"options"`
	Description  *string           `json:"description"`
	Sort         *int              `json:"sort"`
}

// SelectOptionDto 下拉选项DTO
type SelectOptionDto struct {
	Value *string `json:"value"`
	Label *string `json:"label"`
}

// ApiExecuteRequestDto 接口执行请求DTO
type ApiExecuteRequestDto struct {
	InterfaceID *uint64           `json:"interfaceId" binding:"required"`
	Headers     map[string]string `json:"headers"`
	URLParams   map[string]any    `json:"urlParams"`
	BodyParams  map[string]any    `json:"bodyParams"`
	Timeout     *int64            `json:"timeout"`
	Remark      *string           `json:"remark"`
}

// ApiExecuteResponseDto 接口执行响应DTO
type ApiExecuteResponseDto struct {
	Status         int               `json:"status"`
	Headers        map[string]string `json:"headers"`
	Body           *string           `json:"body"`
	ExtractedValue *string           `json:"extractedValue"`
	ResponseTime   int64             `json:"responseTime"`
	Success        bool              `json:"success"`
	Error          *string           `json:"error"`
}
