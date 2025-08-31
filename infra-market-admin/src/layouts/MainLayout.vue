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
                <div class="menu-avatar-wrapper">
                  <a-avatar class="menu-avatar">
                    {{ user?.username?.charAt(0)?.toUpperCase() }}
                  </a-avatar>
                  <div class="online-indicator"></div>
                </div>
                <div class="menu-user-info">
                  <div class="menu-username">{{ user?.username }}</div>
                  <div class="menu-role">管理员</div>
                  <div class="menu-status">在线</div>
                </div>
              </div>
              <div style="height: 1px; background: #f0f0f0; margin: 6px 0;"></div>
              <div @click="handleChangePassword" class="custom-menu-item" style="display: flex; align-items: center; padding: 10px 14px; cursor: pointer; transition: background 0.3s ease; border-radius: 8px; margin: 3px 6px;">
                <div style="width: 28px; height: 28px; border-radius: 50%; background: rgba(24, 144, 255, 0.1); display: flex; align-items: center; justify-content: center; margin-right: 10px; flex-shrink: 0; color: #1890ff;">
                  <KeyOutlined />
                </div>
                <div style="display: flex; flex-direction: column; justify-content: center; flex: 1;">
                  <div style="font-size: 13px; font-weight: 600; color: #333; margin: 0; line-height: 1.2;">修改密码</div>
                  <div style="font-size: 11px; color: #666; margin: 1px 0 0 0; line-height: 1.2;">更新您的账户密码</div>
                </div>
              </div>
              <div style="height: 1px; background: #f0f0f0; margin: 6px 0;"></div>
              <div @click="handleLogout" class="custom-menu-item" style="display: flex; align-items: center; padding: 10px 14px; cursor: pointer; transition: background 0.3s ease; border-radius: 8px; margin: 3px 6px;">
                <div style="width: 28px; height: 28px; border-radius: 50%; background: rgba(255, 77, 79, 0.1); display: flex; align-items: center; justify-content: center; margin-right: 10px; flex-shrink: 0; color: #ff4d4f;">
                  <LogoutOutlined />
                </div>
                <div style="display: flex; flex-direction: column; justify-content: center; flex: 1;">
                  <div style="font-size: 13px; font-weight: 600; color: #333; margin: 0; line-height: 1.2;">退出登录</div>
                  <div style="font-size: 11px; color: #666; margin: 1px 0 0 0; line-height: 1.2;">安全退出系统</div>
                </div>
              </div>
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  position: relative;
  z-index: 1000;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
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
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 140px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.user-dropdown::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(24, 144, 255, 0.3);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.user-dropdown:hover::before {
  opacity: 1;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
  z-index: 1;
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: white;
  font-weight: bold;
  margin-right: 8px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  width: 32px;
  height: 32px;
  font-size: 14px;
}

.user-dropdown:hover .user-avatar {
  background: linear-gradient(135deg, #096dd9 0%, #1890ff 100%);
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  color: #333333;
  font-weight: 600;
  font-size: 13px;
  line-height: 1.2;
  transition: all 0.3s ease;
  margin-bottom: 1px;
}

.user-role {
  color: #666666;
  font-size: 11px;
  line-height: 1.2;
  font-weight: 500;
  transition: all 0.3s ease;
}

.user-dropdown:hover .username {
  color: #1890ff;
}

.user-dropdown:hover .user-role {
  color: #40a9ff;
}

.dropdown-icon {
  color: #666666;
  font-size: 10px;
  transition: all 0.3s ease;
  margin-left: 6px;
  position: relative;
  z-index: 1;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
  color: #1890ff;
}

/* 用户菜单美化 */
.user-menu {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
  padding: 3px;
  background: #ffffff;
  overflow: hidden;
  min-width: 220px;
  max-width: 240px;
  backdrop-filter: blur(20px);
}

.menu-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-radius: 8px;
  margin: 6px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);
}

.menu-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="10" cy="60" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="90" cy="40" r="0.5" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.2;
  border-radius: 8px 8px 0 0;
}

.menu-avatar-wrapper {
  position: relative;
  margin-right: 10px;
}

.menu-avatar {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  font-weight: 700;
  border: 2px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
  width: 32px;
  height: 32px;
  font-size: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 6px;
  background: #52c41a;
  border: 1px solid white;
  border-radius: 50%;
  z-index: 2;
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
  font-size: 13px;
  line-height: 1.2;
  margin-bottom: 2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  letter-spacing: 0.2px;
}

