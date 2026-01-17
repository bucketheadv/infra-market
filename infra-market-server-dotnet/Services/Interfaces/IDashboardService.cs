using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IDashboardService
{
    Task<ApiData<DashboardDataDto>> GetDashboardDataAsync();
}
