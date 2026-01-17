using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IRoleService
{
    Task<ApiData<PageResult<RoleDto>>> GetRolesAsync(RoleQueryDto query);
    Task<ApiData<List<RoleDto>>> GetAllRolesAsync();
    Task<ApiData<RoleDto>> GetRoleAsync(ulong id);
    Task<ApiData<RoleDto>> CreateRoleAsync(RoleFormDto dto);
    Task<ApiData<RoleDto>> UpdateRoleAsync(ulong id, RoleFormDto dto);
    Task<ApiData<object>> DeleteRoleAsync(ulong id);
    Task<ApiData<object>> UpdateRoleStatusAsync(ulong id, StatusUpdateDto dto);
    Task<ApiData<object>> BatchDeleteRolesAsync(BatchRequest request);
}
