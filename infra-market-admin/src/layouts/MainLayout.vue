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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 32px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  height: 64px;
  z-index: 10;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(255, 255, 255, 0.1) 100%);
  pointer-events: none;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.2) 50%, 
    transparent 100%);
}

.logo {
  font-size: 22px;
  font-weight: 800;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo::before {
  content: '🚀';
  font-size: 24px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  transition: all 0.4s ease;
}

.logo:hover {
  transform: scale(1.05) translateY(-1px);
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.logo:hover::before {
  transform: rotate(15deg) scale(1.1);
}

.header-right {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 180px;
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.user-dropdown::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.2) 50%, 
    transparent 100%);
  transition: left 0.6s ease;
}

.user-dropdown:hover::before {
  left: 100%;
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
  z-index: 1;
}

.user-avatar {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.1) 100%);
  color: #ffffff;
  font-weight: bold;
  margin-right: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-dropdown:hover .user-avatar {
  transform: scale(1.1);
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  color: #ffffff;
  font-weight: 700;
  font-size: 15px;
  line-height: 1.2;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-role {
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  line-height: 1.2;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-dropdown:hover .username {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.user-dropdown:hover .user-role {
  color: rgba(255, 255, 255, 1);
}

.dropdown-icon {
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin-left: 12px;
  position: relative;
  z-index: 1;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg) scale(1.1);
  color: #ffffff;
}

/* 用户菜单美化 */
.user-menu {
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0;
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  position: relative;
}

.user-menu::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(102, 126, 234, 0.3) 50%, 
    transparent 100%);
}

.menu-header {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px 16px 0 0;
  margin: -1px -1px 0 -1px;
  position: relative;
  overflow: hidden;
}

.menu-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(255, 255, 255, 0.1) 100%);
  pointer-events: none;
}

.menu-avatar {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.1) 100%);
  color: white;
  font-weight: bold;
  margin-right: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.menu-user-info {
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.menu-username {
  color: white;
  font-weight: 700;
  font-size: 15px;
  line-height: 1.2;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.menu-role {
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  line-height: 1.2;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.user-menu :deep(.ant-menu-item) {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  margin: 0;
  border-radius: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #333;
  position: relative;
  overflow: hidden;
}

.user-menu :deep(.ant-menu-item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(102, 126, 234, 0.1) 50%, 
    transparent 100%);
  transition: left 0.6s ease;
}

.user-menu :deep(.ant-menu-item:hover::before) {
  left: 100%;
}

.user-menu :deep(.ant-menu-item:hover) {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  color: #667eea;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.user-menu :deep(.ant-menu-item .anticon) {
  font-size: 16px;
  color: inherit;
  transition: all 0.3s ease;
}

.user-menu :deep(.ant-menu-item:hover .anticon) {
  transform: scale(1.1);
}

.user-menu :deep(.ant-menu-divider) {
  margin: 6px 0;
  border-color: rgba(0, 0, 0, 0.06);
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(0, 0, 0, 0.06) 50%, 
    transparent 100%);
}

.logout-item {
  color: #ff4d4f !important;
  position: relative;
}

.logout-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 77, 79, 0.2) 50%, 
    transparent 100%);
}

.logout-item:hover {
  background: linear-gradient(135deg, #fff2f0 0%, #ffe7e6 100%) !important;
  color: #ff4d4f !important;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.1);
}

.sider {
  background: #ffffff;
  border-right: 1px solid #f0f0f0;
  height: calc(100vh - 64px);
  overflow-y: auto;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 5;
  margin-top: -1px;
}

.sider::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.2) 50%, transparent 100%);
}

/* 滚动条美化 */
.sider::-webkit-scrollbar {
  width: 6px;
}

.sider::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.sider::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  transition: all 0.3s ease;
}

.sider::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 菜单整体样式 */
.sider :deep(.ant-menu) {
  background: transparent;
  border-right: none;
  color: #666;
}

.sider :deep(.ant-menu-inline) {
  border-right: none;
}

/* 菜单项基础样式 */
.sider :deep(.ant-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  color: #666;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.sider :deep(.ant-menu-item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(102, 126, 234, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.sider :deep(.ant-menu-item:hover::before) {
  opacity: 1;
}

.sider :deep(.ant-menu-item:hover) {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

/* 菜单项选中样式 */
.sider :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #ffffff !important;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  transform: translateX(4px);
  border-right: none !important;
}

.sider :deep(.ant-menu-item-selected::before) {
  display: none;
}

.sider :deep(.ant-menu-item-selected .anticon) {
  color: #ffffff !important;
}

/* 菜单项图标样式 */
.sider :deep(.ant-menu-item .anticon) {
  font-size: 16px;
  margin-right: 12px;
  transition: all 0.3s ease;
  color: inherit;
}

.sider :deep(.ant-menu-item:hover .anticon) {
  transform: scale(1.1);
}

/* 子菜单样式 */
.sider :deep(.ant-menu-submenu) {
  margin: 4px 12px;
}

.sider :deep(.ant-menu-submenu-title) {
  margin: 0;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  color: #666;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.sider :deep(.ant-menu-submenu-title::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(102, 126, 234, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.sider :deep(.ant-menu-submenu-title:hover::before) {
  opacity: 1;
}

.sider :deep(.ant-menu-submenu-title:hover) {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.sider :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title) {
  color: #667eea !important;
  background: rgba(102, 126, 234, 0.1);
}

.sider :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title .anticon) {
  color: #667eea !important;
}

/* 子菜单展开箭头 */
.sider :deep(.ant-menu-submenu-arrow) {
  color: #999;
  transition: all 0.3s ease;
}

.sider :deep(.ant-menu-submenu-title:hover .ant-menu-submenu-arrow) {
  color: #667eea;
}

.sider :deep(.ant-menu-submenu-open > .ant-menu-submenu-title .ant-menu-submenu-arrow) {
  color: #667eea;
}

/* 子菜单内容区域 */
.sider :deep(.ant-menu-sub) {
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  margin: 4px 0;
  padding: 4px 0;
}

.sider :deep(.ant-menu-sub .ant-menu-item) {
  margin: 2px 8px;
  height: 40px;
  line-height: 40px;
  border-radius: 6px;
  font-size: 13px;
}

.sider :deep(.ant-menu-sub .ant-menu-item .anticon) {
  font-size: 14px;
  margin-right: 8px;
}

/* 菜单分组标题 */
.sider :deep(.ant-menu-item-group-title) {
  color: #999;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 16px 8px 16px;
  margin: 0;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .sider :deep(.ant-menu-item),
  .sider :deep(.ant-menu-submenu-title) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
  }
  
  .sider :deep(.ant-menu-sub .ant-menu-item) {
    height: 36px;
    line-height: 36px;
    margin: 1px 6px;
  }
}

.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
  overflow-y: auto;
  position: relative;
  z-index: 1;
}
</style>
