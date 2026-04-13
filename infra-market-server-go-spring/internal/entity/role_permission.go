package entity

// RolePermission 角色权限关联实体类
// 对应数据库表 role_permission
type RolePermission struct {
	BaseEntity
	RoleID       uint64 `gorm:"column:role_id;not null;uniqueIndex:uk_role_permission;index:idx_role_id" json:"roleId"`
	PermissionID uint64 `gorm:"column:permission_id;not null;uniqueIndex:uk_role_permission;index:idx_permission_id" json:"permissionId"`
}

func (RolePermission) TableName() string {
	return "role_permission"
}
