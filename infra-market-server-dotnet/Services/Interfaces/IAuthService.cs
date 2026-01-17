using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IAuthService
{
    Task<ApiData<LoginResponse>> LoginAsync(LoginRequest request);
    Task<ApiData<LoginResponse>> GetCurrentUserAsync(ulong uid);
    Task<ApiData<List<PermissionDto>>> GetUserMenusAsync(ulong uid);
    Task<ApiData<LoginResponse>> RefreshTokenAsync(ulong uid);
    Task<ApiData<object>> LogoutAsync(ulong uid);
    Task<ApiData<object>> ChangePasswordAsync(ulong uid, ChangePasswordRequest request);
}
