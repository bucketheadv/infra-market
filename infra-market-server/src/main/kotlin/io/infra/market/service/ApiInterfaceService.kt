package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.ApiInterfaceDao
import io.infra.market.repository.entity.ApiInterface
import io.infra.market.enums.HttpMethodEnum
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.infra.market.enums.PostTypeEnum
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import java.util.Date

/**
 * 接口管理服务
 */
@Service
class ApiInterfaceService(
    private val apiInterfaceDao: ApiInterfaceDao,
    private val restTemplate: RestTemplate
) {
    
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

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

        apiInterfaceDao.updateById(apiInterface)
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

    fun execute(request: ApiExecuteRequestDto): ApiExecuteResponseDto {
        val startTime = System.currentTimeMillis()
        
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

            val response: ResponseEntity<String> = restTemplate.exchange(
                finalUrl,
                httpMethod,
                httpEntity,
                String::class.java
            )

            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime

            return ApiExecuteResponseDto(
                status = response.statusCode.value(),
                headers = response.headers.toSingleValueMap(),
                body = response.body,
                responseTime = responseTime,
                success = true
            )
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime

            return ApiExecuteResponseDto(
                status = 500,
                headers = emptyMap(),
                body = null,
                responseTime = responseTime,
                success = false,
                error = e.message
            )
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
}
