package io.infra.market.service

import io.infra.market.dto.*
import io.infra.market.repository.dao.ActivityComponentDao
import io.infra.market.repository.entity.ActivityComponent
import com.fasterxml.jackson.databind.ObjectMapper
import io.infra.structure.core.utils.Loggable
import io.infra.market.util.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 活动组件服务
 */
@Service
class ActivityComponentService(
    private val activityComponentDao: ActivityComponentDao,
    private val objectMapper: ObjectMapper
) : Loggable {

    fun findPage(query: ActivityComponentQueryDto): PageResultDto<ActivityComponentDto> {
        val page = activityComponentDao.page(query)
        val componentDtos = page.records.map { convertToDto(it) }
        
        return PageResultDto(
            records = componentDtos,
            total = page.totalRow,
            page = page.pageNumber,
            size = page.pageSize
        )
    }

    fun findById(id: Long): ActivityComponentDto? {
        val component = activityComponentDao.getById(id)
        return component?.let { convertToDto(it) }
    }

    fun findAll(): List<ActivityComponentDto> {
        val components = activityComponentDao.list()
        return components.map { convertToDto(it) }
    }

    @Transactional
    fun save(form: ActivityComponentFormDto): ActivityComponentDto {
        val component = convertToEntity(form)
        component.createTime = System.currentTimeMillis()
        component.updateTime = System.currentTimeMillis()
        component.status = form.status ?: 1

        activityComponentDao.save(component)
        return convertToDto(component)
    }

    @Transactional
    fun update(id: Long, form: ActivityComponentFormDto): ActivityComponentDto {
        val existingComponent = activityComponentDao.getById(id)
            ?: throw RuntimeException("活动组件不存在")

        val component = convertToEntity(form)
        component.id = id
        component.createTime = existingComponent.createTime
        component.updateTime = System.currentTimeMillis()
        component.status = form.status ?: existingComponent.status

        activityComponentDao.updateById(component, false)
        return convertToDto(component)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return activityComponentDao.removeById(id)
    }

    @Transactional
    fun updateStatus(id: Long, status: Int): ActivityComponentDto {
        val existingComponent = activityComponentDao.getById(id)
            ?: throw RuntimeException("活动组件不存在")

        existingComponent.status = status
        existingComponent.updateTime = System.currentTimeMillis()
        activityComponentDao.updateById(existingComponent)
        
        return convertToDto(existingComponent)
    }

    @Transactional
    fun copy(id: Long): ActivityComponentDto {
        val existingComponent = activityComponentDao.getById(id)
            ?: throw RuntimeException("活动组件不存在")

        val newComponent = existingComponent.copy()
        newComponent.id = null
        newComponent.name = "${existingComponent.name}_副本"
        newComponent.createTime = System.currentTimeMillis()
        newComponent.updateTime = System.currentTimeMillis()
        newComponent.status = 0

        activityComponentDao.save(newComponent)
        return convertToDto(newComponent)
    }

    private fun convertToDto(entity: ActivityComponent): ActivityComponentDto {
        val fields = parseFields(entity.fields)

        return ActivityComponentDto(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            fields = fields,
            status = entity.status,
            createTime = TimeUtil.format(entity.createTime),
            updateTime = TimeUtil.format(entity.updateTime)
        )
    }

    private fun convertToEntity(form: ActivityComponentFormDto): ActivityComponent {
        val fieldsJson = try {
            if (form.fields.isNullOrEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(form.fields)
            }
        } catch (e: Exception) {
            log.error("序列化字段/组件配置失败: ${e.message}", e)
            null
        }

        return ActivityComponent(
            name = form.name,
            description = form.description,
            fields = fieldsJson,
            status = form.status
        )
    }

    /**
     * 解析字段/组件配置JSON
     * 
     * @param fieldsJson 字段/组件配置JSON字符串
     * @return 字段/组件配置列表
     */
    private fun parseFields(fieldsJson: String?): List<ActivityComponentFieldDto> {
        return try {
            if (fieldsJson.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(fieldsJson, Array<ActivityComponentFieldDto>::class.java).toList()
            }
        } catch (e: Exception) {
            log.error("解析字段/组件配置失败: ${e.message}", e)
            emptyList()
        }
    }
}
