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
                class="form-input"
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
                class="form-input"
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

            <a-form-item label="接口描述" name="description">
              <a-textarea
                v-model:value="form.description"
                placeholder="请输入接口描述"
                :rows="3"
                size="middle"
                class="form-input"
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
            <div class="params-container">
              <div class="params-header">
                <span>URL参数列表</span>
                <ThemeButton
                  variant="primary"
                  size="small"
                  :icon="PlusOutlined"
                  @click="handleAddUrlParam"
                >
                  添加URL参数
                </ThemeButton>
              </div>
              
              <div v-if="form.urlParams && form.urlParams.length > 0" class="params-list">
                <div v-for="(param, index) in form.urlParams" :key="index" class="param-item">
                  <div class="param-header">
                    <span class="param-title">参数 {{ index + 1 }}</span>
                    <ThemeButton
                      variant="danger"
                      size="small"
                      @click="handleRemoveUrlParam(index)"
                    >
                      删除
                    </ThemeButton>
                  </div>
                  
                  <div class="param-form">
                    <ParamForm
                      :param="param"
                      :disabled="false"
                      @input-type-change="handleInputTypeChange"
                      @data-type-change="handleDataTypeChange"
                      @default-value-change="handleDefaultValueChange"
                      @add-option="handleAddOption"
                      @remove-option="handleRemoveOption"
                    />
                  </div>
                </div>
              </div>
              
              <div v-else class="no-params">
                <a-empty description="暂无URL参数" />
              </div>
            </div>
          </a-form-item>

          <!-- Header参数配置 -->
          <a-form-item label="Header参数">
            <div class="params-container">
              <div class="params-header">
                <span>Header参数列表</span>
                <ThemeButton
                  variant="primary"
                  size="small"
                  :icon="PlusOutlined"
                  @click="handleAddHeaderParam"
                >
                  添加Header参数
                </ThemeButton>
              </div>
              
              <div v-if="form.headerParams && form.headerParams.length > 0" class="params-list">
                <div v-for="(param, index) in form.headerParams" :key="index" class="param-item">
                  <div class="param-header">
                    <span class="param-title">参数 {{ index + 1 }}</span>
                    <ThemeButton
                      variant="danger"
                      size="small"
                      @click="handleRemoveHeaderParam(index)"
                    >
                      删除
                    </ThemeButton>
                  </div>
                  
                  <div class="param-form">
                    <ParamForm
                      :param="param"
                      :disabled="false"
                      @input-type-change="handleInputTypeChange"
                      @data-type-change="handleDataTypeChange"
                      @default-value-change="handleDefaultValueChange"
                      @add-option="handleAddOption"
                      @remove-option="handleRemoveOption"
                    />
                  </div>
                </div>
              </div>
              
              <div v-else class="no-params">
                <a-empty description="暂无Header参数" />
              </div>
            </div>
          </a-form-item>

          <!-- Body参数配置 -->
          <a-form-item 
            v-if="form.method && ['POST', 'PUT', 'PATCH'].includes(form.method)"
            label="Body参数"
          >
            <div class="params-container">
              <div class="params-header">
                <span>Body参数列表</span>
                <ThemeButton
                  variant="primary"
                  size="small"
                  :icon="PlusOutlined"
                  @click="handleAddBodyParam"
                >
                  添加Body参数
                </ThemeButton>
              </div>
              
              <div v-if="form.bodyParams && form.bodyParams.length > 0" class="params-list">
                <div v-for="(param, index) in form.bodyParams" :key="index" class="param-item">
                  <div class="param-header">
                    <span class="param-title">参数 {{ index + 1 }}</span>
                    <ThemeButton
                      variant="danger"
                      size="small"
                      @click="handleRemoveBodyParam(index)"
                    >
                      删除
                    </ThemeButton>
                  </div>
                  
                  <div class="param-form">
                    <ParamForm
                      :param="param"
                      :disabled="false"
                      @input-type-change="handleInputTypeChange"
                      @data-type-change="handleDataTypeChange"
                      @default-value-change="handleDefaultValueChange"
                      @add-option="handleAddOption"
                      @remove-option="handleRemoveOption"
                    />
                  </div>
                </div>
              </div>
              
              <div v-else class="no-params">
                <a-empty description="暂无Body参数" />
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
import { PlusOutlined, ApiOutlined, IdcardOutlined, LinkOutlined, SettingOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { interfaceApi, HTTP_METHODS, POST_TYPES, type ApiInterface, type ApiParam } from '@/api/interface'
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

// 保存接口
const handleSave = async () => {
  try {
    saving.value = true
    await formRef.value?.validate()
    
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
    description: '',
    dataType: 'STRING',
    inputType: 'INPUT',
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
    description: '',
    dataType: 'STRING',
    inputType: 'INPUT',
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
    description: '',
    dataType: 'STRING',
    inputType: 'INPUT',
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
const handleInputTypeChange = (param: ApiParam, inputType: string) => {
  param.inputType = inputType as any
}

const handleDataTypeChange = (param: ApiParam, dataType: string) => {
  param.dataType = dataType as any
}

const handleDefaultValueChange = (param: ApiParam, value: string) => {
  param.defaultValue = value
}

const handleAddOption = (param: ApiParam, option: string) => {
  if (!param.options) {
    param.options = []
  }
  param.options.push(option)
}

const handleRemoveOption = (param: ApiParam, index: number) => {
  if (param.options) {
    param.options.splice(index, 1)
  }
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
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.header-subtitle {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
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

.form-input:hover {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.1));
}

.form-input:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.input-icon {
  color: #bfbfbf;
  font-size: 12px;
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
  background: var(--primary-color, #1890ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  color: white;
  font-size: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.params-container {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
}

.params-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.params-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.param-item {
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 16px;
  background: #fff;
}

.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.param-title {
  font-weight: 500;
  color: #333;
}

.param-form {
  margin-top: 12px;
}

.no-params {
  text-align: center;
  padding: 40px 0;
  color: #999;
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
