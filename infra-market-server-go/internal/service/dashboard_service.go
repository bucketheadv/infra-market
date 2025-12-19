package service

import (
	"runtime"
	"time"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
)

type DashboardService struct {
	userRepo         *repository.UserRepository
	roleRepo         *repository.RoleRepository
	permissionRepo   *repository.PermissionRepository
	apiInterfaceRepo *repository.ApiInterfaceRepository
}

func NewDashboardService(
	userRepo *repository.UserRepository,
	roleRepo *repository.RoleRepository,
	permissionRepo *repository.PermissionRepository,
	apiInterfaceRepo *repository.ApiInterfaceRepository,
) *DashboardService {
	return &DashboardService{
		userRepo:         userRepo,
		roleRepo:         roleRepo,
		permissionRepo:   permissionRepo,
		apiInterfaceRepo: apiInterfaceRepo,
	}
}

// GetDashboardData 获取仪表盘数据
func (s *DashboardService) GetDashboardData() dto.ApiData[dto.DashboardDataDto] {
	statistics := s.getStatistics()
	recentUsers := s.getRecentUsers()
	systemInfo := s.getSystemInfo()

	dashboardData := dto.DashboardDataDto{
		Statistics:  statistics,
		RecentUsers: recentUsers,
		SystemInfo:  systemInfo,
	}

	return dto.Success(dashboardData)
}

// getStatistics 获取统计数据
func (s *DashboardService) getStatistics() dto.DashboardStatisticsDto {
	now := time.Now()
	yesterday := now.AddDate(0, 0, -1)
	yesterdayTimestamp := yesterday.UnixMilli()

	// 获取当前数量
	userCount, _ := s.userRepo.Count()
	roleCount, _ := s.roleRepo.Count()
	permissionCount, _ := s.permissionRepo.Count()
	apiInterfaceCount, _ := s.apiInterfaceRepo.Count()

	// 获取昨天的数量
	userCountYesterday, _ := s.userRepo.CountBeforeDate(yesterdayTimestamp)
	roleCountYesterday, _ := s.roleRepo.CountBeforeDate(yesterdayTimestamp)
	permissionCountYesterday, _ := s.permissionRepo.CountBeforeDate(yesterdayTimestamp)
	apiInterfaceCountYesterday, _ := s.apiInterfaceRepo.CountBeforeDate(yesterdayTimestamp)

	// 计算变化百分比
	userChangePercent := s.calculateChangePercent(userCountYesterday, userCount)
	roleChangePercent := s.calculateChangePercent(roleCountYesterday, roleCount)
	permissionChangePercent := s.calculateChangePercent(permissionCountYesterday, permissionCount)
	interfaceChangePercent := s.calculateChangePercent(apiInterfaceCountYesterday, apiInterfaceCount)

	return dto.DashboardStatisticsDto{
		UserCount:                    userCount,
		RoleCount:                    roleCount,
		PermissionCount:              permissionCount,
		InterfaceCount:               apiInterfaceCount,
		UserCountChangePercent:       userChangePercent,
		RoleCountChangePercent:       roleChangePercent,
		PermissionCountChangePercent: permissionChangePercent,
		InterfaceCountChangePercent:  interfaceChangePercent,
	}
}

// calculateChangePercent 计算变化百分比
func (s *DashboardService) calculateChangePercent(oldValue, newValue int64) float64 {
	if oldValue == 0 {
		if newValue > 0 {
			return 100.0
		}
		return 0.0
	}

	change := newValue - oldValue
	percent := float64(change) / float64(oldValue) * 100.0
	return percent
}

// getRecentUsers 获取最近登录用户
func (s *DashboardService) getRecentUsers() []dto.RecentUserDto {
	users, _ := s.userRepo.GetRecentLoginUsers(5)

	recentUsers := make([]dto.RecentUserDto, len(users))
	for i, user := range users {
		lastLoginTime := ""
		if user.LastLoginTime != nil {
			lastLoginTime = util.Format(user.LastLoginTime)
		}

		recentUsers[i] = dto.RecentUserDto{
			ID:            user.ID,
			Username:      user.Username,
			Email:         user.Email,
			Status:        user.Status,
			LastLoginTime: lastLoginTime,
		}
	}

	return recentUsers
}

// getSystemInfo 获取系统信息
func (s *DashboardService) getSystemInfo() dto.SystemInfoDto {
	return dto.SystemInfoDto{
		SystemVersion: "v1.0.0",
		GoVersion:     runtime.Version(),
		LastUpdate:    time.Now().Format("2006-01-02 15:04:05"),
	}
}
