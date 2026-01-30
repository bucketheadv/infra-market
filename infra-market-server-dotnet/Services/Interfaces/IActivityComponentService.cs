using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IActivityComponentService
{
    Task<ApiData<PageResult<ActivityComponentDto>>> GetActivityComponentsAsync(ActivityComponentQueryDto query);
    Task<ApiData<List<ActivityComponentDto>>> GetAllActivityComponentsAsync();
    Task<ApiData<ActivityComponentDto>> GetActivityComponentAsync(ulong id);
    Task<ApiData<ActivityComponentDto>> CreateActivityComponentAsync(ActivityComponentFormDto dto);
    Task<ApiData<ActivityComponentDto>> UpdateActivityComponentAsync(ulong id, ActivityComponentFormDto dto);
    Task<ApiData<object>> DeleteActivityComponentAsync(ulong id);
    Task<ApiData<ActivityComponentDto>> UpdateActivityComponentStatusAsync(ulong id, int status);
    Task<ApiData<ActivityComponentDto>> CopyActivityComponentAsync(ulong id);
}
