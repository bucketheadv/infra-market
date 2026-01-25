using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;
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
        var uid = User.GetUid();
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
        var uid = User.GetUid();
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
        var uid = User.GetUid();
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
        var uid = User.GetUid();
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
        var uid = User.GetUid();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<object>.Error("未登录", 401));
        }

        var result = await authService.ChangePasswordAsync(uid.Value, request);
        return Ok(result);
    }

}
