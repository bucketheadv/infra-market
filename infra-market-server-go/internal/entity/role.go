package entity

// Role 角色实体类
// 对应数据库表 role_info
type Role struct {
	BaseEntity
	Name        string  `gorm:"column:name;type:varchar(50);not null;index:idx_name" json:"name"`
	Code        string  `gorm:"column:code;type:varchar(50);not null;uniqueIndex:uk_code" json:"code"`
	Description *string `gorm:"column:description;type:varchar(255)" json:"description"`
	Status      string  `gorm:"column:status;type:varchar(20);not null;default:'active';index:idx_status" json:"status"`
}

func (Role) TableName() string {
	return "role_info"
}
