package entity

// UserRole 用户角色关联实体类
// 对应数据库表 user_role
type UserRole struct {
	BaseEntity
	UID    *uint64 `gorm:"column:uid;not null;uniqueIndex:uk_user_role" json:"uid"`
	RoleID *uint64 `gorm:"column:role_id;not null;uniqueIndex:uk_user_role;index:idx_role_id" json:"roleId"`
}

func (UserRole) TableName() string {
	return "user_role"
}
