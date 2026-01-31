package io.infra.market.vertx.service

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.config.JacksonConfig
import io.infra.market.vertx.dto.ActivityDto
import io.infra.market.vertx.dto.ActivityFormDto
import io.infra.market.vertx.dto.ActivityQueryDto
import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.entity.Activity
import io.infra.market.vertx.extensions.awaitForResult
import io.infra.market.vertx.repository.ActivityDao
import io.infra.market.vertx.util.TimeUtil
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Tuple
import org.slf4j.LoggerFactory

/**
 * 活动服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ActivityService @Inject constructor(
    private val activityDao: ActivityDao,
    private val pool: Pool
) {
    
    private val logger = LoggerFactory.getLogger(ActivityService::class.java)
    
    suspend fun getActivities(query: ActivityQueryDto): ApiData<PageResultDto<ActivityDto>> {
        val page = query.page ?: 1
        val size = query.size ?: 10
        
        val pageResult = activityDao.findPage(
            name = query.name,
            templateId = query.templateId,
            status = query.status,
            page = page,
            size = size
        )
        
        // 批量获取模板名称
        val templateIds = pageResult.records.mapNotNull { it.templateId }.distinct()
        val templateNameMap = mutableMapOf<Long, String?>()
        templateIds.forEach { templateId ->
            val templateName = getTemplateName(templateId)
            templateNameMap[templateId] = templateName
        }
        
        val activityDtos = pageResult.records.map { activity ->
            convertToDto(activity, templateNameMap[activity.templateId])
        }
        
        val result = PageResultDto(
            records = activityDtos,
            total = pageResult.total,
            page = pageResult.page,
            size = pageResult.size
        )
        
        return ApiData.success(result)
    }
    
    suspend fun getActivity(id: Long): ApiData<ActivityDto> {
        val activity = activityDao.findById(id) ?: return ApiData.error("活动不存在", 404)
        
        val templateName = activity.templateId?.let { getTemplateName(it) }
        val activityDto = convertToDto(activity, templateName)
        
        return ApiData.success(activityDto)
    }
    
    suspend fun createActivity(form: ActivityFormDto): ApiData<ActivityDto> {
        // 验证模板是否存在
        val templateId = form.templateId
        if (templateId == null) {
            return ApiData.error("模板ID不能为空", 400)
        }
        
        val templateExists = checkTemplateExists(templateId)
        if (!templateExists) {
            return ApiData.error("活动模板不存在", 400)
        }
        
        val activity = convertToEntity(form)
        activity.status = form.status ?: 1
        
        val activityId = activityDao.save(activity)
        activity.id = activityId
        
        val templateName = getTemplateName(templateId)
        val activityDto = convertToDto(activity, templateName)
        
        return ApiData.success(activityDto)
    }
    
    suspend fun updateActivity(id: Long, form: ActivityFormDto): ApiData<ActivityDto> {
        val existingActivity = activityDao.findById(id) ?: return ApiData.error("活动不存在", 404)
        
        // 验证模板是否存在
        val formTemplateId = form.templateId
        if (formTemplateId != null) {
            val templateExists = checkTemplateExists(formTemplateId)
            if (!templateExists) {
                return ApiData.error("活动模板不存在", 400)
            }
        }
        
        existingActivity.name = form.name
        existingActivity.description = form.description
        if (formTemplateId != null) {
            existingActivity.templateId = formTemplateId
        }
        existingActivity.configData = serializeConfigData(form.configData)
        if (form.status != null) {
            existingActivity.status = form.status
        }
        
        activityDao.updateById(existingActivity)
        
        val templateId = existingActivity.templateId
        val templateName = templateId?.let { getTemplateName(it) }
        val activityDto = convertToDto(existingActivity, templateName)
        
        return ApiData.success(activityDto)
    }
    
    suspend fun deleteActivity(id: Long): ApiData<Unit> {
        val activity = activityDao.findById(id) ?: return ApiData.error("活动不存在", 404)
        
        activityDao.deleteById(id)
        
        return ApiData.success()
    }
    
    suspend fun updateActivityStatus(id: Long, status: Int): ApiData<ActivityDto> {
        val activity = activityDao.findById(id) ?: return ApiData.error("活动不存在", 404)
        
        activity.status = status
        activityDao.updateById(activity)
        
        val templateName = activity.templateId?.let { getTemplateName(it) }
        val activityDto = convertToDto(activity, templateName)
        
        return ApiData.success(activityDto)
    }
    
    /**
     * 获取模板名称
     */
    private suspend fun getTemplateName(templateId: Long): String? {
        return try {
            val rows = pool.preparedQuery("SELECT name FROM activity_template WHERE id = ?")
                .execute(Tuple.of(templateId))
                .awaitForResult()
            if (rows.size() > 0) {
                rows.first().getString("name")
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("查询模板名称失败，模板ID: $templateId", e)
            null
        }
    }
    
    /**
     * 检查模板是否存在
     */
    private suspend fun checkTemplateExists(templateId: Long): Boolean {
        return try {
            val rows = pool.preparedQuery("SELECT COUNT(*) as count FROM activity_template WHERE id = ?")
                .execute(Tuple.of(templateId))
                .awaitForResult()
            rows.first().getLong("count") > 0
        } catch (e: Exception) {
            logger.warn("检查模板是否存在失败，模板ID: $templateId", e)
            false
        }
    }
    
    /**
     * 转换为DTO
     */
    private fun convertToDto(activity: Activity, templateName: String?): ActivityDto {
        val configData = parseConfigData(activity.configData)
        
        return ActivityDto(
            id = activity.id,
            name = activity.name,
            description = activity.description,
            templateId = activity.templateId,
            templateName = templateName,
            configData = configData,
            status = activity.status,
            createTime = TimeUtil.format(activity.createTime),
            updateTime = TimeUtil.format(activity.updateTime)
        )
    }
    
    /**
     * 转换为实体
     */
    private fun convertToEntity(form: ActivityFormDto): Activity {
        val configDataJson = serializeConfigData(form.configData)
        
        return Activity(
            name = form.name,
            description = form.description,
            templateId = form.templateId,
            configData = configDataJson,
            status = form.status
        )
    }
    
    /**
     * 序列化配置数据为JSON字符串
     */
    private fun serializeConfigData(configData: Map<String, Any>?): String? {
        return try {
            if (configData.isNullOrEmpty()) {
                null
            } else {
                JacksonConfig.objectMapper.writeValueAsString(configData)
            }
        } catch (e: Exception) {
            logger.error("序列化配置数据失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 解析配置数据JSON字符串
     */
    private fun parseConfigData(configDataJson: String?): Map<String, Any>? {
        return try {
            if (configDataJson.isNullOrBlank()) {
                null
            } else {
                @Suppress("UNCHECKED_CAST")
                JacksonConfig.objectMapper.readValue(configDataJson, Map::class.java) as? Map<String, Any>
            }
        } catch (e: Exception) {
            logger.error("解析配置数据失败: ${e.message}", e)
            null
        }
    }
}
