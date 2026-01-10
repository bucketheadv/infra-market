import request, { createRequestWithTimeout, setupRequestInterceptors } from '@/utils/request'

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
  environment?: string
  timeout?: number
  valuePath?: string
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
  timeout?: number
  valuePath?: string
  urlParams?: ApiParam[]
  headerParams?: ApiParam[]
  bodyParams?: ApiParam[]
}

export interface ApiExecuteRequest {
  interfaceId?: number
  headers?: Record<string, string>
  urlParams?: Record<string, any>
  bodyParams?: Record<string, any>
  timeout?: number
  remark?: string
}

export interface ApiExecuteResponse {
  status: number
  headers: Record<string, string>
  body: string
  responseTime: number
  success: boolean
  error?: string
  extractedValue?: string
}


// 接口管理API
export const interfaceApi = {
  // 获取接口列表（支持分页）
  getList: (params?: {
    name?: string
    method?: string
    status?: number
    environment?: string
    page?: number
    size?: number
  }) => {
    return request.get<PageResult<ApiInterface>>('/interface/list', { params })
  },

  // 获取接口详情
  getById: (id: number) => {
    return request.get<ApiInterface>(`/interface/${id}`)
  },

  // 创建接口
  create: (data: ApiInterfaceForm) => {
    return request.post<ApiInterface>('/interface', data)
  },

  // 更新接口
  update: (id: number, data: ApiInterfaceForm) => {
    return request.put<ApiInterface>(`/interface/${id}`, data)
  },

  // 删除接口
  delete: (id: number) => {
    return request.delete<boolean>(`/interface/${id}`)
  },

  // 更新接口状态
  updateStatus: (id: number, status: number) => {
    return request.put<ApiInterface>(`/interface/${id}/status?status=${status}`)
  },

  // 复制接口
  copy: (id: number) => {
    return request.post<ApiInterface>(`/interface/${id}/copy`)
  },

  // 执行接口
  execute: (data: ApiExecuteRequest) => {
    // 如果请求中指定了超时时间，使用自定义超时时间的请求实例
    if (data.timeout && data.timeout > 0) {
      const customRequest = createRequestWithTimeout(data.timeout)
      setupRequestInterceptors(customRequest)
      return customRequest.post<ApiExecuteResponse>('/interface/execute', data)
    }
    // 否则使用默认请求实例
    return request.post<ApiExecuteResponse>('/interface/execute', data)
  },

  // 获取最近最热门的接口
  getMostUsed: (params?: {
    days?: number
    limit?: number
  }) => {
    return request.get<ApiInterface[]>('/interface/most/used', { params })
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
  { value: 'JSON', label: 'JSON字符串' },
  { value: 'JSON_OBJECT', label: 'JSON对象' },
  { value: 'ARRAY', label: '数组' }
]

// 代码编辑器支持的编程语言类型
export const CODE_EDITOR_LANGUAGES = [
  { value: 'TEXT', label: 'Text' },
  { value: 'JSON', label: 'JSON字符串' },
  { value: 'JSON_OBJECT', label: 'JSON对象' },
  { value: 'XML', label: 'XML' },
  { value: 'HTML', label: 'HTML' },
  { value: 'CSS', label: 'CSS' },
  { value: 'JAVASCRIPT', label: 'JavaScript' },
  { value: 'TYPESCRIPT', label: 'TypeScript' },
  { value: 'JAVA', label: 'Java' },
  { value: 'KOTLIN', label: 'Kotlin' },
  { value: 'SQL', label: 'SQL' },
  { value: 'YAML', label: 'YAML' }
]

export const POST_TYPES = [
  { value: 'application/json', label: 'application/json' },
  { value: 'application/x-www-form-urlencoded', label: 'application/x-www-form-urlencoded' }
]

export const TAGS = [
  { value: 'TEST', label: '测试环境' },
  { value: 'PRODUCTION', label: '正式环境' }
]

// 执行记录相关类型定义
export interface ApiInterfaceExecutionRecord {
  id: number
  interfaceId: number
  executorId: number
  executorName: string
  requestParams?: string
  requestHeaders?: string
  requestBody?: string
  responseStatus?: number
  responseHeaders?: string
  responseBody?: string
  executionTime?: number
  success: boolean
  errorMessage?: string
  remark?: string
  clientIp?: string
  userAgent?: string
  createTime: string
  updateTime: string
}

export interface ApiInterfaceExecutionRecordQuery {
  interfaceId?: number
  keyword?: string
  executorId?: number
  success?: boolean
  minExecutionTime?: number
  maxExecutionTime?: number
  startTime?: number
  endTime?: number
  page?: number
  size?: number
}

export interface ApiInterfaceExecutionRecordStats {
  totalExecutions: number
  successCount: number
  failureCount: number
  successRate: number
  averageExecutionTime: number
  minExecutionTime: number
  maxExecutionTime: number
}

// 分页响应类型
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 执行记录API
export const executionRecordApi = {
  // 分页查询执行记录
  getList: (query: ApiInterfaceExecutionRecordQuery) => 
    request.post<PageResult<ApiInterfaceExecutionRecord>>('/interface/execution/record/list', query),
  
  // 根据ID查询执行记录详情
  getById: (id: number) => 
    request.get<ApiInterfaceExecutionRecord>(`/interface/execution/record/${id}`),
  
  
  // 根据执行人ID查询执行记录
  getByExecutorId: (executorId: number, limit: number = 10) => 
    request.get<ApiInterfaceExecutionRecord[]>(`/interface/execution/record/executor/${executorId}?limit=${limit}`),
  
  // 获取执行统计信息
  getExecutionStats: (interfaceId: number) => 
    request.get<ApiInterfaceExecutionRecordStats>(`/interface/execution/record/stats/${interfaceId}`),
  
  // 获取执行记录数量统计
  getExecutionCount: (startTime: number, endTime: number) => 
    request.get<number>(`/interface/execution/record/count?startTime=${startTime}&endTime=${endTime}`)
}
