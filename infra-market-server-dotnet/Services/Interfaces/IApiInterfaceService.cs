using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IApiInterfaceService
{
    Task<ApiData<PageResult<ApiInterfaceDto>>> ListAsync(ApiInterfaceQueryDto query);
    Task<ApiData<List<ApiInterfaceDto>>> GetMostUsedAsync(int days, int limit);
    Task<ApiData<ApiInterfaceDto>> DetailAsync(ulong id);
    Task<ApiData<ApiInterfaceDto>> CreateAsync(ApiInterfaceFormDto dto);
    Task<ApiData<ApiInterfaceDto>> UpdateAsync(ulong id, ApiInterfaceFormDto dto);
    Task<ApiData<object>> DeleteAsync(ulong id);
    Task<ApiData<object>> UpdateStatusAsync(ulong id, int status);
    Task<ApiData<ApiInterfaceDto>> CopyAsync(ulong id);
    Task<ApiData<ApiExecuteResponseDto>> ExecuteAsync(ApiExecuteRequestDto request, ulong executorId, string clientIp, string userAgent);
}
