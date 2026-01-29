<template>
  <div class="template-edit-page">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <FileTextOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">{{ isEdit ? '编辑模板' : '创建模板' }}</div>
          <div class="header-subtitle">{{ isEdit ? '修改模板信息和字段配置' : '创建新的活动模板并配置字段' }}</div>
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
          class="template-form-content"
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
            
            <a-form-item label="模板名称" name="name">
              <a-input
                v-model:value="form.name"
                placeholder="请输入模板名称"
                size="middle"
                class="form-input"
              />
            </a-form-item>

            <a-form-item label="模板描述" name="description">
              <a-textarea
                v-model:value="form.description"
                placeholder="请输入模板描述"
                :rows="3"
                size="middle"
                class="form-textarea"
                show-count
                :maxlength="500"
              />
            </a-form-item>

            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="form.status" :default-value="0">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </div>

          <!-- 字段配置区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <SettingOutlined />
              </div>
              <div class="section-title">字段配置</div>
              <div class="section-extra">
                <ThemeButton variant="primary" size="small" :icon="PlusOutlined" @click="handleAddField">
                  添加字段
                </ThemeButton>
              </div>
            </div>
            
            <a-form-item label="字段列表">
              <div class="fields-container">
                <a-empty v-if="form.fields?.length === 0" description="暂无字段，请添加字段" />
                <div v-else class="fields-list">
                  <div
                    v-for="(field, index) in form.fields"
                    :key="`field-${index}`"
                    class="field-item"
                    :draggable="true"
                    @dragstart="handleDragStart(index, $event)"
                    @dragover.prevent="handleDragOver($event)"
                    @drop="handleDrop(index, $event)"
                    @dragend="handleDragEnd"
                    :class="{ 'dragging': draggedIndex === index }"
                  >
                    <a-card size="small" class="field-card">
                      <template #title>
                        <div class="field-title">
                          <DragOutlined class="drag-handle" />
                          <span>{{ field.label || field.name || `字段 ${index + 1}` }}</span>
                        </div>
                      </template>
                      <template #extra>
                        <a-space size="small">
                          <a-button 
                            type="text" 
                            size="small" 
                            class="field-action-btn field-edit-btn"
                            @click="handleEditField(index)"
                          >
                            <template #icon><EditOutlined /></template>
                            编辑
                          </a-button>
                          <a-button 
                            type="text" 
                            size="small" 
                            class="field-action-btn field-delete-btn"
                            @click="handleRemoveField(index)"
                          >
                            <template #icon><DeleteOutlined /></template>
                            删除
                          </a-button>
                        </a-space>
                      </template>
                      <div class="field-info">
                        <div class="field-info-grid">
                          <div class="field-info-item">
                            <span class="field-info-label">字段名</span>
                            <span class="field-info-value">{{ field.name || '-' }}</span>
                          </div>
                          <div class="field-info-item">
                            <span class="field-info-label">字段标签</span>
                            <span class="field-info-value">{{ field.label || '-' }}</span>
                          </div>
                          <div class="field-info-item">
                            <span class="field-info-label">数据类型</span>
                            <span class="field-info-value">{{ field.type || '-' }}</span>
                          </div>
                          <div class="field-info-item">
                            <span class="field-info-label">输入类型</span>
                            <span class="field-info-value">{{ field.inputType || '-' }}</span>
                          </div>
                          <div class="field-info-item">
                            <span class="field-info-label">必填</span>
                            <a-tag :color="field.required ? 'red' : 'default'" size="small">
                              {{ field.required ? '是' : '否' }}
                            </a-tag>
                          </div>
                          <div class="field-info-item">
                            <span class="field-info-label">排序</span>
                            <span class="field-info-value">{{ index + 1 }}</span>
                          </div>
                          <div class="field-info-item field-info-item-full">
                            <span class="field-info-label">默认值</span>
                            <span class="field-info-value" v-if="field.defaultValue !== undefined && field.defaultValue !== null">
                              {{ formatDefaultValue(field.defaultValue) }}
                            </span>
                            <span class="field-info-value" v-else>-</span>
                          </div>
                        </div>
                      </div>
                    </a-card>
                  </div>
                </div>
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
                {{ isEdit ? '更新模板' : '创建模板' }}
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

    <!-- 字段编辑对话框 -->
    <a-modal
      v-model:open="fieldModalVisible"
      :title="editingFieldIndex !== null ? '编辑字段' : '添加字段'"
      width="800px"
      @ok="handleSaveField"
      @cancel="handleCancelField"
    >
      <a-form
        ref="fieldFormRef"
        :model="fieldForm"
        :rules="fieldRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="字段名" name="name">
          <a-input v-model:value="fieldForm.name" placeholder="请输入字段名（英文）" />
        </a-form-item>
        <a-form-item label="字段标签" name="label">
          <a-input v-model:value="fieldForm.label" placeholder="请输入字段标签（中文）" />
        </a-form-item>
        <a-form-item label="数据类型" name="type">
          <a-select v-model:value="fieldForm.type" placeholder="请选择数据类型">
            <a-select-option value="STRING">字符串</a-select-option>
            <a-select-option value="INTEGER">整数</a-select-option>
            <a-select-option value="DOUBLE">浮点数</a-select-option>
            <a-select-option value="BOOLEAN">布尔值</a-select-option>
            <a-select-option value="DATE">日期</a-select-option>
            <a-select-option value="DATETIME">日期时间</a-select-option>
            <a-select-option value="ARRAY">数组</a-select-option>
            <a-select-option value="JSON_OBJECT">JSON对象</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="输入类型" name="inputType">
          <a-select v-model:value="fieldForm.inputType" placeholder="请选择输入类型">
            <a-select-option value="TEXT">文本框</a-select-option>
            <a-select-option value="SELECT">下拉框</a-select-option>
            <a-select-option value="MULTI_SELECT">多选下拉框</a-select-option>
            <a-select-option value="NUMBER">数字</a-select-option>
            <a-select-option value="TEXTAREA">多行文本</a-select-option>
            <a-select-option value="DATE">日期</a-select-option>
            <a-select-option value="DATETIME">日期时间</a-select-option>
            <a-select-option value="CHECKBOX">复选框</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="是否必填" name="required">
          <a-switch v-model:checked="fieldForm.required" />
        </a-form-item>
        <a-form-item label="默认值" name="defaultValue">
          <!-- 下拉框类型的默认值：从选项中选择 -->
          <a-select
            v-if="fieldForm.inputType === 'SELECT' && fieldForm.options && fieldForm.options.length > 0"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
            allow-clear
          >
            <a-select-option
              v-for="option in fieldForm.options"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label || option.value }}
            </a-select-option>
          </a-select>
          <!-- 多选下拉框类型的默认值：从选项中选择多个 -->
          <a-select
            v-else-if="fieldForm.inputType === 'MULTI_SELECT' && fieldForm.options && fieldForm.options.length > 0"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
            mode="multiple"
            allow-clear
          >
            <a-select-option
              v-for="option in fieldForm.options"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label || option.value }}
            </a-select-option>
          </a-select>
          <!-- 字符串、日期、日期时间类型 -->
          <a-input
            v-else-if="['STRING', 'DATE', 'DATETIME'].includes(fieldForm.type || '')"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
          />
          <!-- 整数类型 -->
          <a-input-number
            v-else-if="fieldForm.type === 'INTEGER'"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
            style="width: 100%"
            :precision="0"
          />
          <!-- 浮点数类型 -->
          <a-input-number
            v-else-if="fieldForm.type === 'DOUBLE'"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
            style="width: 100%"
            :precision="2"
          />
          <!-- 布尔值类型 -->
          <a-switch
            v-else-if="fieldForm.type === 'BOOLEAN'"
            v-model:checked="fieldForm.defaultValue"
          />
          <!-- 数组类型 -->
          <a-textarea
            v-else-if="fieldForm.type === 'ARRAY'"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
            :rows="3"
          />
          <!-- JSON对象类型 -->
          <a-textarea
            v-else-if="fieldForm.type === 'JSON_OBJECT'"
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
            :rows="4"
          />
          <!-- 其他类型 -->
          <a-input
            v-else
            v-model:value="fieldForm.defaultValue"
            :placeholder="getDefaultValuePlaceholder()"
          />
          <div class="default-value-tip" v-if="fieldForm.type === 'ARRAY'">
            请输入JSON数组格式，例如：["value1", "value2"]
          </div>
          <div class="default-value-tip" v-if="fieldForm.type === 'JSON_OBJECT'">
            请输入JSON对象格式，例如：{"key": "value"}
          </div>
        </a-form-item>
        <a-form-item label="占位符" name="placeholder">
          <a-input v-model:value="fieldForm.placeholder" placeholder="请输入占位符" />
        </a-form-item>
        <a-form-item label="字段描述" name="description">
          <a-textarea v-model:value="fieldForm.description" :rows="2" placeholder="请输入字段描述" />
        </a-form-item>
        <a-form-item v-if="fieldForm.inputType && ['SELECT', 'MULTI_SELECT'].includes(fieldForm.inputType)" label="选项配置">
          <div class="options-container">
            <div v-for="(option, idx) in fieldForm.options" :key="idx" class="option-item">
              <a-input
                v-model:value="option.value"
                placeholder="选项值"
                style="width: 45%; margin-right: 10px"
              />
              <a-input
                v-model:value="option.label"
                placeholder="选项标签"
                style="width: 45%; margin-right: 10px"
              />
              <a-button type="text" danger @click="handleRemoveOption(idx)">删除</a-button>
            </div>
            <a-button type="dashed" block @click="handleAddOption">
              <PlusOutlined /> 添加选项
            </a-button>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  FileTextOutlined,
  IdcardOutlined,
  SettingOutlined,
  CheckOutlined,
  CloseOutlined,
  DragOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { activityTemplateApi, type ActivityTemplateField } from '@/api/activityTemplate'
