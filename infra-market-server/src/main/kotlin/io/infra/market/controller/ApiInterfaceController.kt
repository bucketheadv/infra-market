package io.infra.market.controller

import io.infra.market.dto.*
import io.infra.market.service.ApiInterfaceService
import io.infra.market.annotation.RequiresPermission
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

/**
 * 接口管理控制器
 */
@RestController
@RequestMapping("/api/interface")
class ApiInterfaceController(
    private val apiInterfaceService: ApiInterfaceService
) {

    @GetMapping("/list")
    @RequiresPermission("interface:list")
    fun getList(@RequestParam(required = false) name: String?,
                @RequestParam(required = false) method: String?,
                @RequestParam(required = false) status: Int?): ResponseEntity<ApiResponse<List<ApiInterfaceDto>>> {
        val query = ApiInterfaceQueryDto(
            name = name,
            method = io.infra.market.enums.HttpMethodEnum.fromCode(method ?: ""),
            status = status
        )
        val interfaces = apiInterfaceService.findAll(query)
        return ResponseEntity.ok(ApiResponse.success(interfaces))
    }

    @GetMapping("/{id}")
    @RequiresPermission("interface:view")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<ApiInterfaceDto>> {
        val apiInterface = apiInterfaceService.findById(id)
        return if (apiInterface != null) {
            ResponseEntity.ok(ApiResponse.success(apiInterface))
        } else {
            ResponseEntity.ok(ApiResponse.error("接口不存在"))
        }
    }

    @PostMapping
    @RequiresPermission("interface:create")
    fun create(@Valid @RequestBody form: ApiInterfaceFormDto): ResponseEntity<ApiResponse<ApiInterfaceDto>> {
        val apiInterface = apiInterfaceService.save(form)
        return ResponseEntity.ok(ApiResponse.success(apiInterface))
    }

    @PutMapping("/{id}")
    @RequiresPermission("interface:update")
    fun update(@PathVariable id: Long, @Valid @RequestBody form: ApiInterfaceFormDto): ResponseEntity<ApiResponse<ApiInterfaceDto>> {
        val apiInterface = apiInterfaceService.update(id, form)
        return ResponseEntity.ok(ApiResponse.success(apiInterface))
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("interface:delete")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<Boolean>> {
        val result = apiInterfaceService.delete(id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping("/execute")
    @RequiresPermission("interface:execute")
    fun execute(@Valid @RequestBody request: ApiExecuteRequestDto): ResponseEntity<ApiResponse<ApiExecuteResponseDto>> {
        val response = apiInterfaceService.execute(request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
