using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("activity")]
[Authorize]
public class ActivityController(IActivityService activityService) : ControllerBase
{
    [HttpGet("list")]
    public async Task<ActionResult<ApiData<PageResult<ActivityDto>>>> GetActivities([FromQuery] ActivityQueryDto query)
    {
        var result = await activityService.GetActivitiesAsync(query);
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<ActivityDto>>> GetActivity(ulong id)
    {
        var result = await activityService.GetActivityAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<ActivityDto>>> CreateActivity([FromBody] ActivityFormDto dto)
    {
        var result = await activityService.CreateActivityAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<ActivityDto>>> UpdateActivity(ulong id, [FromBody] ActivityFormDto dto)
    {
        var result = await activityService.UpdateActivityAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> DeleteActivity(ulong id)
    {
        var result = await activityService.DeleteActivityAsync(id);
        return Ok(result);
    }

    [HttpPut("{id}/status")]
    public async Task<ActionResult<ApiData<ActivityDto>>> UpdateActivityStatus(ulong id, [FromQuery] string status)
    {
        if (string.IsNullOrEmpty(status))
        {
            return Ok(ApiData<ActivityDto>.Error("状态参数不能为空", 400));
        }

        var statusInt = status == "1" ? 1 : 0;
        var result = await activityService.UpdateActivityStatusAsync(id, statusInt);
        return Ok(result);
    }
}
