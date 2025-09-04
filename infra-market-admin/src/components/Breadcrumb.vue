<template>
  <div class="breadcrumb-container">
    <a-breadcrumb class="breadcrumb">
              <a-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
          <template v-if="index === 0">
            <!-- 首页项，显示图标 -->
            <router-link 
              :to="item.path" 
              class="breadcrumb-link breadcrumb-home"
              v-if="item.path && item.path !== route.path"
            >
              <HomeOutlined class="breadcrumb-icon" />
              <span>{{ item.title }}</span>
            </router-link>
            <span v-else class="breadcrumb-text breadcrumb-home">
              <HomeOutlined class="breadcrumb-icon" />
              <span>{{ item.title }}</span>
            </span>
          </template>
          <template v-else-if="index === breadcrumbItems.length - 1">
            <!-- 最后一项，不显示链接 -->
            <span class="breadcrumb-current">{{ item.title }}</span>
          </template>
          <template v-else>
            <!-- 可点击的导航项 -->
            <router-link 
              :to="item.path" 
              class="breadcrumb-link"
              v-if="item.path && item.path !== route.path"
            >
              {{ item.title }}
            </router-link>
            <span v-else class="breadcrumb-text">{{ item.title }}</span>
          </template>
        </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { HomeOutlined } from '@ant-design/icons-vue'
import type { Permission } from '@/types'

interface BreadcrumbItem {
  title: string
  path?: string
  icon?: string
}

const route = useRoute()
const authStore = useAuthStore()

// 根据路径在菜单中查找对应的菜单项
const findMenuByPath = (menus: Permission[], targetPath: string): Permission | null => {
  for (const menu of menus) {
    if (menu.path === targetPath) {
      return menu
    }
    if (menu.children && menu.children.length > 0) {
      const found = findMenuByPath(menu.children, targetPath)
      if (found) return found
    }
  }
  return null
}

// 根据路径构建面包屑路径
const buildBreadcrumbPath = (menus: Permission[], targetPath: string): BreadcrumbItem[] => {
  const path: BreadcrumbItem[] = []
  
  // 递归查找路径
  const findPath = (menuList: Permission[], currentPath: string): boolean => {
    for (const menu of menuList) {
      if (menu.path === currentPath) {
        path.unshift({
          title: menu.name,
          path: menu.path,
          icon: menu.icon
        })
        return true
      }
      if (menu.children && menu.children.length > 0) {
        if (findPath(menu.children, currentPath)) {
          // 对于有子菜单的父级菜单，不设置为可点击（path: undefined）
          // 这样可以避免分类菜单被误点击
          path.unshift({
            title: menu.name,
            path: undefined,
            icon: menu.icon
          })
          return true
        }
      }
    }
    return false
  }
  
  findPath(menus, targetPath)
  return path
}

// 生成面包屑数据
const breadcrumbItems = computed((): BreadcrumbItem[] => {
  const items: BreadcrumbItem[] = []
  
  // 添加首页
  items.push({
    title: '首页',
    path: '/',
    icon: 'HomeOutlined'
  })
  
  // 获取当前路径
  const currentPath = route.path
  
  if (currentPath === '/') {
    // 首页
    return items
  }
  
  // 从菜单数据中构建面包屑
  const menuPath = buildBreadcrumbPath(authStore.menus, currentPath)
  
  if (menuPath.length > 0) {
    // 如果找到菜单路径，添加到面包屑中
    items.push(...menuPath)
  } else {
    // 如果没有找到菜单路径，使用路径解析作为备选方案
    const pathSegments = currentPath.split('/').filter(Boolean)
    
    // 处理特殊路径
    if (pathSegments[0] === 'change-password') {
      items.push({
        title: '修改密码',
        path: undefined
      })
    } else if (pathSegments[0] === 'system') {
      // 系统管理模块的备选处理
      items.push({
        title: '系统管理',
        path: undefined
      })
      
      if (pathSegments[1] === 'users') {
        items.push({
          title: '用户管理',
          path: '/system/users'
        })
        
        if (pathSegments[2] === 'create') {
          items.push({
            title: '创建用户',
            path: undefined
          })
        } else if (pathSegments[3] === 'edit') {
          items.push({
            title: '编辑用户',
            path: undefined
          })
        }
      } else if (pathSegments[1] === 'roles') {
        items.push({
          title: '角色管理',
          path: '/system/roles'
        })
        
        if (pathSegments[2] === 'create') {
          items.push({
            title: '创建角色',
            path: undefined
          })
        } else if (pathSegments[3] === 'edit') {
          items.push({
            title: '编辑角色',
            path: undefined
        })
        }
      } else if (pathSegments[1] === 'permissions') {
        items.push({
          title: '权限管理',
          path: '/system/permissions'
        })
        
        if (pathSegments[2] === 'create') {
          items.push({
            title: '创建权限',
            path: undefined
          })
        } else if (pathSegments[3] === 'edit') {
          items.push({
            title: '编辑权限',
            path: undefined
          })
        }
      }
    }
  }
  
  return items
})
</script>

