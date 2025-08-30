import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export interface DashboardStatistics {
  userCount: number
  roleCount: number
  permissionCount: number
  onlineCount: number
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