import ThemeButton from '@/components/ThemeButton.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const formRef = ref()
const fieldFormRef = ref()
const saving = ref(false)
const isEdit = ref(false)
const fieldModalVisible = ref(false)
const editingFieldIndex = ref<number | null>(null)
const draggedIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)

// 表单数据
const form = reactive<{
  name: string
  description: string
  status: number
  fields: ActivityTemplateField[]
}>({
  name: '',
  description: '',
  status: 0,
  fields: []
})

// 字段表单数据
const fieldForm = reactive<ActivityTemplateField & { options?: Array<{ value: string; label?: string }> }>({
  name: '',
  label: '',
  type: 'STRING',
  inputType: 'TEXT',
  required: false,
  sort: 0,
  placeholder: '',
  description: '',
  defaultValue: undefined,
  options: []
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }]
}

const fieldRules = {
  name: [{ required: true, message: '请输入字段名', trigger: 'blur' }],
  label: [{ required: true, message: '请输入字段标签', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  inputType: [{ required: true, message: '请选择输入类型', trigger: 'change' }]
}

// 方法
const loadData = async () => {
  const id = route.params.id as string
  if (!id || id === 'create') {
    isEdit.value = false
    return
  }

  isEdit.value = true
  try {
    const response = await activityTemplateApi.getById(Number(id))
    if (response.data) {
      form.name = response.data.name || ''
      form.description = response.data.description || ''
      form.status = response.data.status ?? 1
      form.fields = response.data.fields || []
      // 确保字段按排序号排序
      form.fields.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      // 更新排序号，确保连续
      updateFieldSorts()
    }
  } catch (error) {
    message.error('加载数据失败')
    router.back()
  }
}

const handleAddField = () => {
  editingFieldIndex.value = null
  resetFieldForm()
  fieldModalVisible.value = true
}

const handleEditField = (index: number) => {
  editingFieldIndex.value = index
  const field = form.fields[index]
  fieldForm.name = field.name || ''
  fieldForm.label = field.label || ''
  fieldForm.type = field.type || 'STRING'
  fieldForm.inputType = field.inputType || 'TEXT'
  fieldForm.required = field.required || false
  fieldForm.placeholder = field.placeholder || ''
  fieldForm.description = field.description || ''
  
  // 处理默认值：如果是对象或数组，转换为JSON字符串；布尔值直接使用；下拉框和多选下拉框直接使用
  if (field.defaultValue !== undefined && field.defaultValue !== null) {
    if (field.type === 'ARRAY' || field.type === 'JSON_OBJECT') {
      if (typeof field.defaultValue === 'string') {
        fieldForm.defaultValue = field.defaultValue
      } else {
        fieldForm.defaultValue = JSON.stringify(field.defaultValue, null, 2)
      }
    } else if (field.inputType === 'MULTI_SELECT' && Array.isArray(field.defaultValue)) {
      // 多选下拉框的默认值是数组，直接使用
      fieldForm.defaultValue = field.defaultValue
    } else {
      // 其他类型（包括下拉框、复选框等）直接使用
      fieldForm.defaultValue = field.defaultValue
    }
  } else {
    fieldForm.defaultValue = undefined
  }
  
  fieldForm.options = field.options?.map(opt => ({ value: opt.value || '', label: opt.label || '' })) || []
  fieldModalVisible.value = true
}

const handleRemoveField = (index: number) => {
  form.fields.splice(index, 1)
}

const handleSaveField = async () => {
  try {
    await fieldFormRef.value?.validate()
    
    // 处理默认值：根据类型转换
    let defaultValue = fieldForm.defaultValue
    if (defaultValue !== undefined && defaultValue !== null) {
      // 空字符串、空数组等需要特殊处理
      if (defaultValue === '' || (Array.isArray(defaultValue) && defaultValue.length === 0)) {
        defaultValue = undefined
      } else if (fieldForm.type === 'ARRAY' || fieldForm.type === 'JSON_OBJECT') {
        // 如果是数组或JSON对象，尝试解析JSON字符串
        if (typeof defaultValue === 'string') {
          try {
            defaultValue = JSON.parse(defaultValue)
          } catch (e) {
            message.warning('默认值格式不正确，将保存为字符串')
          }
        }
      } else if (fieldForm.type === 'INTEGER' && typeof defaultValue === 'string') {
        defaultValue = parseInt(defaultValue, 10)
      } else if (fieldForm.type === 'DOUBLE' && typeof defaultValue === 'string') {
        defaultValue = parseFloat(defaultValue)
      }
      // 下拉框、多选下拉框、复选框的默认值直接使用，不需要转换
    } else {
      defaultValue = undefined
    }
    
    const field: ActivityTemplateField = {
      name: fieldForm.name,
      label: fieldForm.label,
      type: fieldForm.type,
      inputType: fieldForm.inputType,
      required: fieldForm.required,
      sort: editingFieldIndex.value !== null ? editingFieldIndex.value : form.fields.length,
      placeholder: fieldForm.placeholder,
      description: fieldForm.description,
      defaultValue: defaultValue,
      options: fieldForm.options?.filter(opt => opt.value) || []
    }

    if (editingFieldIndex.value !== null) {
      form.fields[editingFieldIndex.value] = field
    } else {
      form.fields.push(field)
    }

    // 更新所有字段的排序号
    updateFieldSorts()

    fieldModalVisible.value = false
    resetFieldForm()
  } catch (error) {
    // 验证失败
  }
}

const handleCancelField = () => {
  fieldModalVisible.value = false
  resetFieldForm()
}

const resetFieldForm = () => {
  fieldForm.name = ''
  fieldForm.label = ''
  fieldForm.type = 'STRING'
  fieldForm.inputType = 'TEXT'
  fieldForm.required = false
  fieldForm.placeholder = ''
  fieldForm.description = ''
  fieldForm.defaultValue = undefined
  fieldForm.options = []
}

// 获取默认值占位符
const getDefaultValuePlaceholder = () => {
  const inputType = fieldForm.inputType
  const type = fieldForm.type
  
  if (inputType === 'SELECT') {
    return '请选择默认值'
  } else if (inputType === 'MULTI_SELECT') {
    return '请选择默认值（可多选）'
  } else if (type === 'STRING') {
    return '请输入默认字符串值'
  } else if (type === 'INTEGER') {
    return '请输入默认整数值'
  } else if (type === 'DOUBLE') {
    return '请输入默认浮点数值'
  } else if (type === 'DATE') {
    return '请输入默认日期，格式：YYYY-MM-DD'
  } else if (type === 'DATETIME') {
    return '请输入默认日期时间，格式：YYYY-MM-DD HH:mm:ss'
  } else if (type === 'ARRAY') {
    return '请输入JSON数组格式，例如：["value1", "value2"]'
  } else if (type === 'JSON_OBJECT') {
    return '请输入JSON对象格式，例如：{"key": "value"}'
  }
  return '请输入默认值'
}

// 拖拽排序相关方法
const handleDragStart = (index: number, event: DragEvent) => {
  draggedIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/html', String(index))
  }
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

const handleDrop = (targetIndex: number, event: DragEvent) => {
  event.preventDefault()
  if (draggedIndex.value === null || draggedIndex.value === targetIndex) {
    return
  }

  const draggedField = form.fields[draggedIndex.value]
  form.fields.splice(draggedIndex.value, 1)
  form.fields.splice(targetIndex, 0, draggedField)

  // 更新所有字段的排序号
  updateFieldSorts()

  dragOverIndex.value = null
}

const handleDragEnd = () => {
  draggedIndex.value = null
  dragOverIndex.value = null
}

// 更新所有字段的排序号
const updateFieldSorts = () => {
  form.fields.forEach((field, index) => {
    field.sort = index
  })
}

// 格式化默认值显示
const formatDefaultValue = (value: any): string => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

const handleAddOption = () => {
  if (!fieldForm.options) {
    fieldForm.options = []
  }
  fieldForm.options.push({ value: '', label: '' })
}

const handleRemoveOption = (index: number) => {
  if (fieldForm.options) {
    fieldForm.options.splice(index, 1)
  }
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    
    // 确保所有字段的排序号都是最新的
    updateFieldSorts()
    
    saving.value = true
    const formData = {
      name: form.name,
      description: form.description,
      status: form.status,
      fields: form.fields
    }

    if (isEdit.value) {
      const id = route.params.id as string
      await activityTemplateApi.update(Number(id), formData)
      message.success('模板更新成功')
    } else {
      await activityTemplateApi.create(formData)
      message.success('模板创建成功')
    }

    router.push('/activity/template')
  } catch (error: any) {
    if (error?.errorFields) {
      message.error('请完善表单信息')
    } else {
      message.error(isEdit.value ? '模板更新失败' : '模板创建失败')
    }
  } finally {
    saving.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="css">
.template-edit-page {
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

.section-extra {
  margin-left: auto;
}

.fields-container {
  width: 100%;
}

.fields-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-item {
  width: 100%;
  cursor: move;
  transition: all 0.3s ease;
  position: relative;
}

.field-item.dragging {
  opacity: 0.5;
}

.field-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.field-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.drag-handle {
  color: #999;
  cursor: grab;
  font-size: 14px;
}

.drag-handle:active {
  cursor: grabbing;
}

.field-card {
  margin-bottom: 0;
}

.field-card :deep(.ant-card-head) {
  min-height: 36px;
  padding: 6px 12px;
}

.field-card :deep(.ant-card-head-title) {
  padding: 0;
  font-size: 13px;
}

.field-card :deep(.ant-card-extra) {
  padding: 0;
}

.field-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 12px;
  height: 24px;
}

.field-edit-btn {
  color: #1890ff;
}

.field-edit-btn:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.field-delete-btn {
  color: #ff4d4f;
}

.field-delete-btn:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.field-card :deep(.ant-card-body) {
  padding: 8px 12px;
}

.field-info {
  margin-top: 0;
}

.field-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 4px 0;
}

.field-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.field-info-item-full {
  grid-column: 1 / -1;
}

.field-info-label {
  font-size: 11px;
  color: #8c8c8c;
  line-height: 1.4;
}

.field-info-value {
  font-size: 12px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
  word-break: break-word;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
}

.default-value-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
</style>
