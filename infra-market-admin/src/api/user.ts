import request from '@/utils/request'
import type { User, UserForm, PageParams, PageResult, ApiData } from '@/types'

export const userApi = {
  // 获取用户列表
  getUsers(params: PageParams): Promise<ApiData<PageResult<User>>> {
    return request.get('/users', { params })
  },

  // 获取用户详情
  getUser(id: number): Promise<ApiData<User>> {
    return request.get(`/users/${id}`)
  },

  // 创建用户
  createUser(data: UserForm): Promise<ApiData<User>> {
    return request.post('/users', data)
  },

  // 更新用户
  updateUser(id: number, data: UserForm): Promise<ApiData<User>> {
    return request.put(`/users/${id}`, data)
  },

  // 删除用户
  deleteUser(id: number): Promise<ApiData<void>> {
    return request.delete(`/users/${id}`)
  },

  // 批量删除用户
  batchDeleteUsers(ids: number[]): Promise<ApiData<void>> {
    return request.delete('/users/batch', { data: { ids } })
  },

  // 重置用户密码
  resetPassword(id: number): Promise<ApiData<{ password: string }>> {
    return request.post(`/users/${id}/reset/password`)
  },

  // 修改用户状态
  updateUserStatus(id: number, status: 'active' | 'inactive'): Promise<ApiData<void>> {
    return request.patch(`/users/${id}/status`, { status })
  },

  // 用户设置相关API
  getUserSettings: () => {
    return request.get('/api/user/settings')
  },

  // 更新用户设置
  updateUserSettings: (settings: any) => {
    return request.put('/api/user/settings', settings)
  },

  // 保存主题偏好
  saveThemePreference: (theme: string) => {
    return request.post('/api/user/theme', { theme })
  },

  // 获取用户主题偏好
  getThemePreference: () => {
    return request.get('/api/user/theme')
  }
}
