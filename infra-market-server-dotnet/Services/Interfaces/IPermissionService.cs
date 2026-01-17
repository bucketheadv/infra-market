using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IPermissionService
{
    Task<ApiData<PageResult<PermissionDto>>> GetPermissionsAsync(PermissionQueryDto query);
    Task<ApiData<List<PermissionDto>>> GetPermissionTreeAsync();
    Task<ApiData<PermissionDto>> GetPermissionAsync(ulong id);
    Task<ApiData<PermissionDto>> CreatePermissionAsync(PermissionFormDto dto);
    Task<ApiData<PermissionDto>> UpdatePermissionAsync(ulong id, PermissionFormDto dto);
    Task<ApiData<object>> DeletePermissionAsync(ulong id);
    Task<ApiData<object>> UpdatePermissionStatusAsync(ulong id, StatusUpdateDto dto);
    Task<ApiData<object>> BatchDeletePermissionsAsync(BatchRequest request);
}
