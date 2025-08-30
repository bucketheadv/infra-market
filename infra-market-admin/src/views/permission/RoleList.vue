<template>
  <div class="role-list">
    <a-card>
      <template #title>角色管理</template>
      <template #extra>
        <a-button type="primary" @click="handleCreate">
          <PlusOutlined />
          创建角色
        </a-button>
      </template>
      
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" style="margin-bottom: 16px;">
        <a-form-item label="角色名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入角色名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="角色编码">
          <a-input
            v-model:value="searchForm.code"
            placeholder="请输入角色编码"
            allow-clear
          />
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
      
      <!-- 角色表格 -->
      <a-table
        :columns="columns"
        :data-source="roles"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
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
                title="确定要删除这个角色吗？"
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
import { roleApi } from '@/api/role'
import type { Role, PageParams } from '@/types'

const router = useRouter()

const loading = ref(false)
const roles = ref<Role[]>([])
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
    title: '角色名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '角色编码',
    dataIndex: 'code',
    key: 'code',
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
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

// 获取角色列表
const fetchRoles = async () => {
  loading.value = true
  try {
    const params: PageParams = {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm,
    }
    
    const response = await roleApi.getRoles(params)
    roles.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    message.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchRoles()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchRoles()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchRoles()
}

// 创建角色
const handleCreate = () => {
  router.push('/roles/create')
}

// 编辑角色
const handleEdit = (record: Role) => {
  router.push(`/roles/${record.id}/edit`)
}

// 切换状态
const handleToggleStatus = async (record: Role) => {
  try {
    const newStatus = record.status === 'active' ? 'inactive' : 'active'
    await roleApi.updateRoleStatus(record.id, newStatus)
    message.success('状态更新成功')
    fetchRoles()
  } catch (error) {
    message.error('状态更新失败')
  }
}

// 删除角色
const handleDelete = async (record: Role) => {
  try {
    await roleApi.deleteRole(record.id)
    message.success('删除成功')
    fetchRoles()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchRoles()
})
</script>

<style scoped>
.role-list {
  padding: 24px;
}
</style>
