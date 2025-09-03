package io.infra.qk.controller

import io.infra.qk.dto.ApiResponse
import io.infra.qk.service.DashboardService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

/**
 * 仪表板控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
class DashboardController @Inject constructor(
    private val dashboardService: DashboardService
) {
    
    @GET
    @Path("/data")
    fun getDashboardData(): ApiResponse<*> {
        return dashboardService.getDashboardData()
    }
}
