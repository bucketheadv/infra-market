package io.infra.market.controller

import io.infra.market.dto.*
import io.infra.market.service.ApiInterfaceService
import io.infra.market.annotation.RequiresPermission
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 接口管理控制器
 * 
 * 提供接口管理的RESTful API，包括接口的CRUD操作和接口执行功能。
 * 所有接口都需要相应的权限验证。
 * 
 * 主要功能：
 * - 接口列表查询（支持多条件筛选）
 * - 接口详情查看
 * - 接口创建和更新
 * - 接口删除
 * - 接口状态管理
 * - 接口复制
 * - 接口执行测试
 * 
 * @author liuqinglin
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/interface")
class ApiInterfaceController(
    private val apiInterfaceService: ApiInterfaceService
) {

    /**
     * 获取接口列表
     * 
     * 支持多条件筛选查询接口列表，包括按名称、方法、状态、标签等条件进行筛选。
     * 所有查询参数都是可选的，支持组合查询。
     * 
     * @param name 接口名称，支持模糊查询
     * @param method HTTP请求方法，如GET、POST等
     * @param status 接口状态，1-启用，0-禁用
     * @param tag 接口标签，如测试环境、正式环境
     * @return 接口列表，包含接口的基本信息和参数配置
     */
    @GetMapping("/list")
    @RequiresPermission("interface:list")
    fun getList(@RequestParam(required = false) name: String?,
                @RequestParam(required = false) method: String?,
                @RequestParam(required = false) status: Int?,
                @RequestParam(required = false) tag: String?): ApiResponse<List<ApiInterfaceDto>> {
        val query = ApiInterfaceQueryDto(
            name = name,
            method = io.infra.market.enums.HttpMethodEnum.fromCode(method ?: ""),
            status = status,
            environment = io.infra.market.enums.EnvironmentEnum.fromCode(tag ?: "")
        )
        val interfaces = apiInterfaceService.findAll(query)
        return ApiResponse.success(interfaces)
    }

    /**
     * 根据ID获取接口详情
     * 
     * 通过接口ID获取单个接口的详细信息，包括接口的基本信息和完整的参数配置。
     * 
     * @param id 接口ID，路径参数
     * @return 接口详情，如果接口不存在则返回错误信息
     */
    @GetMapping("/{id}")
    @RequiresPermission("interface:view")
    fun getById(@PathVariable id: Long): ApiResponse<ApiInterfaceDto> {
        val apiInterface = apiInterfaceService.findById(id)
        return if (apiInterface != null) {
            ApiResponse.success(apiInterface)
        } else {
            ApiResponse.error("接口不存在")
        }
    }

    /**
     * 创建新接口
     * 
     * 创建新的接口记录，包括接口基本信息和参数配置。
     * 系统会自动设置创建时间、更新时间和默认状态。
     * 
     * @param form 接口表单数据，包含接口的所有配置信息
     * @return 创建成功的接口信息
     */
    @PostMapping
    @RequiresPermission("interface:create")
    fun create(@Valid @RequestBody form: ApiInterfaceFormDto): ApiResponse<ApiInterfaceDto> {
        val apiInterface = apiInterfaceService.save(form)
        return ApiResponse.success(apiInterface)
    }

    /**
     * 更新接口信息
     * 
     * 根据接口ID更新接口的基本信息和参数配置。
     * 系统会自动更新修改时间，保持创建时间不变。
     * 
     * @param id 接口ID，路径参数
     * @param form 接口表单数据，包含要更新的配置信息
     * @return 更新后的接口信息
     */
    @PutMapping("/{id}")
    @RequiresPermission("interface:update")
    fun update(@PathVariable id: Long, @Valid @RequestBody form: ApiInterfaceFormDto): ApiResponse<ApiInterfaceDto> {
        val apiInterface = apiInterfaceService.update(id, form)
        return ApiResponse.success(apiInterface)
    }

    /**
     * 删除接口
     * 
     * 根据接口ID删除指定的接口记录。
     * 删除操作不可逆，请谨慎操作。
     * 
     * @param id 接口ID，路径参数
     * @return 删除操作结果，true表示删除成功
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("interface:delete")
    fun delete(@PathVariable id: Long): ApiResponse<Boolean> {
        val result = apiInterfaceService.delete(id)
        return ApiResponse.success(result)
    }

    /**
     * 更新接口状态
     * 
     * 启用或禁用指定的接口。只有启用状态的接口才能被执行。
     * 
     * @param id 接口ID，路径参数
     * @param status 接口状态，1-启用，0-禁用
     * @return 更新后的接口信息
     */
    @PutMapping("/{id}/status")
    @RequiresPermission("interface:update")
    fun updateStatus(@PathVariable id: Long, @RequestParam status: Int): ApiResponse<ApiInterfaceDto> {
        val apiInterface = apiInterfaceService.updateStatus(id, status)
        return ApiResponse.success(apiInterface)
    }

    /**
     * 复制接口
     * 
     * 根据现有接口创建一个新的接口副本，包括所有配置信息。
     * 新接口的名称会自动添加"_副本"后缀，状态为启用。
     * 
     * @param id 要复制的接口ID，路径参数
     * @return 新创建的接口信息
     */
    @PostMapping("/{id}/copy")
    @RequiresPermission("interface:create")
    fun copy(@PathVariable id: Long): ApiResponse<ApiInterfaceDto> {
        val apiInterface = apiInterfaceService.copy(id)
        return ApiResponse.success(apiInterface)
    }

    /**
     * 执行接口测试
     * 
     * 根据接口配置和提供的参数值，实际调用目标接口并返回执行结果。
     * 支持参数校验、请求构建、响应解析等完整流程。
     * 
     * 执行流程：
     * 1. 验证接口存在性和状态
     * 2. 校验必填参数
     * 3. 构建HTTP请求
     * 4. 发送请求并获取响应
     * 5. 返回执行结果
     * 
     * @param request 接口执行请求，包含接口ID和参数值
     * @return 接口执行结果，包括响应状态、响应头、响应体等信息
     */
    @PostMapping("/execute")
    @RequiresPermission("interface:execute")
    fun execute(
        @Valid @RequestBody request: ApiExecuteRequestDto,
        httpRequest: HttpServletRequest
    ): ApiResponse<ApiExecuteResponseDto> {
        val response = apiInterfaceService.execute(request, httpRequest)
        return ApiResponse.success(response)
    }
}
