<template>
  <a-layout class="main-layout">
    <a-layout-header class="header">
      <div class="logo">基础商城后台</div>
      <div class="header-right">
        <a-dropdown>
          <a class="user-dropdown">
            <div class="user-info">
              <a-avatar class="user-avatar">
                {{ user?.username?.charAt(0)?.toUpperCase() }}
              </a-avatar>
              <div class="user-details">
                <span class="username">{{ user?.username }}</span>
                <span class="user-role">管理员</span>
              </div>
            </div>
            <DownOutlined class="dropdown-icon" />
          </a>
          <template #overlay>
            <a-menu class="user-menu">
              <div class="menu-header">
                <a-avatar class="menu-avatar">
                  {{ user?.username?.charAt(0)?.toUpperCase() }}
                </a-avatar>
                <div class="menu-user-info">
                  <div class="menu-username">{{ user?.username }}</div>
                  <div class="menu-role">管理员</div>
                </div>
              </div>
              <a-menu-divider />
              <a-menu-item key="change-password" @click="handleChangePassword">
                <KeyOutlined />
                <span>修改密码</span>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout" @click="handleLogout" class="logout-item">
                <LogoutOutlined />
                <span>退出登录</span>
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

          <!-- 仪表盘 -->
          <a-menu-item key="dashboard">
            <DashboardOutlined />
            <span>仪表盘</span>
          </a-menu-item>
          

          
          <!-- 动态菜单 -->
          <template v-for="menu in validMenus" :key="menu.id">
            <a-sub-menu v-if="menu.children && menu.children.length > 0" :key="`submenu-${menu.id}`">
              <template #title>
                <component :is="getIconComponent(menu.icon)" v-if="menu.icon" />
                <span>{{ menu.name }}</span>
              </template>
              <template v-for="child in menu.children" :key="child.id">
                <a-menu-item 
                  v-if="child && child.path && child.name"
                  :key="child.id.toString()"
                >
                  <component :is="getIconComponent(child.icon)" v-if="child.icon" />
                  <span>{{ child.name }}</span>
                </a-menu-item>
              </template>
            </a-sub-menu>
            <a-menu-item v-else :key="`item-${menu.id}`">
              <component :is="getIconComponent(menu.icon)" v-if="menu.icon" />
              <span>{{ menu.name }}</span>
            </a-menu-item>
          </template>
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
  SettingOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  CloudServerOutlined,
  DesktopOutlined,
  ApiOutlined,
  ShoppingOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  LogoutOutlined,
  QuestionOutlined,
  KeyOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const userMenus = computed(() => authStore.menus)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])
const isUpdatingFromRoute = ref(false)

// 初始化默认展开的菜单
const initializeDefaultOpenKeys = () => {
  const defaultOpenKeys: string[] = []
  
  // 遍历菜单，找到有子菜单的顶级菜单，默认展开
  for (const menu of validMenus.value) {
    if (menu.children && menu.children.length > 0) {
      defaultOpenKeys.push(`submenu-${menu.id}`)
    }
  }
  
  openKeys.value = defaultOpenKeys
}

// 过滤有效的菜单项
const validMenus = computed(() => {
  return userMenus.value.filter(menu => menu && menu.name)
})

// 权限检查
const hasPermission = (permission: string) => {
  return authStore.hasPermission(permission)
}

// 获取图标组件
const getIconComponent = (iconName: string) => {
  const iconMap: Record<string, any> = {
    SettingOutlined,
    UserOutlined,
    TeamOutlined,
    SafetyCertificateOutlined,
    CloudServerOutlined,
    DesktopOutlined,
    ApiOutlined,
    ShoppingOutlined,
    AppstoreOutlined,
    FileTextOutlined,
  }
  return iconMap[iconName] || QuestionOutlined
}

