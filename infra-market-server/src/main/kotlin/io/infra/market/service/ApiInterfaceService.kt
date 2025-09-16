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
import io.infra.market.util.AuthThreadLocal
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.client.SimpleClientHttpRequestFactory
import java.util.Date
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import java.util.concurrent.TimeUnit

/**
 * 接口管理服务
 */
@Service
class ApiInterfaceService(
    private val apiInterfaceDao: ApiInterfaceDao,
    private val apiInterfaceExecutionRecordDao: ApiInterfaceExecutionRecordDao,
    private val userDao: UserDao,
    private val objectMapper: ObjectMapper
) {
    
    /**
     * RestTemplate缓存，避免重复创建
     * Key: 超时时间（毫秒），Value: RestTemplate实例
     * 使用Caffeine缓存，设置最大缓存100个实例，30分钟过期
     */
    private val restTemplateCache: Cache<Long, RestTemplate> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build()

    fun findAll(query: ApiInterfaceQueryDto): List<ApiInterfaceDto> {
        val interfaces = apiInterfaceDao.findByCondition(query)
        return interfaces.map { convertToDto(it) }
    }

    fun findById(id: Long): ApiInterfaceDto? {
        val apiInterface = apiInterfaceDao.getById(id)
        return apiInterface?.let { convertToDto(it) }
    }

    @Transactional
    fun save(form: ApiInterfaceFormDto): ApiInterfaceDto {
        // 验证POST类型字段
        validatePostType(form)
        
        val apiInterface = convertToEntity(form)
        apiInterface.createTime = Date()
        apiInterface.updateTime = Date()
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
        apiInterface.updateTime = Date()
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
        existingInterface.updateTime = Date()
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
        newInterface.createTime = Date()
        newInterface.updateTime = Date()
        newInterface.status = 1

        apiInterfaceDao.save(newInterface)
        return convertToDto(newInterface)
    }

    fun execute(request: ApiExecuteRequestDto, httpRequest: HttpServletRequest? = null): ApiExecuteResponseDto {
        val startTime = System.currentTimeMillis()
        
        // 获取当前用户信息
        val executorId = AuthThreadLocal.getCurrentUserId()
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
                        headers.set("Content-Type", "application/x-www-form-urlencoded")
                    }
                    PostTypeEnum.APPLICATION_JSON, null -> {
                        // JSON格式（默认）
                        body = objectMapper.writeValueAsString(bodyParams)
                        headers.set("Content-Type", "application/json")
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

            val response: ResponseEntity<String> = timeoutRestTemplate.exchange(
                finalUrl,
                httpMethod,
                httpEntity,
                String::class.java
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

            // 记录执行记录
            saveExecutionRecord(
                interfaceId = interfaceId,
                executorId = executorId,
                executorName = executorName,
                request = request,
                response = responseDto,
                clientIp = clientIp,
                userAgent = userAgent
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
                    userAgent = userAgent
                )
            } catch (recordException: Exception) {
                // 记录执行记录失败不影响主流程
                println("保存执行记录失败: ${recordException.message}")
            }

            return responseDto
        }
    }

    private fun encodeURIComponent(str: String): String {
        return java.net.URLEncoder.encode(str, "UTF-8")
    }

    private fun convertToDto(entity: ApiInterface): ApiInterfaceDto {
        val allParams = try {
            if (entity.params.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(entity.params, Array<ApiParamDto>::class.java).toList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        // 分离不同类型的参数
        val urlParams = allParams.filter { it.paramType?.code == "URL_PARAM" }
        val headerParams = allParams.filter { it.paramType?.code == "HEADER_PARAM" }
        val bodyParams = allParams.filter { it.paramType?.code == "BODY_PARAM" }

        return ApiInterfaceDto(
            id = entity.id,
            name = entity.name,
            method = HttpMethodEnum.fromCode(entity.method ?: ""),
            url = entity.url,
            description = entity.description,
            status = entity.status,
            createTime = entity.createTime,
            updateTime = entity.updateTime,
            postType = entity.postType?.let { PostTypeEnum.fromCode(it) },
            environment = entity.environment?.let { EnvironmentEnum.fromCode(it) },
            timeout = entity.timeout,
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
        } catch (e: Exception) {
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
        // 解析接口参数配置
        val allParams = try {
            if (interfaceInfo.params.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(interfaceInfo.params, Array<ApiParamDto>::class.java).toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
        
        // 分离不同类型的参数
        val urlParams = allParams.filter { it.paramType?.code == "URL_PARAM" }
        val headerParams = allParams.filter { it.paramType?.code == "HEADER_PARAM" }
        val bodyParams = allParams.filter { it.paramType?.code == "BODY_PARAM" }
        
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
                val paramValue = requestParams?.get(paramName)
                
                // 检查参数值是否为空或空字符串
                val isEmpty = when (paramValue) {
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
        userAgent: String
    ) {
        try {
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
                clientIp = clientIp,
                userAgent = userAgent
            )

            apiInterfaceExecutionRecordDao.save(executionRecord)
        } catch (e: Exception) {
            // 记录执行记录失败不影响主流程，只打印日志
            println("保存接口执行记录失败: ${e.message}")
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
