<template>
  <div class="dashboard">
    <a-row :gutter="24">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="用户总数"
            :value="statistics.userCount"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="角色总数"
            :value="statistics.roleCount"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="权限总数"
            :value="statistics.permissionCount"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <SafetyOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="在线用户"
            :value="statistics.onlineCount"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <GlobalOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="24" style="margin-top: 24px;">
      <a-col :span="12">
        <a-card title="最近登录用户">
          <a-list
            :data-source="recentUsers"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar>{{ item.username.charAt(0).toUpperCase() }}</a-avatar>
                  </template>
                  <template #title>{{ item.username }}</template>
                  <template #description>{{ item.lastLoginTime }}</template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      
      <a-col :span="12">
        <a-card title="系统信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="系统版本">v1.0.0</a-descriptions-item>
            <a-descriptions-item label="Node.js版本">v18.0.0</a-descriptions-item>
            <a-descriptions-item label="Vue版本">v3.3.4</a-descriptions-item>
            <a-descriptions-item label="Ant Design版本">v4.0.0</a-descriptions-item>
            <a-descriptions-item label="最后更新">2024-01-01</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  UserOutlined,
  TeamOutlined,
  SafetyOutlined,
  GlobalOutlined,
} from '@ant-design/icons-vue'
import type { User } from '@/types'

const loading = ref(false)
const statistics = ref({
  userCount: 0,
  roleCount: 0,
  permissionCount: 0,
  onlineCount: 0,
})

const recentUsers = ref<User[]>([])

onMounted(() => {
  // 模拟数据
  statistics.value = {
    userCount: 125,
    roleCount: 8,
    permissionCount: 45,
    onlineCount: 12,
  }
  
  recentUsers.value = [
    {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      status: 'active',
      createTime: '2024-01-01 10:00:00',
      updateTime: '2024-01-01 10:00:00',
    },
    {
      id: 2,
      username: 'user1',
      email: 'user1@example.com',
      status: 'active',
      createTime: '2024-01-01 09:30:00',
      updateTime: '2024-01-01 09:30:00',
    },
  ]
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
}
</style>
