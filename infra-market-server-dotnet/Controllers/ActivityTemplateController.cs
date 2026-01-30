using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("activity/template")]
[Authorize]
public class ActivityTemplateController(IActivityTemplateService templateService) : ControllerBase
{
    [HttpGet("list")]
    public async Task<ActionResult<ApiData<PageResult<ActivityTemplateDto>>>> GetActivityTemplates([FromQuery] ActivityTemplateQueryDto query)
    {
        var result = await templateService.GetActivityTemplatesAsync(query);
        return Ok(result);
    }

    [HttpGet("all")]
    public async Task<ActionResult<ApiData<List<ActivityTemplateDto>>>> GetAllActivityTemplates()
    {
        var result = await templateService.GetAllActivityTemplatesAsync();
        if (result.Code == 200)
        {
            // 只返回启用的模板
            var enabledTemplates = result.Data?.Where(t => t.Status == 1).ToList() ?? [];
            return Ok(ApiData<List<ActivityTemplateDto>>.Success(enabledTemplates));
        }
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<ActivityTemplateDto>>> GetActivityTemplate(ulong id)
    {
        var result = await templateService.GetActivityTemplateAsync(id);
        return Ok(result);
    }

    [HttpPost]
    public async Task<ActionResult<ApiData<ActivityTemplateDto>>> CreateActivityTemplate([FromBody] ActivityTemplateFormDto dto)
    {
        var result = await templateService.CreateActivityTemplateAsync(dto);
        return Ok(result);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ApiData<ActivityTemplateDto>>> UpdateActivityTemplate(ulong id, [FromBody] ActivityTemplateFormDto dto)
    {
        var result = await templateService.UpdateActivityTemplateAsync(id, dto);
        return Ok(result);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiData<object>>> DeleteActivityTemplate(ulong id)
    {
        var result = await templateService.DeleteActivityTemplateAsync(id);
        return Ok(result);
    }

    [HttpPut("{id}/status")]
    public async Task<ActionResult<ApiData<ActivityTemplateDto>>> UpdateActivityTemplateStatus(ulong id, [FromQuery] string status)
    {
        if (string.IsNullOrEmpty(status))
        {
            return Ok(ApiData<ActivityTemplateDto>.Error("状态参数不能为空", 400));
        }

        var statusInt = status == "1" ? 1 : 0;
        var result = await templateService.UpdateActivityTemplateStatusAsync(id, statusInt);
        return Ok(result);
    }

    [HttpPost("{id}/copy")]
    public async Task<ActionResult<ApiData<ActivityTemplateDto>>> CopyActivityTemplate(ulong id)
    {
        var result = await templateService.CopyActivityTemplateAsync(id);
        return Ok(result);
    }
}
