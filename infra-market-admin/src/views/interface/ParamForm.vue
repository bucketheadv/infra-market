<template>
  <div class="param-form-content">
    <!-- 基本信息区域 -->
    <div class="form-section">
      <div class="section-title">
        <span class="title-icon">📝</span>
        基本信息
      </div>
      <a-row :gutter="[16, 12]">
        <a-col :span="8">
          <a-form-item label="参数名" :name="[paramType, index, 'name']" class="form-item-modern" :rules="[{ required: true, message: '请输入参数名' }]">
            <a-input
              v-model:value="param.name"
              placeholder="请输入参数名"
              :disabled="disabled"
              class="modern-input"
              @input="handleParamNameChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label="中文名" :name="[paramType, index, 'chineseName']" class="form-item-modern">
            <a-input
              v-model:value="param.chineseName"
              placeholder="请输入中文名（可选）"
              :disabled="disabled"
              class="modern-input"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
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
        <a-col :span="8">
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
        <a-col :span="16">
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
              size="small"
              :controls="false"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </div>

    <!-- 下拉选项区域 -->
    <div v-if="param.inputType === 'SELECT' || param.inputType === 'MULTI_SELECT'" class="form-section">
      <div class="section-title">
        <span class="title-icon">📋</span>
        下拉选项
      </div>
      <div class="options-container">
        <div
          v-for="(option, optionIndex) in (param.options || [])"
          :key="optionIndex"
          class="option-item"
        >
          <div class="option-number">{{ optionIndex + 1 }}</div>
          <div class="option-inputs">
            <a-input
              v-model:value="option.value"
              placeholder="选项值（必填）"
              :disabled="disabled"
              class="option-value-input"
              :class="{ 'error': !option.value || option.value.trim() === '' }"
              @input="handleOptionValueChange"
            />
            <a-input
              v-model:value="option.label"
              placeholder="显示标签（可选）"
              :disabled="disabled"
              class="option-label-input"
            />
          </div>
          <button
            v-if="!disabled"
            class="option-remove-btn"
            @click="handleRemoveOption(optionIndex)"
            title="删除选项"
          >
            <DeleteOutlined />
          </button>
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
      v-model:open="codeEditorVisible"
      v-model:value="tempCodeValue"
      :language="getCodeLanguage()"
      :placeholder="getDefaultValuePlaceholder()"
      @confirm="handleCodeConfirm"
      @cancel="handleCodeCancel"
      @language-change="handleCodeLanguageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DeleteOutlined } from '@ant-design/icons-vue'
import { INPUT_TYPES, DATA_TYPES, CODE_EDITOR_LANGUAGES, type ApiParam } from '@/api/interface'
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
  'default-value-change': [paramType: string, index: number, defaultValue: string | any[]]
  'param-name-change': [paramType: string, index: number, name: string]
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
    newDataType = 'JSON' // 代码编辑器默认为JSON字符串类型
  } else if (inputType === 'PASSWORD') {
    newDataType = 'STRING'
  } else if (inputType === 'EMAIL') {
    newDataType = 'STRING'
  } else if (inputType === 'URL') {
    newDataType = 'STRING'
  } else if (inputType === 'SELECT') {
    newDataType = 'STRING' // 下拉框默认为字符串类型
  } else if (inputType === 'MULTI_SELECT') {
    newDataType = 'ARRAY' // 多选下拉框默认为数组类型
  } else {
    newDataType = 'STRING'
  }
  
  // 通过 emit 通知父组件更新数据类型
  emit('data-type-change', props.paramType, props.index, newDataType)
  
  // 清空默认值，因为类型可能不匹配
  if (inputType === 'MULTI_SELECT') {
    emit('default-value-change', props.paramType, props.index, [])
  } else {
    emit('default-value-change', props.paramType, props.index, '')
  }
  
  emit('input-type-change', props.paramType, props.index)
}

