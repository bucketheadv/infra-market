import request from '@/utils/request'
import type { LoginForm, LoginResponse, ApiData, Permission } from '@/types'

export const authApi = {
  // 登录
  login(data: LoginForm): Promise<ApiData<LoginResponse>> {
    return request.post('/auth/login', data)
  },

  // 登出
  logout(): Promise<ApiData<void>> {
    return request.post('/auth/logout')
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiData<LoginResponse>> {
    return request.get('/auth/current/user')
  },

  // 获取用户菜单
  getUserMenus(): Promise<ApiData<Permission[]>> {
    return request.get('/auth/user/menus')
  },

  // 刷新token
  refreshToken(): Promise<ApiData<{ token: string }>> {
    return request.post('/auth/refresh/token')
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiData<void>> {
    return request.post('/auth/change/password', data)
  },
}
