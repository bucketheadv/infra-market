import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export interface DashboardStatistics {
  userCount: number
  roleCount: number
  permissionCount: number
  interfaceCount: number // 接口总数
  userCountChangePercent: number // 用户总数较昨日变化百分比
  roleCountChangePercent: number // 角色总数较昨日变化百分比
  permissionCountChangePercent: number // 权限总数较昨日变化百分比
  interfaceCountChangePercent: number // 接口总数较昨日变化百分比
}

export interface RecentUser {
  id: number
  username: string
  email?: string
  status: string
  lastLoginTime: string
}

export interface SystemInfo {
  systemVersion: string
  javaVersion: string
  springBootVersion: string
  kotlinVersion: string
  lastUpdate: string
}

export interface DashboardData {
  statistics: DashboardStatistics
  recentUsers: RecentUser[]
  systemInfo: SystemInfo
}

export const dashboardApi = {
  // 获取仪表盘数据
  getDashboardData(): Promise<ApiResponse<DashboardData>> {
    return request.get('/dashboard/data')
  },
}
