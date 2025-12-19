package entity

// User 用户实体类
// 对应数据库表 user_info
type User struct {
	BaseEntity
	Username      string  `gorm:"column:username;type:varchar(50);not null;uniqueIndex:uk_username" json:"username"`
	Password      string  `gorm:"column:password;type:varchar(255);not null" json:"-"`
	Email         *string `gorm:"column:email;type:varchar(100);index:idx_email" json:"email"`
	Phone         *string `gorm:"column:phone;type:varchar(20);index:idx_phone" json:"phone"`
	Status        string  `gorm:"column:status;type:varchar(20);not null;default:'active';index:idx_status" json:"status"`
	LastLoginTime *int64  `gorm:"column:last_login_time;index:idx_last_login_time" json:"lastLoginTime"`
}

func (User) TableName() string {
	return "user_info"
}
