<template>
  <div class="permission-list">
    <a-card>
      <template #title>权限管理</template>
      <template #extra>
        <a-button type="primary" @click="handleCreate">
          <PlusOutlined />
          创建权限
        </a-button>
      </template>
      
      <!-- 搜索表单 -->
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="权限名称">
              <a-input
                v-model:value="searchForm.name"
                placeholder="请输入权限名称"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="权限编码">
              <a-input
                v-model:value="searchForm.code"
                placeholder="请输入权限编码"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="权限类型">
              <a-select
                v-model:value="searchForm.type"
                placeholder="请选择权限类型"
                allow-clear
              >
                <a-select-option value="menu">菜单</a-select-option>
                <a-select-option value="button">按钮</a-select-option>
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
                <a-select-option value="active">启用</a-select-option>
                <a-select-option value="inactive">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="24" :md="24" :lg="24">
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleSearch">
                  <SearchOutlined />
                  搜索
                </a-button>
                <a-button @click="handleReset">
                  <ReloadOutlined />
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      
      <!-- 权限表格 -->
      <a-table
        :columns="columns"
        :data-source="permissions"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        class="permission-table"
        :row-class-name="getRowClassName"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="record.type === 'menu' ? 'blue' : 'green'" class="type-tag">
              {{ record.type === 'menu' ? '菜单' : '按钮' }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'icon'">
            <component v-if="record.icon && iconMap[record.icon]" :is="iconMap[record.icon]" class="table-icon" />
            <span v-else class="no-icon">-</span>
          </template>
          
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'red'" class="status-tag">
              {{ record.status === 'active' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button 
                type="link" 
                size="small" 
                class="action-btn edit-btn"
                @click="handleEdit(record)"
              >
                <EditOutlined />
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                class="action-btn status-btn"
                :class="{ 'disable-btn': record.status === 'active' }"
                @click="handleToggleStatus(record)"
              >
                <CheckCircleOutlined v-if="record.status === 'active'" />
                <StopOutlined v-else />
                {{ record.status === 'active' ? '禁用' : '启用' }}
              </a-button>
              <a-popconfirm
                title="确定要删除这个权限吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button 
                  type="link" 
                  size="small" 
                  class="action-btn delete-btn"
                >
                  <DeleteOutlined />
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
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
  SettingOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  EditOutlined,
  CheckCircleOutlined,
  StopOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { permissionApi } from '@/api/permission'
import type { Permission, PageParams } from '@/types'

const router = useRouter()

// 图标映射
const iconMap: Record<string, any> = {
  'SettingOutlined': SettingOutlined,
  'UserOutlined': UserOutlined,
  'TeamOutlined': TeamOutlined,
  'SafetyCertificateOutlined': SafetyCertificateOutlined,
}

const loading = ref(false)
const permissions = ref<Permission[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
})

const searchForm = reactive({
  name: '',
  code: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined,
})

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: '权限名称',
    dataIndex: 'name',
    key: 'name',
    width: 300,
  },
  {
    title: '权限编码',
    dataIndex: 'code',
    key: 'code',
    width: 180,
  },
  {
    title: '权限类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '路径',
    dataIndex: 'path',
    key: 'path',
    width: 200,
  },
  {
    title: '图标',
    dataIndex: 'icon',
    key: 'icon',
    width: 120,
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort',
    width: 150,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 220,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 获取权限列表
const fetchPermissions = async () => {
  loading.value = true
  try {
    const params: PageParams = {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm,
    }
    
    const response = await permissionApi.getPermissions(params)
    permissions.value = response.data.records
    pagination.total = response.data.total
  } catch (error: any) {
    message.error(error.message || '获取权限列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchPermissions()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.type = undefined
  searchForm.status = undefined
  pagination.current = 1
  fetchPermissions()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchPermissions()
}

// 创建权限
const handleCreate = () => {
  router.push('/system/permissions/create')
}

// 编辑权限
const handleEdit = (record: Permission) => {
  router.push(`/system/permissions/${record.id}/edit`)
}

// 切换状态
const handleToggleStatus = async (record: Permission) => {
  try {
    console.log('切换状态 - 权限ID:', record.id, '权限信息:', record)
    const newStatus = record.status === 'active' ? 'inactive' : 'active'
    await permissionApi.updatePermissionStatus(record.id, newStatus)
    message.success('状态更新成功')
    fetchPermissions()
  } catch (error: any) {
    console.error('状态更新失败:', error)
    message.error(error.message || '状态更新失败')
  }
}

// 删除权限
const handleDelete = async (record: Permission) => {
  try {
    await permissionApi.deletePermission(record.id)
    message.success('删除成功')
    fetchPermissions()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

// 获取行样式类名
const getRowClassName = (record: Permission, index: number) => {
  return index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

onMounted(() => {
  fetchPermissions()
})
</script>

<style scoped>
.permission-list {
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
.permission-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.permission-table :deep(.ant-table) {
  border-radius: 8px;
}

.permission-table :deep(.ant-table-thead > tr > th) {
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  font-weight: 500;
  color: #333333;
  padding: 16px 12px;
  font-size: 14px;
  text-align: center;
}

.permission-table :deep(.ant-table-thead > tr > th:first-child) {
  border-top-left-radius: 8px;
}

.permission-table :deep(.ant-table-thead > tr > th:last-child) {
  border-top-right-radius: 8px;
}

.permission-table :deep(.ant-table-tbody > tr > td) {
  padding: 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  text-align: left;
}

/* ID列居中显示 */
.permission-table :deep(.ant-table-tbody > tr > td:first-child) {
  text-align: center;
}

.permission-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #fafafa !important;
}

.table-row-even {
  background-color: #ffffff;
}

.table-row-odd {
  background-color: #fafbfc;
}

/* 标签样式优化 */
.type-tag {
  border-radius: 6px;
  font-weight: 500;
  padding: 4px 8px;
  font-size: 12px;
}

.status-tag {
  border-radius: 6px;
  font-weight: 500;
  padding: 4px 8px;
  font-size: 12px;
}

/* 图标样式 */
.table-icon {
  font-size: 18px;
  color: #1890ff;
}

.no-icon {
  color: #d9d9d9;
  font-size: 14px;
}

/* 操作按钮美化 */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 12px;
  height: 32px;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.edit-btn {
  color: #1890ff;
}

.edit-btn:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-btn {
  color: #52c41a;
}

.status-btn:hover {
  background-color: #f6ffed;
  color: #52c41a;
}

.disable-btn {
  color: #faad14;
}

.disable-btn:hover {
  background-color: #fffbe6;
  color: #faad14;
}

.delete-btn {
  color: #ff4d4f;
}

.delete-btn:hover {
  background-color: #fff2f0;
  color: #ff4d4f;
}

/* 分页样式优化 */
.permission-table :deep(.ant-pagination) {
  margin-top: 16px;
  text-align: right;
}

.permission-table :deep(.ant-pagination-item) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.permission-table :deep(.ant-pagination-item:hover) {
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

.permission-table :deep(.ant-pagination-item-active) {
  background: #1890ff;
  border-color: #1890ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
}

.permission-table :deep(.ant-pagination-item-active a) {
  color: white;
}

.permission-table :deep(.ant-pagination-prev),
.permission-table :deep(.ant-pagination-next) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.permission-table :deep(.ant-pagination-prev:hover),
.permission-table :deep(.ant-pagination-next:hover) {
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

.permission-table :deep(.ant-pagination-options) {
  margin-left: 16px;
}

.permission-table :deep(.ant-pagination-options .ant-select) {
  border-radius: 4px;
}

.permission-table :deep(.ant-pagination-options .ant-select:hover) {
  border-color: #1890ff;
}

.permission-table :deep(.ant-pagination-options .ant-input) {
  border-radius: 4px;
}

.permission-table :deep(.ant-pagination-options .ant-input:hover) {
  border-color: #1890ff;
}



@media (max-width: 768px) {
  .permission-list {
    padding: 16px;
  }
  
  .search-form {
    padding: 12px;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 12px;
  }
  
  .permission-table :deep(.ant-table-thead > tr > th),
  .permission-table :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
  }
  

}
</style>
