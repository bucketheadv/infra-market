<template>
  <div class="activity-edit-page">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <CalendarOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">{{ isEdit ? '编辑活动' : '创建活动' }}</div>
          <div class="header-subtitle">{{ isEdit ? '修改活动信息和配置' : '创建新的活动并配置参数' }}</div>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col="{ span: 3 }"
          :wrapper-col="{ span: 21 }"
          class="activity-form-content"
          size="small"
          layout="horizontal"
        >
          <!-- 基本信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <IdcardOutlined />
              </div>
              <div class="section-title">基本信息</div>
            </div>
            
            <a-form-item label="活动名称" name="name">
              <a-input
                v-model:value="form.name"
                placeholder="请输入活动名称"
                size="middle"
                class="form-input"
              />
            </a-form-item>

            <a-form-item label="活动描述" name="description">
              <a-textarea
                v-model:value="form.description"
                placeholder="请输入活动描述"
                :rows="3"
                size="middle"
                class="form-textarea"
                show-count
                :maxlength="500"
              />
            </a-form-item>

            <a-form-item label="活动模板" name="templateId">
              <a-select
                v-model:value="form.templateId"
                placeholder="请选择活动模板"
                size="middle"
                class="form-input"
                :disabled="isEdit"
                @change="handleTemplateChange"
              >
                <a-select-option
                  v-for="template in templates"
                  :key="template.id"
                  :value="template.id"
                >
                  {{ template.name }}
                </a-select-option>
              </a-select>
              <div class="form-help-text" v-if="!isEdit">
                选择模板后，将根据模板的字段配置生成表单
              </div>
            </a-form-item>

            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="form.status" :default-value="0">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </div>

          <!-- 配置数据区域 -->
          <div v-if="selectedTemplate && selectedTemplate.fields" class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <SettingOutlined />
              </div>
              <div class="section-title">配置数据</div>
            </div>
            
            <a-form-item
              v-for="field in sortedFields"
              :key="field.name || ''"
              :label="field.label || field.name"
              :name="['configData', field.name || '']"
              :rules="getFieldRules(field)"
            >
              <!-- 文本框 -->
              <a-input
                v-if="field.inputType === 'TEXT'"
                v-model:value="form.configData[field.name || '']"
                :placeholder="field.placeholder || `请输入${field.label || field.name}`"
                size="middle"
                class="form-input"
              />
              
              <!-- 多行文本 -->
              <a-textarea
                v-else-if="field.inputType === 'TEXTAREA'"
                v-model:value="form.configData[field.name || '']"
                :placeholder="field.placeholder || `请输入${field.label || field.name}`"
                :rows="4"
                size="middle"
                class="form-textarea"
              />
              
              <!-- 数字 -->
              <a-input-number
                v-else-if="field.inputType === 'NUMBER'"
                v-model:value="form.configData[field.name || '']"
                :min="field.min as number"
                :max="field.max as number"
                :placeholder="field.placeholder || `请输入${field.label || field.name}`"
                size="middle"
                class="form-input"
                style="width: 100%"
              />
              
              <!-- 下拉框 -->
              <a-select
                v-else-if="field.inputType === 'SELECT'"
                v-model:value="form.configData[field.name || '']"
                :placeholder="field.placeholder || `请选择${field.label || field.name}`"
                size="middle"
                class="form-input"
                :mode="field.multiple ? 'multiple' : undefined"
              >
                <a-select-option
                  v-for="option in field.options"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label || option.value }}
                </a-select-option>
              </a-select>
              
              <!-- 多选下拉框 -->
              <a-select
                v-else-if="field.inputType === 'MULTI_SELECT'"
                v-model:value="form.configData[field.name || '']"
                :placeholder="field.placeholder || `请选择${field.label || field.name}`"
                size="middle"
                class="form-input"
                mode="multiple"
              >
                <a-select-option
                  v-for="option in field.options"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label || option.value }}
                </a-select-option>
              </a-select>
              
              <!-- 复选框 -->
              <a-checkbox
                v-else-if="field.inputType === 'CHECKBOX'"
                v-model:checked="form.configData[field.name || '']"
              >
                {{ field.label || field.name }}
              </a-checkbox>
              
              <!-- 日期 -->
              <a-date-picker
                v-else-if="field.inputType === 'DATE'"
                v-model:value="form.configData[field.name || '']"
                :placeholder="field.placeholder || `请选择${field.label || field.name}`"
                size="middle"
                class="form-input"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
              
              <!-- 日期时间 -->
              <a-date-picker
                v-else-if="field.inputType === 'DATETIME'"
                v-model:value="form.configData[field.name || '']"
                :placeholder="field.placeholder || `请选择${field.label || field.name}`"
                size="middle"
                class="form-input"
                style="width: 100%"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
              
              <div v-if="field.description" class="form-help-text">
                {{ field.description }}
              </div>
            </a-form-item>
          </div>

          <!-- 操作按钮区域 -->
          <div class="form-actions">
            <a-space size="small">
              <ThemeButton 
                variant="primary" 
                size="small"
                :icon="CheckOutlined"
                :disabled="saving"
                @click="handleSave"
                class="submit-btn"
              >
                {{ isEdit ? '更新活动' : '创建活动' }}
              </ThemeButton>
              <ThemeButton 
                variant="secondary"
                size="small"
                :icon="CloseOutlined"
                @click="handleBack"
                class="cancel-btn"
              >
                取消
              </ThemeButton>
            </a-space>
          </div>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  CalendarOutlined,
  IdcardOutlined,
  SettingOutlined,
  CheckOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import { activityApi, type Activity, type ActivityForm } from '@/api/activity'
