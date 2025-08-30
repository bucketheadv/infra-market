import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: '/system/users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', permission: 'user:list' }
      },
      {
        path: '/system/users/create',
        name: 'UserCreate',
        component: () => import('@/views/user/UserForm.vue'),
        meta: { title: '创建用户', permission: 'user:create' }
      },
      {
        path: '/system/users/:id/edit',
        name: 'UserEdit',
        component: () => import('@/views/user/UserForm.vue'),
        meta: { title: '编辑用户', permission: 'user:update' }
      },
      {
        path: '/system/roles',
        name: 'Roles',
        component: () => import('@/views/permission/RoleList.vue'),
        meta: { title: '角色管理', permission: 'role:list' }
      },
      {
        path: '/system/roles/create',
        name: 'RoleCreate',
        component: () => import('@/views/permission/RoleForm.vue'),
        meta: { title: '创建角色', permission: 'role:create' }
      },
      {
        path: '/system/roles/:id/edit',
        name: 'RoleEdit',
        component: () => import('@/views/permission/RoleForm.vue'),
        meta: { title: '编辑角色', permission: 'role:update' }
      },
      {
        path: '/system/permissions',
        name: 'Permissions',
        component: () => import('@/views/permission/PermissionList.vue'),
        meta: { title: '权限管理', permission: 'permission:list' }
      },
      {
        path: '/system/permissions/create',
        name: 'PermissionCreate',
        component: () => import('@/views/permission/PermissionForm.vue'),
        meta: { title: '创建权限', permission: 'permission:create' }
      },
      {
        path: '/system/permissions/:id/edit',
        name: 'PermissionEdit',
        component: () => import('@/views/permission/PermissionForm.vue'),
        meta: { title: '编辑权限', permission: 'permission:update' }
      },
      {
        path: '/change-password',
        name: 'ChangePassword',
        component: () => import('@/views/auth/ChangePassword.vue'),
        meta: { title: '修改密码' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 延迟导入避免循环依赖
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()
  
  // 初始化认证状态
  await authStore.initializeAuth()
  
  // 如果需要认证
  if (to.meta.requiresAuth !== false) {
    // 检查是否已登录
    if (!authStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查权限
    if (to.meta.permission && !authStore.hasPermission(to.meta.permission as string)) {
      next('/403')
      return
    }
  }
  
  // 如果已登录且访问登录页，重定向到首页
  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/')
    return
  }
  
  next()
})

export default router
