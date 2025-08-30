<template>
  <div class="dashboard">
    <a-row :gutter="24">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="用户总数"
            :value="dashboardData.statistics.userCount"
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
            :value="dashboardData.statistics.roleCount"
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
            :value="dashboardData.statistics.permissionCount"
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
            :value="dashboardData.statistics.onlineCount"
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
            :data-source="dashboardData.recentUsers"
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
            <a-descriptions-item label="系统版本">{{ dashboardData.systemInfo.systemVersion }}</a-descriptions-item>
            <a-descriptions-item label="Java版本">{{ dashboardData.systemInfo.javaVersion }}</a-descriptions-item>
            <a-descriptions-item label="Spring Boot版本">{{ dashboardData.systemInfo.springBootVersion }}</a-descriptions-item>
            <a-descriptions-item label="Kotlin版本">{{ dashboardData.systemInfo.kotlinVersion }}</a-descriptions-item>
            <a-descriptions-item label="最后更新">{{ dashboardData.systemInfo.lastUpdate }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  TeamOutlined,
  SafetyOutlined,
  GlobalOutlined,
} from '@ant-design/icons-vue'
import { dashboardApi, type DashboardData } from '@/api/dashboard'

const loading = ref(false)
const dashboardData = ref<DashboardData>({
  statistics: {
    userCount: 0,
    roleCount: 0,
    permissionCount: 0,
    onlineCount: 0,
  },
  recentUsers: [],
  systemInfo: {
    systemVersion: '',
    javaVersion: '',
    springBootVersion: '',
    kotlinVersion: '',
    lastUpdate: '',
  },
})

const fetchDashboardData = async () => {
  try {
    loading.value = true
    const response = await dashboardApi.getDashboardData()
    dashboardData.value = response.data
  } catch (error: any) {
    message.error(error.message || '获取仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
}
</style>
