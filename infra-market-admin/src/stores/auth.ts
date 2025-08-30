import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User, LoginForm } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const permissions = ref<string[]>([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const hasPermission = computed(() => (permission: string) => permissions.value.includes(permission))

  // 登录
  const login = async (loginForm: LoginForm) => {
    try {
      const response = await authApi.login(loginForm)
      const { token: newToken, user: userInfo, permissions: userPermissions } = response.data
      
      token.value = newToken
      user.value = userInfo
      permissions.value = userPermissions
      
      localStorage.setItem('token', newToken)
      
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
      localStorage.removeItem('token')
    }
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    try {
      const response = await authApi.getCurrentUser()
      const { user: userInfo, permissions: userPermissions } = response.data
      
      user.value = userInfo
      permissions.value = userPermissions
      
      return response
    } catch (error) {
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
    isLoggedIn,
    hasPermission,
    login,
    logout,
    getCurrentUser,
    refreshToken,
  }
})
