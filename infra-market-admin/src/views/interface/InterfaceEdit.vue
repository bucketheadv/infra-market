<template>
  <div class="interface-edit-page">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <ApiOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">{{ isEdit ? '编辑接口' : '创建接口' }}</div>
          <div class="header-subtitle">{{ isEdit ? '修改接口信息和参数配置' : '创建新的接口并配置参数' }}</div>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col="{ span: 2 }"
          :wrapper-col="{ span: 22 }"
          class="interface-form-content"
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
            
            <a-form-item label="接口名称" name="name">
              <a-input
                v-model:value="form.name"
                placeholder="请输入接口名称"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <ApiOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item label="请求方法" name="method">
              <a-select
                v-model:value="form.method"
                placeholder="请选择请求方法"
                size="middle"
                class="form-input method-select"
                @change="handleMethodChange"
              >
                <a-select-option
                  v-for="method in HTTP_METHODS"
                  :key="method.value"
                  :value="method.value"
                >
                  {{ method.label }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item 
              v-if="form.method && ['POST', 'PUT', 'PATCH'].includes(form.method)"
              label="POST类型" 
              name="postType"
              :rules="[{ required: true, message: '请选择POST类型' }]"
            >
              <a-select
                v-model:value="form.postType"
                placeholder="请选择POST类型"
                size="middle"
                class="form-input post-type-select"
              >
                <a-select-option
                  v-for="type in POST_TYPES"
                  :key="type.value"
                  :value="type.value"
                >
                  {{ type.label }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="请求URL" name="url">
              <a-input
                v-model:value="form.url"
                placeholder="请输入请求URL"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <LinkOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item label="接口标签" name="tag">
              <a-select
                v-model:value="form.tag"
                placeholder="请选择接口标签"
                size="middle"
                class="form-input"
              >
                <a-select-option
                  v-for="tag in TAGS"
                  :key="tag.value"
                  :value="tag.value"
                >
                  {{ tag.label }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="接口描述" name="description">
              <a-textarea
                v-model:value="form.description"
                placeholder="请输入接口描述"
                :rows="4"
                size="middle"
                class="form-textarea"
                show-count
                :maxlength="200"
              />
            </a-form-item>
          </div>

          <!-- 参数配置区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <SettingOutlined />
              </div>
              <div class="section-title">参数配置</div>
            </div>
            
            <!-- URL参数配置 -->
            <a-form-item label="URL参数">
              <div class="compact-params-container">
                <div class="compact-params-header">
                  <span class="params-count">{{ form.urlParams?.length || 0 }} 个参数</span>
                </div>
                
                <div v-if="form.urlParams && form.urlParams.length > 0" class="compact-params-list">
                  <div v-for="(param, index) in form.urlParams" :key="index" class="compact-param-item">
                    <div class="compact-param-content">
                      <ParamForm
                        :param="param"
                        :index="index"
                        param-type="urlParams"
                        :disabled="false"
                        @input-type-change="handleInputTypeChange"
                        @data-type-change="handleDataTypeChange"
                        @default-value-change="handleDefaultValueChange"
                        @param-name-change="handleParamNameChange"
                        @add-option="handleAddOption"
                        @remove-option="handleRemoveOption"
                      />
                    </div>
                    <ThemeButton
                      variant="danger"
                      size="small"
                      :icon="DeleteOutlined"
                      @click="handleRemoveUrlParam(index)"
                      class="compact-delete-btn"
                    />
                  </div>
                </div>
                
                <div v-else class="compact-no-params">
                  <span class="no-params-text">暂无URL参数</span>
                </div>
                
                <div class="compact-params-footer">
                  <ThemeButton
                    variant="primary"
                    size="small"
                    :icon="PlusOutlined"
                    @click="handleAddUrlParam"
                    class="add-param-btn"
                  >
                    添加URL参数
                  </ThemeButton>
                </div>
              </div>
            </a-form-item>

          <!-- Header参数配置 -->
          <a-form-item label="Header参数">
            <div class="compact-params-container">
              <div class="compact-params-header">
                <span class="params-count">{{ form.headerParams?.length || 0 }} 个参数</span>
              </div>
              
              <div v-if="form.headerParams && form.headerParams.length > 0" class="compact-params-list">
                <div v-for="(param, index) in form.headerParams" :key="index" class="compact-param-item">
                  <div class="compact-param-content">
                    <ParamForm
                      :param="param"
                      :index="index"
                      param-type="headerParams"
                      :disabled="false"
                      @input-type-change="handleInputTypeChange"
                      @data-type-change="handleDataTypeChange"
                      @default-value-change="handleDefaultValueChange"
                      @param-name-change="handleParamNameChange"
                      @add-option="handleAddOption"
                      @remove-option="handleRemoveOption"
                    />
                  </div>
                  <ThemeButton
                    variant="danger"
                    size="small"
                    :icon="DeleteOutlined"
                    @click="handleRemoveHeaderParam(index)"
                    class="compact-delete-btn"
                  />
                </div>
              </div>
              
              <div v-else class="compact-no-params">
                <span class="no-params-text">暂无Header参数</span>
              </div>
              
              <div class="compact-params-footer">
                <ThemeButton
                  variant="primary"
                  size="small"
                  :icon="PlusOutlined"
                  @click="handleAddHeaderParam"
                  class="add-param-btn"
                >
                  添加Header参数
                </ThemeButton>
              </div>
            </div>
          </a-form-item>

          <!-- Body参数配置 -->
          <a-form-item 
            v-if="form.method && ['POST', 'PUT', 'PATCH'].includes(form.method)"
            label="Body参数"
          >
            <div class="compact-params-container">
              <div class="compact-params-header">
                <span class="params-count">{{ form.bodyParams?.length || 0 }} 个参数</span>
              </div>
              
              <div v-if="form.bodyParams && form.bodyParams.length > 0" class="compact-params-list">
                <div v-for="(param, index) in form.bodyParams" :key="index" class="compact-param-item">
                  <div class="compact-param-content">
                    <ParamForm
                      :param="param"
                      :index="index"
                      param-type="bodyParams"
                      :disabled="false"
                      @input-type-change="handleInputTypeChange"
                      @data-type-change="handleDataTypeChange"
                      @default-value-change="handleDefaultValueChange"
                      @param-name-change="handleParamNameChange"
                      @add-option="handleAddOption"
                      @remove-option="handleRemoveOption"
                    />
                  </div>
                  <ThemeButton
                    variant="danger"
                    size="small"
                    :icon="DeleteOutlined"
                    @click="handleRemoveBodyParam(index)"
                    class="compact-delete-btn"
                  />
                </div>
              </div>
              
              <div v-else class="compact-no-params">
                <span class="no-params-text">暂无Body参数</span>
              </div>
              
              <div class="compact-params-footer">
                <ThemeButton
                  variant="primary"
                  size="small"
                  :icon="PlusOutlined"
                  @click="handleAddBodyParam"
                  class="add-param-btn"
                >
                  添加Body参数
                </ThemeButton>
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
                {{ isEdit ? '更新接口' : '创建接口' }}
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, ApiOutlined, IdcardOutlined, LinkOutlined, SettingOutlined, CheckOutlined, CloseOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { interfaceApi, HTTP_METHODS, POST_TYPES, TAGS, type ApiInterface, type ApiParam } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import ParamForm from './ParamForm.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const formRef = ref()
const saving = ref(false)
const isEdit = ref(false)
const interfaceData = ref<ApiInterface | null>(null)

// 表单数据
const form = reactive({
  name: '',
  method: '',
  url: '',
  description: '',
  postType: '',
  tag: '',
  urlParams: [] as ApiParam[],
  headerParams: [] as ApiParam[],
  bodyParams: [] as ApiParam[]
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  url: [{ required: true, message: '请输入请求URL', trigger: 'blur' }]
}

// 获取接口ID
const interfaceId = route.params.id as string

// 初始化数据
onMounted(async () => {
  if (interfaceId && interfaceId !== 'create') {
    isEdit.value = true
    await loadInterfaceData()
  } else {
    isEdit.value = false
    initializeForm()
  }
})

// 初始化表单
const initializeForm = () => {
  Object.assign(form, {
    name: '',
    method: '',
    url: '',
    description: '',
    postType: '',
    tag: '',
    urlParams: [],
    headerParams: [],
    bodyParams: []
  })
}

// 加载接口数据
const loadInterfaceData = async () => {
  try {
    const response = await interfaceApi.getById(Number(interfaceId))
    interfaceData.value = response.data
    
    // 填充表单数据
    Object.assign(form, {
      ...response.data,
      urlParams: response.data.urlParams ? [...response.data.urlParams] : [],
      headerParams: response.data.headerParams ? [...response.data.headerParams] : [],
      bodyParams: response.data.bodyParams ? [...response.data.bodyParams] : []
    })
  } catch (error) {
    console.error('加载接口数据失败:', error)
    message.error('加载接口数据失败')
    router.back()
  }
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 验证选项value
const validateOptions = () => {
  const allParams = [...form.urlParams, ...form.headerParams, ...form.bodyParams]
  
  for (const param of allParams) {
    if ((param.inputType === 'SELECT' || param.inputType === 'MULTI_SELECT') && param.options) {
      for (let i = 0; i < param.options.length; i++) {
        const option = param.options[i]
        if (!option.value || option.value.trim() === '') {
          message.error(`参数"${param.name}"的第${i + 1}个选项值不能为空`)
          return false
        }
      }
    }
  }
  return true
}

// 保存接口
const handleSave = async () => {
  try {
    saving.value = true
    await formRef.value?.validate()
    
    // 验证选项value
    if (!validateOptions()) {
      return
    }
    
    // 检查参数名重复
    if (!validateParamNames()) {
      return
    }
    
    if (isEdit.value && interfaceData.value?.id) {
      await interfaceApi.update(interfaceData.value.id, form)
      message.success('接口更新成功')
    } else {
      await interfaceApi.create(form)
      message.success('接口创建成功')
    }
    
    router.back()
  } catch (error) {
    console.error('保存接口失败:', error)
    message.error('保存接口失败')
  } finally {
    saving.value = false
  }
}

// 请求方法变化处理
const handleMethodChange = () => {
  // 清空POST类型
  form.postType = ''
}

// 添加URL参数
const handleAddUrlParam = () => {
  form.urlParams.push({
    name: '',
    chineseName: '',
    description: '',
    dataType: 'STRING',
    inputType: 'TEXT',
    paramType: 'URL_PARAM',
    required: false,
    defaultValue: '',
    options: []
  })
}

// 删除URL参数
const handleRemoveUrlParam = (index: number) => {
  form.urlParams.splice(index, 1)
}

// 添加Header参数
const handleAddHeaderParam = () => {
  form.headerParams.push({
    name: '',
    chineseName: '',
    description: '',
    dataType: 'STRING',
    inputType: 'TEXT',
    paramType: 'HEADER_PARAM',
    required: false,
    defaultValue: '',
    options: []
  })
}

// 删除Header参数
const handleRemoveHeaderParam = (index: number) => {
  form.headerParams.splice(index, 1)
}

// 添加Body参数
const handleAddBodyParam = () => {
  form.bodyParams.push({
    name: '',
    chineseName: '',
    description: '',
    dataType: 'STRING',
    inputType: 'TEXT',
    paramType: 'BODY_PARAM',
    required: false,
    defaultValue: '',
    options: []
  })
}

// 删除Body参数
const handleRemoveBodyParam = (index: number) => {
  form.bodyParams.splice(index, 1)
}

// 参数相关事件处理
const handleInputTypeChange = (_paramType: string, _index: number) => {
  // 输入类型变化时，数据类型会自动由ParamForm组件处理
  // 这里只需要处理其他逻辑
}

const handleParamNameChange = (_paramType: string, _index: number, _name: string) => {
  // 检查参数名是否重复
  checkDuplicateParamNames()
}

const handleDataTypeChange = (paramType: string, index: number, dataType: string) => {
  // 根据参数类型找到对应的参数数组
  let paramArray: ApiParam[] = []
  if (paramType === 'urlParams') {
    paramArray = form.urlParams
  } else if (paramType === 'headerParams') {
    paramArray = form.headerParams
  } else if (paramType === 'bodyParams') {
    paramArray = form.bodyParams
  }
  
  // 更新数据类型
  if (paramArray[index]) {
    paramArray[index].dataType = dataType as any
  }
}

const handleDefaultValueChange = (paramType: string, index: number, value: string | any[]) => {
  // 根据参数类型找到对应的参数数组
  let paramArray: ApiParam[] = []
  if (paramType === 'urlParams') {
    paramArray = form.urlParams
  } else if (paramType === 'headerParams') {
    paramArray = form.headerParams
  } else if (paramType === 'bodyParams') {
    paramArray = form.bodyParams
  }
  
  // 更新默认值
  if (paramArray[index]) {
    paramArray[index].defaultValue = value
  }
}

const handleAddOption = (paramType: string, index: number) => {
  // 根据参数类型找到对应的参数数组
  let paramArray: ApiParam[] = []
  if (paramType === 'urlParams') {
    paramArray = form.urlParams
  } else if (paramType === 'headerParams') {
    paramArray = form.headerParams
  } else if (paramType === 'bodyParams') {
    paramArray = form.bodyParams
  }
  
  // 添加选项
  if (paramArray[index]) {
    if (!paramArray[index].options) {
      paramArray[index].options = []
    }
    paramArray[index].options!.push({ value: '', label: '' })
  }
}

const handleRemoveOption = (paramType: string, index: number, optionIndex: number) => {
  // 根据参数类型找到对应的参数数组
  let paramArray: ApiParam[] = []
  if (paramType === 'urlParams') {
    paramArray = form.urlParams
  } else if (paramType === 'headerParams') {
    paramArray = form.headerParams
  } else if (paramType === 'bodyParams') {
    paramArray = form.bodyParams
  }
  
  // 删除选项
  if (paramArray[index] && paramArray[index].options) {
    paramArray[index].options!.splice(optionIndex, 1)
  }
}

// 检查参数名重复
const checkDuplicateParamNames = () => {
  const duplicates: string[] = []
  
  // 检查URL参数内部重复
  const urlDuplicates = checkDuplicatesInArray(form.urlParams, 'URL参数')
  duplicates.push(...urlDuplicates)
  
  // 检查Header参数内部重复
  const headerDuplicates = checkDuplicatesInArray(form.headerParams, 'Header参数')
  duplicates.push(...headerDuplicates)
  
  // 检查Body参数内部重复
  const bodyDuplicates = checkDuplicatesInArray(form.bodyParams, 'Body参数')
  duplicates.push(...bodyDuplicates)
  
  // 如果有重复，显示警告
  if (duplicates.length > 0) {
    message.warning(duplicates.join('；'))
  }
}

// 检查单个参数数组内的重复
const checkDuplicatesInArray = (params: ApiParam[], paramTypeName: string): string[] => {
  const nameCount = new Map<string, number>()
  const duplicates = new Set<string>()
  
  // 统计每个参数名的出现次数
  params.forEach(param => {
    if (param.name && param.name.trim()) {
      const name = param.name.trim()
      const count = nameCount.get(name) || 0
      nameCount.set(name, count + 1)
      if (count > 0) {
        duplicates.add(name)
      }
    }
  })
  
  // 返回重复的参数名信息
  if (duplicates.size > 0) {
    const duplicateNames = Array.from(duplicates).join('、')
    return [`${paramTypeName}中参数名 "${duplicateNames}" 重复`]
  }
  
  return []
}

// 验证参数名是否重复（用于保存时验证）
const validateParamNames = (): boolean => {
  const duplicates: string[] = []
  
  // 检查URL参数内部重复
  const urlDuplicates = checkDuplicatesInArray(form.urlParams, 'URL参数')
  duplicates.push(...urlDuplicates)
  
  // 检查Header参数内部重复
  const headerDuplicates = checkDuplicatesInArray(form.headerParams, 'Header参数')
  duplicates.push(...headerDuplicates)
  
  // 检查Body参数内部重复
  const bodyDuplicates = checkDuplicatesInArray(form.bodyParams, 'Body参数')
  duplicates.push(...bodyDuplicates)
  
  // 如果有重复，显示错误并返回false
  if (duplicates.length > 0) {
    message.error(duplicates.join('；') + '，请修改参数名后再保存')
    return false
  }
  
  return true
}
</script>

<style scoped>
.interface-edit-page {
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

.interface-form-content {
  padding: 12px 0;
}

/* 确保接口描述标签与其他标签对齐 */
.description-item :deep(.ant-form-item-label) {
  text-align: left;
  padding-left: 0;
  line-height: 32px;
  padding-top: 0;
}

/* 统一标签对齐方式 */
.interface-form-content :deep(.ant-form-item-label) {
  text-align: right;
  padding-right: 6px;
  line-height: 32px;
}

/* 确保所有标签垂直对齐 */
.interface-form-content :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.interface-form-content :deep(.ant-form-item-label > label) {
  height: 32px;
  line-height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 13px;
  white-space: nowrap;
}

/* 统一所有表单项的标签对齐 */
.interface-form-content :deep(.ant-form-item-label) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
}

/* 确保输入框区域对齐 */
.interface-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: flex-start;
  min-height: 32px;
}

/* 统一标签对齐方式 */
.interface-form-content :deep(.ant-form-item-label) {
  text-align: left;
  padding-left: 0;
  padding-right: 0;
  margin-left: 0;
}

/* 输入框样式 */
.form-input {
  height: 32px;
  line-height: 32px;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 13px;
  width: 100%;
}

/* 文本域样式 */
.form-textarea {
  height: auto;
  min-height: 32px;
  line-height: 1.5;
  padding: 6px 11px;
  border-radius: 4px;
  transition: all 0.2s ease;
  resize: vertical;
  font-size: 13px;
  width: 100%;
}

.form-input:hover {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.1));
}

.form-input:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.form-textarea:hover {
  border-color: var(--primary-color, #1890ff);
}

.form-textarea:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.input-icon {
  color: #bfbfbf;
  font-size: 12px;
}

/* 请求方法选择框图标 */
.method-select :deep(.ant-select-selector) {
  position: relative;
  padding-left: 32px !important;
}

.method-select :deep(.ant-select-selector::before) {
  content: '';
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1024 1024'%3E%3Cpath fill='%23bfbfbf' d='M848 359.3H627.7L825.8 109c4.1-5.3.4-13-6.3-13H436c-2.8 0-5.5 1.5-6.9 4L170 547.8c-3.1 5.3.7 12 6.9 12h174.4l-89.4 357.6c-1.9 7.8 7.5 13.3 13.3 7.7L853.5 373c5.2-4.9 1.7-13.7-5.5-13.7z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  pointer-events: none;
  z-index: 1;
}

/* POST类型选择框图标 */
.post-type-select :deep(.ant-select-selector) {
  position: relative;
  padding-left: 32px !important;
}

.post-type-select :deep(.ant-select-selector::before) {
  content: '';
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1024 1024'%3E%3Cpath fill='%23bfbfbf' d='M832 64H192c-17.7 0-32 14.3-32 32v832c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V96c0-17.7-14.3-32-32-32zm-600 72h560v208H232V136zm560 480H232V408h560v208zm0 272H232V680h560v208z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  pointer-events: none;
  z-index: 1;
}


/* 确保列宽正确 */
.interface-form-content :deep(.ant-col-2) {
  flex: 0 0 8.333333%;
  max-width: 8.333333%;
}

/* 确保输入框有足够空间 */
.interface-form-content :deep(.ant-col-22) {
  flex: 0 0 91.666667%;
  max-width: 91.666667%;
}

/* 确保所有输入框完美对齐 */
.interface-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: center;
}

.interface-form-content :deep(.ant-form-item-control-input) {
  width: 100%;
}

.interface-form-content :deep(.ant-form-item-control-input-content) {
  width: 100%;
}

/* 调整标签位置，与系统管理标题x轴对齐 */
.interface-form-content :deep(.ant-form-item-label) {
  padding-left: 0;
  margin-left: 0;
  text-align: left;
}

.interface-form-content :deep(.ant-form-item-label > label) {
  padding-left: 0;
  margin-left: 0;
}

/* 调整输入框样式，让它们更长 */
.form-input {
  width: 100%;
  min-width: 200px;
}

/* 表单区域样式 */
.form-section {
  margin-bottom: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}

.section-icon {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  background: var(--primary-color, #1890ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
  color: white;
  font-size: 10px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

/* 超紧凑型参数容器样式 */
.compact-params-container {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  overflow: hidden;
}

.compact-params-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
}

.params-count {
  font-size: 10px;
  color: #666;
  font-weight: 500;
}

.add-param-btn {
  font-size: 9px !important;
  padding: 2px 6px !important;
  height: 18px !important;
  min-width: 35px !important;
}

.compact-params-list {
  display: flex;
  flex-direction: column;
}

.compact-param-item {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  padding: 4px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  transition: background-color 0.2s ease;
}

.compact-param-item:last-child {
  border-bottom: none;
}

.compact-param-item:hover {
  background: #fafafa;
}

.compact-param-content {
  flex: 1;
  min-width: 0;
}

.compact-delete-btn {
  flex-shrink: 0;
  width: 20px !important;
  height: 20px !important;
  padding: 0 !important;
  min-width: 20px !important;
  border-radius: 3px !important;
  background: #ff4757 !important;
  border: 1px solid #ff3742 !important;
  color: white !important;
  font-size: 8px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.compact-delete-btn:hover {
  background: #ff3742 !important;
  border-color: #ff2f3a !important;
  transform: scale(1.05) !important;
}

.compact-delete-btn .theme-button__icon {
  color: white !important;
  font-size: 8px !important;
}

.compact-delete-btn .theme-button__icon * {
  color: white !important;
}

.compact-no-params {
  padding: 8px;
  text-align: center;
  background: #fff;
}

.no-params-text {
  font-size: 10px;
  color: #999;
}

.compact-params-footer {
  padding: 4px 8px;
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  text-align: center;
}

.compact-params-footer .add-param-btn {
  font-size: 9px !important;
  padding: 3px 8px !important;
  height: 20px !important;
  min-width: 80px !important;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .form-content {
    padding: 0 16px 16px;
  }
  
  .form-header {
    padding: 0 16px;
    margin-top: 12px;
  }
  
  .header-content {
    padding: 14px 18px;
  }
  
  .header-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
  
  .header-title {
    font-size: 16px;
  }
  
  .header-subtitle {
    font-size: 12px;
  }
  
  .interface-form-content {
    padding: 14px 0;
  }
  
  .form-section {
    margin-bottom: 18px;
  }
  
  .section-header {
    margin-bottom: 14px;
  }
}

@media (max-width: 576px) {
  .form-content {
    padding: 0 12px 12px;
  }
  
  .form-header {
    padding: 0 12px;
    margin-top: 10px;
  }
  
  .header-content {
    padding: 12px 16px;
  }
  
  .header-icon {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .header-title {
    font-size: 15px;
  }
  
  .header-subtitle {
    font-size: 11px;
  }
  
  .interface-form-content {
    padding: 12px 0;
  }
  
  .form-section {
    margin-bottom: 16px;
  }
}
</style>
