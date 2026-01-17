using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("users")]
[Authorize]
public class UserController(IUserService userService) : ControllerBase
{
    [HttpGet]
    public async Task<ActionResult<ApiData<PageResult<UserDto>>>> GetUsers([FromQuery] UserQueryDto query)
    {
        var result = await userService.GetUsersAsync(query);
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<UserDto>>> GetUser(ulong id)
    {
        var result = await userService.GetUserAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<UserDto>>> CreateUser([FromBody] UserFormDto dto)
    {
        var result = await userService.CreateUserAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<UserDto>>> UpdateUser(ulong id, [FromBody] UserUpdateDto dto)
    {
        var result = await userService.UpdateUserAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> DeleteUser(ulong id)
    {
        var result = await userService.DeleteUserAsync(id);
        return Ok(result);
    }

    [HttpPatch("{id}/status")]
    public async Task<ActionResult<ApiData<object>>> UpdateUserStatus(ulong id, [FromBody] StatusUpdateDto dto)
    {
        var result = await userService.UpdateUserStatusAsync(id, dto);
        return Ok(result);
    }

    [HttpPost("{id}/reset/password")]
    public async Task<ActionResult<ApiData<object>>> ResetPassword(ulong id)
    {
        var result = await userService.ResetPasswordAsync(id);
        return Ok(result);
    }

    [HttpPost("batch/delete")]
    public async Task<ActionResult<ApiData<object>>> BatchDeleteUsers([FromBody] BatchRequest request)
    {
        var result = await userService.BatchDeleteUsersAsync(request);
        return Ok(result);
    }
}
