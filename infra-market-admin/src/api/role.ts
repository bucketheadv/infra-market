import request from '@/utils/request'
import type { Role, RoleForm, PageParams, PageResult, ApiResponse } from '@/types'

export const roleApi = {
  // 获取角色列表
  getRoles(params: PageParams): Promise<ApiResponse<PageResult<Role>>> {
    return request.get('/roles', { params })
  },

  // 获取所有角色（不分页）
  getAllRoles(): Promise<ApiResponse<Role[]>> {
    return request.get('/roles/all')
  },

  // 获取角色详情
  getRole(id: number): Promise<ApiResponse<Role>> {
    return request.get(`/roles/${id}`)
  },

  // 创建角色
  createRole(data: RoleForm): Promise<ApiResponse<Role>> {
    return request.post('/roles', data)
  },

  // 更新角色
  updateRole(id: number, data: RoleForm): Promise<ApiResponse<Role>> {
    return request.put(`/roles/${id}`, data)
  },

  // 删除角色
  deleteRole(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/roles/${id}`)
  },

  // 批量删除角色
  batchDeleteRoles(ids: number[]): Promise<ApiResponse<void>> {
    return request.delete('/roles/batch', { data: { ids } })
  },

  // 修改角色状态
  updateRoleStatus(id: number, status: 'active' | 'inactive'): Promise<ApiResponse<void>> {
    return request.patch(`/roles/${id}/status`, { status })
  },
}
