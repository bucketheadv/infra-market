package entity

// ApiInterfaceExecutionRecord 接口执行记录实体类
// 对应数据库表 api_interface_execution_record
type ApiInterfaceExecutionRecord struct {
	BaseEntity
	InterfaceID     *uint64 `gorm:"column:interface_id;not null;index:idx_interface_id" json:"interfaceId"`
	ExecutorID      *uint64 `gorm:"column:executor_id;not null;index:idx_executor_id" json:"executorId"`
	ExecutorName    string  `gorm:"column:executor_name;type:varchar(50);not null;index:idx_executor_name" json:"executorName"`
	RequestParams   *string `gorm:"column:request_params;type:longtext" json:"requestParams"`
	RequestHeaders  *string `gorm:"column:request_headers;type:longtext" json:"requestHeaders"`
	RequestBody     *string `gorm:"column:request_body;type:longtext" json:"requestBody"`
	ResponseStatus  *int    `gorm:"column:response_status" json:"responseStatus"`
	ResponseHeaders *string `gorm:"column:response_headers;type:longtext" json:"responseHeaders"`
	ResponseBody    *string `gorm:"column:response_body;type:longtext" json:"responseBody"`
	ExecutionTime   *int64  `gorm:"column:execution_time;type:bigint;index:idx_execution_time" json:"executionTime"`
	Success         *bool   `gorm:"column:success;type:tinyint(1);not null;default:0;index:idx_success" json:"success"`
	ErrorMessage    *string `gorm:"column:error_message;type:text" json:"errorMessage"`
	Remark          *string `gorm:"column:remark;type:text" json:"remark"`
	ClientIP        *string `gorm:"column:client_ip;type:varchar(50)" json:"clientIp"`
	UserAgent       *string `gorm:"column:user_agent;type:varchar(500)" json:"userAgent"`
}

func (ApiInterfaceExecutionRecord) TableName() string {
	return "api_interface_execution_record"
}
