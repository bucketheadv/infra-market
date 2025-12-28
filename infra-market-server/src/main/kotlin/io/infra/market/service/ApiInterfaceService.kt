package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.ApiInterfaceDao
import io.infra.market.repository.dao.ApiInterfaceExecutionRecordDao
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.entity.ApiInterface
import io.infra.market.repository.entity.ApiInterfaceExecutionRecord
import io.infra.market.enums.HttpMethodEnum
import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.market.enums.PostTypeEnum
import io.infra.market.enums.EnvironmentEnum
import io.infra.market.util.AuthHolder
import io.infra.market.util.TimeUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.client.SimpleClientHttpRequestFactory
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import java.util.concurrent.TimeUnit
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.infra.market.enums.ParamTypeEnum
import io.infra.structure.core.utils.Loggable
import org.springframework.web.client.exchange
import java.net.URLEncoder

/**
 * 接口管理服务
 */
@Service
class ApiInterfaceService(
    private val apiInterfaceDao: ApiInterfaceDao,
    private val apiInterfaceExecutionRecordDao: ApiInterfaceExecutionRecordDao,
    private val userDao: UserDao,
    private val objectMapper: ObjectMapper
) : Loggable {

    /**
     * RestTemplate缓存，避免重复创建
     * Key: 超时时间（毫秒），Value: RestTemplate实例
     * 使用Caffeine缓存，设置最大缓存100个实例，30分钟过期
     */
    private val restTemplateCache: Cache<Long, RestTemplate> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build()

    fun findPage(query: ApiInterfaceQueryDto): PageResultDto<ApiInterfaceDto> {
        val page = apiInterfaceDao.page(query)
        val interfaceDtos = page.records.map { convertToDto(it) }
        
        return PageResultDto(
            records = interfaceDtos,
            total = page.totalRow,
            current = page.pageNumber,
            size = page.pageSize
        )
    }

    fun findById(id: Long): ApiInterfaceDto? {
        val apiInterface = apiInterfaceDao.getById(id)
        return apiInterface?.let { convertToDto(it) }
    }

    /**
     * 获取最近最热门的接口
     * 
     * @param days 统计最近多少天的数据
     * @param limit 返回的接口数量
     * @return 热门接口列表
     */
    fun findMostUsedInterfaces(days: Int = 30, limit: Int = 5): List<ApiInterfaceDto> {
        // 查询最近使用最多的接口ID
        val interfaceIds = apiInterfaceExecutionRecordDao.findMostUsedInterfaceIds(days, limit)
        
        if (interfaceIds.isEmpty()) {
            return emptyList()
        }
        
        // 根据ID列表查询接口详情
        val interfaces = apiInterfaceDao.findByIds(interfaceIds)
        
        // 按照interfaceIds的顺序排序结果
        val interfaceMap = interfaces.associateBy { it.id }
        return interfaceIds.mapNotNull { id -> 
            interfaceMap[id]?.let { convertToDto(it) }
        }
    }

    @Transactional
    fun save(form: ApiInterfaceFormDto): ApiInterfaceDto {
        // 验证POST类型字段
        validatePostType(form)
        
        val apiInterface = convertToEntity(form)
        apiInterface.createTime = System.currentTimeMillis()
        apiInterface.updateTime = System.currentTimeMillis()
        apiInterface.status = 1

        apiInterfaceDao.save(apiInterface)
        return convertToDto(apiInterface)
    }

    @Transactional
    fun update(id: Long, form: ApiInterfaceFormDto): ApiInterfaceDto {
        // 验证POST类型字段
        validatePostType(form)
        
        val existingInterface = apiInterfaceDao.getById(id)
            ?: throw RuntimeException("接口不存在")

        val apiInterface = convertToEntity(form)
        apiInterface.id = id
        apiInterface.createTime = existingInterface.createTime
        apiInterface.updateTime = System.currentTimeMillis()
        apiInterface.status = existingInterface.status

        apiInterfaceDao.updateById(apiInterface, false)
        return convertToDto(apiInterface)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return apiInterfaceDao.removeById(id)
    }

    @Transactional
    fun updateStatus(id: Long, status: Int): ApiInterfaceDto {
        val existingInterface = apiInterfaceDao.getById(id)
            ?: throw RuntimeException("接口不存在")

        existingInterface.status = status
        existingInterface.updateTime = System.currentTimeMillis()
        apiInterfaceDao.updateById(existingInterface)
        
        return convertToDto(existingInterface)
    }

    @Transactional
    fun copy(id: Long): ApiInterfaceDto {
        val existingInterface = apiInterfaceDao.getById(id)
            ?: throw RuntimeException("接口不存在")

        val newInterface = existingInterface.copy()
        newInterface.id = null
        newInterface.name = "${existingInterface.name}_副本"
        newInterface.createTime = System.currentTimeMillis()
        newInterface.updateTime = System.currentTimeMillis()
        newInterface.status = 1

        apiInterfaceDao.save(newInterface)
        return convertToDto(newInterface)
    }

    fun execute(request: ApiExecuteRequestDto, httpRequest: HttpServletRequest? = null): ApiExecuteResponseDto {
        val startTime = System.currentTimeMillis()
        
        // 获取当前用户信息
        val executorId = AuthHolder.getUid()
        val executorName = if (executorId != null) {
            val user = userDao.findByUid(executorId)
            user?.username ?: "用户${executorId}"
        } else {
            "未知用户"
        }
        
        // 获取客户端信息
        val clientIp = httpRequest?.remoteAddr ?: "未知IP"
        val userAgent = httpRequest?.getHeader("User-Agent") ?: "未知User-Agent"
        
        try {
            // 从数据库获取接口信息
            val interfaceId = request.interfaceId ?: throw RuntimeException("接口ID不能为空")
            val interfaceInfo = apiInterfaceDao.getById(interfaceId)
                ?: throw RuntimeException("接口不存在")
            
            // 检查接口状态
            if (interfaceInfo.status != 1) {
                return ApiExecuteResponseDto(
                    status = 403,
                    success = false,
                    error = "接口已禁用，无法执行",
                    responseTime = System.currentTimeMillis() - startTime
                )
            }
            
            // 验证必填参数
            validateRequiredParams(interfaceInfo, request)
            
            // 从数据库读取接口基本信息
            val url = interfaceInfo.url ?: throw RuntimeException("接口URL不能为空")
            val method = HttpMethodEnum.fromCode(interfaceInfo.method ?: "GET")
            val postType = interfaceInfo.postType?.let { PostTypeEnum.fromCode(it) }
            
            val headers = HttpHeaders()
            request.headers?.forEach { (key, value) ->
                headers.add(key, value)
            }

            // 构建URL参数
            var finalUrl = url
            request.urlParams?.let { urlParams ->
                if (urlParams.isNotEmpty()) {
                    val paramString = urlParams.entries
                        .filter { (_, value) -> value.toString().isNotBlank() }
                        .joinToString("&") { (key, value) -> 
                            "$key=${encodeURIComponent(value.toString())}" 
                        }
                    if (paramString.isNotEmpty()) {
                        finalUrl += (if (finalUrl.contains("?")) "&" else "?") + paramString
                    }
                }
            }

            // 构建请求体
            var body: String? = null
            if (method != HttpMethodEnum.GET && request.bodyParams?.isNotEmpty() == true) {
                val bodyParams = request.bodyParams!!
                when (postType) {
                    PostTypeEnum.APPLICATION_X_WWW_FORM_URLENCODED -> {
                        // 表单格式
                        body = bodyParams.entries
                            .filter { (_, value) -> value.toString().isNotBlank() }
                            .joinToString("&") { (key, value) -> 
                                "${encodeURIComponent(key)}=${encodeURIComponent(value.toString())}" 
                            }
                        headers.set("Content-Type", postType.code)
                    }
                    PostTypeEnum.APPLICATION_JSON, null -> {
                        // JSON格式（默认）
                        body = objectMapper.writeValueAsString(bodyParams)
                        headers.set("Content-Type", postType?.code ?: PostTypeEnum.APPLICATION_JSON.code)
                    }
                }
            }

            val httpMethod = HttpMethod.valueOf(method!!.code)
            val httpEntity = HttpEntity(body, headers)

            // 确定超时时间：请求中的超时时间 > 接口配置的超时时间 > 系统默认超时时间
            // 注意：接口配置和请求中的超时时间单位为秒，需要转换为毫秒
            val timeoutSeconds = request.timeout ?: interfaceInfo.timeout ?: 60L
            val timeoutMillis = timeoutSeconds * 1000L
            
            // 创建带超时配置的RestTemplate
            val timeoutRestTemplate = createRestTemplateWithTimeout(timeoutMillis)

            val response: ResponseEntity<String> = timeoutRestTemplate.exchange<String>(
                finalUrl,
                httpMethod,
                httpEntity
            )

            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime

            val responseDto = ApiExecuteResponseDto(
                status = response.statusCode.value(),
                headers = response.headers.toSingleValueMap(),
                body = response.body,
                responseTime = responseTime,
                success = true
            )
            
            // 如果接口配置了取值路径，尝试提取值
            val valuePath = interfaceInfo.valuePath
            if (!valuePath.isNullOrBlank() && !response.body.isNullOrBlank()) {
                try {
                    val extractedValue = extractValueByPath(response.body, valuePath)
                    responseDto.extractedValue = extractedValue
                } catch (e: Exception) {
                    // 提取失败时记录日志，但不影响整体执行结果
                    log.warn("JSONPath提取失败: ${e.message}, 路径: $valuePath", e)
                }
            }

            // 记录执行记录
            saveExecutionRecord(
                interfaceId = interfaceId,
                executorId = executorId,
                executorName = executorName,
                request = request,
                response = responseDto,
                clientIp = clientIp,
                userAgent = userAgent,
                remark = request.remark
            )

            return responseDto
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime

            val responseDto = ApiExecuteResponseDto(
                status = 500,
                headers = emptyMap(),
                body = null,
                responseTime = responseTime,
                success = false,
                error = e.message
            )

            // 记录执行记录（失败情况）
            try {
                saveExecutionRecord(
                    interfaceId = request.interfaceId,
                    executorId = executorId,
                    executorName = executorName,
                    request = request,
                    response = responseDto,
                    clientIp = clientIp,
                    userAgent = userAgent,
                    remark = request.remark
                )
            } catch (recordException: Exception) {
                // 记录执行记录失败不影响主流程
                log.error("保存执行记录失败: ${recordException.message}", recordException)
            }

            return responseDto
        }
    }

    private fun encodeURIComponent(str: String): String {
        return URLEncoder.encode(str, "UTF-8")
    }
    
    /**
     * 解析并分离接口参数
     * 
     * @param paramsJson 参数JSON字符串
     * @return Triple(urlParams, headerParams, bodyParams) 分离后的参数列表
     */
    private fun parseAndSeparateParams(paramsJson: String?): Triple<List<ApiParamDto>, List<ApiParamDto>, List<ApiParamDto>> {
        val allParams = try {
            if (paramsJson.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(paramsJson, Array<ApiParamDto>::class.java).toList()
            }
        } catch (_: Exception) {
            emptyList()
        }

        // 分离不同类型的参数
        val urlParams = allParams.filter { it.paramType == ParamTypeEnum.URL_PARAM }
        val headerParams = allParams.filter { it.paramType == ParamTypeEnum.HEADER_PARAM }
        val bodyParams = allParams.filter { it.paramType == ParamTypeEnum.BODY_PARAM }
        
        return Triple(urlParams, headerParams, bodyParams)
    }

    private fun convertToDto(entity: ApiInterface): ApiInterfaceDto {
        val (urlParams, headerParams, bodyParams) = parseAndSeparateParams(entity.params)

        return ApiInterfaceDto(
            id = entity.id,
            name = entity.name,
            method = HttpMethodEnum.fromCode(entity.method ?: ""),
            url = entity.url,
            description = entity.description,
            status = entity.status,
            createTime = TimeUtil.format(entity.createTime),
            updateTime = TimeUtil.format(entity.updateTime),
            postType = entity.postType?.let { PostTypeEnum.fromCode(it) },
            environment = entity.environment?.let { EnvironmentEnum.fromCode(it) },
            timeout = entity.timeout,
            valuePath = entity.valuePath,
            urlParams = urlParams,
            headerParams = headerParams,
            bodyParams = bodyParams
        )
    }

    private fun convertToEntity(form: ApiInterfaceFormDto): ApiInterface {
        // 合并所有参数
        val allParams = mutableListOf<ApiParamDto>()
        form.urlParams?.let { allParams.addAll(it) }
        form.headerParams?.let { allParams.addAll(it) }
        form.bodyParams?.let { allParams.addAll(it) }

        val paramsJson = try {
            if (allParams.isEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(allParams)
            }
        } catch (_: Exception) {
            null
        }

        return ApiInterface(
            name = form.name,
            method = form.method?.code,
            url = form.url,
            description = form.description,
            postType = form.postType?.code,
            environment = form.environment?.code,
            timeout = form.timeout,
            valuePath = form.valuePath,
            params = paramsJson
        )
    }
    
    /**
     * 验证POST类型字段
     */
    private fun validatePostType(form: ApiInterfaceFormDto) {
        // 当请求方法为POST、PUT、PATCH时，POST类型为必填项
        if (form.method in listOf(HttpMethodEnum.POST, HttpMethodEnum.PUT, HttpMethodEnum.PATCH)) {
            if (form.postType == null) {
                throw RuntimeException("POST类型为必填项")
            }
        }
    }
    
    /**
     * 验证必填参数
     */
    private fun validateRequiredParams(interfaceInfo: ApiInterface, request: ApiExecuteRequestDto) {
        val (urlParams, headerParams, bodyParams) = parseAndSeparateParams(interfaceInfo.params)
        
        // 验证URL参数
        validateParams(urlParams, request.urlParams, "URL参数")
        
        // 验证Header参数
        validateParams(headerParams, request.headers, "Header参数")
        
        // 验证Body参数
        validateParams(bodyParams, request.bodyParams, "Body参数")
    }
    
    /**
     * 验证参数列表
     */
    private fun validateParams(
        paramConfigs: List<ApiParamDto>, 
        requestParams: Map<String, Any>?, 
        paramTypeName: String
    ) {
        paramConfigs.forEach { param ->
            // 只验证必填参数
            if (param.required == true) {
                val paramName = param.name ?: return@forEach
                // 检查参数值是否为空或空字符串
                val isEmpty = when (val paramValue = requestParams?.get(paramName)) {
                    null -> true
                    is String -> paramValue.trim().isEmpty()
                    is Collection<*> -> paramValue.isEmpty()
                    else -> false
                }
                
                if (isEmpty) {
                    val displayName = if (param.chineseName.isNullOrBlank()) {
                        paramName
                    } else {
                        "${param.chineseName}（${paramName}）"
                    }
                    throw RuntimeException("$paramTypeName $displayName 为必填项，不能为空")
                }
            }
        }
    }

    /**
     * 保存接口执行记录
     * 
     * @param interfaceId 接口ID
     * @param executorId 执行人ID
     * @param executorName 执行人姓名
     * @param request 执行请求
     * @param response 执行响应
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     */
    private fun saveExecutionRecord(
        interfaceId: Long?,
        executorId: Long?,
        executorName: String,
        request: ApiExecuteRequestDto,
        response: ApiExecuteResponseDto,
        clientIp: String,
        userAgent: String,
        remark: String? = null
    ) {
        try {
            val now = System.currentTimeMillis()
            val executionRecord = ApiInterfaceExecutionRecord(
                interfaceId = interfaceId,
                executorId = executorId,
                executorName = executorName,
                requestParams = objectMapper.writeValueAsString(request.urlParams),
                requestHeaders = objectMapper.writeValueAsString(request.headers),
                requestBody = objectMapper.writeValueAsString(request.bodyParams),
                responseStatus = response.status,
                responseHeaders = objectMapper.writeValueAsString(response.headers),
                responseBody = response.body,
                executionTime = response.responseTime,
                success = response.success,
                errorMessage = response.error,
                remark = remark,
                clientIp = clientIp,
                userAgent = userAgent
            )
            
            // 设置创建时间和更新时间
            executionRecord.createTime = now
            executionRecord.updateTime = now

            apiInterfaceExecutionRecordDao.save(executionRecord)
        } catch (e: Exception) {
            // 记录执行记录失败不影响主流程，只打印日志
            log.error("保存接口执行记录失败: ${e.message}", e)
        }
    }
    
    /**
     * 根据JSONPath提取值
     * 
     * @param jsonString JSON字符串
     * @param path JSONPath表达式
     * @return 提取的值，如果提取失败返回null
     */
    private fun extractValueByPath(jsonString: String?, path: String): String? {
        return try {
            val document = JsonPath.parse(jsonString)
            // 将结果转换为字符串
            when (val result = document.read<Any>(path)) {
                is String -> result
                is Number -> result.toString()
                is Boolean -> result.toString()
                is List<*> -> objectMapper.writeValueAsString(result)
                is Map<*, *> -> objectMapper.writeValueAsString(result)
                null -> null
                else -> result.toString()
            }
        } catch (_: PathNotFoundException) {
            null
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * 创建带超时配置的RestTemplate（使用Caffeine缓存）
     * 
     * @param timeout 超时时间（毫秒）
     * @return 配置了超时时间的RestTemplate实例
     */
    private fun createRestTemplateWithTimeout(timeout: Long): RestTemplate {
        return restTemplateCache.get(timeout) { timeoutMillis ->
            val factory = SimpleClientHttpRequestFactory()
            factory.setConnectTimeout(timeoutMillis.toInt())
            factory.setReadTimeout(timeoutMillis.toInt())
            RestTemplate(factory)
        }
    }
}
