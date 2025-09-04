import request from '@/utils/request'

// 接口管理相关类型定义
export interface ApiInterface {
  id?: number
  name: string
  method: string
  url: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
  postType?: string
  urlParams?: ApiParam[]
  headerParams?: ApiParam[]
  bodyParams?: ApiParam[]
}

export interface SelectOption {
  value: string
  label?: string
}

export interface ApiParam {
  name: string
  chineseName?: string
  paramType: string
  inputType: string
  dataType: string
  required?: boolean
  defaultValue?: string | any[]
  changeable?: boolean
  options?: SelectOption[]
  description?: string
  sort?: number
}

export interface ApiInterfaceForm {
  id?: number
  name: string
  method: string
  url: string
  description?: string
  postType?: string
  urlParams?: ApiParam[]
  headerParams?: ApiParam[]
  bodyParams?: ApiParam[]
}

export interface ApiExecuteRequest {
  interfaceId?: number
  headers?: Record<string, string>
  urlParams?: Record<string, any>
  bodyParams?: Record<string, any>
}

export interface ApiExecuteResponse {
  status: number
  headers: Record<string, string>
  body: string
  responseTime: number
  success: boolean
  error?: string
}

// 接口管理API
export const interfaceApi = {
  // 获取接口列表
  getList: (params?: {
    name?: string
    method?: string
    status?: number
  }) => {
    return request.get<ApiInterface[]>('/api/interface/list', { params })
  },

  // 获取接口详情
  getById: (id: number) => {
    return request.get<ApiInterface>(`/api/interface/${id}`)
  },

  // 创建接口
  create: (data: ApiInterfaceForm) => {
    return request.post<ApiInterface>('/api/interface', data)
  },

  // 更新接口
  update: (id: number, data: ApiInterfaceForm) => {
    return request.put<ApiInterface>(`/api/interface/${id}`, data)
  },

  // 删除接口
  delete: (id: number) => {
    return request.delete<boolean>(`/api/interface/${id}`)
  },

  // 更新接口状态
  updateStatus: (id: number, status: number) => {
    return request.put<ApiInterface>(`/api/interface/${id}/status?status=${status}`)
  },

  // 执行接口
  execute: (data: ApiExecuteRequest) => {
    return request.post<ApiExecuteResponse>('/api/interface/execute', data)
  }
}

// 枚举值
export const HTTP_METHODS = [
  { value: 'GET', label: 'GET' },
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
  { value: 'DELETE', label: 'DELETE' },
  { value: 'PATCH', label: 'PATCH' },
  { value: 'HEAD', label: 'HEAD' },
  { value: 'OPTIONS', label: 'OPTIONS' }
]

export const PARAM_TYPES = [
  { value: 'URL_PARAM', label: 'URL参数' },
  { value: 'BODY_PARAM', label: 'Body参数' },
  { value: 'HEADER_PARAM', label: 'Header参数' }
]

export const INPUT_TYPES = [
  { value: 'TEXT', label: '文本框' },
  { value: 'SELECT', label: '下拉框' },
  { value: 'MULTI_SELECT', label: '多选下拉框' },
  { value: 'DATE', label: '日期' },
  { value: 'DATETIME', label: '日期时间' },
  { value: 'NUMBER', label: '数字' },
  { value: 'TEXTAREA', label: '多行文本' },
  { value: 'CODE', label: '代码编辑器' },
  { value: 'PASSWORD', label: '密码' },
  { value: 'EMAIL', label: '邮箱' },
  { value: 'URL', label: 'URL' }
]

export const DATA_TYPES = [
  { value: 'STRING', label: '字符串' },
  { value: 'INTEGER', label: '整数' },
  { value: 'LONG', label: '长整数' },
  { value: 'DOUBLE', label: '双精度浮点数' },
  { value: 'BOOLEAN', label: '布尔值' },
  { value: 'DATE', label: '日期' },
  { value: 'DATETIME', label: '日期时间' },
  { value: 'JSON', label: 'JSON对象' },
  { value: 'ARRAY', label: '数组' }
]

export const POST_TYPES = [
  { value: 'application/json', label: 'application/json' },
  { value: 'application/x-www-form-urlencoded', label: 'application/x-www-form-urlencoded' }
]
