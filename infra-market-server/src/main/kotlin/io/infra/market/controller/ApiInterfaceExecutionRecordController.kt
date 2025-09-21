package io.infra.market.controller

import io.infra.market.dto.*
import io.infra.market.service.ApiInterfaceExecutionRecordService
import io.infra.market.annotation.RequiresPermission
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 接口执行记录控制器
 * 
 * 提供接口执行记录的RESTful API，包括执行记录的查询、统计等功能。
 * 所有接口都需要相应的权限验证。
 * 
 * 主要功能：
 * - 执行记录列表查询（支持多条件筛选）
 * - 执行记录详情查看
 * - 执行记录统计信息
 * - 执行记录删除（清理历史数据）
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/interface/execution/record")
class ApiInterfaceExecutionRecordController(
    private val apiInterfaceExecutionRecordService: ApiInterfaceExecutionRecordService
) {

    /**
     * 分页查询接口执行记录
     * 
     * 支持按接口、执行人、执行时间、执行结果等条件查询执行记录。
     * 支持分页查询，默认按执行时间倒序排列。
     * 
     * @param queryDto 查询条件
     * @return 执行记录列表
     */
    @PostMapping("/list")
    @RequiresPermission("interface:view")
    fun list(@Valid @RequestBody queryDto: ApiInterfaceExecutionRecordQueryDto): ApiData<PageResultDto<ApiInterfaceExecutionRecordDto>> {
        val pageResult = apiInterfaceExecutionRecordService.findPage(queryDto)
        return ApiData.success(pageResult)
    }

    /**
     * 根据ID查询执行记录详情
     * 
     * 获取指定执行记录的详细信息，包括请求参数、响应数据等。
     * 
     * @param id 执行记录ID
     * @return 执行记录详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("interface:view")
    fun detail(@PathVariable id: Long): ApiData<ApiInterfaceExecutionRecordDto?> {
        val record = apiInterfaceExecutionRecordService.getById(id)
        return ApiData.success(record)
    }

    /**
     * 根据执行人ID查询执行记录
     * 
     * 获取指定用户的执行记录列表，按执行时间倒序排列。
     * 
     * @param executorId 执行人ID
     * @param limit 限制返回数量，默认10条
     * @return 执行记录列表
     */
    @GetMapping("/executor/{executorId}")
    @RequiresPermission("interface:view")
    fun getByExecutorId(
        @PathVariable executorId: Long,
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiData<List<ApiInterfaceExecutionRecordDto>> {
        val records = apiInterfaceExecutionRecordService.findByExecutorId(executorId, limit)
        return ApiData.success(records)
    }

    /**
     * 获取接口执行统计信息
     * 
     * 统计指定接口的执行次数、成功率、平均执行时间等信息。
     * 
     * @param interfaceId 接口ID
     * @return 执行统计信息
     */
    @GetMapping("/stats/{interfaceId}")
    @RequiresPermission("interface:view")
    fun getExecutionStats(@PathVariable interfaceId: Long): ApiData<ApiInterfaceExecutionRecordStatsDto?> {
        val stats = apiInterfaceExecutionRecordService.getExecutionStats(interfaceId)
        return ApiData.success(stats)
    }

    /**
     * 获取执行记录数量统计
     * 
     * 根据时间范围统计执行记录数量。
     * 
     * @param startTime 开始时间（时间戳）
     * @param endTime 结束时间（时间戳）
     * @return 执行记录数量
     */
    @GetMapping("/count")
    @RequiresPermission("interface:view")
    fun getExecutionCount(
        @RequestParam startTime: Long,
        @RequestParam endTime: Long
    ): ApiData<Long> {
        val count = apiInterfaceExecutionRecordService.countByTimeRange(startTime, endTime)
        return ApiData.success(count)
    }

    /**
     * 删除指定时间之前的执行记录
     * 
     * 用于清理历史数据，删除指定时间之前的所有执行记录。
     * 注意：此操作不可逆，请谨慎使用。
     * 
     * @param beforeTime 指定时间（时间戳）
     * @return 删除的记录数量
     */
    @DeleteMapping("/cleanup")
    @RequiresPermission("interface:delete")
    fun cleanupOldRecords(@RequestParam beforeTime: Long): ApiData<Int> {
        val deletedCount = apiInterfaceExecutionRecordService.deleteByTimeBefore(beforeTime)
        return ApiData.success(deletedCount)
    }
}
