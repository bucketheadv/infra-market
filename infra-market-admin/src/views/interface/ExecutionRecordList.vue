<template>
  <div class="execution-record-list">
    <a-card>
      <template #title>接口执行日志</template>
      
      <!-- 搜索表单 -->
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="8" :lg="8">
            <a-form-item label="关键字">
              <a-input
                v-model:value="searchForm.keyword"
                placeholder="搜索执行人、错误信息、备注"
                allow-clear
                @pressEnter="handleSearch"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6" :lg="6">
            <a-form-item label="接口ID">
              <a-input-number
                v-model:value="searchForm.interfaceId"
                placeholder="请输入接口ID"
                :min="1"
                style="width: 100%"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6" :lg="6">
            <a-form-item label="执行结果">
              <a-select
                v-model:value="searchForm.success"
                placeholder="请选择"
                allow-clear
                style="width: 100%"
              >
                <a-select-option :value="true">成功</a-select-option>
                <a-select-option :value="false">失败</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4" :lg="4">
            <a-form-item label=" " :colon="false">
              <a-space>
                <ThemeButton variant="primary" :icon="SearchOutlined" @click="handleSearch">
                  查询
                </ThemeButton>
                <ThemeButton variant="ghost" :icon="ReloadOutlined" @click="handleReset">
                  重置
                </ThemeButton>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      
      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1200 }"
        row-key="id"
        class="interface-table"
        :row-class-name="getRowClassName"
        @change="handleTableChange"
        :locale="{
          emptyText: '暂无数据',
          filterConfirm: '确定',
          filterReset: '重置',
          filterEmptyText: '无筛选项',
          selectAll: '全选',
          selectInvert: '反选',
          sortTitle: '排序',
          expand: '展开行',
          collapse: '收起行'
        }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'success'">
            <a-tag :color="record.success ? 'success' : 'error'">
              {{ record.success ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'executionTime'">
            <span v-if="record.executionTime">{{ record.executionTime }}ms</span>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'responseStatus'">
            <a-tag v-if="record.responseStatus" :color="getStatusColor(record.responseStatus)">
              {{ record.responseStatus }}
            </a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'remark'">
            <span v-if="record.remark" :title="record.remark">{{ record.remark }}</span>
            <span v-else class="text-muted">-</span>
          </template>
          <template v-else-if="column.key === 'environment'">
            <a-tag 
              v-if="getInterfaceEnvironment(record.interfaceId)"
              :color="getInterfaceEnvironment(record.interfaceId) === 'PRODUCTION' ? 'red' : 'blue'"
              :style="{
                fontWeight: '600',
                fontSize: '12px',
                padding: '2px 8px',
                borderRadius: '4px',
                border: getInterfaceEnvironment(record.interfaceId) === 'PRODUCTION' ? '1px solid #ff4d4f' : '1px solid #1890ff'
              }"
            >
              <span style="margin-right: 4px;">
                {{ getInterfaceEnvironment(record.interfaceId) === 'PRODUCTION' ? '🚨' : '🧪' }}
              </span>
              {{ getInterfaceEnvironment(record.interfaceId) === 'PRODUCTION' ? '正式' : '测试' }}
            </a-tag>
            <span v-else class="text-muted">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-buttons">
              <ThemeButton 
                variant="ghost" 
                size="small" 
                class="action-btn view-btn"
                :icon="EyeOutlined"
                @click="handleViewDetail(record)"
              >
                查看详情
              </ThemeButton>
              <ThemeButton 
                v-if="record.interfaceId"
                variant="ghost" 
                size="small" 
                class="action-btn execute-btn"
                :icon="PlayCircleOutlined"
                @click="handleGoToExecute(record.interfaceId)"
              >
                执行
              </ThemeButton>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="执行记录详情"
      width="80%"
      class="record-detail-modal"
    >
      <template #footer>
        <div class="modal-footer-content">
          <ThemeButton 
            v-if="selectedRecord?.interfaceId"
            variant="primary"
            :icon="PlayCircleOutlined"
            @click="handleGoToExecute(selectedRecord.interfaceId)"
            class="go-to-execute-btn"
          >
            跳转到执行
          </ThemeButton>
          <ThemeButton 
            variant="ghost"
            @click="detailVisible = false"
          >
            关闭
          </ThemeButton>
        </div>
      </template>
      <div v-if="selectedRecord" class="detail-content">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="记录ID">{{ selectedRecord.id }}</a-descriptions-item>
          <a-descriptions-item label="接口ID">{{ selectedRecord.interfaceId }}</a-descriptions-item>
          <a-descriptions-item label="执行人">{{ selectedRecord.executorName }}</a-descriptions-item>
          <a-descriptions-item label="执行结果">
            <a-tag :color="selectedRecord.success ? 'success' : 'error'">
              {{ selectedRecord.success ? '成功' : '失败' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="响应状态码">
            <a-tag v-if="selectedRecord.responseStatus" :color="getStatusColor(selectedRecord.responseStatus)">
              {{ selectedRecord.responseStatus }}
            </a-tag>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="执行时间">
            <span v-if="selectedRecord.executionTime">{{ selectedRecord.executionTime }}ms</span>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="客户端IP">{{ selectedRecord.clientIp || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ formatDateTime(selectedRecord.createTime) }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ selectedRecord.remark || '-' }}</a-descriptions-item>
          <a-descriptions-item v-if="selectedRecord.errorMessage" label="错误信息" :span="2">
            <div class="error-message">{{ selectedRecord.errorMessage }}</div>
          </a-descriptions-item>
        </a-descriptions>

        <a-tabs v-model:activeKey="detailActiveTab" class="detail-tabs">
          <!-- 请求参数tab -->
          <a-tab-pane v-if="hasRequestParams" key="request" tab="请求参数">
            <div class="detail-content">
              <!-- URL参数 -->
              <div v-if="urlParams.length > 0" class="param-section">
                <h4>URL参数</h4>
                <div class="record-params-container">
                  <a-row v-for="param in urlParams" :key="param.name" class="record-param-row" :gutter="[6, 0]">
                    <a-col :span="6">
                      <label class="record-param-label">
                        {{ getParamDisplayName(param) }}
                        <span v-if="param.required" class="required">*</span>
                        <a-tooltip v-if="param.description" :title="param.description" placement="top">
                          <QuestionCircleOutlined class="help-icon" />
                        </a-tooltip>
                      </label>
                    </a-col>
                    <a-col :span="18">
                      <div class="record-param-value">
                        <div v-if="param.inputType === 'CODE' || (param.inputType === 'TEXTAREA' && param.dataType === 'JSON')" class="code-value-display">
                          <CodeEditor
                            :model-value="getRecordParamValue(selectedRecord?.requestParams, param.name)"
                            :readonly="true"
                            :height="150"
                            :language="getCodeLanguageForParam(param)"
                            :options="{
                              fontSize: 12,
                              fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                              lineHeight: 18,
                              minimap: { enabled: false },
                              readOnly: true
                            }"
                          />
                        </div>
                        <a-input
                          v-else
                          :value="getRecordParamDisplayValue(selectedRecord?.requestParams, param)"
                          readonly
                          :placeholder="`无${getParamDisplayName(param)}`"
                          class="readonly-input"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </div>
              
              <!-- Body参数 -->
              <div v-if="bodyParams.length > 0 && interfaceData?.method !== 'GET'" class="param-section">
                <h4>请求体</h4>
                <div class="record-params-container">
                  <a-row v-for="param in bodyParams" :key="param.name" class="record-param-row" :gutter="[6, 0]">
                    <a-col :span="6">
                      <label class="record-param-label">
                        {{ getParamDisplayName(param) }}
                        <span v-if="param.required" class="required">*</span>
                        <a-tooltip v-if="param.description" :title="param.description" placement="top">
                          <QuestionCircleOutlined class="help-icon" />
                        </a-tooltip>
                      </label>
                    </a-col>
                    <a-col :span="18">
                      <div class="record-param-value">
                        <div v-if="param.inputType === 'CODE' || (param.inputType === 'TEXTAREA' && param.dataType === 'JSON')" class="code-value-display">
                          <CodeEditor
                            :model-value="getRecordParamValue(selectedRecord?.requestBody, param.name)"
                            :readonly="true"
                            :height="150"
                            :language="getCodeLanguageForParam(param)"
                            :options="{
                              fontSize: 12,
                              fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                              lineHeight: 18,
                              minimap: { enabled: false },
                              readOnly: true
                            }"
                          />
                        </div>
                        <a-input
                          v-else
                          :value="getRecordParamDisplayValue(selectedRecord?.requestBody, param)"
                          readonly
                          :placeholder="`无${getParamDisplayName(param)}`"
                          class="readonly-input"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </div>
              
              <!-- 如果没有接口配置，显示原始JSON -->
              <div v-if="urlParams.length === 0 && bodyParams.length === 0 && selectedRecord.requestParams" class="param-section">
                <h4>原始请求参数</h4>
                <div class="code-content">
                  <CodeEditor
                    :model-value="formatJson(selectedRecord.requestParams)"
                    :readonly="true"
                    :height="300"
                    :language="detectResponseLanguage(selectedRecord.requestParams)"
                    :options="{
                      fontSize: 12,
                      fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                      lineHeight: 18,
                      minimap: { enabled: true },
                      readOnly: true
                    }"
                  />
                </div>
              </div>
              
              <div v-if="urlParams.length === 0 && bodyParams.length === 0 && !selectedRecord.requestParams" class="no-content">
                <a-empty description="无请求参数" />
              </div>
            </div>
          </a-tab-pane>
          
          <!-- 请求头tab -->
          <a-tab-pane v-if="hasRequestHeaders" key="headers" tab="请求头">
            <div class="detail-content">
              <div v-if="headerParams.length > 0" class="param-section">
                <h4>请求头参数</h4>
                <div class="record-params-container">
                  <a-row v-for="param in headerParams" :key="param.name" class="record-param-row" :gutter="[6, 0]">
                    <a-col :span="6">
                      <label class="record-param-label">
                        {{ getParamDisplayName(param) }}
                        <span v-if="param.required" class="required">*</span>
                        <a-tooltip v-if="param.description" :title="param.description" placement="top">
                          <QuestionCircleOutlined class="help-icon" />
                        </a-tooltip>
                      </label>
                    </a-col>
                    <a-col :span="18">
                      <div class="record-param-value">
                        <div v-if="param.inputType === 'CODE' || (param.inputType === 'TEXTAREA' && param.dataType === 'JSON')" class="code-value-display">
                          <CodeEditor
                            :model-value="getRecordParamValue(selectedRecord?.requestHeaders, param.name)"
                            :readonly="true"
                            :height="150"
                            :language="getCodeLanguageForParam(param)"
                            :options="{
                              fontSize: 12,
                              fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                              lineHeight: 18,
                              minimap: { enabled: false },
                              readOnly: true
                            }"
                          />
                        </div>
                        <a-input
                          v-else
                          :value="getRecordParamDisplayValue(selectedRecord?.requestHeaders, param)"
                          readonly
                          :placeholder="`无${getParamDisplayName(param)}`"
                          class="readonly-input"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </div>
              <div v-else-if="selectedRecord.requestHeaders" class="code-content">
                <CodeEditor
                  :model-value="formatJson(selectedRecord.requestHeaders)"
                  :readonly="true"
                  :height="300"
                  language="json"
                  :options="{
                    fontSize: 12,
                    fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                    lineHeight: 18,
                    minimap: { enabled: true },
                    readOnly: true
                  }"
                />
              </div>
              <div v-else class="empty-content">无请求头</div>
            </div>
          </a-tab-pane>
          
          <!-- 响应体tab -->
          <a-tab-pane v-if="hasResponseBody" key="response" tab="响应体">
            <div class="detail-content">
              <div v-if="selectedRecord.responseBody" class="param-section">
                <!-- 如果配置了取值路径且成功提取值，则显示提取值 -->
                <div v-if="interfaceData?.valuePath && recordExtractedValue" class="extracted-value-section-record">
                  <div class="extracted-value-header-record">
                    <h4>
                      提取的值
                      <span class="value-path-badge">{{ interfaceData.valuePath }}</span>
                    </h4>
                  </div>
                  <div class="extracted-value-content-record">
                    <CodeEditor
                      :model-value="recordExtractedValueContent"
                      :readonly="true"
                      :height="300"
                      :language="detectResponseLanguage(recordExtractedValue)"
                      :options="{
                        fontSize: 12,
                        fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                        lineHeight: 18,
                        minimap: { enabled: false },
                        readOnly: true
                      }"
                    />
                  </div>
                </div>
                
                <!-- 响应体 -->
                <div class="response-body-record">
                  <div class="response-body-header-record">
                    <h4>原始响应体</h4>
                    <div class="response-header-right-record">
                      <span class="response-size-record">{{ selectedRecord.responseBody?.length || 0 }} 字符</span>
                    </div>
                  </div>
                  <div class="response-body-content-record">
                    <CodeEditor
                      :model-value="recordResponseBodyContent"
                      :readonly="true"
                      :height="300"
                      :language="detectResponseLanguage(selectedRecord.responseBody)"
                      :options="{
                        fontSize: 12,
                        fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                        lineHeight: 18,
                        minimap: { enabled: true },
                        readOnly: true
                      }"
                    />
                  </div>
                </div>
              </div>
              <div v-if="selectedRecord.responseHeaders" class="param-section">
                <h4>响应头</h4>
                <CodeEditor
                  :model-value="formatJson(selectedRecord.responseHeaders)"
                  :readonly="true"
                  :height="200"
                  language="json"
                  :options="{
                    fontSize: 12,
                    fontFamily: 'Intel One Mono, SF Mono, Monaco, Menlo, monospace',
                    lineHeight: 18
                  }"
                />
              </div>
              <div v-if="!selectedRecord.responseHeaders && !selectedRecord.responseBody" class="no-content">
                <a-empty description="无响应内容" />
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, EyeOutlined, QuestionCircleOutlined, PlayCircleOutlined } from '@ant-design/icons-vue'
import { executionRecordApi, interfaceApi, type ApiInterfaceExecutionRecord, type ApiInterfaceExecutionRecordQuery, type ApiInterface, type ApiParam } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import CodeEditor from '@/components/CodeEditor.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const dataSource = ref<ApiInterfaceExecutionRecord[]>([])
const detailVisible = ref(false)
const selectedRecord = ref<ApiInterfaceExecutionRecord | null>(null)
const interfaceData = ref<ApiInterface | null>(null)
const detailActiveTab = ref('request')
const interfaceMap = ref<Map<number, ApiInterface>>(new Map())

