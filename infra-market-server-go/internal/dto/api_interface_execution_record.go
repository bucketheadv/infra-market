package dto

// ApiInterfaceExecutionRecordDto 执行记录DTO
type ApiInterfaceExecutionRecordDto struct {
	ID              *uint64 `json:"id"`
	InterfaceID     *uint64 `json:"interfaceId"`
	InterfaceName   *string `json:"interfaceName"`
	ExecutorID      *uint64 `json:"executorId"`
	ExecutorName    *string `json:"executorName"`
	RequestParams   *string `json:"requestParams"`
	RequestHeaders  *string `json:"requestHeaders"`
	RequestBody     *string `json:"requestBody"`
	ResponseStatus  *int    `json:"responseStatus"`
	ResponseHeaders *string `json:"responseHeaders"`
	ResponseBody    *string `json:"responseBody"`
	ExecutionTime   *int64  `json:"executionTime"`
	Success         *bool   `json:"success"`
	ErrorMessage    *string `json:"errorMessage"`
	Remark          *string `json:"remark"`
	ClientIP        *string `json:"clientIp"`
	UserAgent       *string `json:"userAgent"`
	CreateTime      *string `json:"createTime"`
	UpdateTime      *string `json:"updateTime"`
}

// ApiInterfaceExecutionRecordQueryDto 执行记录查询DTO
type ApiInterfaceExecutionRecordQueryDto struct {
	InterfaceID      *uint64 `form:"interfaceId"`
	ExecutorID       *uint64 `form:"executorId"`
	ExecutorName     *string `form:"executorName"`
	Success          *bool   `form:"success"`
	StartTime        *int64  `form:"startTime"`
	EndTime          *int64  `form:"endTime"`
	MinExecutionTime *int64  `form:"minExecutionTime"`
	MaxExecutionTime *int64  `form:"maxExecutionTime"`
	Page             *int    `form:"page" binding:"omitempty,min=1" default:"1"`
	Size             *int    `form:"size" binding:"omitempty,min=1,max=1000" default:"10"`
}

// ApiInterfaceExecutionRecordStatsDto 执行记录统计DTO
type ApiInterfaceExecutionRecordStatsDto struct {
	InterfaceID       *uint64 `json:"interfaceId"`
	InterfaceName     *string `json:"interfaceName"`
	TotalExecutions   int64   `json:"totalExecutions"`
	SuccessExecutions int64   `json:"successExecutions"`
	FailedExecutions  int64   `json:"failedExecutions"`
	SuccessRate       float64 `json:"successRate"`
	AvgExecutionTime  float64 `json:"avgExecutionTime"`
	MinExecutionTime  int64   `json:"minExecutionTime"`
	MaxExecutionTime  int64   `json:"maxExecutionTime"`
	LastExecutionTime *string `json:"lastExecutionTime"`
}
