<template>
  <div class="param-form-content">
    <!-- 基本信息区域 -->
    <div class="form-section">
      <div class="section-title">
        <span class="title-icon">📝</span>
        基本信息
      </div>
      <a-row :gutter="[16, 12]">
        <a-col :span="12">
          <a-form-item label="参数名" :name="[paramType, index, 'name']" class="form-item-modern">
            <a-input
              v-model:value="param.name"
              placeholder="请输入参数名"
              :disabled="disabled"
              class="modern-input"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="输入类型" :name="[paramType, index, 'inputType']" class="form-item-modern">
            <a-select
              v-model:value="param.inputType"
              placeholder="选择输入类型"
              :disabled="disabled"
              class="modern-select"
              @change="handleInputTypeChange"
            >
              <a-select-option
                v-for="type in INPUT_TYPES"
                :key="type.value"
                :value="type.value"
              >
                {{ type.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      
      <a-row :gutter="[16, 12]">
        <a-col :span="12">
          <a-form-item label="数据类型" :name="[paramType, index, 'dataType']" class="form-item-modern">
            <a-select
              v-model:value="param.dataType"
              placeholder="选择数据类型"
              :disabled="disabled"
              class="modern-select"
              @change="handleDataTypeChange"
            >
              <a-select-option
                v-for="type in getAvailableDataTypes()"
                :key="type.value"
                :value="type.value"
              >
                {{ type.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="默认值" class="form-item-modern">
            <!-- 代码编辑器弹窗按钮 -->
            <div v-if="param.inputType === 'CODE' || param.dataType === 'JSON'" class="code-editor-input">
              <a-input
                :value="getCodePreview(param.defaultValue)"
                :placeholder="getDefaultValuePlaceholder()"
                :disabled="disabled"
                readonly
                class="code-preview-input"
                @click="!disabled && openCodeEditor()"
              >
                <template #suffix>
                  <ThemeButton
                    v-if="!disabled"
                    variant="secondary"
                    size="small"
                    @click.stop="openCodeEditor"
                  >
                    <template #icon>
                      <span>📝</span>
                    </template>
                    编辑
                  </ThemeButton>
                </template>
              </a-input>
            </div>
            <!-- 其他输入组件 -->
            <component
              v-else
              :is="getDefaultValueComponent()"
              v-bind="getDefaultValueBindings()"
              :placeholder="getDefaultValuePlaceholder()"
              :disabled="disabled"
              :class="getDefaultValueClass()"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </div>

    <!-- 配置选项区域 -->
    <div class="form-section">
      <div class="section-title">
        <span class="title-icon">⚙️</span>
        配置选项
      </div>
      <a-row :gutter="[16, 12]">
        <a-col :span="8">
          <div class="checkbox-group">
            <a-checkbox
              v-model:checked="param.required"
              :disabled="disabled"
              class="modern-checkbox"
            >
              <span class="checkbox-label">
                <span class="checkbox-icon">✅</span>
                必填
              </span>
            </a-checkbox>
          </div>
        </a-col>
        <a-col :span="8">
          <div class="checkbox-group">
            <a-checkbox
              v-model:checked="param.changeable"
              :disabled="disabled"
              class="modern-checkbox"
            >
              <span class="checkbox-label">
                <span class="checkbox-icon">🔄</span>
                可变更
              </span>
            </a-checkbox>
          </div>
        </a-col>
        <a-col :span="8">
          <a-form-item label="排序" class="form-item-modern">
            <a-input-number
              v-model:value="param.sort"
              :min="1"
              :max="999"
              :disabled="disabled"
              class="modern-input-number"
              placeholder="排序"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </div>

    <!-- 下拉选项区域 -->
    <div v-if="param.inputType === 'SELECT'" class="form-section">
      <div class="section-title">
        <span class="title-icon">📋</span>
        下拉选项
      </div>
      <div class="options-container">
        <div
          v-for="(_, optionIndex) in (param.options || [])"
          :key="optionIndex"
          class="option-item"
        >
          <div class="option-number">{{ optionIndex + 1 }}</div>
          <a-input
            v-model:value="param.options![optionIndex]"
            placeholder="请输入选项值"
            :disabled="disabled"
            class="option-input"
          />
          <ThemeButton
            v-if="!disabled"
            variant="danger"
            size="small"
            class="option-remove-btn"
            @click="handleRemoveOption(optionIndex)"
          >
            🗑️
          </ThemeButton>
        </div>
        <ThemeButton
          v-if="!disabled"
          variant="secondary"
          size="small"
          class="add-option-btn"
          @click="handleAddOption"
        >
          ➕ 添加选项
        </ThemeButton>
      </div>
    </div>

    <!-- 描述区域 -->
    <div class="form-section">
      <div class="section-title">
        <span class="title-icon">📄</span>
        参数描述
      </div>
      <a-form-item class="form-item-modern">
        <a-textarea
          v-model:value="param.description"
          placeholder="请输入参数描述（可选）"
          :rows="3"
          :disabled="disabled"
          class="modern-textarea"
        />
      </a-form-item>
    </div>
    
    <!-- 代码编辑器弹窗 -->
    <CodeEditorModal
      v-model:visible="codeEditorVisible"
      v-model:value="tempCodeValue"
      :language="getCodeLanguage()"
      :placeholder="getDefaultValuePlaceholder()"
      @confirm="handleCodeConfirm"
      @cancel="handleCodeCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { INPUT_TYPES, DATA_TYPES, type ApiParam } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import CodeEditorModal from '@/components/CodeEditorModal.vue'

// Props
interface Props {
  param: ApiParam
  index: number
  paramType: string
  disabled: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'input-type-change': [paramType: string, index: number]
  'data-type-change': [paramType: string, index: number, dataType: string]
  'default-value-change': [paramType: string, index: number, defaultValue: string]
  'add-option': [paramType: string, index: number]
  'remove-option': [paramType: string, index: number, optionIndex: number]
}>()

// 响应式数据
const codeEditorVisible = ref(false)
const tempCodeValue = ref('')

// 方法
const handleInputTypeChange = () => {
  // 当输入类型改变时，自动调整数据类型
  const inputType = props.param.inputType
  let newDataType = 'STRING'
  
  if (inputType === 'NUMBER') {
    newDataType = 'INTEGER'
  } else if (inputType === 'DATE') {
    newDataType = 'DATE'
  } else if (inputType === 'DATETIME') {
    newDataType = 'DATETIME'
  } else if (inputType === 'TEXTAREA') {
    newDataType = 'STRING'
  } else if (inputType === 'CODE') {
    newDataType = 'JSON' // 代码编辑器默认为JSON类型
  } else if (inputType === 'PASSWORD') {
    newDataType = 'STRING'
  } else if (inputType === 'EMAIL') {
    newDataType = 'STRING'
  } else if (inputType === 'URL') {
    newDataType = 'STRING'
  } else {
    newDataType = 'STRING'
  }
  
  // 通过 emit 通知父组件更新数据类型
  emit('data-type-change', props.paramType, props.index, newDataType)
  
  // 清空默认值，因为类型可能不匹配
  emit('default-value-change', props.paramType, props.index, '')
  
  emit('input-type-change', props.paramType, props.index)
}

const handleDataTypeChange = () => {
  // 当数据类型改变时，清空默认值
  emit('default-value-change', props.paramType, props.index, '')
}

const getAvailableDataTypes = () => {
  const inputType = props.param.inputType
  switch (inputType) {
    case 'NUMBER':
      return DATA_TYPES.filter(type => ['INTEGER', 'LONG', 'DOUBLE'].includes(type.value))
    case 'DATE':
      return DATA_TYPES.filter(type => type.value === 'DATE')
    case 'DATETIME':
      return DATA_TYPES.filter(type => type.value === 'DATETIME')
    case 'TEXTAREA':
      return DATA_TYPES.filter(type => ['STRING', 'JSON'].includes(type.value))
    case 'CODE':
      return DATA_TYPES.filter(type => ['STRING', 'JSON'].includes(type.value))
    case 'PASSWORD':
    case 'EMAIL':
    case 'URL':
    case 'TEXT':
    case 'SELECT':
      return DATA_TYPES.filter(type => type.value === 'STRING')
    default:
      return DATA_TYPES
  }
}

const getDefaultValueComponent = () => {
  const inputType = props.param.inputType
  const dataType = props.param.dataType
  
  // 如果输入类型是代码编辑器，使用Monaco Editor
  if (inputType === 'CODE') {
    return 'CodeEditor'
  }
  
  switch (dataType) {
    case 'INTEGER':
    case 'LONG':
    case 'DOUBLE':
      return 'a-input-number'
    case 'BOOLEAN':
      return 'a-select'
    case 'DATE':
      return 'a-date-picker'
    case 'DATETIME':
      return 'a-date-picker'
    case 'JSON':
      return 'CodeEditor'
    default:
      return 'a-input'
  }
}

const getDefaultValuePlaceholder = () => {
  const dataType = props.param.dataType
  switch (dataType) {
    case 'INTEGER':
    case 'LONG':
      return '请输入整数'
    case 'DOUBLE':
      return '请输入数字'
    case 'BOOLEAN':
      return '请选择布尔值'
    case 'DATE':
      return '请选择日期'
    case 'DATETIME':
      return '请选择日期时间'
    case 'JSON':
      return '请输入JSON格式数据'
    default:
      return '请输入默认值'
  }
}

const getDefaultValueClass = () => {
  return 'modern-input'
}

const getDefaultValueBindings = () => {
  const inputType = props.param.inputType
  const dataType = props.param.dataType
  const baseProps = getDefaultValueProps()
  
  // 根据组件类型选择不同的 v-model 绑定方式
  if (inputType === 'CODE' || dataType === 'JSON') {
    return {
      ...baseProps,
      modelValue: props.param.defaultValue || '',
      'onUpdate:modelValue': (value: string) => {
        emit('default-value-change', props.paramType, props.index, value)
      }
    }
  } else {
    return {
      ...baseProps,
      value: props.param.defaultValue,
      'onUpdate:value': (value: any) => {
        emit('default-value-change', props.paramType, props.index, value)
      }
    }
  }
}

const getDefaultValueProps = () => {
  const inputType = props.param.inputType
  const dataType = props.param.dataType
  
  // 代码编辑器属性
  if (inputType === 'CODE') {
    return {
      language: 'json',
      height: 200,
      placeholder: '请输入代码...',
      options: {
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        wordWrap: 'on' as const,
        lineNumbers: 'on' as const,
        folding: true,
        fontSize: 14,
        fontFamily: 'Monaco, Menlo, "Ubuntu Mono", monospace'
      }
    }
  }
  
  // JSON数据类型也使用代码编辑器
  if (dataType === 'JSON') {
    return {
      language: 'json',
      height: 150,
      placeholder: '请输入JSON格式数据...',
      options: {
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        wordWrap: 'on' as const,
        lineNumbers: 'on' as const,
        folding: true,
        fontSize: 14,
        fontFamily: 'Monaco, Menlo, "Ubuntu Mono", monospace'
      }
    }
  }
  
  switch (dataType) {
    case 'INTEGER':
    case 'LONG':
      return { precision: 0, step: 1 }
    case 'DOUBLE':
      return { precision: 2, step: 0.01 }
    case 'BOOLEAN':
      return {
        options: [
          { label: '是', value: 'true' },
          { label: '否', value: 'false' }
        ]
      }
    case 'DATE':
      return { showTime: false, format: 'YYYY-MM-DD' }
    case 'DATETIME':
      return { showTime: true, format: 'YYYY-MM-DD HH:mm:ss' }
    default:
      return {}
  }
}

const handleAddOption = () => {
  emit('add-option', props.paramType, props.index)
}

const handleRemoveOption = (optionIndex: number) => {
  emit('remove-option', props.paramType, props.index, optionIndex)
}

// 代码编辑器相关方法
const getCodePreview = (value: string | undefined): string => {
  if (!value || value.trim() === '') {
    return ''
  }
  
  // 如果内容太长，显示前50个字符
  if (value.length > 50) {
    return value.substring(0, 50) + '...'
  }
  
  return value
}

const getCodeLanguage = (): string => {
  if (props.param.dataType === 'JSON') {
    return 'json'
  }
  return 'json' // 默认使用JSON
}

const openCodeEditor = () => {
  tempCodeValue.value = props.param.defaultValue || ''
  codeEditorVisible.value = true
}

const handleCodeConfirm = (value: string) => {
  emit('default-value-change', props.paramType, props.index, value)
  codeEditorVisible.value = false
}

const handleCodeCancel = () => {
  codeEditorVisible.value = false
}
</script>

<style scoped>
.param-form-content {
  background: transparent;
  padding: 0;
}

.form-section {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  border: 1px solid #e1e4e8;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.form-section:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-color: #d0d7de;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #24292f;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e1e4e8;
}

.title-icon {
  margin-right: 8px;
  font-size: 18px;
}

.form-item-modern {
  margin-bottom: 16px;
}

.form-item-modern :deep(.ant-form-item-label) {
  font-weight: 500;
  color: #24292f;
}

.modern-input,
.modern-select,
.modern-textarea {
  border-radius: 8px;
  border: 1px solid #d0d7de;
  transition: all 0.2s ease;
}

.modern-input:focus,
.modern-select:focus,
.modern-textarea:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.modern-input-number {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #d0d7de;
  transition: all 0.2s ease;
}

.modern-input-number:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.checkbox-group {
  display: flex;
  align-items: center;
  height: 32px;
}

.modern-checkbox {
  margin: 0;
}

.modern-checkbox :deep(.ant-checkbox-inner) {
  border-radius: 4px;
  border-color: #d0d7de;
  transition: all 0.2s ease;
}

.modern-checkbox :deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #0969da;
  border-color: #0969da;
}

.checkbox-label {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #24292f;
}

.checkbox-icon {
  margin-right: 6px;
  font-size: 14px;
}

.options-container {
  border: 1px solid #d0d7de;
  border-radius: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f6f8fa 0%, #eaeef2 100%);
  margin-top: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 12px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e1e4e8;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.option-item:hover {
  border-color: #0969da;
  box-shadow: 0 2px 4px rgba(9, 105, 218, 0.1);
  transform: translateY(-1px);
}

.option-item:last-child {
  margin-bottom: 0;
}

.option-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #0969da, #1f883d);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.option-input {
  flex: 1;
  margin-right: 12px;
  border: none;
  background: transparent;
  box-shadow: none;
}

.option-input:focus {
  box-shadow: none;
}

.option-remove-btn {
  flex-shrink: 0;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.option-remove-btn:hover {
  transform: scale(1.05);
}

.add-option-btn {
  width: 100%;
  border-radius: 8px;
  border: 2px dashed #d0d7de;
  background: transparent;
  color: #656d76;
  transition: all 0.2s ease;
  margin-top: 8px;
}

.add-option-btn:hover {
  border-color: #0969da;
  color: #0969da;
  background: rgba(9, 105, 218, 0.05);
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-section {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .section-title {
    font-size: 14px;
    margin-bottom: 12px;
  }
  
  .option-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .option-number {
    align-self: flex-start;
  }
  
  .option-input {
    margin-right: 0;
    margin-bottom: 8px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-section {
  animation: fadeInUp 0.3s ease-out;
}

.option-item {
  animation: fadeInUp 0.2s ease-out;
}

/* 代码编辑器输入样式 */
.code-editor-input {
  width: 100%;
}

.code-preview-input {
  cursor: pointer;
}

.code-preview-input :deep(.ant-input) {
  background-color: #f8f9fa;
  border: 1px solid #d0d7de;
  color: #656d76;
  font-family: Monaco, Menlo, "Ubuntu Mono", monospace;
  font-size: 12px;
  cursor: pointer;
}

.code-preview-input :deep(.ant-input):hover {
  border-color: #0969da;
  background-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(9, 105, 218, 0.1);
}

.code-preview-input :deep(.ant-input):focus {
  border-color: #0969da;
  background-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(9, 105, 218, 0.2);
}
</style>
