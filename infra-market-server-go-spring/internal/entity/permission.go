package entity

// Permission 权限实体类
// 对应数据库表 permission_info
type Permission struct {
	BaseEntity
	Name     string  `gorm:"column:name;type:varchar(50);not null" json:"name"`
	Code     string  `gorm:"column:code;type:varchar(50);not null;uniqueIndex:uk_code" json:"code"`
	Type     string  `gorm:"column:type;type:varchar(20);not null;default:'menu';index:idx_type" json:"type"`
	ParentID *uint64 `gorm:"column:parent_id;index:idx_parent_id" json:"parentId"`
	Path     *string `gorm:"column:path;type:varchar(255)" json:"path"`
	Icon     *string `gorm:"column:icon;type:varchar(100)" json:"icon"`
	Sort     int     `gorm:"column:sort;not null;default:0;index:idx_sort" json:"sort"`
	Status   string  `gorm:"column:status;type:varchar(20);not null;default:'active';index:idx_status" json:"status"`
}

func (Permission) TableName() string {
	return "permission_info"
}
