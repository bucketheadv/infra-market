using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IUserService
{
    Task<ApiData<PageResult<UserDto>>> GetUsersAsync(UserQueryDto query);
    Task<ApiData<UserDto>> GetUserAsync(ulong id);
    Task<ApiData<UserDto>> CreateUserAsync(UserFormDto dto);
    Task<ApiData<UserDto>> UpdateUserAsync(ulong id, UserUpdateDto dto);
    Task<ApiData<object>> DeleteUserAsync(ulong id);
    Task<ApiData<object>> UpdateUserStatusAsync(ulong id, StatusUpdateDto dto);
    Task<ApiData<object>> ResetPasswordAsync(ulong id);
    Task<ApiData<object>> BatchDeleteUsersAsync(BatchRequest request);
}
