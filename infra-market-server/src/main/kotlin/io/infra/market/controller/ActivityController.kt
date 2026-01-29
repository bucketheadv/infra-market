package io.infra.market.controller

import io.infra.market.dto.*
import io.infra.market.service.ActivityService
import io.infra.market.annotation.RequiresPermission
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 活动管理控制器
 * 
 * 提供活动管理的RESTful API，包括活动的CRUD操作。
 * 所有接口都需要相应的权限验证。
 * 
 * 主要功能：
 * - 活动列表查询（支持多条件筛选）
 * - 活动详情查看
 * - 活动创建和更新
 * - 活动删除
 * - 活动状态管理
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@RestController
@RequestMapping("/activity")
class ActivityController(
    private val activityService: ActivityService
) {

    /**
     * 获取活动列表
     * 
     * 支持多条件筛选查询活动列表，包括按名称、模板ID、状态等条件进行筛选。
     * 所有查询参数都是可选的，支持组合查询。
     * 
     * @param name 活动名称，支持模糊查询
     * @param templateId 模板ID，精确匹配查询
     * @param status 活动状态，1-启用，0-禁用
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 活动列表，包含活动的基本信息和配置数据
     */
    @GetMapping("/list")
    @RequiresPermission("activity:list")
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) templateId: Long?,
        @RequestParam(required = false) status: Int?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiData<PageResultDto<ActivityDto>> {
        val query = ActivityQueryDto(
            name = name,
            templateId = templateId,
            status = status,
            page = page,
            size = size
        )
        val pageResult = activityService.findPage(query)
        return ApiData.success(pageResult)
    }

    /**
     * 根据ID获取活动详情
     * 
     * 通过活动ID获取单个活动的详细信息，包括活动的基本信息和配置数据。
     * 
     * @param id 活动ID，路径参数
     * @return 活动详情，如果活动不存在则返回错误信息
     */
    @GetMapping("/{id}")
    @RequiresPermission("activity:view")
    fun detail(@PathVariable id: Long): ApiData<ActivityDto> {
        val activity = activityService.findById(id)
        return if (activity != null) {
            ApiData.success(activity)
        } else {
            ApiData.error("活动不存在")
        }
    }

    /**
     * 创建新活动
     * 
     * 创建新的活动记录，包括活动基本信息和配置数据。
     * 系统会自动设置创建时间、更新时间和默认状态。
     * 
     * @param form 活动表单数据，包含活动的所有配置信息
     * @return 创建成功的活动信息
     */
    @PostMapping
    @RequiresPermission("activity:create")
    fun create(@Valid @RequestBody form: ActivityFormDto): ApiData<ActivityDto> {
        val activity = activityService.save(form)
        return ApiData.success(activity)
    }

    /**
     * 更新活动信息
     * 
     * 根据活动ID更新活动的基本信息和配置数据。
     * 系统会自动更新修改时间，保持创建时间不变。
     * 
     * @param id 活动ID，路径参数
     * @param form 活动表单数据，包含要更新的配置信息
     * @return 更新后的活动信息
     */
    @PutMapping("/{id}")
    @RequiresPermission("activity:update")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody form: ActivityFormDto
    ): ApiData<ActivityDto> {
        val activity = activityService.update(id, form)
        return ApiData.success(activity)
    }

    /**
     * 删除活动
     * 
     * 根据活动ID删除指定的活动记录。
     * 删除操作不可逆，请谨慎操作。
     * 
     * @param id 活动ID，路径参数
     * @return 删除操作结果，true表示删除成功
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("activity:delete")
    fun delete(@PathVariable id: Long): ApiData<Boolean> {
        val result = activityService.delete(id)
        return ApiData.success(result)
    }

    /**
     * 更新活动状态
     * 
     * 启用或禁用指定的活动。
     * 
     * @param id 活动ID，路径参数
     * @param status 活动状态，1-启用，0-禁用
     * @return 更新后的活动信息
     */
    @PutMapping("/{id}/status")
    @RequiresPermission("activity:update")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: Int
    ): ApiData<ActivityDto> {
        val activity = activityService.updateStatus(id, status)
        return ApiData.success(activity)
    }
}
