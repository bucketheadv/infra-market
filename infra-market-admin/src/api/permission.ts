import request from '@/utils/request'
import type { Permission, PermissionForm, PageParams, PageResult, ApiData } from '@/types'

export const permissionApi = {
  // 获取权限列表
  getPermissions(params: PageParams): Promise<ApiData<PageResult<Permission>>> {
    return request.get('/permissions', { params })
  },

  // 获取权限树
  getPermissionTree(): Promise<ApiData<Permission[]>> {
    return request.get('/permissions/tree')
  },

  // 获取权限详情
  getPermission(id: number): Promise<ApiData<Permission>> {
    return request.get(`/permissions/${id}`)
  },

  // 创建权限
  createPermission(data: PermissionForm): Promise<ApiData<Permission>> {
    return request.post('/permissions', data)
  },

  // 更新权限
  updatePermission(id: number, data: PermissionForm): Promise<ApiData<Permission>> {
    return request.put(`/permissions/${id}`, data)
  },

  // 删除权限
  deletePermission(id: number): Promise<ApiData<void>> {
    return request.delete(`/permissions/${id}`)
  },

  // 批量删除权限
  batchDeletePermissions(ids: number[]): Promise<ApiData<void>> {
    return request.delete('/permissions/batch', { data: { ids } })
  },

  // 修改权限状态
  updatePermissionStatus(id: number, status: 'active' | 'inactive'): Promise<ApiData<void>> {
    return request.patch(`/permissions/${id}/status`, { status })
  },
}