const handleDataTypeChange = () => {
  // 当数据类型改变时，清空默认值
  if (props.param.dataType === 'ARRAY') {
    emit('default-value-change', props.paramType, props.index, [])
  } else {
    emit('default-value-change', props.paramType, props.index, '')
  }
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
      return DATA_TYPES.filter(type => ['STRING', 'JSON', 'JSON_OBJECT'].includes(type.value))
    case 'CODE':
      // 代码编辑器显示编程语言类型
      return CODE_EDITOR_LANGUAGES
    case 'PASSWORD':
    case 'EMAIL':
    case 'URL':
    case 'TEXT':
    case 'SELECT':
      return DATA_TYPES.filter(type => type.value === 'STRING')
    case 'MULTI_SELECT':
      return DATA_TYPES.filter(type => type.value === 'ARRAY')
    default:
      return DATA_TYPES
  }
}

const getDefaultValueComponent = () => {
  const inputType = props.param.inputType
  const dataType = props.param.dataType
  
  // 根据输入类型选择组件
  switch (inputType) {
    case 'SELECT':
      return 'a-select'
    case 'MULTI_SELECT':
      return 'a-select'
    case 'TEXTAREA':
      return 'a-textarea'
    case 'CODE':
      return 'CodeEditor'
    case 'NUMBER':
      return 'a-input-number'
    case 'DATE':
      return 'a-date-picker'
    case 'DATETIME':
      return 'a-date-picker'
    default:
      // 根据数据类型选择组件
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
}

const getDefaultValuePlaceholder = () => {
  const inputType = props.param.inputType
  const dataType = props.param.dataType
  
  // 根据输入类型设置占位符
  switch (inputType) {
    case 'SELECT':
      return '请选择默认值（可选）'
    case 'MULTI_SELECT':
      return '请选择默认值（可选）'
    case 'TEXTAREA':
      return '请输入多行文本'
    case 'CODE':
      return '请输入代码'
    case 'NUMBER':
      return '请输入数字'
    case 'DATE':
      return '请选择日期'
    case 'DATETIME':
      return '请选择日期时间'
    default:
      // 根据数据类型设置占位符
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
}

const getDefaultValueClass = () => {
  const inputType = props.param.inputType
  const dataType = props.param.dataType
  
  // 根据输入类型选择样式类
  switch (inputType) {
    case 'SELECT':
    case 'MULTI_SELECT':
      return 'modern-select'
    case 'TEXTAREA':
      return 'modern-textarea'
    case 'NUMBER':
      return 'modern-input-number'
    case 'DATE':
    case 'DATETIME':
      return 'modern-date-picker'
    default:
      // 根据数据类型选择样式类
      switch (dataType) {
        case 'INTEGER':
        case 'LONG':
        case 'DOUBLE':
          return 'modern-input-number'
        case 'BOOLEAN':
          return 'modern-select'
        case 'DATE':
        case 'DATETIME':
          return 'modern-date-picker'
        default:
          return 'modern-input'
      }
  }
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
  
  // SELECT类型使用下拉框选项
  if (inputType === 'SELECT') {
    return {
      options: getSelectOptions(),
      allowClear: true,
      placeholder: '请选择默认值（可选）'
    }
  }
  
  // MULTI_SELECT类型使用多选下拉框选项
  if (inputType === 'MULTI_SELECT') {
    return {
      options: getSelectOptions(),
      mode: 'multiple',
      allowClear: !!(props.param.defaultValue && Array.isArray(props.param.defaultValue) && props.param.defaultValue.length > 0),
      placeholder: '请选择默认值（可选）'
    }
  }
  
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

const handleOptionValueChange = () => {
  // 触发响应式更新，让错误状态实时更新
  // 这里不需要额外处理，v-model已经处理了数据更新
}

const handleParamNameChange = () => {
  // 通知父组件参数名发生变化
  emit('param-name-change', props.paramType, props.index, props.param.name)
}

// 获取下拉选项
const getSelectOptions = () => {
  if ((props.param.inputType === 'SELECT' || props.param.inputType === 'MULTI_SELECT') && props.param.options) {
    return props.param.options.map(option => ({
      label: option.label || option.value,
      value: option.value
    }))
  }
  return []
}

// 代码编辑器相关方法
const getCodePreview = (value: string | any[] | undefined): string => {
  if (!value) {
    return ''
  }
  
  // 如果是数组，转换为JSON字符串
  let stringValue: string
  if (Array.isArray(value)) {
    stringValue = JSON.stringify(value)
  } else {
    stringValue = String(value)
  }
  
  if (stringValue.trim() === '') {
    return ''
  }
  
  // 如果内容太长，显示前50个字符
  if (stringValue.length > 50) {
    return stringValue.substring(0, 50) + '...'
  }
  
  return stringValue
}

const getCodeLanguage = (): string => {
  // 根据数据类型（编程语言类型）返回对应的代码编辑器语言
  switch (props.param.dataType) {
    case 'TEXT':
      return 'text'
    case 'JSON':
    case 'JSON_OBJECT':
      return 'json'
    case 'XML':
      return 'xml'
    case 'HTML':
      return 'html'
    case 'CSS':
      return 'css'
    case 'JAVASCRIPT':
      return 'javascript'
    case 'TYPESCRIPT':
      return 'typescript'
    case 'JAVA':
      return 'java'
    case 'KOTLIN':
      return 'kotlin'
    case 'SQL':
      return 'sql'
    case 'YAML':
      return 'yaml'
    default:
      return 'json' // 默认使用JSON
  }
}

// 语言类型到数据类型的反向映射
const getLanguageToDataTypeMapping = (): Record<string, string> => {
  return {
    'text': 'TEXT',
    'json': 'JSON',
    'xml': 'XML',
    'html': 'HTML',
    'css': 'CSS',
    'javascript': 'JAVASCRIPT',
    'typescript': 'TYPESCRIPT',
    'java': 'JAVA',
    'kotlin': 'KOTLIN',
    'sql': 'SQL',
    'yaml': 'YAML'
  }
}

const openCodeEditor = () => {
  // 处理不同类型的默认值
  let defaultValue = props.param.defaultValue || ''
  if (Array.isArray(defaultValue)) {
    defaultValue = JSON.stringify(defaultValue, null, 2)
  } else {
    defaultValue = String(defaultValue)
  }
  
  tempCodeValue.value = defaultValue
  codeEditorVisible.value = true
}

const handleCodeConfirm = (value: string) => {
  emit('default-value-change', props.paramType, props.index, value)
  codeEditorVisible.value = false
}

const handleCodeLanguageChange = (language: string) => {
  // 当代码编辑器中的语言变化时，同步更新数据类型
  const languageMapping = getLanguageToDataTypeMapping()
  const newDataType = languageMapping[language]
  
  if (newDataType && newDataType !== props.param.dataType) {
    emit('data-type-change', props.paramType, props.index, newDataType)
  }
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
  margin-bottom: 4px;
  padding: 4px;
  background: transparent;
  border-radius: 0;
  border: none;
  box-shadow: none;
  transition: none;
}

.form-section:hover {
  box-shadow: none;
  border-color: transparent;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 600;
  color: #24292f;
  margin-bottom: 4px;
  padding-bottom: 2px;
  border-bottom: 1px solid #e1e4e8;
}

.title-icon {
  margin-right: 3px;
  font-size: 11px;
}

.form-item-modern {
  margin-bottom: 4px;
}

.form-item-modern :deep(.ant-form-item-label) {
  font-weight: 500;
  color: #24292f;
  font-size: 12px;
}

.form-item-modern :deep(.ant-form-item-label > label) {
  font-size: 12px;
}

.form-item-modern :deep(.ant-input) {
  font-size: 12px;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  transition: all 0.2s ease;
  padding: 6px 11px;
  height: 32px;
}

.form-item-modern :deep(.ant-input:hover) {
  border-color: #40a9ff;
}

.form-item-modern :deep(.ant-input:focus) {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-item-modern :deep(.ant-select-selector) {
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: border-color 0.2s ease;
  padding: 4px 8px;
  height: 32px;
  background-color: #fafafa;
}

.form-item-modern :deep(.ant-select-selector:hover) {
  border-color: #40a9ff;
}

.form-item-modern :deep(.ant-select-focused .ant-select-selector) {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

.form-item-modern :deep(.ant-select-selection-item) {
  font-size: 12px;
  line-height: 24px;
}

/* 多选下拉框样式调整 */
.form-item-modern :deep(.ant-select-multiple .ant-select-selector) {
  min-height: 32px;
  height: auto;
  padding: 2px 11px;
}

.form-item-modern :deep(.ant-select-multiple .ant-select-selection-overflow) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.form-item-modern :deep(.ant-select-multiple .ant-select-selection-item) {
  margin: 2px 4px 2px 0;
  height: 24px;
  line-height: 22px;
  font-size: 12px;
}

.form-item-modern :deep(.ant-checkbox-wrapper) {
  font-size: 12px;
}

.form-item-modern :deep(.ant-checkbox-wrapper span) {
  font-size: 12px;
}

.modern-input,
.modern-select,
.modern-textarea {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: border-color 0.2s ease;
  background-color: #fafafa;
  font-size: 12px;
}

.modern-input:focus,
.modern-select:focus,
.modern-textarea:focus {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

.modern-input:hover,
.modern-select:hover,
.modern-textarea:hover {
  border-color: #40a9ff;
}

/* 普通输入框样式 */
.modern-input {
  height: 32px;
  padding: 4px 8px;
}

.modern-input :deep(.ant-input) {
  height: 32px;
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #666;
}

.modern-input :deep(.ant-input):hover {
  border-color: #40a9ff;
}

.modern-input :deep(.ant-input):focus {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

/* 专门针对下拉框的样式 */
.modern-select :deep(.ant-select-selector) {
  height: 32px !important;
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  display: flex;
  align-items: center;
}

.modern-select :deep(.ant-select-selector:hover) {
  border-color: #40a9ff;
}

.modern-select :deep(.ant-select-focused .ant-select-selector) {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

.modern-select :deep(.ant-select-selection-item) {
  font-size: 12px;
  line-height: 24px;
  display: flex;
  align-items: center;
  height: 100%;
}

/* 数字输入框样式 - 与主题保持一致 */
.modern-input-number {
  width: 100%;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: border-color 0.2s ease;
  font-size: 12px;
  height: 32px;
  background-color: #fafafa;
  display: flex;
  align-items: center;
}

.modern-input-number:hover {
  border-color: #40a9ff;
}

.modern-input-number:focus {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

/* 数字输入框内部样式 */
.form-item-modern :deep(.modern-input-number .ant-input-number) {
  width: 100%;
  border: none;
  background: transparent;
  box-shadow: none;
  height: 32px;
  display: flex;
  align-items: center;
}

.form-item-modern :deep(.modern-input-number .ant-input-number .ant-input-number-input) {
  height: 32px !important;
  font-size: 12px;
  border: none;
  background: transparent;
  padding: 0 8px !important;
  color: #666;
  line-height: 32px !important;
  box-sizing: border-box;
  display: flex !important;
  align-items: center !important;
  justify-content: flex-start !important;
}

.form-item-modern :deep(.modern-input-number .ant-input-number .ant-input-number-input::placeholder) {
  color: #999;
}

.form-item-modern :deep(.modern-input-number .ant-input-number:hover) {
  border: none;
  box-shadow: none;
}

.form-item-modern :deep(.modern-input-number .ant-input-number-focused) {
  border: none;
  box-shadow: none;
}

.form-item-modern :deep(.modern-input-number .ant-input-number-disabled) {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: #999;
}

.form-item-modern :deep(.modern-input-number .ant-input-number-disabled .ant-input-number-input) {
  color: #999;
  background: transparent;
}

/* 强制数字输入框文字居中 */
.modern-input-number :deep(.ant-input-number-input-wrap) {
  height: 32px !important;
  display: flex !important;
  align-items: center !important;
}

.modern-input-number :deep(.ant-input-number-input) {
  height: 32px !important;
  line-height: 32px !important;
  padding: 0 8px !important;
  display: flex !important;
  align-items: center !important;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

/* 日期选择器样式 */
.modern-date-picker {
  width: 100%;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: border-color 0.2s ease;
  font-size: 12px;
  height: 32px;
  background-color: #fafafa;
}

.modern-date-picker:hover {
  border-color: #40a9ff;
}

.modern-date-picker:focus {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

.modern-date-picker :deep(.ant-picker) {
  width: 100%;
  height: 32px;
  border: none;
  background: transparent;
  box-shadow: none;
  padding: 4px 8px;
}

.modern-date-picker :deep(.ant-picker-input) {
  height: 24px;
  font-size: 12px;
}

.modern-date-picker :deep(.ant-picker-input input) {
  font-size: 12px;
  color: #666;
  background: transparent;
  border: none;
  padding: 0;
}

.modern-date-picker :deep(.ant-picker-input input::placeholder) {
  color: #999;
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
  font-size: 12px;
}

.checkbox-icon {
  margin-right: 6px;
  font-size: 11px;
}

.options-container {
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 8px;
  background: #fafafa;
  margin-top: 4px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  padding: 6px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.option-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1);
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

.option-inputs {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  margin-right: 12px;
}

.option-value-input,
.option-label-input {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-size: 12px;
  padding: 6px 11px;
  height: 32px;
  background: #ffffff;
}

.option-value-input:focus,
.option-label-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  outline: none;
}

.option-value-input:hover,
.option-label-input:hover {
  border-color: #40a9ff;
}

.option-value-input.error {
  border-color: #ff4d4f;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.1);
}

.option-input {
  flex: 1;
  margin-right: 12px;
  border: none;
  background: transparent;
  box-shadow: none;
  font-size: 12px;
}

.option-input:focus {
  box-shadow: none;
}

.option-remove-btn {
  flex-shrink: 0;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: 40px;
  height: 40px;
  padding: 0;
  min-width: 40px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
  border: none;
  color: white;
  box-shadow: 0 3px 6px rgba(255, 82, 82, 0.3), 0 1px 3px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.option-remove-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
  border-radius: 8px;
}

.option-remove-btn:hover {
  transform: translateY(-2px) scale(1.05);
  background: linear-gradient(135deg, #ff5252 0%, #f44336 100%);
  box-shadow: 0 5px 10px rgba(255, 82, 82, 0.4), 0 2px 5px rgba(0, 0, 0, 0.15);
}

.option-remove-btn:hover::before {
  opacity: 1;
}

.option-remove-btn:active {
  transform: translateY(-1px) scale(1.02);
  box-shadow: 0 3px 6px rgba(255, 82, 82, 0.3), 0 1px 3px rgba(0, 0, 0, 0.1);
}

.option-remove-btn :deep(.anticon) {
  font-size: 16px;
  font-weight: bold;
  color: white;
  z-index: 2;
  position: relative;
}

.add-option-btn {
  width: 100%;
  border-radius: 6px;
  border: 1px dashed #d0d7de;
  background: transparent;
  color: #656d76;
  transition: all 0.2s ease;
  margin-top: 8px;
  font-size: 12px !important;
  padding: 8px 12px !important;
  height: 36px !important;
  font-weight: 500 !important;
}

.add-option-btn:hover {
  border-color: #0969da;
  color: #0969da;
  background: rgba(9, 105, 218, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 2px 4px rgba(9, 105, 218, 0.1);
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

.code-editor-input :deep(.ant-input) {
  font-family: Intel One Mono, SF Mono, Monaco, Menlo, monospace;
}

.code-preview-input {
  cursor: pointer;
}

.code-preview-input :deep(.ant-input) {
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  color: #666;
  font-family: Intel One Mono, SF Mono, Monaco, Menlo, monospace;
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease;
  height: 32px;
  padding: 4px 8px;
  margin-top: 4px;
  margin-bottom: 4px;
}

.code-preview-input :deep(.ant-input):hover {
  border-color: #40a9ff;
}

.code-preview-input :deep(.ant-input):focus {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

.code-preview-input :deep(.ant-input-suffix) {
  padding-right: 4px;
}

.code-preview-input :deep(.ant-input-suffix .ant-btn) {
  border-radius: 3px;
  font-size: 11px;
  height: 24px;
  padding: 0 8px;
  line-height: 22px;
}
</style>
