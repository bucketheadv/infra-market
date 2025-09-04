<template>
  <a-modal
    v-model:visible="visible"
    :title="isEdit ? '编辑接口' : (formData ? '查看接口' : '新增接口')"
    width="800px"
    :footer="formData && !isEdit ? null : undefined"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
    >
      <a-form-item label="接口名称" name="name">
        <a-input
          v-model:value="form.name"
          placeholder="请输入接口名称"
          :disabled="formData && !isEdit"
        />
      </a-form-item>

      <a-form-item label="请求方法" name="method">
        <a-select
          v-model:value="form.method"
          placeholder="请选择请求方法"
          :disabled="formData && !isEdit"
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
          :disabled="formData && !isEdit"
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

      <a-form-item label="接口URL" name="url">
        <a-input
          v-model:value="form.url"
          placeholder="请输入接口URL"
          :disabled="formData && !isEdit"
        />
      </a-form-item>

      <a-form-item label="接口描述" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="请输入接口描述"
          :rows="3"
          :disabled="formData && !isEdit"
        />
      </a-form-item>

      <!-- URL参数配置 -->
      <a-form-item label="URL参数">
        <div class="params-container">
          <div class="params-header">
            <span>URL参数列表</span>
            <ThemeButton
              v-if="!formData || isEdit"
              variant="primary"
              size="small"
              :icon="PlusOutlined"
              @click="handleAddUrlParam"
            >
              添加URL参数
            </ThemeButton>
          </div>
          
          <div v-if="form.urlParams && form.urlParams.length > 0" class="params-list">
            <div
              v-for="(param, index) in form.urlParams"
              :key="index"
              class="param-item"
            >
              <div class="param-header">
                <span>URL参数 {{ index + 1 }}</span>
                <ThemeButton
                  v-if="!formData || isEdit"
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
                  :index="index"
                  :param-type="'urlParams'"
                  :disabled="!!(formData && !isEdit)"
                  @input-type-change="handleInputTypeChange"
                  @data-type-change="handleDataTypeChange"
                  @default-value-change="handleDefaultValueChange"
                  @add-option="handleAddOption"
                  @remove-option="handleRemoveOption"
                />
              </div>
            </div>
          </div>
          
          <div v-else class="empty-params">
            <a-empty description="暂无URL参数配置" />
          </div>
        </div>
      </a-form-item>

      <!-- Header参数配置 -->
      <a-form-item label="Header参数">
        <div class="params-container">
          <div class="params-header">
            <span>Header参数列表</span>
            <ThemeButton
              v-if="!formData || isEdit"
              variant="primary"
              size="small"
              :icon="PlusOutlined"
              @click="handleAddHeaderParam"
            >
              添加Header参数
            </ThemeButton>
          </div>
          
          <div v-if="form.headerParams && form.headerParams.length > 0" class="params-list">
            <div
              v-for="(param, index) in form.headerParams"
              :key="index"
              class="param-item"
            >
              <div class="param-header">
                <span>Header参数 {{ index + 1 }}</span>
                <ThemeButton
                  v-if="!formData || isEdit"
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
                  :index="index"
                  :param-type="'headerParams'"
                  :disabled="!!(formData && !isEdit)"
                  @input-type-change="handleInputTypeChange"
                  @data-type-change="handleDataTypeChange"
                  @default-value-change="handleDefaultValueChange"
                  @add-option="handleAddOption"
                  @remove-option="handleRemoveOption"
                />
              </div>
            </div>
          </div>
          
          <div v-else class="empty-params">
            <a-empty description="暂无Header参数配置" />
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
              v-if="!formData || isEdit"
              variant="primary"
              size="small"
              :icon="PlusOutlined"
              @click="handleAddBodyParam"
            >
              添加Body参数
            </ThemeButton>
          </div>
          
          <div v-if="form.bodyParams && form.bodyParams.length > 0" class="params-list">
            <div
              v-for="(param, index) in form.bodyParams"
              :key="index"
              class="param-item"
            >
              <div class="param-header">
                <span>Body参数 {{ index + 1 }}</span>
                <ThemeButton
                  v-if="!formData || isEdit"
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
                  :index="index"
                  :param-type="'bodyParams'"
                  :disabled="!!(formData && !isEdit)"
                  @input-type-change="handleInputTypeChange"
                  @data-type-change="handleDataTypeChange"
                  @default-value-change="handleDefaultValueChange"
                  @add-option="handleAddOption"
                  @remove-option="handleRemoveOption"
                />
              </div>
            </div>
          </div>
          
          <div v-else class="empty-params">
            <a-empty description="暂无Body参数配置" />
          </div>
        </div>
      </a-form-item>
    </a-form>
    
    <!-- 自定义 footer -->
    <template v-if="!formData || isEdit" #footer>
      <div class="modal-footer">
        <ThemeButton variant="secondary" @click="handleCancel">
          取消
        </ThemeButton>
        <ThemeButton variant="primary" @click="handleSubmit">
          确定
        </ThemeButton>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { interfaceApi, HTTP_METHODS, POST_TYPES, type ApiInterface, type ApiParam } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import ParamForm from './ParamForm.vue'

