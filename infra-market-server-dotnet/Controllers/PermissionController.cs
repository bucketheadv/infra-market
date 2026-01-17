using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("permissions")]
[Authorize]
public class PermissionController(IPermissionService permissionService) : ControllerBase
{
    [HttpGet]
    public async Task<ActionResult<ApiData<PageResult<PermissionDto>>>> GetPermissions([FromQuery] PermissionQueryDto query)
    {
        var result = await permissionService.GetPermissionsAsync(query);
        return Ok(result);
    }

    [HttpGet("tree")]
    public async Task<ActionResult<ApiData<List<PermissionDto>>>> GetPermissionTree()
    {
        var result = await permissionService.GetPermissionTreeAsync();
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<PermissionDto>>> GetPermission(ulong id)
    {
        var result = await permissionService.GetPermissionAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<PermissionDto>>> CreatePermission([FromBody] PermissionFormDto dto)
    {
        var result = await permissionService.CreatePermissionAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<PermissionDto>>> UpdatePermission(ulong id, [FromBody] PermissionFormDto dto)
    {
        var result = await permissionService.UpdatePermissionAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> DeletePermission(ulong id)
    {
        var result = await permissionService.DeletePermissionAsync(id);
        return Ok(result);
    }

    [HttpPatch("{id}/status")]
    public async Task<ActionResult<ApiData<object>>> UpdatePermissionStatus(ulong id, [FromBody] StatusUpdateDto dto)
    {
        var result = await permissionService.UpdatePermissionStatusAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("batch")]
    public async Task<ActionResult<ApiData<object>>> BatchDeletePermissions([FromBody] BatchRequest request)
    {
        var result = await permissionService.BatchDeletePermissionsAsync(request);
        return Ok(result);
    }
}