<style scoped>
.breadcrumb-container {
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.breadcrumb {
  font-size: 14px;
  line-height: 1.5;
  display: flex;
  align-items: center;
}

.breadcrumb :deep(.ant-breadcrumb-separator) {
  margin: 0 8px;
  color: #d9d9d9;
  font-size: 12px;
}

.breadcrumb :deep(.ant-breadcrumb-item) {
  display: flex;
  align-items: center;
  min-height: 32px;
}

.breadcrumb-link {
  color: var(--primary-color, #1890ff);
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 4px;
  margin: -4px -8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  line-height: 1;
}

.breadcrumb-link:hover {
  color: var(--secondary-color, #40a9ff);
  background: rgba(24, 144, 255, 0.1);
  text-decoration: none;
}

.breadcrumb-current {
  color: var(--text-color, #333);
  font-weight: 500;
  padding: 4px 8px;
  margin: -4px -8px;
  background: rgba(24, 144, 255, 0.05);
  border-radius: 4px;
  display: inline-block;
  white-space: nowrap;
  line-height: 1;
}

.breadcrumb-text {
  color: #666;
  padding: 4px 8px;
  margin: -4px -8px;
  display: inline-block;
  white-space: nowrap;
  line-height: 1;
}

.breadcrumb-home {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.breadcrumb-icon {
  font-size: 14px;
  color: var(--primary-color, #1890ff);
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .breadcrumb-container {
    padding: 12px 16px;
  }
  
  .breadcrumb {
    font-size: 13px;
  }
  
  .breadcrumb :deep(.ant-breadcrumb-separator) {
    margin: 0 6px;
  }
  
  .breadcrumb-link,
  .breadcrumb-current,
  .breadcrumb-text {
    padding: 2px 6px;
    margin: -2px -6px;
  }
  
  .breadcrumb-home {
    gap: 4px;
  }
  
  .breadcrumb-icon {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .breadcrumb-container {
    padding: 10px 12px;
  }
  
  .breadcrumb {
    font-size: 12px;
  }
  
  .breadcrumb :deep(.ant-breadcrumb-separator) {
    margin: 0 4px;
  }
  
  .breadcrumb-link,
  .breadcrumb-current,
  .breadcrumb-text {
    padding: 1px 4px;
    margin: -1px -4px;
  }
  
  .breadcrumb-home {
    gap: 3px;
  }
  
  .breadcrumb-icon {
    font-size: 11px;
  }
}

/* 主题色支持 */
:root.theme-purple .breadcrumb-container {
  background: #f9f0ff;
  border-bottom-color: #d9d9d9;
}

:root.theme-purple .breadcrumb-current {
  background: rgba(114, 46, 209, 0.1);
  color: #333;
}

:root.theme-purple .breadcrumb-text {
  color: #666;
}

:root.theme-purple .breadcrumb-link {
  color: #722ed1;
}

:root.theme-purple .breadcrumb-link:hover {
  color: #9254de;
  background: rgba(114, 46, 209, 0.1);
}

:root.theme-purple .breadcrumb-icon {
  color: #722ed1;
}

:root.theme-purple .breadcrumb :deep(.ant-breadcrumb-separator) {
  color: #d9d9d9;
}

:root.theme-green .breadcrumb-container {
  background: #f6ffed;
  border-bottom-color: #d9f7be;
}

:root.theme-green .breadcrumb-current {
  background: rgba(82, 196, 26, 0.1);
}

:root.theme-green .breadcrumb-link:hover {
  background: rgba(82, 196, 26, 0.1);
}

:root.theme-green .breadcrumb :deep(.ant-breadcrumb-separator) {
  color: #b7eb8f;
}

:root.theme-orange .breadcrumb-container {
  background: #fff7e6;
  border-bottom-color: #ffe7ba;
}

:root.theme-orange .breadcrumb-current {
  background: rgba(250, 140, 22, 0.1);
}

:root.theme-orange .breadcrumb-link:hover {
  background: rgba(250, 140, 22, 0.1);
}

:root.theme-orange .breadcrumb :deep(.ant-breadcrumb-separator) {
  color: #ffd591;
}

:root.theme-red .breadcrumb-container {
  background: #fff2f0;
  border-bottom-color: #ffccc7;
}

:root.theme-red .breadcrumb-current {
  background: rgba(245, 34, 45, 0.1);
}

:root.theme-red .breadcrumb-link:hover {
  background: rgba(245, 34, 45, 0.1);
}

:root.theme-red .breadcrumb :deep(.ant-breadcrumb-separator) {
  color: #ffadd2;
}
</style>
