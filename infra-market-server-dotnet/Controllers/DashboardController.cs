using InfraMarket.Server.DTOs;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InfraMarket.Server.Controllers;

[ApiController]
[Route("dashboard")]
[Authorize]
public class DashboardController(IDashboardService dashboardService) : ControllerBase
{
    [HttpGet("data")]
    public async Task<ActionResult<ApiData<DashboardDataDto>>> GetDashboardData()
    {
        var result = await dashboardService.GetDashboardDataAsync();
        return Ok(result);
    }
}
