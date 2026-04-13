package dto

// DashboardStatisticsDto 仪表盘统计数据
type DashboardStatisticsDto struct {
	UserCount                    int64   `json:"userCount"`
	RoleCount                    int64   `json:"roleCount"`
	PermissionCount              int64   `json:"permissionCount"`
	InterfaceCount               int64   `json:"interfaceCount"`
	UserCountChangePercent       float64 `json:"userCountChangePercent"`
	RoleCountChangePercent       float64 `json:"roleCountChangePercent"`
	PermissionCountChangePercent float64 `json:"permissionCountChangePercent"`
	InterfaceCountChangePercent  float64 `json:"interfaceCountChangePercent"`
}

// RecentUserDto 最近登录用户DTO
type RecentUserDto struct {
	ID            uint64  `json:"id"`
	Username      string  `json:"username"`
	Email         *string `json:"email"`
	Status        string  `json:"status"`
	LastLoginTime string  `json:"lastLoginTime"`
}

// SystemInfoDto 系统信息DTO
type SystemInfoDto struct {
	SystemVersion string `json:"systemVersion"`
	GoVersion     string `json:"goVersion"`
	LastUpdate    string `json:"lastUpdate"`
}

// DashboardDataDto 仪表盘数据DTO
type DashboardDataDto struct {
	Statistics  DashboardStatisticsDto `json:"statistics"`
	RecentUsers []RecentUserDto        `json:"recentUsers"`
	SystemInfo  SystemInfoDto          `json:"systemInfo"`
}
