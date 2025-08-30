import request from '@/utils/request'
import type { LoginForm, LoginResponse, ApiResponse, Permission } from '@/types'

export const authApi = {
  // 登录
  login(data: LoginForm): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/login', data)
  },

  // 登出
  logout(): Promise<ApiResponse<void>> {
    return request.post('/auth/logout')
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiResponse<LoginResponse>> {
    return request.get('/auth/current/user')
  },

  // 获取用户菜单
  getUserMenus(): Promise<ApiResponse<Permission[]>> {
    return request.get('/auth/user/menus')
  },

  // 刷新token
  refreshToken(): Promise<ApiResponse<{ token: string }>> {
    return request.post('/auth/refresh/token')
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
    return request.post('/auth/change/password', data)
  },
}
