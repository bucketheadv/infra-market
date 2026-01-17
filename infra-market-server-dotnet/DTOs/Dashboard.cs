namespace InfraMarket.Server.DTOs;

public class DashboardStatisticsDto
{
    public long UserCount { get; set; }
    public long RoleCount { get; set; }
    public long PermissionCount { get; set; }
    public long InterfaceCount { get; set; }
    public double UserCountChangePercent { get; set; }
    public double RoleCountChangePercent { get; set; }
    public double PermissionCountChangePercent { get; set; }
    public double InterfaceCountChangePercent { get; set; }
}

public class RecentUserDto
{
    public ulong Id { get; set; }
    public string Username { get; set; } = string.Empty;
    public string? Email { get; set; }
    public string Status { get; set; } = string.Empty;
    public string LastLoginTime { get; set; } = string.Empty;
}

public class SystemInfoDto
{
    public string SystemVersion { get; set; } = string.Empty;
    public string NetVersion { get; set; } = string.Empty;
    public string LastUpdate { get; set; } = string.Empty;
}

public class DashboardDataDto
{
    public DashboardStatisticsDto Statistics { get; set; } = new();
    public List<RecentUserDto> RecentUsers { get; set; } = new();
    public SystemInfoDto SystemInfo { get; set; } = new();
}
