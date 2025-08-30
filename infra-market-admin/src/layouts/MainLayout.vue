<template>
  <a-layout class="main-layout">
    <a-layout-header class="header">
      <div class="logo">基础商城后台</div>
      <div class="header-right">
        <a-dropdown>
          <a class="user-dropdown">
            <a-avatar>
              {{ user?.username?.charAt(0)?.toUpperCase() }}
            </a-avatar>
            <span class="username">{{ user?.username }}</span>
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="handleChangePassword">
                <KeyOutlined />
                修改密码
              </a-menu-item>
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
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const userMenus = computed(() => authStore.menus)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])
const isUpdatingFromRoute = ref(false)

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
    openKeys.value = []
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
  // 注意：如果是顶级菜单，我们不重置openKeys，保持用户的手动展开状态
  
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
