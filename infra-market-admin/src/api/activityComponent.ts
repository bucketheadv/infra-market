import request from '@/utils/request'

// 活动组件相关类型定义
export interface ActivityComponentField {
  name?: string
  label?: string
  type?: string
  inputType?: string
  required?: boolean
  defaultValue?: any
  description?: string
  sort?: number
  options?: SelectOption[]
  multiple?: boolean
  min?: number
  max?: number
  minLength?: number
  maxLength?: number
  pattern?: string
  placeholder?: string
  itemType?: ActivityComponentField
  properties?: Record<string, ActivityComponentField>
  // 组件相关字段
  componentId?: number
  isArray?: boolean
  allowDynamic?: boolean
}

export interface SelectOption {
  value: string
  label?: string
}

export interface ActivityComponent {
  id?: number
  name: string
  description?: string
  fields?: ActivityComponentField[]
  status?: number
  createTime?: string
  updateTime?: string
}

export interface ActivityComponentForm {
  id?: number
  name: string
  description?: string
  fields?: ActivityComponentField[]
  status?: number
}

// 活动组件API
export const activityComponentApi = {
  // 获取组件列表（支持分页）
  getList: (params?: {
    name?: string
    status?: number
    page?: number
    size?: number
  }) => {
    return request.get<PageResult<ActivityComponent>>('/activity/component/list', { params })
  },

  // 获取所有启用的组件（用于下拉选择）
  getAll: () => {
    return request.get<ActivityComponent[]>('/activity/component/all')
  },

  // 获取组件详情
  getById: (id: number) => {
    return request.get<ActivityComponent>(`/activity/component/${id}`)
  },

  // 创建组件
  create: (data: ActivityComponentForm) => {
    return request.post<ActivityComponent>('/activity/component', data)
  },

  // 更新组件
  update: (id: number, data: ActivityComponentForm) => {
    return request.put<ActivityComponent>(`/activity/component/${id}`, data)
  },

  // 删除组件
  delete: (id: number) => {
    return request.delete<boolean>(`/activity/component/${id}`)
  },

  // 更新组件状态
  updateStatus: (id: number, status: number) => {
    return request.put<ActivityComponent>(`/activity/component/${id}/status`, null, {
      params: { status }
    })
  },

  // 复制组件
  copy: (id: number) => {
    return request.post<ActivityComponent>(`/activity/component/${id}/copy`)
  }
}
