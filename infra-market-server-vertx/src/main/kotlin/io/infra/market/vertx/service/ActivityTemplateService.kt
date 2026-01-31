package io.infra.market.vertx.service

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.config.JacksonConfig
import io.infra.market.vertx.dto.ActivityTemplateDto
import io.infra.market.vertx.dto.ActivityTemplateFormDto
import io.infra.market.vertx.dto.ActivityTemplateQueryDto
import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.entity.ActivityTemplate
import io.infra.market.vertx.repository.ActivityTemplateDao
import io.infra.market.vertx.util.TimeUtil
import org.slf4j.LoggerFactory

/**
 * 活动模板服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ActivityTemplateService @Inject constructor(
    private val activityTemplateDao: ActivityTemplateDao
) {
    
    private val logger = LoggerFactory.getLogger(ActivityTemplateService::class.java)
    
    suspend fun getActivityTemplates(query: ActivityTemplateQueryDto): ApiData<PageResultDto<ActivityTemplateDto>> {
        val page = query.page ?: 1
        val size = query.size ?: 10
        
        val pageResult = activityTemplateDao.findPage(
            name = query.name,
            status = query.status,
            page = page,
            size = size
        )
        
        val templateDtos = pageResult.records.map { convertToDto(it) }
        
        val result = PageResultDto(
            records = templateDtos,
            total = pageResult.total,
            page = pageResult.page,
            size = pageResult.size
        )
        
        return ApiData.success(result)
    }
    
    suspend fun getAllActivityTemplates(): ApiData<List<ActivityTemplateDto>> {
        val templates = activityTemplateDao.findAll()
        val templateDtos = templates.map { convertToDto(it) }
        // 只返回启用的模板
        val enabledTemplates = templateDtos.filter { it.status == 1 }
        return ApiData.success(enabledTemplates)
    }
    
    suspend fun getActivityTemplate(id: Long): ApiData<ActivityTemplateDto> {
        val template = activityTemplateDao.findById(id) ?: return ApiData.error("活动模板不存在", 404)
        
        val templateDto = convertToDto(template)
        return ApiData.success(templateDto)
    }
    
    suspend fun createActivityTemplate(form: ActivityTemplateFormDto): ApiData<ActivityTemplateDto> {
        val template = convertToEntity(form)
        template.status = form.status ?: 1
        
        val templateId = activityTemplateDao.save(template)
        template.id = templateId
        
        val templateDto = convertToDto(template)
        return ApiData.success(templateDto)
    }
    
    suspend fun updateActivityTemplate(id: Long, form: ActivityTemplateFormDto): ApiData<ActivityTemplateDto> {
        val existingTemplate = activityTemplateDao.findById(id) ?: return ApiData.error("活动模板不存在", 404)
        
        existingTemplate.name = form.name
        existingTemplate.description = form.description
        existingTemplate.fields = serializeFields(form.fields)
        if (form.status != null) {
            existingTemplate.status = form.status
        }
        
        activityTemplateDao.updateById(existingTemplate)
        
        val templateDto = convertToDto(existingTemplate)
        return ApiData.success(templateDto)
    }
    
    suspend fun deleteActivityTemplate(id: Long): ApiData<Unit> {
        val template = activityTemplateDao.findById(id) ?: return ApiData.error("活动模板不存在", 404)
        
        activityTemplateDao.deleteById(id)
        
        return ApiData.success()
    }
    
    suspend fun updateActivityTemplateStatus(id: Long, status: Int): ApiData<ActivityTemplateDto> {
        val template = activityTemplateDao.findById(id) ?: return ApiData.error("活动模板不存在", 404)
        
        template.status = status
        activityTemplateDao.updateById(template)
        
        val templateDto = convertToDto(template)
        return ApiData.success(templateDto)
    }
    
    suspend fun copyActivityTemplate(id: Long): ApiData<ActivityTemplateDto> {
        val existingTemplate = activityTemplateDao.findById(id) ?: return ApiData.error("活动模板不存在", 404)
        
        val newTemplate = existingTemplate.copy(
            id = null,
            name = "${existingTemplate.name}_副本",
            status = 0
        )
        
        val templateId = activityTemplateDao.save(newTemplate)
        newTemplate.id = templateId
        
        val templateDto = convertToDto(newTemplate)
        return ApiData.success(templateDto)
    }
    
    /**
     * 转换为DTO
     */
    private fun convertToDto(template: ActivityTemplate): ActivityTemplateDto {
        val fields = parseFields(template.fields)
        
        return ActivityTemplateDto(
            id = template.id,
            name = template.name,
            description = template.description,
            fields = fields,
            status = template.status,
            createTime = TimeUtil.format(template.createTime),
            updateTime = TimeUtil.format(template.updateTime)
        )
    }
    
    /**
     * 转换为实体
     */
    private fun convertToEntity(form: ActivityTemplateFormDto): ActivityTemplate {
        val fieldsJson = serializeFields(form.fields)
        
        return ActivityTemplate(
            name = form.name,
            description = form.description,
            fields = fieldsJson,
            status = form.status
        )
    }
    
    /**
     * 序列化字段配置为JSON字符串
     */
    private fun serializeFields(fields: List<Map<String, Any>>?): String? {
        return try {
            if (fields.isNullOrEmpty()) {
                null
            } else {
                JacksonConfig.objectMapper.writeValueAsString(fields)
            }
        } catch (e: Exception) {
            logger.error("序列化字段配置失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 解析字段配置JSON字符串
     */
    private fun parseFields(fieldsJson: String?): List<Map<String, Any>>? {
        return try {
            if (fieldsJson.isNullOrBlank()) {
                null
            } else {
                @Suppress("UNCHECKED_CAST")
                val fieldsList = JacksonConfig.objectMapper.readValue(fieldsJson, List::class.java) as? List<Map<String, Any>>
                fieldsList
            }
        } catch (e: Exception) {
            logger.error("解析字段配置失败: ${e.message}", e)
            null
        }
    }
}
