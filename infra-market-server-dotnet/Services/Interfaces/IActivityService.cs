using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IActivityService
{
    Task<ApiData<PageResult<ActivityDto>>> GetActivitiesAsync(ActivityQueryDto query);
    Task<ApiData<ActivityDto>> GetActivityAsync(ulong id);
    Task<ApiData<ActivityDto>> CreateActivityAsync(ActivityFormDto dto);
    Task<ApiData<ActivityDto>> UpdateActivityAsync(ulong id, ActivityFormDto dto);
    Task<ApiData<object>> DeleteActivityAsync(ulong id);
    Task<ApiData<ActivityDto>> UpdateActivityStatusAsync(ulong id, int status);
}
