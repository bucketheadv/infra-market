package io.infra.market.vertx.service

import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.entity.ApiInterface
import io.infra.market.vertx.repository.ApiInterfaceDao
import io.infra.market.vertx.extensions.awaitForResult
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
class ApiInterfaceService(
    private val apiInterfaceDao: ApiInterfaceDao,
    private val vertx: Vertx? = null
) {
    
    suspend fun list(name: String?, method: String?, status: Int?, environment: String?, page: Int, size: Int): ApiData<PageResultDto<JsonObject>> {
        val (interfaces, total) = apiInterfaceDao.page(name, method, status, environment, page, size)
        val interfaceDtos = interfaces.map { it.toJsonObject() }
        return ApiData.success(PageResultDto(interfaceDtos, total, page.toLong(), size.toLong()))
    }
    
    suspend fun getMostUsedInterfaces(days: Int, limit: Int): ApiData<List<JsonObject>> {
        val interfaceIds = apiInterfaceDao.findMostUsedInterfaceIds(days, limit)
        
        if (interfaceIds.isEmpty()) {
            return ApiData.success(emptyList())
        }
        
        val interfaces = apiInterfaceDao.findByIds(interfaceIds)
        val interfaceMap = interfaces.associateBy { it.id }
        val result = interfaceIds.mapNotNull { id ->
            interfaceMap[id]?.toJsonObject()
        }
        return ApiData.success(result)
    }
    
    suspend fun detail(id: Long): ApiData<JsonObject?> {
        val apiInterface = apiInterfaceDao.findById(id) ?: return ApiData.error("接口不存在")

        return ApiData.success(apiInterface.toJsonObject())
    }
    
    suspend fun create(body: JsonObject): ApiData<JsonObject> {
        val apiInterface = ApiInterface(
            name = body.getString("name"),
            method = body.getString("method"),
            url = body.getString("url"),
            description = body.getString("description"),
            postType = body.getString("postType"),
            params = body.getString("params"),
            status = body.getInteger("status") ?: 1,
            environment = body.getString("environment"),
            timeout = body.getLong("timeout"),
            valuePath = body.getString("valuePath")
        )
        
        val id = apiInterfaceDao.save(apiInterface)
        apiInterface.id = id
        return ApiData.success(apiInterface.toJsonObject())
    }
    
    suspend fun update(id: Long, body: JsonObject): ApiData<JsonObject> {
        val existingInterface = apiInterfaceDao.findById(id)
        
        if (existingInterface == null) {
            return ApiData.error("接口不存在")
        }
        
        existingInterface.name = body.getString("name") ?: existingInterface.name
        existingInterface.method = body.getString("method") ?: existingInterface.method
        existingInterface.url = body.getString("url") ?: existingInterface.url
        existingInterface.description = body.getString("description") ?: existingInterface.description
        existingInterface.postType = body.getString("postType") ?: existingInterface.postType
        existingInterface.params = body.getString("params") ?: existingInterface.params
        existingInterface.environment = body.getString("environment") ?: existingInterface.environment
        existingInterface.timeout = body.getLong("timeout") ?: existingInterface.timeout
        existingInterface.valuePath = body.getString("valuePath") ?: existingInterface.valuePath
        existingInterface.updateTime = System.currentTimeMillis()
        
        apiInterfaceDao.updateById(existingInterface)
        return ApiData.success(existingInterface.toJsonObject())
    }
    
    suspend fun delete(id: Long): ApiData<Boolean> {
        apiInterfaceDao.deleteById(id)
        return ApiData.success(true)
    }
    
    suspend fun updateStatus(id: Long, status: Int): ApiData<JsonObject> {
        val apiInterface = apiInterfaceDao.findById(id)
        
        if (apiInterface == null) {
            return ApiData.error("接口不存在")
        }
        
        apiInterface.status = status
        apiInterface.updateTime = System.currentTimeMillis()
        
        apiInterfaceDao.updateById(apiInterface)
        return ApiData.success(apiInterface.toJsonObject())
    }
    
    suspend fun copy(id: Long): ApiData<JsonObject> {
        val existingInterface = apiInterfaceDao.findById(id)
        
        if (existingInterface == null) {
            return ApiData.error("接口不存在")
        }
        
        val newInterface = existingInterface.copy()
        newInterface.id = null
        newInterface.name = "${existingInterface.name}_副本"
        newInterface.status = 1
        newInterface.createTime = System.currentTimeMillis()
        newInterface.updateTime = System.currentTimeMillis()
        
        val newId = apiInterfaceDao.save(newInterface)
        newInterface.id = newId
        return ApiData.success(newInterface.toJsonObject())
    }
    
    suspend fun execute(body: JsonObject): ApiData<JsonObject> {
        val startTime = System.currentTimeMillis()
        val interfaceId = body.getLong("interfaceId") ?: return ApiData.error("接口ID不能为空")
        
        val apiInterface = apiInterfaceDao.findById(interfaceId)
        
        if (apiInterface == null) {
            return ApiData.error("接口不存在")
        }
        
        if (apiInterface.status != 1) {
            return ApiData.error("接口已禁用，无法执行")
        }
        
        val url = apiInterface.url ?: return ApiData.error("接口URL不能为空")
        
        val method = HttpMethod.valueOf(apiInterface.method ?: "GET")
        val timeoutSeconds = body.getLong("timeout") ?: apiInterface.timeout ?: 60L
        val timeoutMillis = timeoutSeconds * 1000L
        
        val urlParams = body.getJsonObject("urlParams")
        val headers = body.getJsonObject("headers")
        val bodyParams = body.getJsonObject("bodyParams")
        
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
        val request: HttpRequest<Buffer> = when (method) {
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
                    request.addQueryParam(parts[0], URLDecoder.decode(parts[1], "UTF-8"))
                }
            }
        }
        
        // 添加新的 URL 参数
        urlParams?.forEach { (key, value) ->
            if (value != null && value.toString().isNotBlank()) {
                request.addQueryParam(key, value.toString())
            }
        }
        
        // 设置请求头
        headers?.forEach { (key, value) ->
            request.putHeader(key, value.toString())
        }
        
        // 发送请求
        val response = try {
            if (method != HttpMethod.GET && bodyParams != null) {
                if (postType == "application/x-www-form-urlencoded") {
                    val formData = bodyParams.joinToString("&") { (key, value) ->
                        "${encodeURIComponent(key)}=${encodeURIComponent(value.toString())}"
                    }
                    request.putHeader("Content-Type", postType)
                        .sendBuffer(Buffer.buffer(formData))
                        .awaitForResult()
                } else {
                    request.putHeader("Content-Type", "application/json")
                        .sendJsonObject(bodyParams)
                        .awaitForResult()
                }
            } else {
                request.send().awaitForResult()
            }
        } catch (error: Exception) {
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            val responseJson = JsonObject()
                .put("status", 500)
                .put("headers", JsonObject())
                .put("body", null)
                .put("responseTime", responseTime)
                .put("success", false)
                .put("error", error.message ?: "请求失败")
            
            return ApiData.success(responseJson)
        }
        
        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime
        val statusCode = response.statusCode()
        
        val responseJson = JsonObject()
            .put("status", statusCode)
            .put("headers", JsonObject())
            .put("body", response.bodyAsString())
            .put("responseTime", responseTime)
            .put("success", statusCode in 200..299)
        
        return ApiData.success(responseJson)
    }
    
    private fun ApiInterface.toJsonObject(): JsonObject {
        val result = JsonObject()
            .put("id", id)
            .put("name", name)
            .put("method", method)
            .put("url", url)
            .put("description", description)
            .put("postType", postType)
            .put("status", status)
            .put("environment", environment)
            .put("timeout", timeout)
            .put("valuePath", valuePath)
            .put("createTime", createTime)
            .put("updateTime", updateTime)
        
        // 解析并分离参数
        val (urlParams, headerParams, bodyParams) = parseAndSeparateParams(params)
        result.put("urlParams", urlParams)
        result.put("headerParams", headerParams)
        result.put("bodyParams", bodyParams)
        
        return result
    }
    
    /**
     * 解析并分离参数
     */
    private fun parseAndSeparateParams(paramsJson: String?): Triple<JsonArray, JsonArray, JsonArray> {
        val urlParams = JsonArray()
        val headerParams = JsonArray()
        val bodyParams = JsonArray()
        
        if (paramsJson.isNullOrBlank()) {
            return Triple(urlParams, headerParams, bodyParams)
        }
        
        try {
            val paramsArray = JsonArray(paramsJson)
            for (i in 0 until paramsArray.size()) {
                val param = paramsArray.getJsonObject(i)
                val paramType = param.getString("paramType")
                
                when (paramType) {
                    "URL_PARAM" -> urlParams.add(param)
                    "HEADER_PARAM" -> headerParams.add(param)
                    "BODY_PARAM" -> bodyParams.add(param)
                }
            }
        } catch (_: Exception) {
            // 解析失败时返回空数组
        }
        
        return Triple(urlParams, headerParams, bodyParams)
    }
    
    private fun encodeURIComponent(str: String): String {
        return URLEncoder.encode(str, "UTF-8")
    }
}
