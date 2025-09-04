<template>
  <div class="interface-execute-page">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <PlayCircleOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">接口执行</div>
          <div class="header-subtitle">{{ interfaceData?.name || '接口测试' }}</div>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <!-- 基本信息区域 -->
        <div class="form-section">
          <div class="section-header">
            <div class="section-icon">
              <span>📋</span>
            </div>
            <div class="section-title">接口基本信息</div>
          </div>
          
          <div v-if="loading" class="loading-container">
            <a-spin size="large" />
          </div>
          <div v-else-if="interfaceData" class="interface-info">
            <a-descriptions :column="2" :bordered="false" size="small">
              <a-descriptions-item label="接口名称">
                <span class="info-value">{{ interfaceData.name }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="请求方法">
                <a-tag :color="getMethodColor(interfaceData.method)" class="method-tag">
                  {{ interfaceData.method }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="请求URL" :span="2">
                <code class="url-text">{{ interfaceData.url }}</code>
              </a-descriptions-item>
              <a-descriptions-item label="接口描述" :span="2">
                <span class="description-text">{{ interfaceData.description || '暂无描述' }}</span>
              </a-descriptions-item>
              <a-descriptions-item v-if="interfaceData.postType" label="POST类型">
                <a-tag color="blue" class="post-type-tag">{{ getPostTypeLabel(interfaceData.postType) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="interfaceData.status === 1 ? 'green' : 'red'" class="status-tag">
                  {{ interfaceData.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                <span class="time-text">{{ formatDateTime(interfaceData.createTime) }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                <span class="time-text">{{ formatDateTime(interfaceData.updateTime) }}</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </div>

        <a-row :gutter="24" class="content-row">
        <!-- 左侧：参数配置 -->
        <a-col :span="12">
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <span>⚙️</span>
              </div>
              <div class="section-title">参数配置</div>
            </div>
            <div v-if="loading" class="loading-container">
              <a-spin size="large" />
            </div>
            <div v-else>
              <a-form ref="formRef" :model="executeForm" layout="vertical">
                <!-- URL参数 -->
                <div v-if="urlParams.length > 0" class="param-group">
                  <h4>URL参数</h4>
                  <a-row v-for="param in urlParams" :key="param.name" class="param-row">
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

                <!-- Header参数 -->
                <div v-if="headerParams.length > 0" class="param-group">
                  <h4>Header参数</h4>
                  <a-row v-for="param in headerParams" :key="param.name" class="param-row">
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

                <!-- Body参数 -->
                <div v-if="bodyParams.length > 0 && interfaceData?.method !== 'GET'" class="param-group">
                  <h4>Body参数</h4>
                  <a-row v-for="param in bodyParams" :key="param.name" class="param-row">
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
          </div>
        </a-col>

        <!-- 右侧：执行结果 -->
        <a-col :span="12">
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <span>📊</span>
              </div>
              <div class="section-title">执行结果</div>
            </div>
            <div v-if="!executeResult" class="no-result">
              <a-empty description="点击执行按钮开始测试接口" />
            </div>
            <div v-else>
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
                        :height="400"
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
        </a-col>
        </a-row>

        <!-- 操作按钮区域 -->
        <div class="form-actions">
          <a-space size="small">
            <ThemeButton 
              variant="primary" 
              size="small"
              :icon="PlayCircleOutlined"
              :disabled="executing"
              @click="handleExecute"
              class="submit-btn"
            >
              执行接口
            </ThemeButton>
            <ThemeButton 
              variant="secondary"
              size="small"
              :icon="CloseOutlined"
              @click="handleBack"
              class="cancel-btn"
            >
              返回
            </ThemeButton>
          </a-space>
        </div>
      </a-card>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlayCircleOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { interfaceApi, POST_TYPES, type ApiInterface, type ApiParam, type ApiExecuteRequest, type ApiExecuteResponse } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import CodeEditor from '@/components/CodeEditor.vue'
import CodeEditorModal from '@/components/CodeEditorModal.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const formRef = ref()
const loading = ref(false)
const executing = ref(false)
const interfaceData = ref<ApiInterface | null>(null)
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

// 获取接口ID
const interfaceId = route.params.id as string

// 计算属性
const urlParams = computed(() => interfaceData.value?.urlParams || [])
const headerParams = computed(() => interfaceData.value?.headerParams || [])
const bodyParams = computed(() => interfaceData.value?.bodyParams || [])

// 初始化数据
onMounted(async () => {
  if (interfaceId) {
    await loadInterfaceData()
  } else {
    message.error('接口ID不存在')
    router.back()
  }
})

// 加载接口数据
const loadInterfaceData = async () => {
  try {
    loading.value = true
    const response = await interfaceApi.getById(Number(interfaceId))
    interfaceData.value = response.data
    
    // 初始化表单数据
    initializeFormData()
  } catch (error) {
    console.error('加载接口数据失败:', error)
    message.error('加载接口数据失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 初始化表单数据
const initializeFormData = () => {
  if (!interfaceData.value) return
  
  // 初始化URL参数
  interfaceData.value.urlParams?.forEach(param => {
    if (param.defaultValue !== undefined) {
      executeForm.params[param.name] = param.defaultValue
    }
  })
  
  // 初始化Header参数
  interfaceData.value.headerParams?.forEach(param => {
    if (param.defaultValue !== undefined) {
      executeForm.headers[param.name] = param.defaultValue
    }
  })
  
  // 初始化Body参数
  interfaceData.value.bodyParams?.forEach(param => {
    if (param.defaultValue !== undefined) {
      executeForm.bodyParams[param.name] = param.defaultValue
    }
  })
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 执行接口
const handleExecute = async () => {
  if (!interfaceData.value) return
  
  try {
    executing.value = true
    executeResult.value = null
    
    const request: ApiExecuteRequest = {
      interfaceId: interfaceData.value.id!,
      url: interfaceData.value.url!,
      method: interfaceData.value.method!,
      postType: interfaceData.value.postType,
      headers: executeForm.headers,
      urlParams: executeForm.params,
      bodyParams: executeForm.bodyParams
    }
    
    const response = await interfaceApi.execute(request)
    executeResult.value = response.data
    activeTab.value = 'response'
    
    message.success('接口执行成功')
  } catch (error: any) {
    console.error('接口执行失败:', error)
    message.error(error.response?.data?.message || '接口执行失败')
  } finally {
    executing.value = false
  }
}

// 获取输入组件
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

// 获取输入组件绑定属性
const getInputBindings = (param: ApiParam, type: 'params' | 'headers' | 'bodyParams') => {
  const baseProps = {
    ...getDatePickerProps(param),
    ...getCodeEditorProps(param)
  }
  
  // 根据组件类型选择不同的 v-model 绑定方式
  if (param.inputType === 'CODE') {
    return {
      ...baseProps,
      modelValue: executeForm[type][param.name] || '',
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

// 获取日期选择器属性
const getDatePickerProps = (param: ApiParam) => {
  if (param.inputType === 'DATE') {
    return {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD'
    }
  } else if (param.inputType === 'DATETIME') {
    return {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss'
    }
  }
  return {}
}

// 获取代码编辑器属性
const getCodeEditorProps = (param: ApiParam) => {
  if (param.inputType === 'CODE') {
    return {
      height: 200,
      options: {
        minimap: { enabled: true },
        scrollBeyondLastLine: false,
        wordWrap: 'on' as const,
        lineNumbers: 'on' as const,
        folding: true,
        fontSize: 16,
        fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
        lineHeight: 24,
        readOnly: false
      }
    }
  }
  return {}
}

// 格式化响应体
const formatResponseBody = (body: string) => {
  try {
    const parsed = JSON.parse(body)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return body
  }
}

// 格式化响应头
const formatHeaders = (headers: Record<string, string>) => {
  return Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n')
}

// 检测响应语言
const detectResponseLanguage = (body: string): string => {
  if (!body || body.trim() === '') {
    return 'text'
  }

  const trimmedBody = body.trim()
  
  // JSON检测
  if (trimmedBody.startsWith('{') && trimmedBody.endsWith('}') ||
      trimmedBody.startsWith('[') && trimmedBody.endsWith(']')) {
    try {
      JSON.parse(trimmedBody)
      return 'json'
    } catch {
      // 不是有效的JSON，继续检测其他格式
    }
  }
  
  // XML检测
  if (trimmedBody.startsWith('<') && trimmedBody.endsWith('>')) {
    return 'xml'
  }
  
  // HTML检测
  if (trimmedBody.includes('<html') || trimmedBody.includes('<div') || trimmedBody.includes('<p')) {
    return 'html'
  }
  
  // CSS检测
  if (trimmedBody.includes('{') && trimmedBody.includes('}') && trimmedBody.includes(':')) {
    return 'css'
  }
  
  // JavaScript检测
  if (trimmedBody.includes('function') || trimmedBody.includes('=>') || trimmedBody.includes('const ')) {
    return 'javascript'
  }
  
  // SQL检测
  if (trimmedBody.toUpperCase().includes('SELECT') || trimmedBody.toUpperCase().includes('INSERT') || 
      trimmedBody.toUpperCase().includes('UPDATE') || trimmedBody.toUpperCase().includes('DELETE')) {
    return 'sql'
  }
  
  // YAML检测
  if (trimmedBody.includes(':') && trimmedBody.includes('\n') && !trimmedBody.includes('{')) {
    return 'yaml'
  }
  
  // 默认返回文本
  return 'text'
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

// 获取请求方法颜色
const getMethodColor = (method: string) => {
  const colors: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple',
    HEAD: 'cyan',
    OPTIONS: 'geekblue'
  }
  return colors[method] || 'default'
}

// 获取POST类型标签
const getPostTypeLabel = (postType: string) => {
  const type = POST_TYPES.find(t => t.value === postType)
  return type ? type.label : postType
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date | undefined): string => {
  if (!dateTime) return '暂无'
  
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
.interface-execute-page {
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
  padding: 12px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.header-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary-color, #1890ff), var(--secondary-color, #40a9ff));
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 2px 6px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.header-icon :deep(.anticon) {
  font-size: 18px;
  color: white;
}

.header-text {
  flex: 1;
}

.header-title {
  margin: 0 0 2px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.2;
}

.header-subtitle {
  margin: 0;
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

/* 表单区域样式 */
.form-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.section-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: linear-gradient(135deg, var(--primary-color, #1890ff), var(--secondary-color, #40a9ff));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
  box-shadow: 0 1px 3px var(--shadow-color, rgba(24, 144, 255, 0.12));
}

.section-icon :deep(.anticon) {
  font-size: 10px;
  color: white;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  margin-right: 6px;
}

/* 操作按钮样式 */
.form-actions {
  padding: 12px 0 0 0;
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.submit-btn {
  border-radius: 4px;
  transition: all 0.2s ease;
  min-width: 100px;
  font-size: 13px;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.cancel-btn {
  border-radius: 4px;
  transition: all 0.2s ease;
  min-width: 80px;
  font-size: 13px;
}

.cancel-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.no-result {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.param-group {
  margin-bottom: 24px;
}

.param-group h4 {
  margin-bottom: 16px;
  color: #1890ff;
  font-weight: 600;
}

.param-row {
  margin-bottom: 16px;
}

.param-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #ff4d4f;
  margin-left: 4px;
}

.no-params {
  text-align: center;
  padding: 40px 0;
}

.response-container {
  height: 100%;
}

.response-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
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

/* 基本信息样式 */
.info-card {
  margin: 0 0 24px 0;
  margin-right: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.content-row {
  margin: 0;
  margin-right: 24px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1890ff;
}

.title-icon {
  font-size: 16px;
}

.interface-info {
  padding: 8px 0;
}

.info-value {
  font-weight: 500;
  color: #333;
}

.url-text {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  padding: 6px 12px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #1890ff;
  word-break: break-all;
  border: 1px solid #d6e4ff;
}

.description-text {
  color: #666;
  line-height: 1.5;
}

.method-tag,
.post-type-tag,
.status-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 2px 8px;
}

.time-text {
  color: #999;
  font-size: 13px;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: #666;
  background: #fafafa;
  padding: 8px 12px;
  border-radius: 6px;
  margin-right: 12px;
  min-width: 80px;
}

:deep(.ant-descriptions-item-content) {
  color: #333;
  padding: 8px 0;
}

:deep(.ant-descriptions-item) {
  margin-bottom: 8px;
  padding: 4px 0;
}
</style>
