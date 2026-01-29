package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.ActivityTemplateDao
import io.infra.market.repository.entity.ActivityTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.structure.core.utils.Loggable
import io.infra.market.util.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 活动模板服务
 */
@Service
class ActivityTemplateService(
    private val activityTemplateDao: ActivityTemplateDao,
    private val objectMapper: ObjectMapper
) : Loggable {

    fun findPage(query: ActivityTemplateQueryDto): PageResultDto<ActivityTemplateDto> {
        val page = activityTemplateDao.page(query)
        val templateDtos = page.records.map { convertToDto(it) }
        
        return PageResultDto(
            records = templateDtos,
            total = page.totalRow,
            page = page.pageNumber,
            size = page.pageSize
        )
    }

    fun findById(id: Long): ActivityTemplateDto? {
        val template = activityTemplateDao.getById(id)
        return template?.let { convertToDto(it) }
    }

    fun findAll(): List<ActivityTemplateDto> {
        val templates = activityTemplateDao.list()
        return templates.map { convertToDto(it) }
    }

    @Transactional
    fun save(form: ActivityTemplateFormDto): ActivityTemplateDto {
        val template = convertToEntity(form)
        template.createTime = System.currentTimeMillis()
        template.updateTime = System.currentTimeMillis()
        template.status = form.status ?: 1

        activityTemplateDao.save(template)
        return convertToDto(template)
    }

    @Transactional
    fun update(id: Long, form: ActivityTemplateFormDto): ActivityTemplateDto {
        val existingTemplate = activityTemplateDao.getById(id)
            ?: throw RuntimeException("活动模板不存在")

        val template = convertToEntity(form)
        template.id = id
        template.createTime = existingTemplate.createTime
        template.updateTime = System.currentTimeMillis()
        template.status = form.status ?: existingTemplate.status

        activityTemplateDao.updateById(template, false)
        return convertToDto(template)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return activityTemplateDao.removeById(id)
    }

    @Transactional
    fun updateStatus(id: Long, status: Int): ActivityTemplateDto {
        val existingTemplate = activityTemplateDao.getById(id)
            ?: throw RuntimeException("活动模板不存在")

        existingTemplate.status = status
        existingTemplate.updateTime = System.currentTimeMillis()
        activityTemplateDao.updateById(existingTemplate)
        
        return convertToDto(existingTemplate)
    }

    @Transactional
    fun copy(id: Long): ActivityTemplateDto {
        val existingTemplate = activityTemplateDao.getById(id)
            ?: throw RuntimeException("活动模板不存在")

        val newTemplate = existingTemplate.copy()
        newTemplate.id = null
        newTemplate.name = "${existingTemplate.name}_副本"
        newTemplate.createTime = System.currentTimeMillis()
        newTemplate.updateTime = System.currentTimeMillis()
        newTemplate.status = 0

        activityTemplateDao.save(newTemplate)
        return convertToDto(newTemplate)
    }

    private fun convertToDto(entity: ActivityTemplate): ActivityTemplateDto {
        val fields = parseFields(entity.fields)

        return ActivityTemplateDto(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            fields = fields,
            status = entity.status,
            createTime = TimeUtil.format(entity.createTime),
            updateTime = TimeUtil.format(entity.updateTime)
        )
    }

    private fun convertToEntity(form: ActivityTemplateFormDto): ActivityTemplate {
        val fieldsJson = try {
            if (form.fields.isNullOrEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(form.fields)
            }
        } catch (e: Exception) {
            log.error("序列化字段配置失败: ${e.message}", e)
            null
        }

        return ActivityTemplate(
            name = form.name,
            description = form.description,
            fields = fieldsJson,
            status = form.status
        )
    }

    /**
     * 解析字段配置JSON
     * 
     * @param fieldsJson 字段配置JSON字符串
     * @return 字段配置列表
     */
    private fun parseFields(fieldsJson: String?): List<ActivityTemplateFieldDto> {
        return try {
            if (fieldsJson.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(fieldsJson, Array<ActivityTemplateFieldDto>::class.java).toList()
            }
        } catch (e: Exception) {
            log.error("解析字段配置失败: ${e.message}", e)
            emptyList()
        }
    }
}
