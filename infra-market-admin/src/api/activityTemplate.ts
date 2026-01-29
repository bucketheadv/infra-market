import request from '@/utils/request'

// 活动模板相关类型定义
export interface ActivityTemplateField {
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
  itemType?: ActivityTemplateField
  properties?: Record<string, ActivityTemplateField>
}

export interface SelectOption {
  value: string
  label?: string
}

export interface ActivityTemplate {
  id?: number
  name: string
  description?: string
  fields?: ActivityTemplateField[]
  status?: number
  createTime?: string
  updateTime?: string
}

export interface ActivityTemplateForm {
  id?: number
  name: string
  description?: string
  fields?: ActivityTemplateField[]
  status?: number
}

// 活动模板API
export const activityTemplateApi = {
  // 获取模板列表（支持分页）
  getList: (params?: {
    name?: string
    status?: number
    page?: number
    size?: number
  }) => {
    return request.get<PageResult<ActivityTemplate>>('/activity/template/list', { params })
  },

  // 获取所有启用的模板（用于下拉选择）
  getAll: () => {
    return request.get<ActivityTemplate[]>('/activity/template/all')
  },

  // 获取模板详情
  getById: (id: number) => {
    return request.get<ActivityTemplate>(`/activity/template/${id}`)
  },

  // 创建模板
  create: (data: ActivityTemplateForm) => {
    return request.post<ActivityTemplate>('/activity/template', data)
  },

  // 更新模板
  update: (id: number, data: ActivityTemplateForm) => {
    return request.put<ActivityTemplate>(`/activity/template/${id}`, data)
  },

  // 删除模板
  delete: (id: number) => {
    return request.delete<boolean>(`/activity/template/${id}`)
  },

  // 更新模板状态
  updateStatus: (id: number, status: number) => {
    return request.put<ActivityTemplate>(`/activity/template/${id}/status`, null, {
      params: { status }
    })
  },

  // 复制模板
  copy: (id: number) => {
    return request.post<ActivityTemplate>(`/activity/template/${id}/copy`)
  }
}
