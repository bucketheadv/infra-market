import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.set('Authorization', `Bearer ${authStore.token}`)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 如果响应成功但业务状态码不是200，直接返回错误，让组件处理
    if (data.code !== 200) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    
    return data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message.error('登录已过期，请重新登录')
          const authStore = useAuthStore()
          authStore.logout()
          // 重定向到登录页
          window.location.href = '/login'
          break
        case 403:
          // 403错误不在这里显示消息，但需要创建包含错误消息的Error对象
          const errorMessage = data?.message || '权限不足，无法访问此功能'
          return Promise.reject(new Error(errorMessage))
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      // 处理超时错误
      const timeoutMessage = '请求超时，请检查网络连接或增加超时时间'
      return Promise.reject(new Error(timeoutMessage))
    } else {
      message.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

// 创建带自定义超时时间的请求实例
export const createRequestWithTimeout = (timeout: number): AxiosInstance => {
  return axios.create({
    baseURL: '/api',
    timeout: timeout * 1000, // 转换为毫秒
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

// 为自定义请求实例添加拦截器
export const setupRequestInterceptors = (requestInstance: AxiosInstance) => {
  // 请求拦截器
  requestInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const authStore = useAuthStore()
      if (authStore.token) {
        config.headers.set('Authorization', `Bearer ${authStore.token}`)
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  requestInstance.interceptors.response.use(
    (response: AxiosResponse) => {
      const { data } = response
      
      // 如果响应成功但业务状态码不是200，直接返回错误，让组件处理
      if (data.code !== 200) {
        return Promise.reject(new Error(data.message || '请求失败'))
      }
      
      return data
    },
    (error) => {
      if (error.response) {
        const { status, data } = error.response
        
        switch (status) {
          case 401:
            message.error('登录已过期，请重新登录')
            const authStore = useAuthStore()
            authStore.logout()
            // 重定向到登录页
            window.location.href = '/login'
            break
          case 403:
            // 403错误不在这里显示消息，但需要创建包含错误消息的Error对象
            const errorMessage = data?.message || '权限不足，无法访问此功能'
            return Promise.reject(new Error(errorMessage))
          case 404:
            message.error('请求的资源不存在')
            break
          case 500:
            message.error('服务器内部错误')
            break
          default:
            message.error(data?.message || '请求失败')
        }
      } else if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        // 处理超时错误
        const timeoutMessage = '请求超时，请检查网络连接或增加超时时间'
        return Promise.reject(new Error(timeoutMessage))
      } else {
        message.error('网络错误，请检查网络连接')
      }
      
      return Promise.reject(error)
    }
  )
}

export default request
