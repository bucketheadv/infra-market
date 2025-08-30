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
      <a-form layout="inline" :model="searchForm" style="margin-bottom: 16px;">
        <a-form-item label="权限名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入权限名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="权限编码">
          <a-input
            v-model:value="searchForm.code"
            placeholder="请输入权限编码"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="权限类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="请选择权限类型"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option value="menu">菜单</a-select-option>
            <a-select-option value="button">按钮</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option value="active">启用</a-select-option>
            <a-select-option value="inactive">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            <SearchOutlined />
            搜索
          </a-button>
          <a-button style="margin-left: 8px;" @click="handleReset">
            <ReloadOutlined />
            重置
          </a-button>
        </a-form-item>
      </a-form>
      
      <!-- 权限表格 -->
      <a-table
        :columns="columns"
        :data-source="permissions"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="record.type === 'menu' ? 'blue' : 'green'">
              {{ record.type === 'menu' ? '菜单' : '按钮' }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'red'">
              {{ record.status === 'active' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                :danger="record.status === 'active'"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 'active' ? '禁用' : '启用' }}
              </a-button>
              <a-popconfirm
                title="确定要删除这个权限吗？"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>
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
} from '@ant-design/icons-vue'
import { permissionApi } from '@/api/permission'
import type { Permission, PageParams } from '@/types'

const router = useRouter()

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
  },
  {
    title: '权限名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '权限编码',
    dataIndex: 'code',
    key: 'code',
  },
  {
    title: '权限类型',
    dataIndex: 'type',
    key: 'type',
    width: 100,
  },
  {
    title: '路径',
    dataIndex: 'path',
    key: 'path',
  },
  {
    title: '图标',
    dataIndex: 'icon',
    key: 'icon',
    width: 80,
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort',
    width: 80,
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
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
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
  } catch (error) {
    message.error('获取权限列表失败')
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
  } catch (error) {
    console.error('状态更新失败:', error)
    message.error('状态更新失败')
  }
}

// 删除权限
const handleDelete = async (record: Permission) => {
  try {
    await permissionApi.deletePermission(record.id)
    message.success('删除成功')
    fetchPermissions()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchPermissions()
})
</script>

<style scoped>
.permission-list {
  padding: 24px;
}
</style>
