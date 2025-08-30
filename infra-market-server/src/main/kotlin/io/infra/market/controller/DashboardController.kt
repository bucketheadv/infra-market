package io.infra.market.controller

import io.infra.market.service.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 仪表盘控制器
 * @author liuqinglin
 * Date: 2025/8/30
 */
@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    
    @GetMapping("/data")
    fun getDashboardData() = dashboardService.getDashboardData()
}