.menu-role {
  color: rgba(255, 255, 255, 0.95);
  font-size: 11px;
  line-height: 1.2;
  font-weight: 500;
  margin-bottom: 1px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.menu-status {
  color: rgba(255, 255, 255, 0.85);
  font-size: 10px;
  line-height: 1.1;
  font-weight: 400;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 强制覆盖 Ant Design 菜单项样式 */
.user-menu :deep(.ant-menu-item.menu-item-change-password),
.user-menu :deep(.ant-menu-item.menu-item-logout) {
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  margin: 0 !important;
  border-radius: 0 !important;
  transition: all 0.3s ease !important;
  color: #333 !important;
  border-bottom: 1px solid #f5f5f5 !important;
  height: auto !important;
  line-height: normal !important;
  width: 100% !important;
  justify-content: flex-start !important;
  box-sizing: border-box !important;
}

.user-menu :deep(.ant-menu-item) {
  display: flex !important;
  align-items: center !important;
  padding: 12px 16px !important;
  margin: 0 !important;
  border-radius: 0 !important;
  transition: all 0.3s ease !important;
  color: #333 !important;
  border-bottom: 1px solid #f5f5f5 !important;
  height: auto !important;
  line-height: normal !important;
  width: 100% !important;
  justify-content: flex-start !important;
}

.user-menu :deep(.ant-menu-item:last-child) {
  border-bottom: none;
}

.user-menu :deep(.ant-menu-item:hover) {
  background: #f8f9fa;
  color: #1890ff;
  transform: translateX(4px);
}

.user-menu :deep(.ant-menu-item .anticon) {
  font-size: 14px;
  color: inherit;
  margin-right: 0 !important;
  margin-left: 0 !important;
}

.user-menu :deep(.ant-menu-item) > * {
  margin-right: 0 !important;
  margin-left: 0 !important;
}

.user-menu :deep(.ant-menu-item) .menu-item-icon + .menu-item-content {
  flex: 1 !important;
  display: flex !important;
  flex-direction: column !important;
}

.user-menu :deep(.ant-menu-item-content) {
  width: 100% !important;
  flex: 1 !important;
}

.user-menu :deep(.ant-menu-divider) {
  margin: 0;
  border-color: #f0f0f0;
}

/* 自定义菜单项样式 */
.custom-menu-item:hover {
  background: rgba(0, 0, 0, 0.04) !important;
}

.custom-menu-item:active {
  background: rgba(0, 0, 0, 0.08) !important;
}

.menu-item-change-password, .menu-item-logout {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #f0f0f0, #ffffff);
}

.menu-item-change-password:hover, .menu-item-logout:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.user-menu .menu-item-icon {
  width: 36px !important;
  height: 36px !important;
  border-radius: 50% !important;
  background: rgba(24, 144, 255, 0.1) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin-right: 16px !important;
  flex-shrink: 0 !important;
}

/* 强制菜单项内容布局 */
.user-menu :deep(.menu-item-change-password) .menu-item-content,
.user-menu :deep(.menu-item-logout) .menu-item-content,
.user-menu .menu-item-content {
  display: flex !important;
  flex-direction: column !important;
  justify-content: center !important;
  flex: 1 !important;
  width: calc(100% - 52px) !important;
  min-width: 0 !important;
  margin-left: 0 !important;
  padding-left: 0 !important;
}

.user-menu .menu-item-title {
  font-size: 15px !important;
  font-weight: 600 !important;
  color: #333 !important;
  margin: 0 !important;
  line-height: 1.4 !important;
}

.user-menu .menu-item-desc {
  font-size: 13px !important;
  color: #666 !important;
  margin: 2px 0 0 0 !important;
  line-height: 1.4 !important;
}

.user-menu .menu-item-change-password .menu-item-icon {
  background: rgba(24, 144, 255, 0.1) !important;
  color: #1890ff !important;
}

.user-menu .menu-item-logout .menu-item-icon {
  background: rgba(255, 77, 79, 0.1) !important;
  color: #ff4d4f !important;
}

.menu-item-change-password:hover .menu-item-desc {
  color: #1890ff;
}

.menu-item-logout:hover .menu-item-desc {
  color: #ff4d4f;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }
  
  .logo {
    font-size: 18px;
  }
  
  .user-dropdown {
    min-width: 120px;
    padding: 6px 10px;
  }
  
  .user-details {
    display: none;
  }
  
  .dropdown-icon {
    margin-left: 4px;
  }
  
  .user-menu {
    min-width: 240px;
  }
  
  .menu-header {
    padding: 12px 16px;
  }
  
  .user-menu :deep(.ant-menu-item) {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 12px;
  }
  
  .logo {
    font-size: 16px;
  }
  
  .user-dropdown {
    min-width: 100px;
    padding: 6px 8px;
  }
  
  .user-avatar {
    margin-right: 6px;
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .user-menu {
    min-width: 200px;
  }
}
</style>
