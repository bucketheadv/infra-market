using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("interface")]
[Authorize]
public class ApiInterfaceController(IApiInterfaceService apiInterfaceService) : ControllerBase
{
    [HttpGet("list")]
    public async Task<ActionResult<ApiData<PageResult<ApiInterfaceDto>>>> List([FromQuery] ApiInterfaceQueryDto query)
    {
        var result = await apiInterfaceService.ListAsync(query);
        return Ok(result);
    }

    [HttpGet("most/used")]
    public async Task<ActionResult<ApiData<List<ApiInterfaceDto>>>> GetMostUsed([FromQuery] int days = 30, [FromQuery] int limit = 5)
    {
        var result = await apiInterfaceService.GetMostUsedAsync(days, limit);
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<ApiInterfaceDto>>> Detail(ulong id)
    {
        var result = await apiInterfaceService.DetailAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<ApiInterfaceDto>>> Create([FromBody] ApiInterfaceFormDto dto)
    {
        var result = await apiInterfaceService.CreateAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<ApiInterfaceDto>>> Update(ulong id, [FromBody] ApiInterfaceFormDto dto)
    {
        var result = await apiInterfaceService.UpdateAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> Delete(ulong id)
    {
        var result = await apiInterfaceService.DeleteAsync(id);
        return Ok(result);
    }

    [HttpPut("{id}/status")]
    public async Task<ActionResult<ApiData<object>>> UpdateStatus(ulong id, [FromQuery] int status)
    {
        var result = await apiInterfaceService.UpdateStatusAsync(id, status);
        return Ok(result);
    }

    [HttpPost("{id}/copy")]
    public async Task<ActionResult<ApiData<ApiInterfaceDto>>> Copy(ulong id)
    {
        var result = await apiInterfaceService.CopyAsync(id);
        return Ok(result);
    }

    [HttpPost("execute")]
    public async Task<ActionResult<ApiData<ApiExecuteResponseDto>>> Execute([FromBody] ApiExecuteRequestDto request)
    {
        var uid = GetUidFromClaims();
        if (!uid.HasValue)
        {
            return Unauthorized(ApiData<ApiExecuteResponseDto>.Error("未登录", 401));
        }

        var clientIp = HttpContext.Connection.RemoteIpAddress?.ToString() ?? "";
        var userAgent = Request.Headers["User-Agent"].ToString();

        var result = await apiInterfaceService.ExecuteAsync(request, uid.Value, clientIp, userAgent);
        return Ok(result);
    }

    private ulong? GetUidFromClaims()
    {
        var uidClaim = User.FindFirst("uid");
        if (uidClaim != null && ulong.TryParse(uidClaim.Value, out var uid))
        {
            return uid;
        }
        return null;
    }
}
