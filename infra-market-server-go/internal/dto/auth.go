package dto

// LoginRequest 登录请求
type LoginRequest struct {
	Username string `json:"username" binding:"required,min=2,max=20"`
	Password string `json:"password" binding:"required,min=6,max=20"`
}

// LoginResponse 登录响应
type LoginResponse struct {
	Token       string   `json:"token"`
	User        UserDto  `json:"user"`
	Permissions []string `json:"permissions"`
}

// UserDto 用户信息DTO
type UserDto struct {
	ID            uint64   `json:"id"`
	Username      string   `json:"username"`
	Email         *string  `json:"email"`
	Phone         *string  `json:"phone"`
	Status        string   `json:"status"`
	LastLoginTime *string  `json:"lastLoginTime"`
	RoleIds       []uint64 `json:"roleIds"`
	CreateTime    string   `json:"createTime"`
	UpdateTime    string   `json:"updateTime"`
}

// UserFormDto 用户创建表单
type UserFormDto struct {
	Username string   `json:"username" binding:"required,min=2,max=20"`
	Email    *string  `json:"email" binding:"omitempty,email"`
	Phone    *string  `json:"phone" binding:"omitempty"`
	Password *string  `json:"password" binding:"omitempty,min=6,max=20"`
	RoleIds  []uint64 `json:"roleIds" binding:"required,min=1"`
}

// UserUpdateDto 用户更新表单
type UserUpdateDto struct {
	Username string   `json:"username" binding:"required,min=2,max=20"`
	Email    *string  `json:"email" binding:"omitempty,email"`
	Phone    *string  `json:"phone" binding:"omitempty"`
	Password *string  `json:"password"`
	RoleIds  []uint64 `json:"roleIds" binding:"required,min=1"`
}

// UserQueryDto 用户查询DTO
type UserQueryDto struct {
	Username *string `form:"username"`
	Status   *string `form:"status" binding:"omitempty,oneof=active inactive"`
	Pagination
}
