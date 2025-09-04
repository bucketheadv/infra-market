<template>
  <div class="interface-list">
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
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="接口名称">
              <a-input
                v-model:value="searchForm.name"
                placeholder="请输入接口名称"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="请求方法">
              <a-select
                v-model:value="searchForm.method"
                placeholder="请选择请求方法"
                allow-clear
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
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="状态">
              <a-select
                v-model:value="searchForm.status"
                placeholder="请选择状态"
                allow-clear
              >
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="标签">
              <a-select
                v-model:value="searchForm.tag"
                placeholder="请选择标签"
                allow-clear
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
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
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
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'method'">
            <a-tag :color="getMethodColor(record.method)">
              {{ record.method }}
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
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined, PlayCircleOutlined, DeleteOutlined, CheckCircleOutlined, StopOutlined, CopyOutlined } from '@ant-design/icons-vue'
import { interfaceApi, HTTP_METHODS, TAGS, type ApiInterface } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const dataSource = ref<ApiInterface[]>([])

// 搜索表单
const searchForm = reactive({
  name: '',
  method: undefined,
  status: undefined,
  tag: undefined
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
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
    width: 120,
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
    title: '标签',
    dataIndex: 'tag',
    key: 'tag',
    width: 100,
    customRender: ({ record }: { record: ApiInterface }) => {
      if (!record.tag) return '-'
      const tagInfo = TAGS.find(t => t.value === record.tag)
      return h('a-tag', {
        color: record.tag === 'TEST' ? 'blue' : 'green'
      }, tagInfo?.label || record.tag)
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
    width: 150
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
    const response = await interfaceApi.getList({
      name: searchForm.name || undefined,
      method: searchForm.method || undefined,
      status: searchForm.status,
      tag: searchForm.tag || undefined
    })
    dataSource.value = response.data || []
    pagination.total = response.data?.length || 0
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.method = undefined
  searchForm.status = undefined
  searchForm.tag = undefined
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
const getRowClassName = (_record: ApiInterface, index: number) => {
  return index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.interface-list {
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

.interface-table :deep(.ant-table-tbody > tr > td) {
  text-align: center;
}

.interface-table :deep(.ant-table-tbody > tr > td .action-buttons) {
  text-align: center;
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
