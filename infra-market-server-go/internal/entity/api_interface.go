package entity

// ApiInterface 接口管理实体类
// 对应数据库表 api_interface
type ApiInterface struct {
	BaseEntity
	Name        string  `gorm:"column:name;type:varchar(100);not null" json:"name"`
	Method      string  `gorm:"column:method;type:varchar(20);not null" json:"method"`
	URL         string  `gorm:"column:url;type:varchar(500);not null" json:"url"`
	Description *string `gorm:"column:description;type:text" json:"description"`
	PostType    *string `gorm:"column:post_type;type:varchar(50)" json:"postType"`
	Params      *string `gorm:"column:params;type:text" json:"params"`
	Status      *int    `gorm:"column:status;type:tinyint;default:1" json:"status"`
	Environment *string `gorm:"column:environment;type:varchar(20)" json:"environment"`
	Timeout     *int64  `gorm:"column:timeout;type:bigint" json:"timeout"`
	ValuePath   *string `gorm:"column:value_path;type:varchar(255)" json:"valuePath"`
}

func (ApiInterface) TableName() string {
	return "api_interface"
}
