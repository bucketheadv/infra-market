package io.infra.market.vertx.service

import com.google.inject.Inject
import com.google.inject.Singleton
import io.infra.market.vertx.config.JacksonConfig
import io.infra.market.vertx.dto.ActivityComponentDto
import io.infra.market.vertx.dto.ActivityComponentFormDto
import io.infra.market.vertx.dto.ActivityComponentQueryDto
import io.infra.market.vertx.dto.ApiData
import io.infra.market.vertx.dto.PageResultDto
import io.infra.market.vertx.entity.ActivityComponent
import io.infra.market.vertx.repository.ActivityComponentDao
import io.infra.market.vertx.util.TimeUtil
import org.slf4j.LoggerFactory

/**
 * 活动组件服务
 * 
 * 规则1：任何调用 xxx.awaitForResult() 的函数，必须用 suspend 修饰
 */
@Singleton
class ActivityComponentService @Inject constructor(
    private val activityComponentDao: ActivityComponentDao
) {
    
    private val logger = LoggerFactory.getLogger(ActivityComponentService::class.java)
    
    suspend fun getActivityComponents(query: ActivityComponentQueryDto): ApiData<PageResultDto<ActivityComponentDto>> {
        val page = query.page ?: 1
        val size = query.size ?: 10
        
        val pageResult = activityComponentDao.findPage(
            name = query.name,
            status = query.status,
            page = page,
            size = size
        )
        
        val componentDtos = pageResult.records.map { convertToDto(it) }
        
        val result = PageResultDto(
            records = componentDtos,
            total = pageResult.total,
            page = pageResult.page,
            size = pageResult.size
        )
        
        return ApiData.success(result)
    }
    
    suspend fun getAllActivityComponents(): ApiData<List<ActivityComponentDto>> {
        val components = activityComponentDao.findAll()
        val componentDtos = components.map { convertToDto(it) }
        // 只返回启用的组件
        val enabledComponents = componentDtos.filter { it.status == 1 }
        return ApiData.success(enabledComponents)
    }
    
    suspend fun getActivityComponent(id: Long): ApiData<ActivityComponentDto> {
        val component = activityComponentDao.findById(id) ?: return ApiData.error("活动组件不存在", 404)
        
        val componentDto = convertToDto(component)
        return ApiData.success(componentDto)
    }
    
    suspend fun createActivityComponent(form: ActivityComponentFormDto): ApiData<ActivityComponentDto> {
        val component = convertToEntity(form)
        component.status = form.status ?: 1
        
        val componentId = activityComponentDao.save(component)
        component.id = componentId
        
        val componentDto = convertToDto(component)
        return ApiData.success(componentDto)
    }
    
    suspend fun updateActivityComponent(id: Long, form: ActivityComponentFormDto): ApiData<ActivityComponentDto> {
        val existingComponent = activityComponentDao.findById(id) ?: return ApiData.error("活动组件不存在", 404)
        
        existingComponent.name = form.name
        existingComponent.description = form.description
        existingComponent.fields = serializeFields(form.fields)
        if (form.status != null) {
            existingComponent.status = form.status
        }
        
        activityComponentDao.updateById(existingComponent)
        
        val componentDto = convertToDto(existingComponent)
        return ApiData.success(componentDto)
    }
    
    suspend fun deleteActivityComponent(id: Long): ApiData<Unit> {
        activityComponentDao.findById(id) ?: return ApiData.error("活动组件不存在", 404)
        
        activityComponentDao.deleteById(id)
        
        return ApiData.success()
    }
    
    suspend fun updateActivityComponentStatus(id: Long, status: Int): ApiData<ActivityComponentDto> {
        val component = activityComponentDao.findById(id) ?: return ApiData.error("活动组件不存在", 404)
        
        component.status = status
        activityComponentDao.updateById(component)
        
        val componentDto = convertToDto(component)
        return ApiData.success(componentDto)
    }
    
    suspend fun copyActivityComponent(id: Long): ApiData<ActivityComponentDto> {
        val existingComponent = activityComponentDao.findById(id) ?: return ApiData.error("活动组件不存在", 404)
        
        val newComponent = existingComponent.copy(
            id = null,
            name = "${existingComponent.name}_副本",
            status = 0
        )
        
        val componentId = activityComponentDao.save(newComponent)
        newComponent.id = componentId
        
        val componentDto = convertToDto(newComponent)
        return ApiData.success(componentDto)
    }
    
    /**
     * 转换为DTO
     */
    private fun convertToDto(component: ActivityComponent): ActivityComponentDto {
        val fields = parseFields(component.fields)
        
        return ActivityComponentDto(
            id = component.id,
            name = component.name,
            description = component.description,
            fields = fields,
            status = component.status,
            createTime = TimeUtil.format(component.createTime),
            updateTime = TimeUtil.format(component.updateTime)
        )
    }
    
    /**
     * 转换为实体
     */
    private fun convertToEntity(form: ActivityComponentFormDto): ActivityComponent {
        val fieldsJson = serializeFields(form.fields)
        
        return ActivityComponent(
            name = form.name,
            description = form.description,
            fields = fieldsJson,
            status = form.status
        )
    }
    
    /**
     * 序列化字段/组件配置为JSON字符串
     */
    private fun serializeFields(fields: List<Map<String, Any>>?): String? {
        return try {
            if (fields.isNullOrEmpty()) {
                null
            } else {
                JacksonConfig.objectMapper.writeValueAsString(fields)
            }
        } catch (e: Exception) {
            logger.error("序列化字段/组件配置失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 解析字段/组件配置JSON字符串
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
            logger.error("解析字段/组件配置失败: ${e.message}", e)
            null
        }
    }
}
