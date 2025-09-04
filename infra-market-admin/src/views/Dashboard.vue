<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1 class="banner-title">欢迎回来！</h1>
        <p class="banner-subtitle">基础设施市场管理系统</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-circle"></div>
        <div class="decoration-circle"></div>
        <div class="decoration-circle"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-card-users">
        <div class="stat-icon">
          <UserOutlined />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.statistics.userCount }}</div>
          <div class="stat-label">用户总数</div>
          <div class="stat-trend">
            <span class="trend-text">较昨日</span>
            <span class="trend-value positive">+12%</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-card-roles">
        <div class="stat-icon">
          <TeamOutlined />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.statistics.roleCount }}</div>
          <div class="stat-label">角色总数</div>
          <div class="stat-trend">
            <span class="trend-text">较昨日</span>
            <span class="trend-value positive">+5%</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-card-permissions">
        <div class="stat-icon">
          <SafetyOutlined />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.statistics.permissionCount }}</div>
          <div class="stat-label">权限总数</div>
          <div class="stat-trend">
            <span class="trend-text">较昨日</span>
            <span class="trend-value neutral">0%</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-card-online">
        <div class="stat-icon">
          <GlobalOutlined />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.statistics.onlineCount }}</div>
          <div class="stat-label">在线用户</div>
          <div class="stat-trend">
            <span class="trend-text">实时</span>
            <span class="trend-indicator online"></span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <div class="content-grid">
      <!-- 最近登录用户 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">
            <ClockCircleOutlined class="card-title-icon" />
            最近登录用户
          </h3>
        </div>
        <div class="card-content">
          <a-list
            :data-source="dashboardData.recentUsers"
            :loading="loading"
            class="user-list"
          >
            <template #renderItem="{ item }">
              <a-list-item class="user-list-item">
                <a-list-item-meta>
                  <template #avatar>
                    <div class="user-avatar">
                      {{ item.username.charAt(0).toUpperCase() }}
                    </div>
                  </template>
                  <template #title>
                    <span class="username">{{ item.username }}</span>
                  </template>
                  <template #description>
                    <span class="login-time">{{ item.lastLoginTime }}</span>
                  </template>
                </a-list-item-meta>
                <div class="user-status">
                  <span class="status-dot online"></span>
                  <span class="status-text">在线</span>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </div>
      
      <!-- 系统信息 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">
            <InfoCircleOutlined class="card-title-icon" />
            系统信息
          </h3>
        </div>
        <div class="card-content">
          <div class="system-info">
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">{{ dashboardData.systemInfo.systemVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Java版本</span>
              <span class="info-value">{{ dashboardData.systemInfo.javaVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Spring Boot版本</span>
              <span class="info-value">{{ dashboardData.systemInfo.springBootVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Kotlin版本</span>
              <span class="info-value">{{ dashboardData.systemInfo.kotlinVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后更新</span>
              <span class="info-value">{{ dashboardData.systemInfo.lastUpdate }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快速操作</h3>
      <div class="actions-grid">
        <div class="action-item">
          <a-button type="primary" size="large" class="action-button" @click="handleQuickAction('addUser')">
            <UserAddOutlined />
            添加用户
          </a-button>
        </div>
        <div class="action-item">
          <a-button type="primary" size="large" class="action-button" @click="handleQuickAction('createRole')">
            <TeamOutlined />
            创建角色
          </a-button>
        </div>
        <div class="action-item">
          <a-button type="primary" size="large" class="action-button" @click="handleQuickAction('configPermission')">
            <SafetyOutlined />
            权限管理
          </a-button>
        </div>
        <div class="action-item">
          <a-button type="primary" size="large" class="action-button" @click="handleQuickAction('systemSettings')">
            <SettingOutlined />
            用户管理
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  TeamOutlined,
  SafetyOutlined,
  GlobalOutlined,
  ClockCircleOutlined,
  InfoCircleOutlined,
  UserAddOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import { dashboardApi, type DashboardData } from '@/api/dashboard'

const router = useRouter()
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

const handleQuickAction = (action: string) => {
  switch (action) {
    case 'addUser':
      router.push('/system/users/create')
      break
    case 'createRole':
      router.push('/system/roles/create')
      break
    case 'configPermission':
      router.push('/system/permissions')
      break
    case 'systemSettings':
      // 系统设置页面暂时跳转到用户管理页面
      router.push('/system/users')
      break
    default:
      message.info('功能开发中...')
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard {
  min-height: 100%;
  background: var(--background-color);
  padding: 16px;
}

/* 欢迎横幅 */
.welcome-banner {
  position: relative;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  color: white;
  overflow: hidden;
  box-shadow: 0 8px 32px var(--shadow-color);
}

.banner-content {
  position: relative;
  z-index: 2;
}

.banner-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.banner-subtitle {
  font-size: 13px;
  margin: 0;
  opacity: 0.9;
  font-weight: 400;
}

.banner-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 200px;
  height: 100%;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.decoration-circle:nth-child(1) {
  width: 80px;
  height: 80px;
  top: 20px;
  right: 20px;
}

.decoration-circle:nth-child(2) {
  width: 120px;
  height: 120px;
  top: 60px;
  right: 80px;
}

.decoration-circle:nth-child(3) {
  width: 60px;
  height: 60px;
  top: 100px;
  right: 40px;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--light-color);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.stat-card-users .stat-icon {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.stat-card-roles .stat-icon {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
}

.stat-card-permissions .stat-icon {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.stat-card-online .stat-icon {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-color);
  line-height: 1;
  margin-bottom: 3px;
}

.stat-label {
  font-size: 11px;
  color: var(--text-color);
  opacity: 0.7;
  margin-bottom: 6px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend-text {
  font-size: 12px;
  color: var(--text-color);
  opacity: 0.6;
}

.trend-value {
  font-size: 12px;
  font-weight: 600;
}

.trend-value.positive {
  color: #52c41a;
}

.trend-value.negative {
  color: #f5222d;
}

.trend-value.neutral {
  color: var(--text-color);
  opacity: 0.6;
}

.trend-indicator.online {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #52c41a;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.content-card {
  background: var(--light-color);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--light-accent-color);
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-title-icon {
  color: var(--primary-color);
}

.card-content {
  padding: 16px;
}

/* 用户列表样式 */
.user-list {
  background: transparent;
}

.user-list-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}

.user-list-item:last-child {
  border-bottom: none;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
}

.username {
  font-weight: 600;
  color: var(--text-color);
  font-size: 13px;
}

.login-time {
  color: var(--text-color);
  opacity: 0.7;
  font-size: 10px;
}

.user-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: #52c41a;
}

.status-text {
  font-size: 12px;
  color: var(--text-color);
  opacity: 0.7;
}

/* 系统信息样式 */
.system-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: var(--text-color);
  opacity: 0.8;
  font-size: 12px;
}

.info-value {
  font-weight: 600;
  color: var(--text-color);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 11px;
}

/* 快速操作 */
.quick-actions {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0 0 16px 0;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 14px;
}

.action-button {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  box-shadow: 0 4px 16px var(--shadow-color);
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }
  
  .welcome-banner {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .banner-title {
    font-size: 18px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
}

/* 紫色主题适配 */
.theme-purple .stat-card {
  background: var(--light-color);
  border-color: var(--border-color);
}

.theme-purple .content-card {
  background: var(--light-color);
  border-color: var(--border-color);
}

.theme-purple .card-header {
  background: var(--light-accent-color);
  border-color: var(--border-color);
}

.theme-purple .info-item {
  border-color: var(--border-color);
}

.theme-purple .user-list-item {
  border-color: var(--border-color);
}
</style>