// 搜索表单
const searchForm = reactive<ApiInterfaceExecutionRecordQuery>({
  keyword: undefined,
  interfaceId: undefined,
  success: undefined,
  page: 1,
  size: 10
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100'],
  size: 'small'
})

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    align: 'center'
  },
  {
    title: '接口ID',
    dataIndex: 'interfaceId',
    key: 'interfaceId',
    width: 100,
    align: 'center'
  },
  {
    title: '环境',
    key: 'environment',
    width: 100,
    align: 'center'
  },
  {
    title: '执行人',
    dataIndex: 'executorName',
    key: 'executorName',
    width: 120
  },
  {
    title: '执行结果',
    dataIndex: 'success',
    key: 'success',
    width: 100,
    align: 'center'
  },
  {
    title: '响应状态码',
    dataIndex: 'responseStatus',
    key: 'responseStatus',
    width: 120,
    align: 'center'
  },
  {
    title: '执行时间',
    dataIndex: 'executionTime',
    key: 'executionTime',
    width: 120,
    align: 'center'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '备注',
    dataIndex: 'remark',
    key: 'remark',
    width: 150,
    ellipsis: true
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    align: 'center',
    fixed: 'right'
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const query: ApiInterfaceExecutionRecordQuery = {
      keyword: searchForm.keyword || undefined,
      interfaceId: searchForm.interfaceId || undefined,
      success: searchForm.success,
      page: pagination.current,
      size: pagination.pageSize
    }
    
    const response = await executionRecordApi.getList(query)
    if (response.data) {
      dataSource.value = response.data.records || []
      pagination.total = response.data.total || 0
      
      // 加载接口信息以获取环境
      await loadInterfaceEnvironments()
    }
  } catch (error: any) {
    message.error(error?.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载接口环境信息
const loadInterfaceEnvironments = async () => {
  const interfaceIds = new Set<number>()
  dataSource.value.forEach(record => {
    if (record.interfaceId) {
      interfaceIds.add(record.interfaceId)
    }
  })
  
  // 只加载未缓存的接口
  const idsToLoad = Array.from(interfaceIds).filter(id => !interfaceMap.value.has(id))
  
  if (idsToLoad.length === 0) return
  
  try {
    // 批量加载接口信息
    const promises = idsToLoad.map(id => 
      interfaceApi.getById(id).catch(() => null)
    )
    const results = await Promise.all(promises)
    
    results.forEach((result, index) => {
      if (result?.data) {
        interfaceMap.value.set(idsToLoad[index], result.data)
      }
    })
  } catch (error) {
    console.error('加载接口环境信息失败:', error)
  }
}

// 获取接口环境
const getInterfaceEnvironment = (interfaceId?: number): string | null => {
  if (!interfaceId) return null
  const interfaceInfo = interfaceMap.value.get(interfaceId)
  return interfaceInfo?.environment || null
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = undefined
  searchForm.interfaceId = undefined
  searchForm.success = undefined
  pagination.current = 1
  loadData()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.page
  pagination.pageSize = pag.pageSize
  loadData()
}

// 计算属性：参数列表
const urlParams = computed(() => interfaceData.value?.urlParams || [])
const headerParams = computed(() => interfaceData.value?.headerParams || [])
const bodyParams = computed(() => interfaceData.value?.bodyParams || [])

// 计算属性：判断是否有数据
const hasRequestParams = computed(() => {
  if (urlParams.value.length > 0 || bodyParams.value.length > 0) {
    return true
  }
  if (selectedRecord.value?.requestParams) {
    return true
  }
  return false
})

const hasRequestHeaders = computed(() => {
  if (headerParams.value.length > 0) {
    return true
  }
  if (selectedRecord.value?.requestHeaders) {
    return true
  }
  return false
})

const hasResponseBody = computed(() => {
  return !!(selectedRecord.value?.responseBody || selectedRecord.value?.responseHeaders)
})

// 查看详情
const handleViewDetail = async (record: ApiInterfaceExecutionRecord) => {
  try {
    const response = await executionRecordApi.getById(record.id)
    if (response.data) {
      selectedRecord.value = response.data
      detailVisible.value = true
      
      // 加载接口配置信息
      if (record.interfaceId) {
        try {
          const interfaceResponse = await interfaceApi.getById(record.interfaceId)
          interfaceData.value = interfaceResponse.data || null
        } catch (error) {
          console.error('加载接口配置失败:', error)
          interfaceData.value = null
        }
      } else {
        interfaceData.value = null
      }
      
      // 初始化编辑器内容
      const extractedValue = extractValueFromResponse(record.responseBody)
      if (extractedValue) {
        recordExtractedValueContent.value = extractedValue
      }
      if (record.responseBody) {
        recordResponseBodyContent.value = formatJson(record.responseBody)
      }
      
      // 设置默认激活的tab
      if (hasRequestParams.value) {
        detailActiveTab.value = 'request'
      } else if (hasRequestHeaders.value) {
        detailActiveTab.value = 'headers'
      } else if (hasResponseBody.value) {
        detailActiveTab.value = 'response'
      }
    }
  } catch (error: any) {
    message.error(error?.response?.data?.message || '获取详情失败')
  }
}

// 跳转到执行页
const handleGoToExecute = (interfaceId: number) => {
  if (interfaceId) {
    router.push(`/tools/interface/${interfaceId}/execute`)
    // 如果弹窗打开，关闭弹窗
    if (detailVisible.value) {
      detailVisible.value = false
    }
  } else {
    message.error('接口ID不存在')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  return dateTime
}

// 格式化JSON
const formatJson = (jsonStr?: string) => {
  if (!jsonStr) return ''
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

// 获取状态码颜色
const getStatusColor = (status: number) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 300 && status < 400) return 'warning'
  if (status >= 400) return 'error'
  return 'default'
}

// 获取行类名（用于奇偶行样式）
const getRowClassName = (_record: ApiInterfaceExecutionRecord, index: number) => {
  return index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

// 响应体相关数据
const recordExtractedValueContent = ref('')
const recordResponseBodyContent = ref('')

// 从响应体中提取值（根据valuePath）
const extractValueFromResponse = (responseBody: string | undefined): string => {
  if (!responseBody || !interfaceData.value?.valuePath) {
    return ''
  }
  
  try {
    const jsonData = JSON.parse(responseBody)
    // 简单的JSONPath实现，支持 $.key 格式
    const path = interfaceData.value.valuePath
    if (path.startsWith('$.')) {
      const keys = path.substring(2).split('.')
      let value = jsonData
      for (const key of keys) {
        if (value && typeof value === 'object' && key in value) {
          value = value[key]
        } else {
          return ''
        }
      }
      return typeof value === 'object' ? JSON.stringify(value, null, 2) : String(value)
    }
    return ''
  } catch (error) {
    console.error('提取值失败:', error)
    return ''
  }
}

// 计算属性：执行记录的提取值
const recordExtractedValue = computed(() => {
  if (!selectedRecord.value?.responseBody) {
    return ''
  }
  return extractValueFromResponse(selectedRecord.value.responseBody)
})

// 获取参数显示名称
const getParamDisplayName = (param: ApiParam): string => {
  if (param.chineseName) {
    return `${param.chineseName}（${param.name}）`
  }
  return param.name
}

// 从JSON字符串中获取参数值
const getRecordParamValue = (jsonString: string | undefined, paramName: string): string => {
  if (!jsonString) return ''
  
  try {
    const params = JSON.parse(jsonString)
    const value = params[paramName]
    
    if (value === undefined || value === null) {
      return ''
    }
    
    // 如果是对象或数组,格式化为JSON字符串
    if (typeof value === 'object') {
      return JSON.stringify(value, null, 2)
    }
    
    return String(value)
  } catch (error) {
    console.error('解析参数JSON失败:', error)
    return ''
  }
}

// 获取参数的显示值(用于只读输入框)
const getRecordParamDisplayValue = (jsonString: string | undefined, param: ApiParam): string => {
  if (!jsonString) return ''
  
  try {
    const params = JSON.parse(jsonString)
    const value = params[param.name]
    
    if (value === undefined || value === null) {
      return ''
    }
    
    // 多选下拉框显示
    if (param.inputType === 'MULTI_SELECT' && Array.isArray(value)) {
      // 尝试从选项中获取标签
      if (param.options && param.options.length > 0) {
        const labels = value.map(v => {
          const option = param.options?.find(opt => opt.value === v)
          return option?.label || v
        })
        return labels.join(', ')
      }
      return value.join(', ')
    }
    
    // 单选下拉框显示
    if (param.inputType === 'SELECT' && param.options && param.options.length > 0) {
      const option = param.options.find(opt => opt.value === value)
      return option?.label || String(value)
    }
    
    // 对象或数组显示为JSON字符串
    if (typeof value === 'object') {
      return JSON.stringify(value)
    }
    
    return String(value)
  } catch (error) {
    console.error('解析参数JSON失败:', error)
    return ''
  }
}

// 根据参数的dataType获取代码编辑器的语言
const getCodeLanguageForParam = (param: ApiParam): string => {
  const languageMapping: Record<string, string> = {
    'STRING': 'text',
    'INTEGER': 'text',
    'LONG': 'text', 
    'DOUBLE': 'text',
    'BOOLEAN': 'text',
    'DATE': 'text',
    'DATETIME': 'text',
    'JSON': 'json',
    'JSON_OBJECT': 'json',
    'ARRAY': 'json',
    'TEXT': 'text',
    'XML': 'xml',
    'HTML': 'html',
    'CSS': 'css',
    'JAVASCRIPT': 'javascript',
    'TYPESCRIPT': 'typescript',
    'JAVA': 'java',
    'KOTLIN': 'kotlin',
    'SQL': 'sql',
    'YAML': 'yaml'
  }
  return languageMapping[param.dataType] || 'text'
}

// 检测响应语言
const detectResponseLanguage = (body: string | undefined): string => {
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
  
  return 'text'
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.execution-record-list {
  min-height: 100%;
  background: #f0f2f5;
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.search-form .ant-form-item {
  margin-bottom: 16px;
}

/* 表格样式优化 */
.interface-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.interface-table :deep(.ant-table-container) {
  border-radius: 8px;
  overflow: hidden;
}

.interface-table :deep(.ant-table) {
  border-radius: 8px;
  table-layout: fixed;
}

.interface-table :deep(.ant-table-thead > tr > th) {
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  font-weight: 500;
  color: #333333;
  padding: 12px 8px;
  font-size: 13px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.interface-table :deep(.ant-table-thead > tr > th:first-child) {
  border-top-left-radius: 8px;
}

.interface-table :deep(.ant-table-thead > tr > th:last-child) {
  border-top-right-radius: 8px;
}

.interface-table :deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  min-width: 0;
}

.interface-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #fafafa !important;
}

.table-row-even {
  background-color: #ffffff;
}

.table-row-odd {
  background-color: #fafbfc;
}

/* 环境标签特殊样式 */
.interface-table :deep(.ant-table-tbody > tr .ant-tag) {
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
  padding: 2px 8px;
  border-width: 1px;
  border-style: solid;
}

/* 测试环境标签样式 */
.interface-table :deep(.ant-table-tbody > tr .ant-tag[color='blue']) {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #0050b3;
}

/* 生产环境标签样式 */
.interface-table :deep(.ant-table-tbody > tr .ant-tag[color='red']) {
  background: #fff2f0;
  border-color: #ffccc7;
  color: #cf1322;
  box-shadow: 0 1px 3px rgba(255, 77, 79, 0.2);
}

/* 分页样式 */
.interface-table :deep(.ant-pagination) {
  margin-top: 16px;
  text-align: right;
}

.interface-table :deep(.ant-pagination-item) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.interface-table :deep(.ant-pagination-item:hover) {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.interface-table :deep(.ant-pagination-item-active) {
  background: var(--primary-color, #1890ff);
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.3));
}

.interface-table :deep(.ant-pagination-item-active a) {
  color: white;
}

.interface-table :deep(.ant-pagination-prev),
.interface-table :deep(.ant-pagination-next) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.interface-table :deep(.ant-pagination-prev:hover),
.interface-table :deep(.ant-pagination-next:hover) {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

/* 分页跳转和每页条数选择器 */
.interface-table :deep(.ant-pagination-options) {
  margin-left: 16px;
}

.interface-table :deep(.ant-pagination-options-quick-jumper) {
  margin-left: 8px;
}

.interface-table :deep(.ant-pagination-options-quick-jumper input) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.interface-table :deep(.ant-pagination-options-quick-jumper input:hover) {
  border-color: var(--primary-color, #1890ff);
}

.interface-table :deep(.ant-pagination-options-quick-jumper input:focus) {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 2px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.interface-table :deep(.ant-select-selector) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.interface-table :deep(.ant-select:hover .ant-select-selector) {
  border-color: var(--primary-color, #1890ff);
}

.interface-table :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 2px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.detail-content .json-content {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  max-height: 400px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.6;
  margin: 0;
}

.detail-content .empty-content {
  text-align: center;
  color: #999;
  padding: 20px;
}

.detail-content .error-message {
  color: #ff4d4f;
  word-break: break-all;
}

.code-content {
  margin-top: 8px;
}

.text-muted {
  color: #999;
}

.detail-content {
  padding: 16px 0;
}

.param-section {
  margin-bottom: 24px;
}

.param-section h4 {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.record-params-container {
  background: #fafafa;
  border-radius: 4px;
  padding: 12px;
}

.record-param-row {
  margin-bottom: 12px;
}

.record-param-row:last-child {
  margin-bottom: 0;
}

.record-param-label {
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  color: #595959;
  margin-bottom: 0;
}

.record-param-label .required {
  color: #ff4d4f;
  margin-left: 2px;
}

.record-param-label .help-icon {
  margin-left: 4px;
  color: #8c8c8c;
  cursor: help;
}

.record-param-value {
  width: 100%;
}

.code-value-display {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}

.readonly-input {
  background: #f5f5f5;
}

.no-content {
  text-align: center;
  padding: 40px 20px;
}

/* 执行记录响应体样式 */
.extracted-value-section-record {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 2px solid #52c41a;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.15);
  overflow: hidden;
  position: relative;
}

.extracted-value-section-record::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 50%, #52c41a 100%);
}

.extracted-value-header-record {
  padding: 16px 16px 8px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.extracted-value-header-record h4 {
  margin: 0;
  color: #389e0d;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.extracted-value-header-record h4::before {
  content: '✨';
  font-size: 15px;
}

.value-path-badge {
  background: rgba(255, 255, 255, 0.9);
  color: #52c41a;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #b7eb8f;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  letter-spacing: 0.5px;
}

.extracted-value-content-record {
  background: #fff;
  margin: 0 12px 12px 12px;
  border-radius: 8px;
  border: 1px solid #b7eb8f;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.response-body-record {
  margin-top: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid #1890ff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15);
  overflow: hidden;
  position: relative;
}

.response-body-record::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #1890ff 0%, #40a9ff 50%, #1890ff 100%);
}

.response-body-header-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 8px 16px;
  background: transparent;
  border-bottom: 1px solid rgba(24, 144, 255, 0.1);
  transition: background-color 0.2s ease;
}

.response-header-right-record {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.response-body-header-record h4 {
  margin: 0;
  color: #0050b3;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.response-body-header-record h4::before {
  content: '📄';
  font-size: 15px;
}

.response-size-record {
  font-size: 12px;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 4px 12px;
  border-radius: 16px;
  border: 1px solid rgba(24, 144, 255, 0.2);
  font-weight: 500;
}

.response-body-content-record {
  background: #fff;
  margin: 0 12px 12px 12px;
  border-radius: 8px;
  border: 1px solid rgba(24, 144, 255, 0.1);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 弹窗底部按钮样式 */
.modal-footer-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 0;
}

.go-to-execute-btn {
  min-width: 140px;
  height: 36px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.go-to-execute-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color, rgba(24, 144, 255, 0.3));
}

/* 操作按钮样式 */
.interface-table :deep(.ant-table-tbody > tr > td .action-buttons) {
  text-align: center;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
  min-width: 120px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 6px;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 11px;
  height: 24px;
  font-weight: 500;
  white-space: nowrap;
  min-width: 50px;
  justify-content: center;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.view-btn {
  color: #1890ff;
}

.view-btn:hover {
  background-color: #1890ff;
  color: white;
}

.execute-btn {
  color: #722ed1;
}

.execute-btn:hover {
  background-color: #722ed1;
  color: white;
}
</style>

