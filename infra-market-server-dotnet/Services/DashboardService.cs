using InfraMarket.Server.DTOs;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;

namespace InfraMarket.Server.Services;

public class DashboardService(
    IUserRepository userRepository,
    IRoleRepository roleRepository,
    IPermissionRepository permissionRepository,
    IApiInterfaceRepository apiInterfaceRepository) : IDashboardService
{
    public async Task<ApiData<DashboardDataDto>> GetDashboardDataAsync()
    {
        var statistics = await GetStatisticsAsync();
        var recentUsers = await GetRecentUsersAsync();
        var systemInfo = GetSystemInfo();

        var dashboardData = new DashboardDataDto
        {
            Statistics = statistics,
            RecentUsers = recentUsers,
            SystemInfo = systemInfo
        };

        return ApiData<DashboardDataDto>.Success(dashboardData);
    }

    private async Task<DashboardStatisticsDto> GetStatisticsAsync()
    {
        var now = DateTimeOffset.UtcNow;
        var yesterday = now.AddDays(-1);
        var yesterdayTimestamp = yesterday.ToUnixTimeMilliseconds();

        // 获取当前数量
        var userCount = await userRepository.CountAsync();
        var roleCount = await roleRepository.CountAsync();
        var permissionCount = await permissionRepository.CountAsync();
        var apiInterfaceCount = await apiInterfaceRepository.CountAsync();

        // 获取昨天的数量
        var userCountYesterday = await userRepository.CountBeforeDateAsync(yesterdayTimestamp);
        var roleCountYesterday = await roleRepository.CountBeforeDateAsync(yesterdayTimestamp);
        var permissionCountYesterday = await permissionRepository.CountBeforeDateAsync(yesterdayTimestamp);
        var apiInterfaceCountYesterday = await apiInterfaceRepository.CountBeforeDateAsync(yesterdayTimestamp);

        // 计算变化百分比
        var userChangePercent = CalculateChangePercent(userCountYesterday, userCount);
        var roleChangePercent = CalculateChangePercent(roleCountYesterday, roleCount);
        var permissionChangePercent = CalculateChangePercent(permissionCountYesterday, permissionCount);
        var interfaceChangePercent = CalculateChangePercent(apiInterfaceCountYesterday, apiInterfaceCount);

        return new DashboardStatisticsDto
        {
            UserCount = userCount,
            RoleCount = roleCount,
            PermissionCount = permissionCount,
            InterfaceCount = apiInterfaceCount,
            UserCountChangePercent = userChangePercent,
            RoleCountChangePercent = roleChangePercent,
            PermissionCountChangePercent = permissionChangePercent,
            InterfaceCountChangePercent = interfaceChangePercent
        };
    }

    private static double CalculateChangePercent(long oldValue, long newValue)
    {
        if (oldValue == 0)
        {
            return newValue > 0 ? 100.0 : 0.0;
        }

        var change = newValue - oldValue;
        var percent = (double)change / oldValue * 100.0;
        return percent;
    }

    private async Task<List<RecentUserDto>> GetRecentUsersAsync()
    {
        var users = await userRepository.GetRecentLoginUsersAsync(5);

        return users.Select(user =>
        {
            var lastLoginTime = "";
            if (user.LastLoginTime.HasValue)
            {
                lastLoginTime = DateTimeUtil.FormatLocalDateTime(user.LastLoginTime.Value);
            }

            return new RecentUserDto
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Status = user.Status,
                LastLoginTime = lastLoginTime
            };
        }).ToList();
    }

    private static SystemInfoDto GetSystemInfo()
    {
        return new SystemInfoDto
        {
            SystemVersion = "v1.0.0",
            NetVersion = Environment.Version.ToString(),
            LastUpdate = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss")
        };
    }
}
