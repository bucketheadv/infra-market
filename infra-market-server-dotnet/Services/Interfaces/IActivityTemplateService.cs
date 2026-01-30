using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IActivityTemplateService
{
    Task<ApiData<PageResult<ActivityTemplateDto>>> GetActivityTemplatesAsync(ActivityTemplateQueryDto query);
    Task<ApiData<List<ActivityTemplateDto>>> GetAllActivityTemplatesAsync();
    Task<ApiData<ActivityTemplateDto>> GetActivityTemplateAsync(ulong id);
    Task<ApiData<ActivityTemplateDto>> CreateActivityTemplateAsync(ActivityTemplateFormDto dto);
    Task<ApiData<ActivityTemplateDto>> UpdateActivityTemplateAsync(ulong id, ActivityTemplateFormDto dto);
    Task<ApiData<object>> DeleteActivityTemplateAsync(ulong id);
    Task<ApiData<ActivityTemplateDto>> UpdateActivityTemplateStatusAsync(ulong id, int status);
    Task<ApiData<ActivityTemplateDto>> CopyActivityTemplateAsync(ulong id);
}
