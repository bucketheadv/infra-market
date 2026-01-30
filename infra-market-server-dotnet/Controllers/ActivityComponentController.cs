using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("activity/component")]
[Authorize]
public class ActivityComponentController(IActivityComponentService componentService) : ControllerBase
{
    [HttpGet("list")]
    public async Task<ActionResult<ApiData<PageResult<ActivityComponentDto>>>> GetActivityComponents([FromQuery] ActivityComponentQueryDto query)
    {
        var result = await componentService.GetActivityComponentsAsync(query);
        return Ok(result);
    }

    [HttpGet("all")]
    public async Task<ActionResult<ApiData<List<ActivityComponentDto>>>> GetAllActivityComponents()
    {
        var result = await componentService.GetAllActivityComponentsAsync();
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<ActivityComponentDto>>> GetActivityComponent(ulong id)
    {
        var result = await componentService.GetActivityComponentAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<ActivityComponentDto>>> CreateActivityComponent([FromBody] ActivityComponentFormDto dto)
    {
        var result = await componentService.CreateActivityComponentAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<ActivityComponentDto>>> UpdateActivityComponent(ulong id, [FromBody] ActivityComponentFormDto dto)
    {
        var result = await componentService.UpdateActivityComponentAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> DeleteActivityComponent(ulong id)
    {
        var result = await componentService.DeleteActivityComponentAsync(id);
        return Ok(result);
    }

    [HttpPut("{id}/status")]
    public async Task<ActionResult<ApiData<ActivityComponentDto>>> UpdateActivityComponentStatus(ulong id, [FromQuery] string status)
    {
        if (string.IsNullOrEmpty(status))
        {
            return Ok(ApiData<ActivityComponentDto>.Error("状态参数不能为空", 400));
        }

        var statusInt = status == "1" ? 1 : 0;
        var result = await componentService.UpdateActivityComponentStatusAsync(id, statusInt);
        return Ok(result);
    }

    [HttpPost("{id}/copy")]
    public async Task<ActionResult<ApiData<ActivityComponentDto>>> CopyActivityComponent(ulong id)
    {
        var result = await componentService.CopyActivityComponentAsync(id);
        return Ok(result);
    }
}
