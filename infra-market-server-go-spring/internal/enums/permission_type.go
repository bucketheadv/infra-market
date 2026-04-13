package enums

// PermissionType 权限类型枚举
type PermissionType string

const (
	PermissionTypeMenu   PermissionType = "menu"
	PermissionTypeButton PermissionType = "button"
	PermissionTypeAPI    PermissionType = "api"
)

func (p PermissionType) Code() string {
	return string(p)
}

func PermissionTypeFromCode(code string) *PermissionType {
	types := map[string]PermissionType{
		"menu":   PermissionTypeMenu,
		"button": PermissionTypeButton,
		"api":    PermissionTypeAPI,
	}
	if permissionType, ok := types[code]; ok {
		return &permissionType
	}
	return nil
}
