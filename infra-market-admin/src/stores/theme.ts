import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 主题类型定义
export interface ThemeConfig {
  name: string
  label: string
  primaryColor: string
  secondaryColor: string
  accentColor: string
  backgroundColor: string
  textColor: string
  borderColor: string
  shadowColor: string
  lightColor: string
  lightAccentColor: string
  icon: string
}

// 预定义主题
export const themes: ThemeConfig[] = [
  {
    name: 'default',
    label: '默认主题',
    primaryColor: '#1890ff',
    secondaryColor: '#40a9ff',
    accentColor: '#69c0ff',
    backgroundColor: '#f0f2f5',
    textColor: '#333333',
    borderColor: '#f0f0f0',
    shadowColor: 'rgba(24, 144, 255, 0.08)',
    lightColor: '#ffffff',
    lightAccentColor: '#e6f7ff',
    icon: '🌊'
  },
  {
    name: 'purple',
    label: '紫色主题',
    primaryColor: '#722ed1',
    secondaryColor: '#9254de',
    accentColor: '#b37feb',
    backgroundColor: '#f9f0ff',
    textColor: '#333333',
    borderColor: '#d9d9d9',
    shadowColor: 'rgba(114, 46, 209, 0.08)',
    lightColor: '#ffffff',
    lightAccentColor: '#f0f0f0',
    icon: '💜'
  },
  {
    name: 'green',
    label: '绿色主题',
    primaryColor: '#52c41a',
    secondaryColor: '#73d13d',
    accentColor: '#95de64',
    backgroundColor: '#f6ffed',
    textColor: '#333333',
    borderColor: '#d9f7be',
    shadowColor: 'rgba(82, 196, 26, 0.08)',
    lightColor: '#ffffff',
    lightAccentColor: '#f6ffed',
    icon: '🌿'
  },
  {
    name: 'orange',
    label: '橙色主题',
    primaryColor: '#fa8c16',
    secondaryColor: '#ffa940',
    accentColor: '#ffc53d',
    backgroundColor: '#fff7e6',
    textColor: '#333333',
    borderColor: '#ffe7ba',
    shadowColor: 'rgba(250, 140, 22, 0.08)',
    lightColor: '#ffffff',
    lightAccentColor: '#fff7e6',
    icon: '🍊'
  },
  {
    name: 'red',
    label: '红色主题',
    primaryColor: '#f5222d',
    secondaryColor: '#ff4d4f',
    accentColor: '#ff7875',
    backgroundColor: '#fff2f0',
    textColor: '#333333',
    borderColor: '#ffccc7',
    shadowColor: 'rgba(245, 34, 45, 0.08)',
    lightColor: '#ffffff',
    lightAccentColor: '#fff2f0',
    icon: '🌹'
  }
]

export const useThemeStore = defineStore('theme', () => {
  // 当前主题
  const currentTheme = ref<string>(localStorage.getItem('theme') || 'default')
  
  // 获取当前主题配置
  const getCurrentThemeConfig = (): ThemeConfig => {
    return themes.find(theme => theme.name === currentTheme.value) || themes[0]
  }
  
  // 切换主题
  const setTheme = (themeName: string) => {
    if (themes.find(theme => theme.name === themeName)) {
      currentTheme.value = themeName
      localStorage.setItem('theme', themeName)
      // 同时保存到cookie
      document.cookie = `theme=${themeName};path=/;max-age=31536000` // 1年有效期
      applyTheme(themeName)
    }
  }
  
  // 应用主题到CSS变量
  const applyTheme = (themeName: string) => {
    const theme = themes.find(t => t.name === themeName)
    if (!theme) return
    
    const root = document.documentElement
    root.style.setProperty('--primary-color', theme.primaryColor)
    root.style.setProperty('--secondary-color', theme.secondaryColor)
    root.style.setProperty('--accent-color', theme.accentColor)
    root.style.setProperty('--background-color', theme.backgroundColor)
    root.style.setProperty('--text-color', theme.textColor)
    root.style.setProperty('--border-color', theme.borderColor)
    root.style.setProperty('--shadow-color', theme.shadowColor)
    root.style.setProperty('--light-color', theme.lightColor)
    root.style.setProperty('--light-accent-color', theme.lightAccentColor)
    
    // 更新html和body类名用于全局样式控制
    document.documentElement.className = document.documentElement.className.replace(/theme-\w+/g, '')
    document.documentElement.classList.add(`theme-${themeName}`)
    document.body.className = document.body.className.replace(/theme-\w+/g, '')
    document.body.classList.add(`theme-${themeName}`)
    

  }
  
  // 初始化主题
  const initializeTheme = () => {
    // 优先从cookie获取，然后从localStorage获取
    const getCookieTheme = () => {
      const cookies = document.cookie.split(';')
      for (const cookie of cookies) {
        const [name, value] = cookie.trim().split('=')
        if (name === 'theme') {
          return value
        }
      }
      return null
    }
    
    const cookieTheme = getCookieTheme()
    const localTheme = localStorage.getItem('theme')
    const savedTheme = cookieTheme || localTheme
    
    if (savedTheme && themes.find(theme => theme.name === savedTheme)) {
      currentTheme.value = savedTheme
      localStorage.setItem('theme', savedTheme)
      // 同步到cookie
      document.cookie = `theme=${savedTheme};path=/;max-age=31536000`
    }
    
    applyTheme(currentTheme.value)
  }
  
  // 监听主题变化，自动应用
  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  })
  
  return {
    currentTheme,
    themes,
    getCurrentThemeConfig,
    setTheme,
    applyTheme,
    initializeTheme
  }
})
