<template>
  <a-layout class="main-layout">
    <a-layout-header class="header">
      <div class="logo">Infra Market Admin</div>
      <div class="header-right">
        <a-dropdown>
          <a class="user-dropdown">
y           <a-avatar>
              {{ user?.username?.charAt(0)?.toUpperCase() }}
            </a-avatar>
            <span class="username">{{ user?.username }}</span>
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="handleLogout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    
    <a-layout>
      <a-layout-sider width="200" class="sider">
        <a-menu
          mode="inline"
          :selectedKeys="selectedKeys"
          :openKeys="openKeys"
          @click="handleMenuClick"
          @openChange="handleOpenChange"
        >
          <a-menu-item key="/">
            <DashboardOutlined />
            <span>仪表盘</span>
          </a-menu-item>
          
          <a-sub-menu key="user">
            <template #title>
              <UserOutlined />
              <span>用户管理</span>
            </template>
            <a-menu-item key="/users">用户列表</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="permission">
            <template #title>
              <SafetyOutlined />
              <span>权限管理</span>
            </template>
            <a-menu-item key="/roles">角色管理</a-menu-item>
            <a-menu-item key="/permissions">权限管理</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      
      <a-layout-content class="main-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  UserOutlined,
  SafetyOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
    
    // 设置展开的菜单
    if (path.startsWith('/users')) {
      openKeys.value = ['user']
    } else if (path.startsWith('/roles') || path.startsWith('/permissions')) {
      openKeys.value = ['permission']
    }
  },
  { immediate: true }
)

// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

// 菜单展开/收起处理
const handleOpenChange = (keys: string[]) => {
  openKeys.value = keys
}

// 退出登录
const handleLogout = async () => {
  try {
    await authStore.logout()
    message.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    message.error('退出登录失败')
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f5f5;
}

.username {
  margin-left: 8px;
  color: #333;
}

.sider {
  background: #fff;
  border-right: 1px solid #f0f0f0;
}

.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}
</style>
