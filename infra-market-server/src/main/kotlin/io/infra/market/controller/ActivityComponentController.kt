package io.infra.market.controller

import io.infra.market.dto.*
import io.infra.market.service.ActivityComponentService
import io.infra.market.annotation.RequiresPermission
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 活动组件控制器
 * 
 * 提供活动组件管理的RESTful API，包括组件的CRUD操作。
 * 所有接口都需要相应的权限验证。
 * 
 * 主要功能：
 * - 组件列表查询（支持多条件筛选）
 * - 组件详情查看
 * - 组件创建和更新
 * - 组件删除
 * - 组件状态管理
 * - 组件复制
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@RestController
@RequestMapping("/activity/component")
class ActivityComponentController(
    private val activityComponentService: ActivityComponentService
) {

    /**
     * 获取组件列表
     * 
     * 支持多条件筛选查询组件列表，包括按名称、状态等条件进行筛选。
     * 所有查询参数都是可选的，支持组合查询。
     * 
     * @param name 组件名称，支持模糊查询
     * @param status 组件状态，1-启用，0-禁用
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 组件列表，包含组件的基本信息和字段/组件配置
     */
    @GetMapping("/list")
    @RequiresPermission("activity:component:list")
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) status: Int?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiData<PageResultDto<ActivityComponentDto>> {
        val query = ActivityComponentQueryDto(
            name = name,
            status = status,
            page = page,
            size = size
        )
        val pageResult = activityComponentService.findPage(query)
        return ApiData.success(pageResult)
    }

    /**
     * 获取所有启用的组件列表
     * 
     * 用于活动模板或活动管理中选择组件时的下拉列表。
     * 只返回状态为启用（status=1）的组件。
     * 
     * @return 启用的组件列表
     */
    @GetMapping("/all")
    @RequiresPermission("activity:component:list")
    fun findAll(): ApiData<List<ActivityComponentDto>> {
        val components = activityComponentService.findAll()
        val enabledComponents = components.filter { it.status == 1 }
        return ApiData.success(enabledComponents)
    }

    /**
     * 根据ID获取组件详情
     * 
     * 通过组件ID获取单个组件的详细信息，包括组件的基本信息和完整的字段/组件配置。
     * 
     * @param id 组件ID，路径参数
     * @return 组件详情，如果组件不存在则返回错误信息
     */
    @GetMapping("/{id}")
    @RequiresPermission("activity:component:view")
    fun detail(@PathVariable id: Long): ApiData<ActivityComponentDto> {
        val component = activityComponentService.findById(id)
        return if (component != null) {
            ApiData.success(component)
        } else {
            ApiData.error("活动组件不存在")
        }
    }

    /**
     * 创建新组件
     * 
     * 创建新的活动组件记录，包括组件基本信息和字段/组件配置。
     * 系统会自动设置创建时间、更新时间和默认状态。
     * 
     * @param form 组件表单数据，包含组件的所有配置信息
     * @return 创建成功的组件信息
     */
    @PostMapping
    @RequiresPermission("activity:component:create")
    fun create(@Valid @RequestBody form: ActivityComponentFormDto): ApiData<ActivityComponentDto> {
        val component = activityComponentService.save(form)
        return ApiData.success(component)
    }

    /**
     * 更新组件信息
     * 
     * 根据组件ID更新组件的基本信息和字段/组件配置。
     * 系统会自动更新修改时间，保持创建时间不变。
     * 
     * @param id 组件ID，路径参数
     * @param form 组件表单数据，包含要更新的配置信息
     * @return 更新后的组件信息
     */
    @PutMapping("/{id}")
    @RequiresPermission("activity:component:update")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody form: ActivityComponentFormDto
    ): ApiData<ActivityComponentDto> {
        val component = activityComponentService.update(id, form)
        return ApiData.success(component)
    }

    /**
     * 删除组件
     * 
     * 根据组件ID删除指定的组件记录。
     * 删除操作不可逆，请谨慎操作。
     * 
     * @param id 组件ID，路径参数
     * @return 删除操作结果，true表示删除成功
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("activity:component:delete")
    fun delete(@PathVariable id: Long): ApiData<Boolean> {
        val result = activityComponentService.delete(id)
        return ApiData.success(result)
    }

    /**
     * 更新组件状态
     * 
     * 启用或禁用指定的组件。只有启用状态的组件才能在活动模板中被选择。
     * 
     * @param id 组件ID，路径参数
     * @param status 组件状态，1-启用，0-禁用
     * @return 更新后的组件信息
     */
    @PutMapping("/{id}/status")
    @RequiresPermission("activity:component:update")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: Int
    ): ApiData<ActivityComponentDto> {
        val component = activityComponentService.updateStatus(id, status)
        return ApiData.success(component)
    }

    /**
     * 复制组件
     * 
     * 根据现有组件创建一个新的组件副本，包括所有配置信息。
     * 新组件的名称会自动添加"_副本"后缀，状态为禁用。
     * 
     * @param id 要复制的组件ID，路径参数
     * @return 新创建的组件信息
     */
    @PostMapping("/{id}/copy")
    @RequiresPermission("activity:component:create")
    fun copy(@PathVariable id: Long): ApiData<ActivityComponentDto> {
        val component = activityComponentService.copy(id)
        return ApiData.success(component)
    }
}
