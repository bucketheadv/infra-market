package io.infra.market.controller

import io.infra.market.service.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 仪表盘控制器
 * 
 * 处理仪表盘相关的HTTP请求，提供系统统计数据和概览信息。
 * 包括用户统计、角色统计、权限统计和最近登录用户等信息。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    
    /**
     * 获取仪表盘数据
     * 
     * 获取仪表盘页面所需的所有数据，包括统计数据、最近登录用户和系统信息。
     * 
     * @return 仪表盘数据，包含统计数据、最近登录用户列表和系统信息
     */
    @GetMapping("/data")
    fun getDashboardData() = dashboardService.getDashboardData()
}
