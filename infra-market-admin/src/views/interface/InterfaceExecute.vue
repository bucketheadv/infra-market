<template>
  <a-modal
    v-model:visible="visible"
    title="接口执行"
    width="1000px"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="execute-container">
      <!-- 接口信息 -->
      <div class="interface-info">
        <h3>接口信息</h3>
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="接口名称">
            {{ interfaceData?.name }}
          </a-descriptions-item>
          <a-descriptions-item label="请求方法">
            <a-tag :color="getMethodColor(interfaceData?.method || '')">
              {{ interfaceData?.method }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="接口URL" :span="2">
            {{ interfaceData?.url }}
          </a-descriptions-item>
          <a-descriptions-item label="接口描述" :span="2">
            {{ interfaceData?.description || '无' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 参数配置 -->
      <div class="params-section">
        <h3>参数配置</h3>
        <a-form
          ref="formRef"
          :model="executeForm"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
        >
          <div v-if="urlParams.length > 0" class="param-group">
            <h4>URL参数</h4>
            <a-row v-for="param in urlParams" :key="param.name" :gutter="16">
              <a-col :span="8">
                <label class="param-label">
                  {{ param.name }}
                  <span v-if="param.required" class="required">*</span>
                </label>
              </a-col>
              <a-col :span="16">
                <a-form-item
                  :name="['params', param.name]"
                  :rules="param.required ? [{ required: true, message: `请输入${param.name}` }] : []"
                >
                  <!-- 代码编辑器弹窗按钮 -->
                  <div v-if="param.inputType === 'CODE'" class="code-editor-input">
                    <a-input
                      :value="getCodePreview(executeForm.params[param.name])"
                      :placeholder="`请输入${param.name}`"
                      :disabled="!param.changeable"
                      readonly
                      class="code-preview-input"
                      @click="!param.changeable || openCodeEditor(param, 'params')"
                    >
                      <template #suffix>
                        <ThemeButton
                          v-if="param.changeable"
                          variant="secondary"
                          size="small"
                          @click.stop="openCodeEditor(param, 'params')"
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
                    :is="getInputComponent(param)"
                    v-bind="getInputBindings(param, 'params')"
                    :placeholder="`请输入${param.name}`"
                    :options="param.options"
                    :disabled="!param.changeable"
                    :required="param.required"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </div>

          <div v-if="headerParams.length > 0" class="param-group">
            <h4>Header参数</h4>
            <a-row v-for="param in headerParams" :key="param.name" :gutter="16">
              <a-col :span="8">
                <label class="param-label">
                  {{ param.name }}
                  <span v-if="param.required" class="required">*</span>
                </label>
              </a-col>
              <a-col :span="16">
                <a-form-item
                  :name="['headers', param.name]"
                  :rules="param.required ? [{ required: true, message: `请输入${param.name}` }] : []"
                >
                  <!-- 代码编辑器弹窗按钮 -->
                  <div v-if="param.inputType === 'CODE'" class="code-editor-input">
                    <a-input
                      :value="getCodePreview(executeForm.headers[param.name])"
                      :placeholder="`请输入${param.name}`"
                      :disabled="!param.changeable"
                      readonly
                      class="code-preview-input"
                      @click="!param.changeable || openCodeEditor(param, 'headers')"
                    >
                      <template #suffix>
                        <ThemeButton
                          v-if="param.changeable"
                          variant="secondary"
                          size="small"
                          @click.stop="openCodeEditor(param, 'headers')"
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
                    :is="getInputComponent(param)"
                    v-bind="getInputBindings(param, 'headers')"
                    :placeholder="`请输入${param.name}`"
                    :options="param.options"
                    :disabled="!param.changeable"
                    :required="param.required"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </div>

          <div v-if="bodyParams.length > 0 && interfaceData?.method !== 'GET'" class="param-group">
            <h4>Body参数</h4>
            <a-row v-for="param in bodyParams" :key="param.name" :gutter="16">
              <a-col :span="8">
                <label class="param-label">
                  {{ param.name }}
                  <span v-if="param.required" class="required">*</span>
                </label>
              </a-col>
              <a-col :span="16">
                <a-form-item
                  :name="['bodyParams', param.name]"
                  :rules="param.required ? [{ required: true, message: `请输入${param.name}` }] : []"
                >
                  <!-- 代码编辑器弹窗按钮 -->
                  <div v-if="param.inputType === 'CODE'" class="code-editor-input">
                    <a-input
                      :value="getCodePreview(executeForm.bodyParams[param.name])"
                      :placeholder="`请输入${param.name}`"
                      :disabled="!param.changeable"
                      readonly
                      class="code-preview-input"
                      @click="!param.changeable || openCodeEditor(param, 'bodyParams')"
                    >
                      <template #suffix>
                        <ThemeButton
                          v-if="param.changeable"
                          variant="secondary"
                          size="small"
                          @click.stop="openCodeEditor(param, 'bodyParams')"
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
                    :is="getInputComponent(param)"
                    v-bind="getInputBindings(param, 'bodyParams')"
                    :placeholder="`请输入${param.name}`"
                    :options="param.options"
                    :disabled="!param.changeable"
                    :required="param.required"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </div>

          <div v-if="urlParams.length === 0 && headerParams.length === 0 && bodyParams.length === 0" class="no-params">
            <a-empty description="该接口无需配置参数" />
          </div>
        </a-form>
      </div>

      <!-- 执行按钮 -->
      <div class="execute-actions">
        <ThemeButton
          variant="primary"
          size="large"
          :loading="executing"
          :icon="PlayCircleOutlined"
          @click="handleExecute"
        >
          执行接口
        </ThemeButton>
      </div>

      <!-- 执行结果 -->
      <div v-if="executeResult" class="result-section">
        <h3>执行结果</h3>
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="response" tab="响应内容">
            <div class="response-container">
              <div class="response-header">
                <a-tag :color="executeResult.success ? 'green' : 'red'">
                  {{ executeResult.status }}
                </a-tag>
                <span class="response-time">
                  响应时间: {{ executeResult.responseTime }}ms
                </span>
              </div>
              <div class="response-body">
                <CodeEditor
                  :model-value="formatResponseBody(executeResult.body)"
                  :readonly="true"
                  :height="250"
                  :language="detectResponseLanguage(executeResult.body)"
                  :options="{
                    minimap: { enabled: true },
                    scrollBeyondLastLine: false,
                    wordWrap: 'on',
                    lineNumbers: 'on',
                    folding: true,
                    fontSize: 10,
                    fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                    lineHeight: 16,
                    readOnly: true
                  }"
                />
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane key="headers" tab="响应头">
            <div class="headers-container">
              <pre>{{ formatHeaders(executeResult.headers) }}</pre>
            </div>
          </a-tab-pane>
          <a-tab-pane v-if="executeResult.error" key="error" tab="错误信息">
            <div class="error-container">
              <pre>{{ executeResult.error }}</pre>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
    
    <!-- 代码编辑器弹窗 -->
    <CodeEditorModal
      v-model:visible="codeEditorVisible"
      v-model:value="tempCodeValue"
      :language="getCodeLanguage()"
      :placeholder="getCodePlaceholder()"
      @confirm="handleCodeConfirm"
      @cancel="handleCodeCancel"
    />
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlayCircleOutlined } from '@ant-design/icons-vue'
import { interfaceApi, type ApiInterface, type ApiParam, type ApiExecuteRequest, type ApiExecuteResponse } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import CodeEditor from '@/components/CodeEditor.vue'
import CodeEditorModal from '@/components/CodeEditorModal.vue'

// Props
interface Props {
  visible: boolean
  interfaceData: ApiInterface | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

// 响应式数据
const formRef = ref()
const visible = ref(props.visible)
const executing = ref(false)
const executeResult = ref<ApiExecuteResponse | null>(null)
const activeTab = ref('response')

// 代码编辑器弹窗相关
const codeEditorVisible = ref(false)
const tempCodeValue = ref('')
const currentCodeParam = ref<{ param: ApiParam, type: 'params' | 'headers' | 'bodyParams' } | null>(null)

const executeForm = reactive({
  params: {} as Record<string, any>,
  headers: {} as Record<string, any>,
  bodyParams: {} as Record<string, any>
})

// 计算属性
const urlParams = computed(() => {
  return props.interfaceData?.urlParams || []
})

const headerParams = computed(() => {
  return props.interfaceData?.headerParams || []
})

const bodyParams = computed(() => {
  return props.interfaceData?.bodyParams || []
})

// 监听visible变化
watch(() => props.visible, (newVal) => {
  visible.value = newVal
  if (newVal) {
    initForm()
  }
})

watch(visible, (newVal) => {
  emit('update:visible', newVal)
})

// 方法
const initForm = () => {
  executeForm.params = {}
  executeForm.headers = {}
  executeForm.bodyParams = {}
  executeResult.value = null
  activeTab.value = 'response'

  // 设置默认值
  props.interfaceData?.urlParams?.forEach(param => {
    executeForm.params[param.name] = param.defaultValue || ''
  })
  
  props.interfaceData?.headerParams?.forEach(param => {
    executeForm.headers[param.name] = param.defaultValue || ''
  })
  
  props.interfaceData?.bodyParams?.forEach(param => {
    executeForm.bodyParams[param.name] = param.defaultValue || ''
  })
}

const getInputComponent = (param: ApiParam) => {
  switch (param.inputType) {
    case 'SELECT':
      return 'a-select'
    case 'TEXTAREA':
      return 'a-textarea'
    case 'CODE':
      return 'a-textarea'
    case 'NUMBER':
      return 'a-input-number'
    case 'DATE':
      return 'a-date-picker'
    case 'DATETIME':
      return 'a-date-picker'
    default:
      return 'a-input'
  }
}

const getDatePickerProps = (param: ApiParam) => {
  if (param.inputType === 'DATETIME') {
    return {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '请选择日期时间'
    }
  } else if (param.inputType === 'DATE') {
    return {
      showTime: false,
      format: 'YYYY-MM-DD',
      placeholder: '请选择日期'
    }
  }
  return {}
}

const getCodeEditorProps = (param: ApiParam) => {
  if (param.inputType === 'CODE') {
    return {
      language: 'json',
      height: 300,
      placeholder: `请输入${param.name}...`,
      options: {
        minimap: { enabled: true },
        scrollBeyondLastLine: false,
        wordWrap: 'on',
        lineNumbers: 'on',
        folding: true,
        fontSize: 18,
        fontFamily: 'Monaco, Menlo, "Ubuntu Mono", monospace',
        lineHeight: 28
      }
    }
  }
  return {}
}

const getInputBindings = (param: ApiParam, type: 'params' | 'headers' | 'bodyParams') => {
  const baseProps = {
    ...getDatePickerProps(param),
    ...getCodeEditorProps(param)
  }
  
  // 根据组件类型选择不同的 v-model 绑定方式
  if (param.inputType === 'CODE') {
    return {
      ...baseProps,
      modelValue: executeForm[type][param.name],
      'onUpdate:modelValue': (value: string) => {
        executeForm[type][param.name] = value
      }
    }
  } else {
    return {
      ...baseProps,
      value: executeForm[type][param.name],
      'onUpdate:value': (value: any) => {
        executeForm[type][param.name] = value
      }
    }
  }
}

const getMethodColor = (method: string) => {
  const colors: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple',
    HEAD: 'cyan',
    OPTIONS: 'magenta'
  }
  return colors[method] || 'default'
}


const handleExecute = async () => {
  if (!props.interfaceData) return

  // 使用 Ant Design 表单验证
  try {
    await formRef.value.validate()
  } catch (error) {
    message.error('请填写必填参数')
    return
  }

  executing.value = true
  try {
    // 构建请求URL
    let url = props.interfaceData.url
    const urlParams = Object.entries(executeForm.params).filter(([_, value]) => value !== '')
    if (urlParams.length > 0) {
      const paramString = urlParams.map(([key, value]) => `${key}=${encodeURIComponent(value)}`).join('&')
      url += (url.includes('?') ? '&' : '?') + paramString
    }

    // 构建请求体
    let body: string | undefined
    if (props.interfaceData.method !== 'GET' && Object.keys(executeForm.bodyParams).length > 0) {
      const bodyData = Object.fromEntries(
        Object.entries(executeForm.bodyParams).filter(([_, value]) => value !== '')
      )
      
      // 根据POST类型构造不同的body
      if (props.interfaceData.postType === 'application/x-www-form-urlencoded') {
        // 表单格式
        body = Object.entries(bodyData)
          .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
          .join('&')
      } else {
        // JSON格式（默认）
        body = JSON.stringify(bodyData)
      }
    }

    const request: ApiExecuteRequest = {
      interfaceId: props.interfaceData.id,
      url,
      method: props.interfaceData.method,
      postType: props.interfaceData.postType,
      headers: Object.fromEntries(
        Object.entries(executeForm.headers).filter(([_, value]) => value !== '')
      ),
      urlParams: Object.fromEntries(
        Object.entries(executeForm.params).filter(([_, value]) => value !== '')
      ),
      bodyParams: Object.fromEntries(
        Object.entries(executeForm.bodyParams).filter(([_, value]) => value !== '')
      ),
      body
    }

    const response = await interfaceApi.execute(request)
    executeResult.value = response.data
    activeTab.value = 'response'
  } catch (error) {
    message.error('执行失败')
  } finally {
    executing.value = false
  }
}

const handleCancel = () => {
  visible.value = false
}

const formatResponseBody = (body: string) => {
  try {
    const parsed = JSON.parse(body)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return body
  }
}

const detectResponseLanguage = (body: string): string => {
  if (!body || body.trim() === '') {
    return 'text'
  }

  const trimmedBody = body.trim()
  
  // JSON检测
  if ((trimmedBody.startsWith('{') && trimmedBody.endsWith('}')) ||
      (trimmedBody.startsWith('[') && trimmedBody.endsWith(']'))) {
    try {
      JSON.parse(trimmedBody)
      return 'json'
    } catch {
      return 'json' // 可能是格式错误的JSON，但仍然使用json语言
    }
  }
  
  // XML检测
  if (trimmedBody.startsWith('<') && trimmedBody.includes('>')) {
    return 'xml'
  }
  
  // HTML检测
  if (trimmedBody.includes('<html') || trimmedBody.includes('<!DOCTYPE')) {
    return 'html'
  }
  
  // CSS检测
  if (trimmedBody.includes('{') && trimmedBody.includes('}') && 
      (trimmedBody.includes(':') || trimmedBody.includes(';'))) {
    return 'css'
  }
  
  // JavaScript检测
  if (trimmedBody.includes('function') || trimmedBody.includes('=>') || 
      trimmedBody.includes('const ') || trimmedBody.includes('let ') || 
      trimmedBody.includes('var ')) {
    return 'javascript'
  }
  
  // SQL检测
  if (trimmedBody.match(/^\s*(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP)\s+/i)) {
    return 'sql'
  }
  
  // YAML检测
  if (trimmedBody.includes(':') && (trimmedBody.includes('-') || trimmedBody.includes('|'))) {
    return 'yaml'
  }
  
  // 默认返回文本
  return 'text'
}


const formatHeaders = (headers: Record<string, string>) => {
  return Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n')
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
  if (currentCodeParam.value?.param.dataType === 'JSON') {
    return 'json'
  }
  return 'json' // 默认使用JSON
}

const getCodePlaceholder = (): string => {
  if (currentCodeParam.value) {
    return `请输入${currentCodeParam.value.param.name}...`
  }
  return '请输入代码...'
}

const openCodeEditor = (param: ApiParam, type: 'params' | 'headers' | 'bodyParams') => {
  currentCodeParam.value = { param, type }
  tempCodeValue.value = executeForm[type][param.name] || ''
  codeEditorVisible.value = true
}

const handleCodeConfirm = (value: string) => {
  if (currentCodeParam.value) {
    executeForm[currentCodeParam.value.type][currentCodeParam.value.param.name] = value
  }
  codeEditorVisible.value = false
  currentCodeParam.value = null
}

const handleCodeCancel = () => {
  codeEditorVisible.value = false
  currentCodeParam.value = null
}
</script>

<style scoped>
.execute-container {
  max-height: 70vh;
  overflow-y: auto;
}

.interface-info,
.params-section,
.result-section {
  margin-bottom: 24px;
}

.interface-info h3,
.params-section h3,
.result-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.param-group {
  margin-bottom: 24px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  background: #fafafa;
}

.param-group h4 {
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #1890ff;
}

.param-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.required {
  color: #ff4d4f;
}

.no-params {
  text-align: center;
  padding: 40px 0;
}

.execute-actions {
  text-align: center;
  margin: 24px 0;
}

.response-container {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.response-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #d9d9d9;
}

.response-time {
  font-size: 12px;
  color: #666;
}

.response-body {
  padding: 0;
}

.headers-container,
.error-container {
  padding: 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
  max-height: 300px;
  overflow-y: auto;
}

.headers-container pre,
.error-container pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
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
  border-color: #40a9ff;
  background-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(9, 105, 218, 0.1);
}

.code-preview-input :deep(.ant-input):focus {
  border-color: #0969da;
  background-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(9, 105, 218, 0.2);
}
</style>