import { activityTemplateApi, type ActivityTemplate, type ActivityTemplateField } from '@/api/activityTemplate'
import ThemeButton from '@/components/ThemeButton.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const formRef = ref()
const saving = ref(false)
const isEdit = ref(false)
const templates = ref<ActivityTemplate[]>([])
const selectedTemplate = ref<ActivityTemplate | null>(null)

// 表单数据
const form = reactive<ActivityForm & { configData: Record<string, any> }>({
  name: '',
  description: '',
  templateId: undefined,
  status: 0,
  configData: {}
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  templateId: [{ required: true, message: '请选择活动模板', trigger: 'change' }]
}

// 计算属性：排序后的字段列表
const sortedFields = computed(() => {
  if (!selectedTemplate.value?.fields) {
    return []
  }
  return [...selectedTemplate.value.fields].sort((a, b) => (a.sort || 0) - (b.sort || 0))
})

// 方法
const loadTemplates = async () => {
  try {
    const response = await activityTemplateApi.getAll()
    templates.value = response.data || []
  } catch (error) {
    message.error('加载模板列表失败')
  }
}

const loadData = async () => {
  const id = route.params.id as string
  if (!id || id === 'create') {
    isEdit.value = false
    return
  }

  isEdit.value = true
  try {
    const response = await activityApi.getById(Number(id))
    if (response.data) {
      form.name = response.data.name || ''
      form.description = response.data.description || ''
      form.templateId = response.data.templateId
      form.status = response.data.status ?? 0
      form.configData = response.data.configData || {}
      
      // 加载模板信息
      if (form.templateId) {
        await loadTemplate(form.templateId)
      }
    }
  } catch (error) {
    message.error('加载数据失败')
    router.back()
  }
}

const loadTemplate = async (templateId: number) => {
  try {
    const response = await activityTemplateApi.getById(templateId)
    if (response.data) {
      selectedTemplate.value = response.data
      // 初始化配置数据
      if (!isEdit.value) {
        initializeConfigData()
      }
    }
  } catch (error) {
    message.error('加载模板信息失败')
  }
}

const handleTemplateChange = async (templateId: number) => {
  await loadTemplate(templateId)
}

const initializeConfigData = () => {
  if (!selectedTemplate.value?.fields) {
    return
  }
  
  form.configData = {}
  selectedTemplate.value.fields.forEach(field => {
    const fieldName = field.name || ''
    if (field.defaultValue !== undefined && field.defaultValue !== null) {
      // 如果默认值是字符串且类型是数组或JSON对象，尝试解析
      if (typeof field.defaultValue === 'string') {
        if (field.type === 'ARRAY' || field.type === 'JSON_OBJECT') {
          try {
            form.configData[fieldName] = JSON.parse(field.defaultValue)
          } catch (e) {
            // 解析失败，使用原值
            form.configData[fieldName] = field.defaultValue
          }
        } else {
          form.configData[fieldName] = field.defaultValue
        }
      } else {
        // 直接使用默认值（可能是数字、布尔值、对象、数组等）
        // 包括下拉框的选项值、多选下拉框的数组、复选框的布尔值等
        form.configData[fieldName] = field.defaultValue
      }
    } else if (field.type === 'ARRAY' || field.inputType === 'MULTI_SELECT') {
      form.configData[fieldName] = []
    } else if (field.type === 'JSON_OBJECT') {
      form.configData[fieldName] = {}
    } else if (field.inputType === 'CHECKBOX') {
      // 复选框默认值为false
      form.configData[fieldName] = false
    }
  })
}

const getFieldRules = (field: ActivityTemplateField) => {
  const rules: any[] = []
  
  if (field.required) {
    rules.push({
      required: true,
      message: `请输入${field.label || field.name}`,
      trigger: 'blur'
    })
  }
  
  // 判断是否为数组类型（多选下拉框）
  const isArrayType = field.type === 'ARRAY' || field.inputType === 'MULTI_SELECT' || field.multiple === true
  
  // 检查minLength和maxLength是否有有效值
  const hasMinLength = field.minLength !== undefined && field.minLength !== null && field.minLength >= 0
  const hasMaxLength = field.maxLength !== undefined && field.maxLength !== null && field.maxLength >= 0
  
  // 对于字符串类型，使用自定义验证器验证长度
  // 只有当minLength或maxLength有有效值时才添加验证规则
  if (!isArrayType && field.type === 'STRING' && (hasMinLength || hasMaxLength)) {
    rules.push({
      validator: (_rule: any, value: any) => {
        // 如果字段不是必填且值为空，则通过验证（必填验证由上面的required规则处理）
        if (!field.required && (value === null || value === undefined || value === '')) {
          return Promise.resolve()
        }
        const length = typeof value === 'string' ? value.length : 0
        if (hasMinLength && length < field.minLength!) {
          return Promise.reject(`长度应不少于${field.minLength}个字符`)
        }
        if (hasMaxLength && length > field.maxLength!) {
          return Promise.reject(`长度应不超过${field.maxLength}个字符`)
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    })
  }
  
  if (isArrayType && (hasMinLength || hasMaxLength)) {
    rules.push({
      validator: (_rule: any, value: any) => {
        // 如果字段不是必填且值为空，则通过验证
        if (!field.required && (value === null || value === undefined || (Array.isArray(value) && value.length === 0))) {
          return Promise.resolve()
        }
        const length = Array.isArray(value) ? value.length : (value ? 1 : 0)
        if (hasMinLength && length < field.minLength!) {
          return Promise.reject(`至少选择${field.minLength}项`)
        }
        if (hasMaxLength && length > field.maxLength!) {
          return Promise.reject(`最多选择${field.maxLength}项`)
        }
        return Promise.resolve()
      },
      trigger: 'change'
    })
  }
  
  if (field.pattern && field.type === 'STRING' && !isArrayType) {
    rules.push({
      pattern: new RegExp(field.pattern),
      message: '格式不正确',
      trigger: 'blur'
    })
  }
  
  return rules
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    
    saving.value = true
    const formData: ActivityForm = {
      name: form.name,
      description: form.description,
      templateId: form.templateId,
      status: form.status,
      configData: form.configData
    }

    if (isEdit.value) {
      const id = route.params.id as string
      await activityApi.update(Number(id), formData)
      message.success('活动更新成功')
    } else {
      await activityApi.create(formData)
      message.success('活动创建成功')
    }

    router.push('/activity/list')
  } catch (error: any) {
    if (error?.errorFields) {
      message.error('请完善表单信息')
    } else {
      message.error(isEdit.value ? '活动更新失败' : '活动创建失败')
    }
  } finally {
    saving.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(async () => {
  await loadTemplates()
  await loadData()
})
</script>

<style scoped lang="css">
.activity-edit-page {
  min-height: 100%;
  background: #f0f2f5;
  padding: 0;
}

.form-header {
  margin-bottom: 8px;
  padding: 0 16px;
  margin-top: 16px;
}

.header-content {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--primary-color, #1890ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 18px;
}

.header-text {
  flex: 1;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 2px;
  line-height: 1.2;
}

.header-subtitle {
  font-size: 11px;
  color: #666;
  line-height: 1.2;
}

.form-content {
  padding: 0 16px 16px;
}

.form-card {
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.form-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 18px;
  color: #1890ff;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.form-help-text {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
</style>
