import request from '@/utils/request'
import type { User, UserForm, PageParams, PageResult, ApiResponse } from '@/types'

export const userApi = {
  // 获取用户列表
  getUsers(params: PageParams): Promise<ApiResponse<PageResult<User>>> {
    return request.get('/users', { params })
  },

  // 获取用户详情
  getUser(id: number): Promise<ApiResponse<User>> {
    return request.get(`/users/${id}`)
  },

  // 创建用户
  createUser(data: UserForm): Promise<ApiResponse<User>> {
    return request.post('/users', data)
  },

  // 更新用户
  updateUser(id: number, data: UserForm): Promise<ApiResponse<User>> {
    return request.put(`/users/${id}`, data)
  },

  // 删除用户
  deleteUser(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/users/${id}`)
  },

  // 批量删除用户
  batchDeleteUsers(ids: number[]): Promise<ApiResponse<void>> {
    return request.delete('/users/batch', { data: { ids } })
  },

  // 重置用户密码
  resetPassword(id: number): Promise<ApiResponse<{ password: string }>> {
    return request.post(`/users/${id}/reset-password`)
  },

  // 修改用户状态
  updateUserStatus(id: number, status: 'active' | 'inactive'): Promise<ApiResponse<void>> {
    return request.patch(`/users/${id}/status`, { status })
  },
}
