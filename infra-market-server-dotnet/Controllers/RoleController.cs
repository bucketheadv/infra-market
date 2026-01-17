using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("roles")]
[Authorize]
public class RoleController(IRoleService roleService) : ControllerBase
{
    [HttpGet]
    public async Task<ActionResult<ApiData<PageResult<RoleDto>>>> GetRoles([FromQuery] RoleQueryDto query)
    {
        var result = await roleService.GetRolesAsync(query);
        return Ok(result);
    }

    [HttpGet("all")]
    public async Task<ActionResult<ApiData<List<RoleDto>>>> GetAllRoles()
    {
        var result = await roleService.GetAllRolesAsync();
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<RoleDto>>> GetRole(ulong id)
    {
        var result = await roleService.GetRoleAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<RoleDto>>> CreateRole([FromBody] RoleFormDto dto)
    {
        var result = await roleService.CreateRoleAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<RoleDto>>> UpdateRole(ulong id, [FromBody] RoleFormDto dto)
    {
        var result = await roleService.UpdateRoleAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> DeleteRole(ulong id)
    {
        var result = await roleService.DeleteRoleAsync(id);
        return Ok(result);
    }

    [HttpPatch("{id}/status")]
    public async Task<ActionResult<ApiData<object>>> UpdateRoleStatus(ulong id, [FromBody] StatusUpdateDto dto)
    {
        var result = await roleService.UpdateRoleStatusAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("batch")]
    public async Task<ActionResult<ApiData<object>>> BatchDeleteRoles([FromBody] BatchRequest request)
    {
        var result = await roleService.BatchDeleteRolesAsync(request);
        return Ok(result);
    }
}
