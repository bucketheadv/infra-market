<template>
  <div class="activity-list">
    <a-card>
      <template #title>活动管理</template>
      <template #extra>
        <ThemeButton variant="primary" size="medium" :icon="PlusOutlined" @click="handleCreate">
          创建活动
        </ThemeButton>
      </template>
      
      <!-- 搜索表单 -->
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="活动名称">
              <a-input
                v-model:value="searchForm.name"
                placeholder="请输入活动名称"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="模板">
              <a-select
                v-model:value="searchForm.templateId"
                placeholder="请选择模板"
                allow-clear
                show-search
                :filter-option="filterTemplateOption"
              >
                <a-select-option
                  v-for="template in templates"
                  :key="template.id"
                  :value="template.id"
                >
                  {{ template.name }}
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
          <a-col :xs="24" :sm="24" :md="8" :lg="6">
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
      
      <!-- 活动表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        class="activity-table"
        :scroll="{ x: 1200 }"
        :row-class-name="getRowClassName"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
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
              <a-popconfirm
                title="确定要删除这个活动吗？"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import { activityApi, type Activity } from '@/api/activity'
import { activityTemplateApi, type ActivityTemplate } from '@/api/activityTemplate'
import ThemeButton from '@/components/ThemeButton.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const dataSource = ref<Activity[]>([])
const templates = ref<ActivityTemplate[]>([])

// 搜索表单
const searchForm = reactive({
  name: '',
  templateId: undefined as number | undefined,
  status: undefined as number | undefined
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

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 60,
    align: 'center'
  },
  {
    title: '活动名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
    ellipsis: true
  },
  {
    title: '模板名称',
    dataIndex: 'templateName',
    key: 'templateName',
    width: 180,
    ellipsis: true
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    align: 'center'
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    align: 'center'
  }
]

// 方法
const loadTemplates = async () => {
  try {
    const response = await activityTemplateApi.getAll()
    templates.value = response.data || []
  } catch (error) {
    console.error('加载模板列表失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const response = await activityApi.getList({
      name: searchForm.name || undefined,
      templateId: searchForm.templateId,
      status: searchForm.status,
      page: pagination.current,
      size: pagination.pageSize
    })
    dataSource.value = response.data?.records || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const filterTemplateOption = (input: string, option: any) => {
  return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.templateId = undefined
  searchForm.status = undefined
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const handleCreate = () => {
  router.push('/activity/create')
}

const handleEdit = (record: Activity) => {
  router.push(`/activity/${record.id}/edit`)
}

const handleToggleStatus = async (record: Activity) => {
  if (!record.id) return
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await activityApi.updateStatus(record.id, newStatus)
    message.success('状态更新成功')
    loadData()
  } catch (error) {
    message.error('状态更新失败')
  }
}

const handleDelete = async (record: Activity) => {
  if (!record.id) return
  try {
    await activityApi.delete(record.id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
  }
}

const getRowClassName = (_record: Activity, index: number) => {
  return index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

onMounted(() => {
  loadTemplates()
  loadData()
})
</script>

<style scoped lang="css">
.activity-list {
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
.activity-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.activity-table :deep(.ant-table-container) {
  border-radius: 8px;
  overflow: hidden;
}

.activity-table :deep(.ant-table) {
  border-radius: 8px;
  table-layout: fixed;
}

.activity-table :deep(.ant-table-thead > tr > th) {
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

.activity-table :deep(.ant-table-thead > tr > th:first-child) {
  border-top-left-radius: 8px;
}

.activity-table :deep(.ant-table-thead > tr > th:last-child) {
  border-top-right-radius: 8px;
}

.activity-table :deep(.ant-table-tbody > tr > td) {
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

.activity-table :deep(.ant-table-tbody > tr:hover > td) {
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

.delete-btn {
  color: #ff4d4f;
}

.delete-btn:hover {
  background-color: #ff4d4f;
  color: white;
}

/* 分页样式 */
.activity-table :deep(.ant-pagination) {
  margin-top: 16px;
  text-align: right;
}

.activity-table :deep(.ant-pagination-item) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.activity-table :deep(.ant-pagination-item:hover) {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.activity-table :deep(.ant-pagination-item-active) {
  background: var(--primary-color, #1890ff);
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.3));
}

.activity-table :deep(.ant-pagination-item-active a) {
  color: white;
}

.activity-table :deep(.ant-pagination-prev),
.activity-table :deep(.ant-pagination-next) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.activity-table :deep(.ant-pagination-prev:hover),
.activity-table :deep(.ant-pagination-next:hover) {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

/* 分页跳转和每页条数选择器 */
.activity-table :deep(.ant-pagination-options) {
  margin-left: 16px;
}

.activity-table :deep(.ant-pagination-options-quick-jumper) {
  margin-left: 8px;
}

.activity-table :deep(.ant-pagination-options-quick-jumper input) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.activity-table :deep(.ant-pagination-options-quick-jumper input:hover) {
  border-color: var(--primary-color, #1890ff);
}

.activity-table :deep(.ant-pagination-options-quick-jumper input:focus) {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 2px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.activity-table :deep(.ant-select-selector) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.activity-table :deep(.ant-select:hover .ant-select-selector) {
  border-color: var(--primary-color, #1890ff);
}

.activity-table :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 2px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.activity-table :deep(.ant-pagination-options .ant-select) {
  border-radius: 4px;
}

.activity-table :deep(.ant-pagination-options .ant-select:hover) {
  border-color: #1890ff;
}

.activity-table :deep(.ant-pagination-options .ant-input) {
  border-radius: 4px;
}

.activity-table :deep(.ant-pagination-options .ant-input:hover) {
  border-color: #1890ff;
}

@media (max-width: 768px) {
  .activity-list {
    padding: 16px;
  }
  
  .search-form {
    padding: 12px;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 12px;
  }
  
  /* 移动端表格优化 */
  .activity-table :deep(.ant-table) {
    font-size: 12px;
  }
  
  .activity-table :deep(.ant-table-thead > tr > th) {
    font-size: 12px;
    padding: 8px 4px;
  }
  
  .activity-table :deep(.ant-table-tbody > tr > td) {
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
