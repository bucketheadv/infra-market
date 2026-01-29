import request from '@/utils/request'

// 活动管理相关类型定义
export interface Activity {
  id?: number
  name: string
  description?: string
  templateId?: number
  templateName?: string
  configData?: Record<string, any>
  status?: number
  createTime?: string
  updateTime?: string
}

export interface ActivityForm {
  id?: number
  name: string
  description?: string
  templateId?: number
  configData?: Record<string, any>
  status?: number
}

// 活动管理API
export const activityApi = {
  // 获取活动列表（支持分页）
  getList: (params?: {
    name?: string
    templateId?: number
    status?: number
    page?: number
    size?: number
  }) => {
    return request.get<PageResult<Activity>>('/activity/list', { params })
  },

  // 获取活动详情
  getById: (id: number) => {
    return request.get<Activity>(`/activity/${id}`)
  },

  // 创建活动
  create: (data: ActivityForm) => {
    return request.post<Activity>('/activity', data)
  },

  // 更新活动
  update: (id: number, data: ActivityForm) => {
    return request.put<Activity>(`/activity/${id}`, data)
  },

  // 删除活动
  delete: (id: number) => {
    return request.delete<boolean>(`/activity/${id}`)
  },

  // 更新活动状态
  updateStatus: (id: number, status: number) => {
    return request.put<Activity>(`/activity/${id}/status`, null, {
      params: { status }
    })
  }
}
