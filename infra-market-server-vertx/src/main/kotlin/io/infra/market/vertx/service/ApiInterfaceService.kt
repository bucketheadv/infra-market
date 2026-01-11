package io.infra.market.vertx.service

import com.google.inject.Inject
import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.ApiExecuteRequestDto
import io.infra.market.vertx.dto.ApiExecuteResponseDto
import io.infra.market.vertx.dto.ApiInterfaceDto
import io.infra.market.vertx.dto.ApiInterfaceFormDto
import io.infra.market.vertx.dto.ApiInterfaceQueryDto
import io.infra.market.vertx.dto.ApiParamDto
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.entity.ApiInterface
import io.infra.market.vertx.extensions.awaitForResult
import io.infra.market.vertx.repository.ApiInterfaceDao
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 接口管理服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
class ApiInterfaceService @Inject constructor(
    private val apiInterfaceDao: ApiInterfaceDao,
    private val vertx: Vertx
) {
    
    suspend fun list(query: ApiInterfaceQueryDto): ApiData<PageResultDto<ApiInterfaceDto>> {
        val pageResult = apiInterfaceDao.page(query)
        val interfaceDtos = pageResult.records.map { convertToDto(it) }
        return ApiData.success(PageResultDto(interfaceDtos, pageResult.total, pageResult.page, pageResult.size))
    }
    
    suspend fun getMostUsedInterfaces(days: Int, limit: Int): ApiData<List<ApiInterfaceDto>> {
        val interfaceIds = apiInterfaceDao.findMostUsedInterfaceIds(days, limit)
        
        if (interfaceIds.isEmpty()) {
            return ApiData.success(emptyList())
        }
        
        val interfaces = apiInterfaceDao.findByIds(interfaceIds)
        val interfaceMap = interfaces.associateBy { it.id }
        val result = interfaceIds.mapNotNull { id ->
            interfaceMap[id]?.let { convertToDto(it) }
        }
        return ApiData.success(result)
    }
    
    suspend fun detail(id: Long): ApiData<ApiInterfaceDto?> {
        val apiInterface = apiInterfaceDao.findById(id) ?: return ApiData.error("接口不存在")

        return ApiData.success(convertToDto(apiInterface))
    }
    
    suspend fun create(form: ApiInterfaceFormDto): ApiData<ApiInterfaceDto> {
        val apiInterface = convertToEntity(form)
        apiInterface.createTime = System.currentTimeMillis()
        apiInterface.updateTime = System.currentTimeMillis()
        apiInterface.status = 1

        val id = apiInterfaceDao.save(apiInterface)
        apiInterface.id = id
        return ApiData.success(convertToDto(apiInterface))
    }
    
    suspend fun update(id: Long, form: ApiInterfaceFormDto): ApiData<ApiInterfaceDto> {
        val existingInterface = apiInterfaceDao.findById(id) ?: return ApiData.error("接口不存在")

        val apiInterface = convertToEntity(form)
        apiInterface.id = id
        apiInterface.createTime = existingInterface.createTime
        apiInterface.updateTime = System.currentTimeMillis()
        apiInterface.status = existingInterface.status
        
        apiInterfaceDao.updateById(apiInterface)
        return ApiData.success(convertToDto(apiInterface))
    }
    
    suspend fun delete(id: Long): ApiData<Boolean> {
        apiInterfaceDao.deleteById(id)
        return ApiData.success(true)
    }
    
    suspend fun updateStatus(id: Long, status: Int): ApiData<ApiInterfaceDto> {
        val apiInterface = apiInterfaceDao.findById(id) ?: return ApiData.error("接口不存在")

        apiInterface.status = status
        apiInterface.updateTime = System.currentTimeMillis()
        
        apiInterfaceDao.updateById(apiInterface)
        return ApiData.success(convertToDto(apiInterface))
    }
    
    suspend fun copy(id: Long): ApiData<ApiInterfaceDto> {
        val existingInterface = apiInterfaceDao.findById(id) ?: return ApiData.error("接口不存在")

        val newInterface = existingInterface.copy()
        newInterface.id = null
        newInterface.name = "${existingInterface.name}_副本"
        newInterface.status = 1
        newInterface.createTime = System.currentTimeMillis()
        newInterface.updateTime = System.currentTimeMillis()
        
        val newId = apiInterfaceDao.save(newInterface)
        newInterface.id = newId
        return ApiData.success(convertToDto(newInterface))
    }
    
    suspend fun execute(request: ApiExecuteRequestDto): ApiData<ApiExecuteResponseDto> {
        val startTime = System.currentTimeMillis()
        val interfaceId = request.interfaceId ?: return ApiData.error("接口ID不能为空")

        val apiInterface = apiInterfaceDao.findById(interfaceId) ?: return ApiData.error("接口不存在")

        if (apiInterface.status != 1) {
            return ApiData.error("接口已禁用，无法执行")
        }
        
        val url = apiInterface.url ?: return ApiData.error("接口URL不能为空")
        
        val method = HttpMethod.valueOf(apiInterface.method ?: "GET")
        val timeoutSeconds = request.timeout ?: apiInterface.timeout ?: 60L
        val timeoutMillis = timeoutSeconds * 1000L
        
        val urlParams = request.urlParams
        val headers = request.headers
        val bodyParams = request.bodyParams
        
        // 构建完整的绝对 URL
        val finalUrl = url.trim()
        
        // 解析 URI 以分离基础 URL 和查询参数
        val uri = try {
            URI(finalUrl)
        } catch (e: Exception) {
            return ApiData.error("URL格式错误: ${e.message}")
        }
        
        val scheme = uri.scheme ?: "http"
        val host = uri.host
        val port = if (uri.port != -1) uri.port else (if (scheme == "https") 443 else 80)
        val path = uri.path ?: "/"
        val baseUrl = "$scheme://$host:$port$path"
        
        val vertxInstance = vertx ?: return ApiData.error("HTTP客户端未初始化")
        
        val clientOptions = WebClientOptions()
            .setConnectTimeout(timeoutMillis.toInt())
            .setIdleTimeout(timeoutMillis.toInt())
        
        val webClient = WebClient.create(vertxInstance, clientOptions)
        
        val postType = apiInterface.postType ?: "application/json"
        
        // 创建请求对象
        val httpRequest: HttpRequest<Buffer> = when (method) {
            HttpMethod.GET -> webClient.getAbs(baseUrl)
            HttpMethod.POST -> webClient.postAbs(baseUrl)
            HttpMethod.PUT -> webClient.putAbs(baseUrl)
            HttpMethod.DELETE -> webClient.deleteAbs(baseUrl)
            HttpMethod.PATCH -> webClient.patchAbs(baseUrl)
            else -> webClient.getAbs(baseUrl)
        }
        
        // 添加现有的查询参数
        if (uri.query != null) {
            uri.query.split("&").forEach { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    httpRequest.addQueryParam(parts[0], URLDecoder.decode(parts[1], "UTF-8"))
                }
            }
        }
        
        // 添加新的 URL 参数
        urlParams?.forEach { (key, value) ->
            if (value.toString().isNotBlank()) {
                httpRequest.addQueryParam(key, value.toString())
            }
        }
        
        // 设置请求头
        headers?.forEach { (key, value) ->
            httpRequest.putHeader(key, value)
        }
        
        // 发送请求
        val response = try {
            if (method != HttpMethod.GET && !bodyParams.isNullOrEmpty()) {
                if (postType == "application/x-www-form-urlencoded") {
                    val formData = bodyParams.entries.joinToString("&") { (key, value) ->
                        "${encodeURIComponent(key)}=${encodeURIComponent(value.toString())}"
                    }
                    httpRequest.putHeader("Content-Type", postType)
                        .sendBuffer(Buffer.buffer(formData))
                        .awaitForResult()
                } else {
                    val bodyJson = JsonObject()
                    bodyParams.forEach { (key, value) ->
                        bodyJson.put(key, value)
                    }
                    httpRequest.putHeader("Content-Type", "application/json")
                        .sendJsonObject(bodyJson)
                        .awaitForResult()
                }
            } else {
                httpRequest.send().awaitForResult()
            }
        } catch (error: Exception) {
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            return ApiData.success(ApiExecuteResponseDto(
                status = 500,
                headers = emptyMap(),
                body = null,
                responseTime = responseTime,
                success = false,
                error = error.message ?: "请求失败"
            ))
        }
        
        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime
        val statusCode = response.statusCode()
        
        return ApiData.success(ApiExecuteResponseDto(
            status = statusCode,
            headers = emptyMap(),
            body = response.bodyAsString(),
            responseTime = responseTime,
            success = statusCode in 200..299
        ))
    }
    
    private fun convertToDto(entity: ApiInterface): ApiInterfaceDto {
        val (urlParams, headerParams, bodyParams) = parseAndSeparateParams(entity.params)
        
        return ApiInterfaceDto(
            id = entity.id,
            name = entity.name,
            method = entity.method,
            url = entity.url,
            description = entity.description,
            status = entity.status,
            createTime = entity.createTime,
            updateTime = entity.updateTime,
            postType = entity.postType,
            environment = entity.environment,
            timeout = entity.timeout,
            valuePath = entity.valuePath,
            urlParams = urlParams,
            headerParams = headerParams,
            bodyParams = bodyParams
        )
    }
    
    private fun convertToEntity(form: ApiInterfaceFormDto): ApiInterface {
        val allParams = mutableListOf<ApiParamDto>()
        form.urlParams?.let { allParams.addAll(it) }
        form.headerParams?.let { allParams.addAll(it) }
        form.bodyParams?.let { allParams.addAll(it) }
        
        val paramsJson = try {
            if (allParams.isEmpty()) {
                null
            } else {
                JsonArray(allParams.map { JsonObject.mapFrom(it) }).encode()
            }
        } catch (_: Exception) {
            null
        }
        
        return ApiInterface(
            name = form.name,
            method = form.method,
            url = form.url,
            description = form.description,
            postType = form.postType,
            environment = form.environment,
            timeout = form.timeout,
            valuePath = form.valuePath,
            params = paramsJson
        )
    }
    
    /**
     * 解析并分离参数
     */
    private fun parseAndSeparateParams(paramsJson: String?): Triple<List<ApiParamDto>, List<ApiParamDto>, List<ApiParamDto>> {
        val allParams = try {
            if (paramsJson.isNullOrBlank()) {
                emptyList()
            } else {
                val paramsArray = JsonArray(paramsJson)
                (0 until paramsArray.size()).mapNotNull { i ->
                    try {
                        paramsArray.getJsonObject(i).mapTo(ApiParamDto::class.java)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
        
        val urlParams = allParams.filter { it.paramType == "URL_PARAM" }
        val headerParams = allParams.filter { it.paramType == "HEADER_PARAM" }
        val bodyParams = allParams.filter { it.paramType == "BODY_PARAM" }
        
        return Triple(urlParams, headerParams, bodyParams)
    }
    
    private fun encodeURIComponent(str: String): String {
        return URLEncoder.encode(str, "UTF-8")
    }
}
