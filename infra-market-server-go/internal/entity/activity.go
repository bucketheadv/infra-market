package entity

// Activity 活动实体类
// 对应数据库表 activity
type Activity struct {
	BaseEntity
	Name        string  `gorm:"column:name;type:varchar(100);not null;index:idx_name" json:"name"`
	Description *string `gorm:"column:description;type:varchar(500)" json:"description"`
	TemplateID  uint64  `gorm:"column:template_id;not null;index:idx_template_id" json:"templateId"`
	ConfigData  *string `gorm:"column:config_data;type:longtext" json:"configData"`
	Status      int     `gorm:"column:status;type:int;not null;default:1;index:idx_status" json:"status"`
}

func (Activity) TableName() string {
	return "activity"
}

// ActivityTemplate 活动模板实体类
// 对应数据库表 activity_template
type ActivityTemplate struct {
	BaseEntity
	Name        string  `gorm:"column:name;type:varchar(100);not null;index:idx_name" json:"name"`
	Description *string `gorm:"column:description;type:varchar(500)" json:"description"`
	Fields      *string `gorm:"column:fields;type:longtext" json:"fields"`
	Status      int     `gorm:"column:status;type:int;not null;default:1;index:idx_status" json:"status"`
}

func (ActivityTemplate) TableName() string {
	return "activity_template"
}

// ActivityComponent 活动组件实体类
// 对应数据库表 activity_component
type ActivityComponent struct {
	BaseEntity
	Name        string  `gorm:"column:name;type:varchar(100);not null;index:idx_name" json:"name"`
	Description *string `gorm:"column:description;type:varchar(500)" json:"description"`
	Fields      *string `gorm:"column:fields;type:longtext" json:"fields"`
	Status      int     `gorm:"column:status;type:int;not null;default:1;index:idx_status" json:"status"`
}

func (ActivityComponent) TableName() string {
	return "activity_component"
}
