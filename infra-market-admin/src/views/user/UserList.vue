<template>
  <div class="user-list">
    <a-card>
      <template #title>用户管理</template>
      <template #extra>
        <ThemeButton variant="primary" size="medium" :icon="PlusOutlined" @click="handleCreate">
          创建用户
        </ThemeButton>
      </template>
      
      <!-- 搜索表单 -->
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item label="用户名">
              <a-input
                v-model:value="searchForm.username"
                placeholder="请输入用户名"
                allow-clear
              />
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
          <a-col :xs="24" :sm="24" :md="8" :lg="12">
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
      
      <!-- 用户表格 -->
      <a-table
        :columns="columns"
        :data-source="users"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        class="user-table"
        :row-class-name="getRowClassName"
        :scroll="{ x: 1120 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'roles'">
            <div class="roles-display">
              <template v-if="record.roleIds && record.roleIds.length > 0">
                <a-tag 
                  v-for="roleId in record.roleIds" 
                  :key="roleId" 
                  color="blue" 
                  class="role-tag"
                >
                  {{ getRoleName(roleId) }}
                </a-tag>
              </template>
              <span v-else class="no-role">无角色</span>
            </div>
          </template>
          
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'red'" class="status-tag">
              {{ record.status === 'active' ? '启用' : '禁用' }}
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
              <a-popconfirm
                title="确定要重置该用户的密码吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleResetPassword(record)"
              >
                <ThemeButton 
                  variant="ghost" 
                  size="small" 
                  class="action-btn reset-btn"
                  :icon="KeyOutlined"
                >
                  重置密码
                </ThemeButton>
              </a-popconfirm>
              <ThemeButton
                variant="ghost"
                size="small"
                class="action-btn status-btn"
                :class="{ 'disable-btn': record.status === 'active' }"
                :icon="record.status === 'active' ? CheckCircleOutlined : StopOutlined"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 'active' ? '禁用' : '启用' }}
              </ThemeButton>
              <a-popconfirm
                title="确定要删除这个用户吗？"
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
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  StopOutlined,
  KeyOutlined,
} from '@ant-design/icons-vue'
import { userApi } from '@/api/user'
import { roleApi } from '@/api/role'
import ThemeButton from '@/components/ThemeButton.vue'
import type { User, Role, PageParams } from '@/types'

const router = useRouter()

const loading = ref(false)
const users = ref<User[]>([])
const roles = ref<Role[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
  pageSizeOptions: ['10', '20', '50', '100'],
  size: 'default',
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
  },
})

const searchForm = reactive({
  username: '',
  status: undefined as string | undefined,
})

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 60,
    align: 'center',
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 140,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 180,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 140,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '角色',
    dataIndex: 'roles',
    key: 'roles',
    width: 160,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
    align: 'center',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    width: 140,
    fixed: 'right',
    align: 'center',
  },
]

// 获取角色列表
const fetchRoles = async () => {
  try {
    const response = await roleApi.getRoles({ current: 1, size: 1000 })
    roles.value = response.data.records
  } catch (error: any) {
    console.error('获取角色列表失败:', error)
  }
}

// 根据角色ID获取角色名称
const getRoleName = (roleId: number) => {
  const role = roles.value.find(r => r.id === roleId)
  return role ? role.name : `角色${roleId}`
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params: PageParams = {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm,
    }
    
    const response = await userApi.getUsers(params)
    users.value = response.data.records
    pagination.total = response.data.total
  } catch (error: any) {
    message.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchUsers()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUsers()
}

// 创建用户
const handleCreate = () => {
  router.push('/system/users/create')
}

// 编辑用户
const handleEdit = (record: User) => {
  router.push(`/system/users/${record.id}/edit`)
}

// 重置密码
const handleResetPassword = async (record: User) => {
  try {
    const response = await userApi.resetPassword(record.id)
    Modal.success({
      title: '密码重置成功',
      content: `新密码：${response.data.password}`,
    })
  } catch (error: any) {
    message.error(error.message || '密码重置失败')
  }
}

// 切换状态
const handleToggleStatus = async (record: User) => {
  try {
    const newStatus = record.status === 'active' ? 'inactive' : 'active'
    await userApi.updateUserStatus(record.id, newStatus)
    message.success('状态更新成功')
    fetchUsers()
  } catch (error: any) {
    message.error(error.message || '状态更新失败')
  }
}

