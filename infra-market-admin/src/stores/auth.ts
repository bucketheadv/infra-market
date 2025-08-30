import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User, LoginForm, Permission } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const permissions = ref<string[]>([])
  const menus = ref<Permission[]>([])
  
  // 初始化时尝试恢复用户信息
  const initializeAuth = async () => {
    if (token.value && !user.value) {
      try {
        await getCurrentUser()
        await getUserMenus()
      } catch (error) {
        console.error('Failed to restore user session:', error)
        // 清除无效的token
        token.value = ''
        localStorage.removeItem('token')
      }
    }
  }

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  
  // 权限检查方法
  const hasPermission = (permission: string) => permissions.value.includes(permission)

  // 登录
  const login = async (loginForm: LoginForm) => {
    try {
      const response = await authApi.login(loginForm)
      const { token: newToken, user: userInfo, permissions: userPermissions } = response.data
      
      token.value = newToken
      user.value = userInfo
      permissions.value = userPermissions
      
      localStorage.setItem('token', newToken)
      
      // 获取用户菜单
      await getUserMenus()
      
      return response
    } catch (error) {
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      user.value = null
      permissions.value = []
      menus.value = []
      localStorage.removeItem('token')
    }
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    try {
      // 确保有token才请求
      if (!token.value) {
        throw new Error('No token available')
      }
      
      const response = await authApi.getCurrentUser()
      const { user: userInfo, permissions: userPermissions } = response.data
      
      user.value = userInfo
      permissions.value = userPermissions
      
      return response
    } catch (error) {
      // 如果获取用户信息失败，清除token
      token.value = ''
      user.value = null
      permissions.value = []
      menus.value = []
      localStorage.removeItem('token')
      throw error
    }
  }
  
  // 获取用户菜单
  const getUserMenus = async () => {
    try {
      // 确保有token才请求
      if (!token.value) {
        throw new Error('No token available')
      }
      
      const response = await authApi.getUserMenus()
      menus.value = response.data
      
      return response
    } catch (error) {
      menus.value = []
      throw error
    }
  }

  // 刷新token
  const refreshToken = async () => {
    try {
      const response = await authApi.refreshToken()
      const { token: newToken } = response.data
      
      token.value = newToken
      localStorage.setItem('token', newToken)
      
      return response
    } catch (error) {
      throw error
    }
  }

  return {
    token,
    user,
    permissions,
    menus,
    isLoggedIn,
    hasPermission,
    login,
    logout,
    getCurrentUser,
    getUserMenus,
    refreshToken,
    initializeAuth,
  }
})
