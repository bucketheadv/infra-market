// 用户相关类型
export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  status: 'active' | 'inactive'
  createTime: string
  updateTime: string
}

export interface UserForm {
  username: string
  email?: string
  phone?: string
  password?: string
  roleIds: number[]
}

// 角色相关类型
export interface Role {
  id: number
  name: string
  code: string
  description?: string
  status: 'active' | 'inactive'
  createTime: string
  updateTime: string
}

export interface RoleForm {
  name: string
  code: string
  description?: string
  permissionIds: number[]
}

// 权限相关类型
export interface Permission {
  id: number
  name: string
  code: string
  type: 'menu' | 'button'
  parentId?: number
  path?: string
  icon?: string
  sort: number
  status: 'active' | 'inactive'
  createTime: string
  updateTime: string
  children?: Permission[]
}

export interface PermissionForm {
  name: string
  code: string
  type: 'menu' | 'button'
  parentId?: number
  path?: string
  icon?: string
  sort: number
}

// 登录相关类型
export interface LoginForm {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
  permissions: string[]
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

// 分页查询参数
export interface PageParams {
  current: number
  size: number
  [key: string]: any
}