// Props
interface Props {
  visible: boolean
  formData: ApiInterface | null
  isEdit: boolean
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

const form = reactive<ApiInterface>({
  name: '',
  method: '',
  url: '',
  description: '',
  postType: '',
  urlParams: [],
  headerParams: [],
  bodyParams: []
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入接口名称', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ],
  url: [
    { required: true, message: '请输入接口URL', trigger: 'blur' }
  ]
}

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
  if (props.formData) {
    Object.assign(form, {
      ...props.formData,
      urlParams: props.formData.urlParams ? [...props.formData.urlParams] : [],
      headerParams: props.formData.headerParams ? [...props.formData.headerParams] : [],
      bodyParams: props.formData.bodyParams ? [...props.formData.bodyParams] : []
    })
  } else {
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
}

const handleMethodChange = () => {
  // 当请求方法改变时，清空POST类型
  if (!['POST', 'PUT', 'PATCH'].includes(form.method)) {
    form.postType = ''
  }
  
  // 触发表单验证，确保POST类型字段的验证状态更新
  nextTick(() => {
    if (formRef.value) {
      formRef.value.validateFields(['postType']).catch(() => {
        // 忽略验证错误，只是触发验证状态更新
      })
    }
  })
}

const handleAddUrlParam = () => {
  const newParam: ApiParam = {
    name: '',
    paramType: 'URL_PARAM',
    inputType: 'TEXT',
    dataType: 'STRING',
    required: false,
    changeable: true,
    options: [],
    description: '',
    sort: (form.urlParams?.length || 0) + 1
  }
  form.urlParams = form.urlParams || []
  form.urlParams.push(newParam)
}

const handleRemoveUrlParam = (index: number) => {
  form.urlParams?.splice(index, 1)
}

const handleAddHeaderParam = () => {
  const newParam: ApiParam = {
    name: '',
    paramType: 'HEADER_PARAM',
    inputType: 'TEXT',
    dataType: 'STRING',
    required: false,
    changeable: true,
    options: [],
    description: '',
    sort: (form.headerParams?.length || 0) + 1
  }
  form.headerParams = form.headerParams || []
  form.headerParams.push(newParam)
}

const handleRemoveHeaderParam = (index: number) => {
  form.headerParams?.splice(index, 1)
}

const handleAddBodyParam = () => {
  const newParam: ApiParam = {
    name: '',
    paramType: 'BODY_PARAM',
    inputType: 'TEXT',
    dataType: 'STRING',
    required: false,
    changeable: true,
    options: [],
    description: '',
    sort: (form.bodyParams?.length || 0) + 1
  }
  form.bodyParams = form.bodyParams || []
  form.bodyParams.push(newParam)
}

const handleRemoveBodyParam = (index: number) => {
  form.bodyParams?.splice(index, 1)
}

const handleAddOption = (paramType: string, paramIndex: number) => {
  let params: ApiParam[] | undefined
  switch (paramType) {
    case 'urlParams':
      params = form.urlParams
      break
    case 'headerParams':
      params = form.headerParams
      break
    case 'bodyParams':
      params = form.bodyParams
      break
  }
  
  if (params && params[paramIndex]) {
    params[paramIndex].options = params[paramIndex].options || []
    params[paramIndex].options!.push('')
  }
}

const handleRemoveOption = (paramType: string, paramIndex: number, optionIndex: number) => {
  let params: ApiParam[] | undefined
  switch (paramType) {
    case 'urlParams':
      params = form.urlParams
      break
    case 'headerParams':
      params = form.headerParams
      break
    case 'bodyParams':
      params = form.bodyParams
      break
  }
  
  if (params && params[paramIndex] && params[paramIndex].options) {
    params[paramIndex].options!.splice(optionIndex, 1)
  }
}

const handleInputTypeChange = (paramType: string, index: number) => {
  let params: ApiParam[] | undefined
  switch (paramType) {
    case 'urlParams':
      params = form.urlParams
      break
    case 'headerParams':
      params = form.headerParams
      break
    case 'bodyParams':
      params = form.bodyParams
      break
  }
  
  if (params && params[index]) {
    const param = params[index]
    if (param.inputType === 'SELECT') {
      param.options = param.options || ['']
    } else {
      param.options = []
    }
  }
}

const handleDataTypeChange = (paramType: string, index: number, dataType: string) => {
  let params: ApiParam[] | undefined
  switch (paramType) {
    case 'urlParams':
      params = form.urlParams
      break
    case 'headerParams':
      params = form.headerParams
      break
    case 'bodyParams':
      params = form.bodyParams
      break
  }
  
  if (params && params[index]) {
    params[index].dataType = dataType
  }
}

const handleDefaultValueChange = (paramType: string, index: number, defaultValue: string) => {
  let params: ApiParam[] | undefined
  switch (paramType) {
    case 'urlParams':
      params = form.urlParams
      break
    case 'headerParams':
      params = form.headerParams
      break
    case 'bodyParams':
      params = form.bodyParams
      break
  }
  
  if (params && params[index]) {
    params[index].defaultValue = defaultValue
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (props.isEdit && props.formData?.id) {
      await interfaceApi.update(props.formData.id, form)
      message.success('更新成功')
    } else {
      await interfaceApi.create(form)
      message.success('创建成功')
    }
    
    emit('success')
    handleCancel()
  } catch (error) {
    message.error('操作失败')
  }
}

const handleCancel = () => {
  visible.value = false
  formRef.value?.resetFields()
}
</script>

<style scoped>
.params-container {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #fafbfc 0%, #f8f9fa 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.params-container:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #d0d7de;
}

/* 确保主题按钮样式不被覆盖 */
.params-container :deep(.theme-button) {
  position: relative;
  z-index: 1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.params-container :deep(.theme-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.params-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: 600;
  font-size: 16px;
  color: #24292f;
  padding-bottom: 12px;
  border-bottom: 2px solid #e1e4e8;
}

.params-list {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.params-list::-webkit-scrollbar {
  width: 6px;
}

.params-list::-webkit-scrollbar-track {
  background: #f1f3f4;
  border-radius: 3px;
}

.params-list::-webkit-scrollbar-thumb {
  background: #c1c8cd;
  border-radius: 3px;
}

.params-list::-webkit-scrollbar-thumb:hover {
  background: #a8b2ba;
}

.param-item {
  border: 1px solid #e1e4e8;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.param-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #0969da, #1f883d, #cf222e);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.param-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #d0d7de;
  transform: translateY(-2px);
}

.param-item:hover::before {
  opacity: 1;
}

.param-item:last-child {
  margin-bottom: 0;
}

.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: 600;
  color: #0969da;
  font-size: 15px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f6f8fa 0%, #eaeef2 100%);
  border-radius: 8px;
  border: 1px solid #d0d7de;
}

.param-form {
  background: transparent;
  padding: 0;
}

.options-container {
  border: 1px solid #d0d7de;
  border-radius: 8px;
  padding: 12px;
  background: #f6f8fa;
  margin-top: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #e1e4e8;
  transition: all 0.2s ease;
}

.option-item:hover {
  border-color: #0969da;
  box-shadow: 0 1px 3px rgba(9, 105, 218, 0.1);
}

.option-item:last-child {
  margin-bottom: 0;
}

.option-item .ant-input {
  flex: 1;
  margin-right: 12px;
  border: none;
  background: transparent;
  box-shadow: none;
}

.option-item .ant-input:focus {
  box-shadow: none;
}

.empty-params {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f6f8fa 0%, #eaeef2 100%);
  border-radius: 12px;
  border: 2px dashed #d0d7de;
  color: #656d76;
}

.empty-params :deep(.ant-empty-description) {
  color: #656d76;
  font-size: 14px;
}

/* Modal footer 样式 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #e1e4e8;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .params-container {
    padding: 16px;
  }
  
  .param-item {
    padding: 16px;
  }
  
  .param-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>


