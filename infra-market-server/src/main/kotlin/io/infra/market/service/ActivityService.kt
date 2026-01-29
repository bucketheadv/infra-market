package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.ActivityDao
import io.infra.market.repository.dao.ActivityTemplateDao
import io.infra.market.repository.entity.Activity
import io.infra.market.repository.entity.ActivityTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.structure.core.utils.Loggable
import io.infra.market.util.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 活动服务
 */
@Service
class ActivityService(
    private val activityDao: ActivityDao,
    private val activityTemplateDao: ActivityTemplateDao,
    private val objectMapper: ObjectMapper
) : Loggable {

    fun findPage(query: ActivityQueryDto): PageResultDto<ActivityDto> {
        val page = activityDao.page(query)
        val activityDtos = page.records.map { convertToDto(it) }
        
        return PageResultDto(
            records = activityDtos,
            total = page.totalRow,
            page = page.pageNumber,
            size = page.pageSize
        )
    }

    fun findById(id: Long): ActivityDto? {
        val activity = activityDao.getById(id)
        return activity?.let { convertToDto(it) }
    }

    @Transactional
    fun save(form: ActivityFormDto): ActivityDto {
        // 验证模板是否存在
        if (form.templateId == null) {
            throw RuntimeException("模板ID不能为空")
        }
        val template = activityTemplateDao.getById(form.templateId)
            ?: throw RuntimeException("活动模板不存在")
        
        // 验证配置数据
        validateConfigData(form.configData, template)
        
        val activity = convertToEntity(form)
        activity.createTime = System.currentTimeMillis()
        activity.updateTime = System.currentTimeMillis()
        activity.status = form.status ?: 1

        activityDao.save(activity)
        return convertToDto(activity)
    }

    @Transactional
    fun update(id: Long, form: ActivityFormDto): ActivityDto {
        val existingActivity = activityDao.getById(id)
            ?: throw RuntimeException("活动不存在")

        // 验证模板是否存在
        if (form.templateId != null) {
            val template = activityTemplateDao.getById(form.templateId)
                ?: throw RuntimeException("活动模板不存在")
            
            // 验证配置数据
            validateConfigData(form.configData, template)
        }

        val activity = convertToEntity(form)
        activity.id = id
        activity.createTime = existingActivity.createTime
        activity.updateTime = System.currentTimeMillis()
        activity.status = form.status ?: existingActivity.status

        activityDao.updateById(activity, false)
        return convertToDto(activity)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return activityDao.removeById(id)
    }

    @Transactional
    fun updateStatus(id: Long, status: Int): ActivityDto {
        val existingActivity = activityDao.getById(id)
            ?: throw RuntimeException("活动不存在")

        existingActivity.status = status
        existingActivity.updateTime = System.currentTimeMillis()
        activityDao.updateById(existingActivity)
        
        return convertToDto(existingActivity)
    }

    private fun convertToDto(entity: Activity): ActivityDto {
        val configData = parseConfigData(entity.configData)
        
        // 获取模板名称
        var templateName: String? = null
        if (entity.templateId != null) {
            val template = activityTemplateDao.getById(entity.templateId)
            templateName = template?.name
        }

        return ActivityDto(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            templateId = entity.templateId,
            templateName = templateName,
            configData = configData,
            status = entity.status,
            createTime = TimeUtil.format(entity.createTime),
            updateTime = TimeUtil.format(entity.updateTime)
        )
    }

    private fun convertToEntity(form: ActivityFormDto): Activity {
        val configDataJson = try {
            if (form.configData.isNullOrEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(form.configData)
            }
        } catch (e: Exception) {
            log.error("序列化配置数据失败: ${e.message}", e)
            null
        }

        return Activity(
            name = form.name,
            description = form.description,
            templateId = form.templateId,
            configData = configDataJson,
            status = form.status
        )
    }

    /**
     * 解析配置数据JSON
     * 
     * @param configDataJson 配置数据JSON字符串
     * @return 配置数据Map
     */
    private fun parseConfigData(configDataJson: String?): Map<String, Any>? {
        return try {
            if (configDataJson.isNullOrBlank()) {
                null
            } else {
                @Suppress("UNCHECKED_CAST")
                objectMapper.readValue(configDataJson, Map::class.java) as? Map<String, Any>
            }
        } catch (e: Exception) {
            log.error("解析配置数据失败: ${e.message}", e)
            null
        }
    }

    /**
     * 验证配置数据是否符合模板的字段配置
     * 
     * @param configData 配置数据
     * @param template 活动模板
     */
    private fun validateConfigData(configData: Map<String, Any>?, template: ActivityTemplate) {
        // 解析模板字段配置
        val fields = try {
            if (template.fields.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(template.fields, Array<ActivityTemplateFieldDto>::class.java).toList()
            }
        } catch (e: Exception) {
            log.error("解析模板字段配置失败: ${e.message}", e)
            emptyList()
        }

        // 验证必填字段
        fields.forEach { field ->
            if (field.required == true) {
                val fieldName = field.name ?: return@forEach
                val value = configData?.get(fieldName)
                
                val isEmpty = when (value) {
                    null -> true
                    is String -> value.trim().isEmpty()
                    is Collection<*> -> value.isEmpty()
                    else -> false
                }
                
                if (isEmpty) {
                    val displayName = if (field.label.isNullOrBlank()) {
                        fieldName
                    } else {
                        "${field.label}（${fieldName}）"
                    }
                    throw RuntimeException("字段 $displayName 为必填项，不能为空")
                }
            }
        }
    }
}