// 更新菜单状态的函数
const updateMenuState = (path: string) => {
  isUpdatingFromRoute.value = true
  
  // 特殊处理仪表盘
  if (path === '/') {
    selectedKeys.value = ['dashboard']
    // 保持现有的展开状态，不清空
    isUpdatingFromRoute.value = false
    return
  }
  
  // 根据路径找到对应的菜单ID和父菜单ID
  const findMenuInfoByPath = (menus: any[], targetPath: string): { menuId: string | null, parentId: string | null } => {
    for (const menu of menus) {
      if (menu.path === targetPath) {
        return { menuId: menu.id.toString(), parentId: null }
      }
      if (menu.children) {
        for (const child of menu.children) {
          if (child.path === targetPath) {
            return { menuId: child.id.toString(), parentId: menu.id.toString() }
          }
        }
      }
    }
    return { menuId: null, parentId: null }
  }
  
  const { menuId, parentId } = findMenuInfoByPath(validMenus.value, path)
  selectedKeys.value = menuId ? [menuId.toString()] : []
  
  // 设置展开的菜单状态
  if (parentId) {
    // 如果是子菜单，确保父菜单展开
    const currentOpenKeys = new Set(openKeys.value)
    currentOpenKeys.add(parentId.toString())
    openKeys.value = Array.from(currentOpenKeys)
  }
  // 如果是顶级菜单，保持现有的展开状态
  
  isUpdatingFromRoute.value = false
}

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (path) => {
    updateMenuState(path)
  },
  { immediate: true }
)

// 监听菜单数据变化，确保数据加载后更新状态
watch(
  () => validMenus.value,
  (newMenus, oldMenus) => {
    // 只在菜单数据从空变为有数据时更新状态
    if (newMenus.length > 0 && (!oldMenus || oldMenus.length === 0)) {
      // 初始化默认展开的菜单
      initializeDefaultOpenKeys()
      // 更新当前路由对应的菜单状态
      updateMenuState(route.path)
    }
  },
  { immediate: true }
)

// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  // 特殊处理仪表盘
  if (key === 'dashboard') {
    router.push('/')
    return
  }
  
  // 根据菜单ID找到对应的路径
  const findPathById = (menus: any[], targetId: string): string | null => {
    for (const menu of menus) {
      if (menu.id == targetId) {
        return menu.path
      }
      if (menu.children) {
        for (const child of menu.children) {
          if (child.id == targetId) {
            return child.path
          }
        }
      }
    }
    return null
  }
  
  const path = findPathById(validMenus.value, key)
  if (path) {
    router.push(path)
  }
}

// 菜单展开/收起处理
const handleOpenChange = (keys: string[]) => {
  // 只在用户手动操作时更新，忽略程序自动设置
  if (!isUpdatingFromRoute.value) {
    openKeys.value = keys
  }
}

// 修改密码
const handleChangePassword = () => {
  router.push('/change-password')
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
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(102, 126, 234, 0.1);
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.logo:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(102, 126, 234, 0.2);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  min-width: 160px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.user-dropdown:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: bold;
  margin-right: 12px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  color: white;
  font-weight: 600;
  font-size: 14px;
  line-height: 1.2;
}

.user-role {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  line-height: 1.2;
}

.dropdown-icon {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  transition: transform 0.3s ease;
  margin-left: 8px;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 用户菜单美化 */
.user-menu {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.menu-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px 12px 0 0;
  margin: -1px -1px 0 -1px;
}

.menu-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: bold;
  margin-right: 12px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.menu-user-info {
  display: flex;
  flex-direction: column;
}

.menu-username {
  color: white;
  font-weight: 600;
  font-size: 14px;
  line-height: 1.2;
}

.menu-role {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  line-height: 1.2;
}

.user-menu :deep(.ant-menu-item) {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 0;
  border-radius: 0;
  transition: all 0.3s ease;
  color: #333;
}

.user-menu :deep(.ant-menu-item:hover) {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  color: #667eea;
  transform: translateX(4px);
}

.user-menu :deep(.ant-menu-item .anticon) {
  font-size: 16px;
  color: inherit;
}

.user-menu :deep(.ant-menu-divider) {
  margin: 4px 0;
  border-color: #f0f0f0;
}

.logout-item {
  color: #ff4d4f !important;
}

.logout-item:hover {
  background: linear-gradient(135deg, #fff2f0 0%, #ffe7e6 100%) !important;
  color: #ff4d4f !important;
}

.sider {
  background: #fff;
  border-right: 1px solid #f0f0f0;
  height: calc(100vh - 64px);
  overflow-y: auto;
  flex-shrink: 0;
}

/* 菜单选中样式 */
.sider :deep(.ant-menu-item-selected) {
  background-color: #e6f7ff !important;
  border-right: 3px solid #1890ff !important;
  color: #1890ff !important;
}

.sider :deep(.ant-menu-item-selected .anticon) {
  color: #1890ff !important;
}

.sider :deep(.ant-menu-item:hover) {
  background-color: #f5f5f5 !important;
}

.sider :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title) {
  color: #1890ff !important;
}

.sider :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title .anticon) {
  color: #1890ff !important;
}

.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
  overflow-y: auto;
}
</style>
