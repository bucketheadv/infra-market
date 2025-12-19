package dto

// RoleDto 角色信息DTO
type RoleDto struct {
	ID            uint64   `json:"id"`
	Name          string   `json:"name"`
	Code          string   `json:"code"`
	Description   *string  `json:"description"`
	Status        string   `json:"status"`
	PermissionIds []uint64 `json:"permissionIds"`
	CreateTime    string   `json:"createTime"`
	UpdateTime    string   `json:"updateTime"`
}

// RoleFormDto 角色创建/更新表单
type RoleFormDto struct {
	Name          string   `json:"name" binding:"required,min=2,max=50"`
	Code          string   `json:"code" binding:"required,min=2,max=100"`
	Description   *string  `json:"description" binding:"omitempty,max=200"`
	PermissionIds []uint64 `json:"permissionIds" binding:"required,min=1"`
}

// RoleQueryDto 角色查询DTO
type RoleQueryDto struct {
	Name    *string `form:"name"`
	Code    *string `form:"code"`
	Status  *string `form:"status" binding:"omitempty,oneof=active inactive"`
	Current int     `form:"current" binding:"omitempty,min=1" default:"1"`
	Size    int     `form:"size" binding:"omitempty,min=1,max=1000" default:"10"`
}
