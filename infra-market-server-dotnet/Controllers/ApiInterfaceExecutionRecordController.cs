using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("interface/execution/record")]
[Authorize]
public class ApiInterfaceExecutionRecordController(IApiInterfaceExecutionRecordService service) : ControllerBase
{
    [HttpPost("list")]
    public async Task<ActionResult<ApiData<PageResult<ApiInterfaceExecutionRecordDto>>>> List([FromBody] ApiInterfaceExecutionRecordQueryDto query)
    {
        var result = await service.ListAsync(query);
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiData<ApiInterfaceExecutionRecordDto>>> Detail(ulong id)
    {
        var result = await service.DetailAsync(id);
        return Ok(result);
    }

    [HttpGet("executor/{executorId}")]
    public async Task<ActionResult<ApiData<List<ApiInterfaceExecutionRecordDto>>>> GetByExecutorId(ulong executorId, [FromQuery] int limit = 10)
    {
        var result = await service.GetByExecutorIdAsync(executorId, limit);
        return Ok(result);
    }

    [HttpGet("stats/{interfaceId}")]
    public async Task<ActionResult<ApiData<ApiInterfaceExecutionRecordStatsDto>>> GetExecutionStats(ulong interfaceId)
    {
        var result = await service.GetExecutionStatsAsync(interfaceId);
        return Ok(result);
    }

    [HttpGet("count")]
    public async Task<ActionResult<ApiData<long>>> GetExecutionCount([FromQuery] long startTime, [FromQuery] long endTime)
    {
        var result = await service.GetExecutionCountAsync(startTime, endTime);
        return Ok(result);
    }

    [HttpDelete("cleanup")]
    public async Task<ActionResult<ApiData<long>>> CleanupOldRecords([FromQuery] long beforeTime)
    {
        var result = await service.CleanupOldRecordsAsync(beforeTime);
        return Ok(result);
    }
}
