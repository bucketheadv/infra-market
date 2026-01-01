<template>
  <div class="interface-list">
    <!-- 您可能想要找 -->
    <a-card v-if="mostUsedInterfaces.length > 0" class="recommend-card">
      <template #title>
        <span class="recommend-title">
          <FireOutlined style="color: #ff4d4f; margin-right: 8px;" />
          您可能想要找
        </span>
      </template>
      <div class="recommend-content">
        <div 
          v-for="item in mostUsedInterfaces" 
          :key="item.id" 
          class="recommend-item"
          @click="handleExecute(item)"
        >
          <div class="recommend-item-header">
            <a-tag :color="getMethodColor(item.method)" style="margin-right: 8px;">
              {{ item.method }}
            </a-tag>
            <span class="recommend-item-name">{{ item.name }}</span>
            <a-tag 
              v-if="item.environment"
              :color="item.environment === 'PRODUCTION' ? 'red' : 'blue'"
              style="margin-left: 8px; font-size: 11px;"
            >
              {{ getTagLabel(item.environment) }}
            </a-tag>
          </div>
          <div class="recommend-item-url">{{ item.url }}</div>
          <div v-if="item.description" class="recommend-item-desc">{{ item.description }}</div>
        </div>
      </div>
    </a-card>

    <a-card>
      <template #title>接口管理</template>
      <template #extra>
        <ThemeButton variant="primary" size="medium" :icon="PlusOutlined" @click="handleCreate">
          创建接口
        </ThemeButton>
      </template>
      
      <!-- 搜索表单 -->
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="10" :lg="10">
            <a-form-item label="接口名称">
              <a-auto-complete
                v-model:value="searchForm.name"
                :options="interfaceNameOptions"
                placeholder="请输入接口名称"
                allow-clear
                :filter-option="false"
                class="interface-name-autocomplete"
                popup-class-name="interface-name-dropdown"
                @search="handleSearchInterfaceName"
                @select="handleSelectInterfaceName"
                @change="handleNameChange"
              >
                <template #option="{ label, environment }">
                  <div class="interface-option">
                    <span class="interface-option-name">{{ label }}</span>
                    <a-tag 
                      v-if="environment"
                      :color="environment === 'PRODUCTION' ? 'red' : 'blue'"
                      size="small"
                      class="interface-option-env-tag"
                    >
                      {{ environment === 'PRODUCTION' ? '🚨 正式' : '🧪 测试' }}
                    </a-tag>
                  </div>
                </template>
              </a-auto-complete>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="5" :lg="5">
            <a-form-item label="请求方法">
              <a-select
                v-model:value="searchForm.method"
                placeholder="请选择"
                allow-clear
                @change="handleConditionChange"
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
          </a-col>
          <a-col :xs="24" :sm="12" :md="4" :lg="4">
            <a-form-item label="状态">
              <a-select
                v-model:value="searchForm.status"
                placeholder="请选择"
                allow-clear
                @change="handleConditionChange"
              >
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="5" :lg="5">
            <a-form-item label="环境">
              <a-select
                v-model:value="searchForm.environment"
                placeholder="请选择"
                allow-clear
                @change="handleConditionChange"
              >
                <template #suffixIcon>
                  <EnvironmentOutlined />
                </template>
                <a-select-option
                  v-for="tag in TAGS"
                  :key="tag.value"
                  :value="tag.value"
                >
                  <span style="margin-right: 6px;">
                    {{ tag.value === 'PRODUCTION' ? '🚨' : '🧪' }}
                  </span>
                  {{ tag.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="24" :md="24" :lg="24">
            <a-form-item>
              <a-space>
                <ThemeButton variant="primary" size="medium" :icon="SearchOutlined" @click="handleSearch">
                  搜索
                </ThemeButton>
                <ThemeButton variant="secondary" size="medium" :icon="ReloadOutlined" @click="handleReset">
                  重置
                </ThemeButton>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <!-- 接口表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        class="interface-table"
        :row-class-name="getRowClassName"
        :scroll="{ x: 1000 }"
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
          <template v-if="column.key === 'method'">
            <a-tag :color="getMethodColor(record.method)">
              {{ record.method }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'environment'">
            <a-tag 
              :color="record.environment === 'PRODUCTION' ? 'red' : 'blue'"
              :style="{
                fontWeight: '600',
                fontSize: '12px',
                padding: '2px 8px',
                borderRadius: '4px',
                border: record.environment === 'PRODUCTION' ? '1px solid #ff4d4f' : '1px solid #1890ff'
              }"
            >
              <span style="margin-right: 4px;">
                {{ record.environment === 'PRODUCTION' ? '🚨' : '🧪' }}
              </span>
              {{ getTagLabel(record.environment) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ formatDateTime(record.updateTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-buttons">
              <ThemeButton 
                variant="ghost" 
                size="small" 
                class="action-btn edit-btn"
                :icon="EditOutlined"
                @click="handleEdit(record)"
              >
                编辑
              </ThemeButton>
              <a-popconfirm
                title="确定要复制这个接口吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleCopy(record)"
              >
                <ThemeButton 
                  variant="ghost" 
                  size="small" 
                  class="action-btn copy-btn"
                  :icon="CopyOutlined"
                >
                  复制
                </ThemeButton>
              </a-popconfirm>
              <ThemeButton
                variant="ghost"
                size="small"
                class="action-btn status-btn"
                :class="{ 'disable-btn': record.status === 1 }"
                :icon="record.status === 1 ? CheckCircleOutlined : StopOutlined"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </ThemeButton>
              <ThemeButton 
                variant="ghost" 
                size="small" 
                class="action-btn execute-btn"
                :icon="PlayCircleOutlined"
                @click="handleExecute(record)"
              >
                执行
              </ThemeButton>
              <a-popconfirm
                title="确定要删除这个接口吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <ThemeButton 
                  variant="ghost" 
                  size="small" 
                  class="action-btn delete-btn"
                  :icon="DeleteOutlined"
                >
                  删除
                </ThemeButton>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined, PlayCircleOutlined, DeleteOutlined, CheckCircleOutlined, StopOutlined, CopyOutlined, EnvironmentOutlined, FireOutlined } from '@ant-design/icons-vue'
import { interfaceApi, HTTP_METHODS, TAGS, type ApiInterface } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const dataSource = ref<ApiInterface[]>([])
const mostUsedInterfaces = ref<ApiInterface[]>([])
const interfaceNameOptions = ref<Array<{ value: string, label: string, method: string, environment?: string, interfaceId: number }>>([])

// 搜索表单
const searchForm = reactive({
  name: '',
  method: undefined,
  status: undefined,
  environment: undefined,
  selectedId: undefined as number | undefined
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
  showSizeChange: true,
  size: 'small',
  locale: {
    items_per_page: '条/页',
    jump_to: '跳至',
    jump_to_confirm: '确定',
    page: '页',
    prev_page: '上一页',
    next_page: '下一页',
    prev_5: '向前 5 页',
    next_5: '向后 5 页',
    prev_3: '向前 3 页',
    next_3: '向后 3 页',
  }
})

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 60,
    align: 'center'
  },
  {
    title: '接口名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
    ellipsis: true
  },
  {
    title: '请求方法',
    dataIndex: 'method',
    key: 'method',
    width: 80
  },
  {
    title: '接口URL',
    dataIndex: 'url',
    key: 'url',
    width: 200,
    ellipsis: true
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    width: 120,
    ellipsis: true
  },
  {
    title: '环境',
    dataIndex: 'environment',
    key: 'environment',
    width: 100,
    customRender: ({ record }: { record: ApiInterface }) => {
      if (!record.environment) return h('span', { style: 'color: #999;' }, '未设置')
      const tagInfo = TAGS.find(t => t.value === record.environment)
      const isProduction = record.environment === 'PRODUCTION'
      return h('a-tag', {
        color: isProduction ? 'red' : 'blue',
        style: {
          fontWeight: '600',
          fontSize: '12px',
          padding: '2px 8px',
          borderRadius: '4px',
          border: isProduction ? '1px solid #ff4d4f' : '1px solid #1890ff'
        }
      }, [
        h('span', { style: 'margin-right: 4px;' }, isProduction ? '🚨' : '🧪'),
        tagInfo?.label || record.environment
      ])
    }
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
    customRender: ({ record }: { record: ApiInterface }) => {
      return h('a-tag', {
        color: record.status === 1 ? 'green' : 'red'
      }, record.status === 1 ? '启用' : '禁用')
    }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 方法
const loadData = async () => {
  loading.value = true
  try {
    // 如果选择了下拉框中的接口，则根据ID精确查询
    if (searchForm.selectedId) {
      const response = await interfaceApi.getById(searchForm.selectedId)
      dataSource.value = response.data ? [response.data] : []
      pagination.total = response.data ? 1 : 0
    } else {
      // 否则按条件模糊查询
      const response = await interfaceApi.getList({
        name: searchForm.name || undefined,
        method: searchForm.method || undefined,
        status: searchForm.status,
        environment: searchForm.environment || undefined,
        page: pagination.current,
        size: pagination.pageSize
      })
      dataSource.value = response.data?.records || []
      pagination.total = response.data?.total || 0
    }
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadMostUsedInterfaces = async () => {
  try {
    const response = await interfaceApi.getMostUsed({
      days: 30,
      limit: 6
    })
    mostUsedInterfaces.value = response.data || []
  } catch (error) {
    // 静默失败，不影响主页面
    console.error('加载热门接口失败', error)
  }
}

const handleSearch = () => {
  // 如果有其他搜索条件，清空selectedId使用条件搜索（双重保险）
  if (searchForm.method || searchForm.status !== undefined || searchForm.environment) {
    searchForm.selectedId = undefined
  }
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.method = undefined
  searchForm.status = undefined
  searchForm.environment = undefined
  searchForm.selectedId = undefined
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const handleCreate = () => {
  router.push('/tools/interface/create')
}


const handleEdit = (record: ApiInterface) => {
  router.push(`/tools/interface/${record.id}/edit`)
}

const handleCopy = async (record: ApiInterface) => {
  try {
    await interfaceApi.copy(record.id!)
    message.success('接口复制成功')
    loadData()
  } catch (error) {
    message.error('接口复制失败')
  }
}

const handleDelete = async (record: ApiInterface) => {
  try {
    await interfaceApi.delete(record.id!)
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleExecute = (record: ApiInterface) => {
  router.push(`/tools/interface/${record.id}/execute`)
}

const handleToggleStatus = async (record: ApiInterface) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await interfaceApi.updateStatus(record.id!, newStatus)
    message.success(newStatus === 1 ? '启用成功' : '禁用成功')
    loadData()
  } catch (error) {
    message.error('状态更新失败')
  }
}

const handleSearchInterfaceName = async (searchText: string) => {
  if (!searchText || searchText.trim() === '') {
    interfaceNameOptions.value = []
    // 清空选中的ID，恢复模糊搜索
    searchForm.selectedId = undefined
    return
  }
  
  try {
    const response = await interfaceApi.getList({
      name: searchText,
      page: 1,
      size: 10
    })
    interfaceNameOptions.value = response.data?.records?.map((item: ApiInterface) => {
      const envTag = item.environment === 'PRODUCTION' ? '🚨正式' : '🧪测试'
      // value 使用格式: "接口名称 [环境 #ID]" 确保唯一且用户友好
      const displayValue = `${item.name} [${envTag} #${item.id}]`
      return {
        value: displayValue,
        label: item.name,
        method: item.method,
        environment: item.environment,
        interfaceId: item.id!
      }
    }) || []
  } catch (error) {
    console.error('搜索接口名称失败', error)
  }
}

const handleSelectInterfaceName = async (_value: string, option: any) => {
  // 从 option 中直接获取 interfaceId
  searchForm.selectedId = option.interfaceId
  // 使用 nextTick 确保在 DOM 更新后设置显示的名称
  await nextTick()
  // 设置显示的名称（仅显示接口名称，不带环境和ID）
  searchForm.name = option.label
  // 立即触发查询
  handleSearch()
}

const handleNameChange = (value: string) => {
  // 当用户手动修改输入内容时，清空选中的ID
  if (!value) {
    searchForm.selectedId = undefined
  } else {
    // 如果值不包含方括号，说明是手动输入
    // 清空选中的ID，使用名称模糊搜索
    if (!value.includes('[')) {
      searchForm.selectedId = undefined
    }
  }
}

const handleConditionChange = () => {
  // 当用户修改其他搜索条件时，清空选中的接口ID
  // 这样就会使用条件搜索而不是ID精确查询
  if (searchForm.selectedId) {
    searchForm.selectedId = undefined
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
  return colors[method] || 'blue'
}

// 获取表格行样式类名
const getRowClassName = (record: ApiInterface, index: number) => {
  let className = index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
  if (record.environment === 'PRODUCTION') {
    className += ' production-row'
  }
  return className
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  // 后端返回的是 "yyyy-MM-dd HH:mm:ss" 格式的字符串，直接返回即可
  return dateTime
}

// 获取标签显示名称
const getTagLabel = (tag: string) => {
  if (!tag) return '未设置'
  const tagInfo = TAGS.find(t => t.value === tag)
  return tagInfo?.label || tag
}

// 生命周期
onMounted(() => {
  loadData()
  loadMostUsedInterfaces()
})
</script>

<style scoped>
.interface-list {
  min-height: 100%;
  background: #f0f2f5;
  padding: 24px;
}

/* 推荐卡片样式 */
.recommend-card {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  background: linear-gradient(135deg, #fff9e6 0%, #ffffff 100%);
  border: 1px solid #ffd666;
}

.recommend-card :deep(.ant-card-head) {
  border-bottom: 1px solid #ffe58f;
  background: linear-gradient(135deg, #fffbe6 0%, #ffffff 100%);
}

.recommend-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.recommend-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.recommend-item {
  padding: 12px;
  border-radius: 6px;
  background: white;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommend-item:hover {
  border-color: #ffd666;
  box-shadow: 0 4px 12px rgba(255, 214, 102, 0.3);
  transform: translateY(-2px);
}

.recommend-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.recommend-item-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-item-url {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-item-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .recommend-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .recommend-content {
    grid-template-columns: 1fr;
  }
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

.interface-table :deep(.ant-table-tbody > tr > td) {
  text-align: center;
}

.interface-table :deep(.ant-table-tbody > tr > td .action-buttons) {
  text-align: center;
}

/* 生产环境行特殊样式 */
.interface-table :deep(.ant-table-tbody > tr.production-row) {
  background: linear-gradient(90deg, #fff2f0 0%, #ffffff 100%);
  border-left: 3px solid #ff4d4f;
}

.interface-table :deep(.ant-table-tbody > tr.production-row:hover) {
  background: linear-gradient(90deg, #ffebe6 0%, #f6f8fa 100%) !important;
}

.interface-table :deep(.ant-table-tbody > tr.production-row > td) {
  border-bottom: 1px solid #ffccc7;
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
.interface-table :deep(.ant-table-tbody > tr .ant-tag[color="blue"]) {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #0050b3;
}

/* 生产环境标签样式 */
.interface-table :deep(.ant-table-tbody > tr .ant-tag[color="red"]) {
  background: #fff2f0;
  border-color: #ffccc7;
  color: #cf1322;
  box-shadow: 0 1px 3px rgba(255, 77, 79, 0.2);
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

/* 操作按钮样式 */
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


.edit-btn {
  color: #1890ff;
}

.edit-btn:hover {
  background-color: #1890ff;
  color: white;
}

.copy-btn {
  color: #52c41a;
}

.copy-btn:hover {
  background-color: #52c41a;
  color: white;
}

.status-btn {
  color: #52c41a;
}

.status-btn:hover {
  background-color: #52c41a;
  color: white;
}

.disable-btn {
  color: #faad14;
}

.disable-btn:hover {
  background-color: #faad14;
  color: white;
}

.execute-btn {
  color: #722ed1;
}

.execute-btn:hover {
  background-color: #722ed1;
  color: white;
}

.delete-btn {
  color: #ff4d4f;
}

.delete-btn:hover {
  background-color: #ff4d4f;
  color: white;
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

.interface-table :deep(.ant-pagination-options .ant-select) {
  border-radius: 4px;
}

.interface-table :deep(.ant-pagination-options .ant-select:hover) {
  border-color: #1890ff;
}

.interface-table :deep(.ant-pagination-options .ant-input) {
  border-radius: 4px;
}

.interface-table :deep(.ant-pagination-options .ant-input:hover) {
  border-color: #1890ff;
}

/* 接口名称下拉选项样式 */
.interface-option {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.interface-option-name {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 280px;
  min-width: 0;
}

.interface-option-env-tag {
  flex-shrink: 0;
  font-size: 11px !important;
  margin-left: 8px;
}

@media (max-width: 768px) {
  .interface-list {
    padding: 16px;
  }
  
  .search-form {
    padding: 12px;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 12px;
  }
  
  /* 移动端表格优化 */
  .interface-table :deep(.ant-table) {
    font-size: 12px;
  }
  
  .interface-table :deep(.ant-table-thead > tr > th) {
    font-size: 12px;
    padding: 8px 4px;
  }
  
  .interface-table :deep(.ant-table-tbody > tr > td) {
    font-size: 12px;
    padding: 8px 4px;
  }
  
  .action-btn {
    font-size: 9px;
    padding: 2px 4px;
    height: 20px;
    min-width: 40px;
  }
  
  .action-buttons {
    gap: 1px;
    min-width: 100px;
  }
}
</style>

<style>
/* 接口名称下拉框全局样式 */
.interface-name-dropdown {
  min-width: 400px !important;
}

.interface-name-dropdown .ant-select-item {
  padding: 8px 12px;
}

.interface-name-dropdown .interface-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
</style>
