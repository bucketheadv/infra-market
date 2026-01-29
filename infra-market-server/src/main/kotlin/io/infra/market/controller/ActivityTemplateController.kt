package io.infra.market.controller

import io.infra.market.dto.*
import io.infra.market.service.ActivityTemplateService
import io.infra.market.annotation.RequiresPermission
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 活动模板控制器
 * 
 * 提供活动模板管理的RESTful API，包括模板的CRUD操作。
 * 所有接口都需要相应的权限验证。
 * 
 * 主要功能：
 * - 模板列表查询（支持多条件筛选）
 * - 模板详情查看
 * - 模板创建和更新
 * - 模板删除
 * - 模板状态管理
 * - 模板复制
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@RestController
@RequestMapping("/activity/template")
class ActivityTemplateController(
    private val activityTemplateService: ActivityTemplateService
) {

    /**
     * 获取模板列表
     * 
     * 支持多条件筛选查询模板列表，包括按名称、状态等条件进行筛选。
     * 所有查询参数都是可选的，支持组合查询。
     * 
     * @param name 模板名称，支持模糊查询
     * @param status 模板状态，1-启用，0-禁用
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 模板列表，包含模板的基本信息和字段配置
     */
    @GetMapping("/list")
    @RequiresPermission("activity:template:list")
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) status: Int?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiData<PageResultDto<ActivityTemplateDto>> {
        val query = ActivityTemplateQueryDto(
            name = name,
            status = status,
            page = page,
            size = size
        )
        val pageResult = activityTemplateService.findPage(query)
        return ApiData.success(pageResult)
    }

    /**
     * 获取所有启用的模板列表
     * 
     * 用于活动管理中选择模板时的下拉列表。
     * 只返回状态为启用（status=1）的模板。
     * 
     * @return 启用的模板列表
     */
    @GetMapping("/all")
    @RequiresPermission("activity:template:list")
    fun findAll(): ApiData<List<ActivityTemplateDto>> {
        val templates = activityTemplateService.findAll()
        val enabledTemplates = templates.filter { it.status == 1 }
        return ApiData.success(enabledTemplates)
    }

    /**
     * 根据ID获取模板详情
     * 
     * 通过模板ID获取单个模板的详细信息，包括模板的基本信息和完整的字段配置。
     * 
     * @param id 模板ID，路径参数
     * @return 模板详情，如果模板不存在则返回错误信息
     */
    @GetMapping("/{id}")
    @RequiresPermission("activity:template:view")
    fun detail(@PathVariable id: Long): ApiData<ActivityTemplateDto> {
        val template = activityTemplateService.findById(id)
        return if (template != null) {
            ApiData.success(template)
        } else {
            ApiData.error("活动模板不存在")
        }
    }

    /**
     * 创建新模板
     * 
     * 创建新的活动模板记录，包括模板基本信息和字段配置。
     * 系统会自动设置创建时间、更新时间和默认状态。
     * 
     * @param form 模板表单数据，包含模板的所有配置信息
     * @return 创建成功的模板信息
     */
    @PostMapping
    @RequiresPermission("activity:template:create")
    fun create(@Valid @RequestBody form: ActivityTemplateFormDto): ApiData<ActivityTemplateDto> {
        val template = activityTemplateService.save(form)
        return ApiData.success(template)
    }

    /**
     * 更新模板信息
     * 
     * 根据模板ID更新模板的基本信息和字段配置。
     * 系统会自动更新修改时间，保持创建时间不变。
     * 
     * @param id 模板ID，路径参数
     * @param form 模板表单数据，包含要更新的配置信息
     * @return 更新后的模板信息
     */
    @PutMapping("/{id}")
    @RequiresPermission("activity:template:update")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody form: ActivityTemplateFormDto
    ): ApiData<ActivityTemplateDto> {
        val template = activityTemplateService.update(id, form)
        return ApiData.success(template)
    }

    /**
     * 删除模板
     * 
     * 根据模板ID删除指定的模板记录。
     * 删除操作不可逆，请谨慎操作。
     * 
     * @param id 模板ID，路径参数
     * @return 删除操作结果，true表示删除成功
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("activity:template:delete")
    fun delete(@PathVariable id: Long): ApiData<Boolean> {
        val result = activityTemplateService.delete(id)
        return ApiData.success(result)
    }

    /**
     * 更新模板状态
     * 
     * 启用或禁用指定的模板。只有启用状态的模板才能在活动管理中被选择。
     * 
     * @param id 模板ID，路径参数
     * @param status 模板状态，1-启用，0-禁用
     * @return 更新后的模板信息
     */
    @PutMapping("/{id}/status")
    @RequiresPermission("activity:template:update")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: Int
    ): ApiData<ActivityTemplateDto> {
        val template = activityTemplateService.updateStatus(id, status)
        return ApiData.success(template)
    }

    /**
     * 复制模板
     * 
     * 根据现有模板创建一个新的模板副本，包括所有配置信息。
     * 新模板的名称会自动添加"_副本"后缀，状态为禁用。
     * 
     * @param id 要复制的模板ID，路径参数
     * @return 新创建的模板信息
     */
    @PostMapping("/{id}/copy")
    @RequiresPermission("activity:template:create")
    fun copy(@PathVariable id: Long): ApiData<ActivityTemplateDto> {
        val template = activityTemplateService.copy(id)
        return ApiData.success(template)
    }
}