// 删除用户
const handleDelete = async (record: User) => {
  try {
    await userApi.deleteUser(record.id)
    message.success('删除成功')
    fetchUsers()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

// 获取行样式类名
const getRowClassName = (record: User, index: number) => {
  return index % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

onMounted(() => {
  fetchRoles()
  fetchUsers()
})
</script>

<style scoped>
.user-list {
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
.user-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-table :deep(.ant-table-container) {
  border-radius: 8px;
  overflow: hidden;
}

.user-table :deep(.ant-table) {
  border-radius: 8px;
  table-layout: fixed;
}



.user-table :deep(.ant-table-thead > tr > th) {
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
  min-width: 0;
}

.user-table :deep(.ant-table-thead > tr > th:first-child) {
  border-top-left-radius: 8px;
}

.user-table :deep(.ant-table-thead > tr > th:last-child) {
  border-top-right-radius: 8px;
}

.user-table :deep(.ant-table-tbody > tr > td) {
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

/* 表格内容居中显示 */
.user-table :deep(.ant-table-tbody > tr > td) {
  text-align: center;
}

.user-table :deep(.ant-table-tbody > tr > td .action-buttons) {
  text-align: center;
}

.user-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #fafafa !important;
}

.table-row-even {
  background-color: #ffffff;
}

.table-row-odd {
  background-color: #fafbfc;
}

/* 标签样式优化 */
.status-tag {
  border-radius: 6px;
  font-weight: 500;
  padding: 4px 8px;
  font-size: 12px;
}

/* 角色显示样式 */
.roles-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
  align-items: center;
}

.role-tag {
  border-radius: 4px;
  font-size: 11px;
  padding: 2px 6px;
  margin: 0;
  white-space: nowrap;
}

.no-role {
  color: #999;
  font-size: 12px;
  font-style: italic;
}

/* 操作按钮美化 */
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

.reset-btn {
  color: #722ed1;
}

.reset-btn:hover {
  background-color: #722ed1;
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

/* 分页样式优化 */
.user-table :deep(.ant-pagination) {
  margin-top: 16px;
  text-align: right;
}

.user-table :deep(.ant-pagination-item) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.user-table :deep(.ant-pagination-item:hover) {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.user-table :deep(.ant-pagination-item-active) {
  background: var(--primary-color, #1890ff);
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.3));
}

.user-table :deep(.ant-pagination-item-active a) {
  color: white;
}

.user-table :deep(.ant-pagination-prev),
.user-table :deep(.ant-pagination-next) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.user-table :deep(.ant-pagination-prev:hover),
.user-table :deep(.ant-pagination-next:hover) {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

/* 分页跳转和每页条数选择器 */
.user-table :deep(.ant-pagination-options) {
  margin-left: 16px;
}

.user-table :deep(.ant-pagination-options-quick-jumper) {
  margin-left: 8px;
}

.user-table :deep(.ant-pagination-options-quick-jumper input) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.user-table :deep(.ant-pagination-options-quick-jumper input:hover) {
  border-color: var(--primary-color, #1890ff);
}

.user-table :deep(.ant-pagination-options-quick-jumper input:focus) {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 2px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.user-table :deep(.ant-select-selector) {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.user-table :deep(.ant-select:hover .ant-select-selector) {
  border-color: var(--primary-color, #1890ff);
}

.user-table :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 2px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.user-table :deep(.ant-pagination-options .ant-select) {
  border-radius: 4px;
}

.user-table :deep(.ant-pagination-options .ant-select:hover) {
  border-color: #1890ff;
}

.user-table :deep(.ant-pagination-options .ant-input) {
  border-radius: 4px;
}

.user-table :deep(.ant-pagination-options .ant-input:hover) {
  border-color: #1890ff;
}

@media (max-width: 768px) {
  .user-list {
    padding: 16px;
  }
  
  .search-form {
    padding: 12px;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 12px;
  }
  
  .user-table :deep(.ant-table-thead > tr > th),
  .user-table :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
  }
  
  /* 移动端表格优化 */
  .user-table :deep(.ant-table) {
    font-size: 12px;
  }
  
  .user-table :deep(.ant-table-thead > tr > th) {
    font-size: 12px;
    padding: 8px 4px;
  }
  
  .user-table :deep(.ant-table-tbody > tr > td) {
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
  
  .roles-display {
    gap: 2px;
  }
  
  .role-tag {
    font-size: 10px;
    padding: 1px 4px;
  }
}
</style>
