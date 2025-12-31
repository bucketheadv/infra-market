package dto

// PermissionDto 权限信息DTO
type PermissionDto struct {
	ID               uint64               `json:"id"`
	Name             string               `json:"name"`
	Code             string               `json:"code"`
	Type             string               `json:"type"`
	ParentID         *uint64              `json:"parentId"`
	Path             *string              `json:"path"`
	Icon             *string              `json:"icon"`
	Sort             int                  `json:"sort"`
	Status           string               `json:"status"`
	Children         []PermissionDto      `json:"children,omitempty"`
	ParentPermission *ParentPermissionDto `json:"parentPermission,omitempty"`
	AccessPath       []string             `json:"accessPath,omitempty"`
	CreateTime       string               `json:"createTime"`
	UpdateTime       string               `json:"updateTime"`
}

// ParentPermissionDto 父级权限信息
type ParentPermissionDto struct {
	ID   uint64 `json:"id"`
	Name string `json:"name"`
	Code string `json:"code"`
}

// PermissionFormDto 权限创建/更新表单
type PermissionFormDto struct {
	Name     string  `json:"name" binding:"required,min=2,max=50"`
	Code     string  `json:"code" binding:"required,min=2,max=100"`
	Type     string  `json:"type" binding:"required,oneof=menu button api"`
	ParentID *uint64 `json:"parentId"`
	Path     *string `json:"path" binding:"omitempty,max=200"`
	Icon     *string `json:"icon" binding:"omitempty,max=100"`
	Sort     int     `json:"sort" binding:"required,min=0"`
}

// PermissionQueryDto 权限查询DTO
type PermissionQueryDto struct {
	Name    *string `form:"name"`
	Code    *string `form:"code"`
	Type    *string `form:"type" binding:"omitempty,oneof=menu button api"`
	Status  *string `form:"status" binding:"omitempty,oneof=active inactive"`
	Pagination
}
