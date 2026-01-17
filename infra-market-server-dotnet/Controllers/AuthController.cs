using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("auth")]
public class AuthController(
    IAuthService authService) : ControllerBase
{
    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<ActionResult<ApiData<LoginResponse>>> Login([FromBody] LoginRequest request)
    {
        var result = await authService.LoginAsync(request);
        return Ok(result);
    }

    [HttpGet("current/user")]
    [Authorize]
    public async Task<ActionResult<ApiData<LoginResponse>>> GetCurrentUser()
    {
        var uid = GetUidFromClaims();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<LoginResponse>.Error("未登录", 401));
        }

        var result = await authService.GetCurrentUserAsync(uid.Value);
        return Ok(result);
    }

    [HttpGet("user/menus")]
    [Authorize]
    public async Task<ActionResult<ApiData<List<PermissionDto>>>> GetUserMenus()
    {
        var uid = GetUidFromClaims();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<List<PermissionDto>>.Error("未登录", 401));
        }

        var result = await authService.GetUserMenusAsync(uid.Value);
        return Ok(result);
    }

    [HttpPost("refresh/token")]
    [Authorize]
    public async Task<ActionResult<ApiData<LoginResponse>>> RefreshToken()
    {
        var uid = GetUidFromClaims();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<LoginResponse>.Error("未登录", 401));
        }

        var result = await authService.RefreshTokenAsync(uid.Value);
        return Ok(result);
    }

    [HttpPost("logout")]
    [Authorize]
    public async Task<ActionResult<ApiData<object>>> Logout()
    {
        var uid = GetUidFromClaims();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<object>.Error("未登录", 401));
        }

        var result = await authService.LogoutAsync(uid.Value);
        return Ok(result);
    }

    [HttpPost("change/password")]
    [Authorize]
    public async Task<ActionResult<ApiData<object>>> ChangePassword([FromBody] ChangePasswordRequest request)
    {
        var uid = GetUidFromClaims();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<object>.Error("未登录", 401));
        }

        var result = await authService.ChangePasswordAsync(uid.Value, request);
        return Ok(result);
    }

    private ulong? GetUidFromClaims()
    {
        // 从 subject claim 中提取 uid，与 Java 版本保持一致
        var subClaim = User.FindFirst("sub") ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier);
        if (subClaim != null && ulong.TryParse(subClaim.Value, out var uid))
        {
            return uid;
        }
        return null;
    }
}
