<template>
  <div class="theme-selector">
    <a-dropdown placement="bottomRight" trigger="click">
      <div class="theme-button">
        <span class="theme-icon">{{ currentThemeIcon }}</span>
      </div>
      
      <template #overlay>
        <div class="theme-dropdown">
          <div class="theme-options">
            <div
              v-for="theme in themes"
              :key="theme.name"
              class="theme-option"
              :class="{ active: currentTheme === theme.name }"
              @click="handleThemeChange(theme.name)"
            >
              <div class="theme-option-icon">{{ theme.icon }}</div>
              <div class="theme-option-name">{{ theme.label }}</div>
              <div v-if="currentTheme === theme.name" class="theme-option-check">
                <CheckOutlined />
              </div>
            </div>
          </div>
          
          <div class="theme-dropdown-footer">
            <a-button 
              type="link" 
              size="small" 
              @click="resetToDefault"
              class="reset-button"
            >
              重置默认
            </a-button>
          </div>
        </div>
      </template>
    </a-dropdown>
    

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CheckOutlined } from '@ant-design/icons-vue'
import { useThemeStore } from '@/stores/theme'
import { message } from 'ant-design-vue'

const themeStore = useThemeStore()

// 计算属性
const currentTheme = computed(() => themeStore.currentTheme)
const themes = computed(() => themeStore.themes)
const currentThemeIcon = computed(() => {
  const theme = themeStore.getCurrentThemeConfig()
  return theme.icon
})

// 方法
const handleThemeChange = (themeName: string) => {
  themeStore.setTheme(themeName)
  message.success(`已切换到${themes.value.find(t => t.name === themeName)?.label}`)
}

const resetToDefault = () => {
  themeStore.setTheme('default')
  message.success('已重置为默认主题')
}
</script>

<style scoped>
.theme-selector {
  display: flex;
  align-items: center;
}

.theme-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid var(--primary-color, rgba(24, 144, 255, 0.15));
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.85) 100%);
  backdrop-filter: blur(8px);
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 1px 3px var(--shadow-color, rgba(24, 144, 255, 0.1));
}

.theme-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color, #1890ff) 0%, var(--secondary-color, #40a9ff) 100%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

/* 主题特定的伪元素背景 */
html.theme-default .theme-button::before {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%) !important;
}

html.theme-purple .theme-button::before {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%) !important;
}

html.theme-green .theme-button::before {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%) !important;
}

html.theme-orange .theme-button::before {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%) !important;
}

html.theme-red .theme-button::before {
  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%) !important;
}

.theme-button:hover {
  border-color: var(--primary-color, #1890ff);
  transform: translateY(-1px);
  box-shadow: 0 3px 8px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

/* 主题特定的悬停样式 */
html.theme-default .theme-button:hover {
  box-shadow: 0 3px 8px rgba(24, 144, 255, 0.2) !important;
}

html.theme-purple .theme-button:hover {
  box-shadow: 0 3px 8px rgba(114, 46, 209, 0.2) !important;
}

html.theme-green .theme-button:hover {
  box-shadow: 0 3px 8px rgba(82, 196, 26, 0.2) !important;
}

html.theme-orange .theme-button:hover {
  box-shadow: 0 3px 8px rgba(250, 140, 22, 0.2) !important;
}

html.theme-red .theme-button:hover {
  box-shadow: 0 3px 8px rgba(245, 34, 45, 0.2) !important;
}

/* 主题特定的悬停边框颜色 */
html.theme-default .theme-button:hover {
  border-color: rgba(24, 144, 255, 0.3) !important;
}

html.theme-purple .theme-button:hover {
  border-color: rgba(114, 46, 209, 0.3) !important;
}

html.theme-green .theme-button:hover {
  border-color: rgba(82, 196, 26, 0.3) !important;
}

html.theme-orange .theme-button:hover {
  border-color: rgba(250, 140, 22, 0.3) !important;
}

html.theme-red .theme-button:hover {
  border-color: rgba(245, 34, 45, 0.3) !important;
}

.theme-button:hover::before {
  opacity: 0.1;
}

/* 主题特定的悬停伪元素样式 */
html.theme-default .theme-button:hover::before {
  opacity: 0.1 !important;
}

html.theme-purple .theme-button:hover::before {
  opacity: 0.15 !important;
}

html.theme-green .theme-button:hover::before {
  opacity: 0.12 !important;
}

html.theme-orange .theme-button:hover::before {
  opacity: 0.1 !important;
}

html.theme-red .theme-button:hover::before {
  opacity: 0.1 !important;
}

/* 主题特定的按钮样式 */
html.theme-default .theme-button {
  border-color: rgba(24, 144, 255, 0.15) !important;
  box-shadow: 0 1px 3px rgba(24, 144, 255, 0.1) !important;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(24, 144, 255, 0.02) 100%) !important;
}

html.theme-purple .theme-button {
  border-color: rgba(114, 46, 209, 0.15) !important;
  box-shadow: 0 1px 3px rgba(114, 46, 209, 0.1) !important;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(114, 46, 209, 0.02) 100%) !important;
}

html.theme-green .theme-button {
  border-color: rgba(82, 196, 26, 0.15) !important;
  box-shadow: 0 1px 3px rgba(82, 196, 26, 0.1) !important;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(82, 196, 26, 0.02) 100%) !important;
}

html.theme-orange .theme-button {
  border-color: rgba(250, 140, 22, 0.15) !important;
  box-shadow: 0 1px 3px rgba(250, 140, 22, 0.1) !important;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(250, 140, 22, 0.02) 100%) !important;
}

html.theme-red .theme-button {
  border-color: rgba(245, 34, 45, 0.15) !important;
  box-shadow: 0 1px 3px rgba(245, 34, 45, 0.1) !important;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(245, 34, 45, 0.02) 100%) !important;
}



.theme-icon {
  font-size: 14px;
  line-height: 1;
  position: relative;
  z-index: 1;
  color: var(--primary-color, #1890ff);
  transition: all 0.2s ease;
}

.theme-button:hover .theme-icon {
  color: var(--primary-color, #1890ff);
  transform: scale(1.1);
}

/* 主题特定的悬停图标颜色 */
html.theme-default .theme-button:hover .theme-icon {
  color: #1890ff !important;
}

html.theme-purple .theme-button:hover .theme-icon {
  color: #722ed1 !important;
}

html.theme-green .theme-button:hover .theme-icon {
  color: #52c41a !important;
}

html.theme-orange .theme-button:hover .theme-icon {
  color: #fa8c16 !important;
}

html.theme-red .theme-button:hover .theme-icon {
  color: #f5222d !important;
}

/* 主题特定的图标颜色 */
html.theme-default .theme-icon {
  color: #1890ff !important;
}

html.theme-purple .theme-icon {
  color: #722ed1 !important;
}

html.theme-green .theme-icon {
  color: #52c41a !important;
}

html.theme-orange .theme-icon {
  color: #fa8c16 !important;
}

html.theme-red .theme-icon {
  color: #f5222d !important;
}

/* 主题下拉菜单样式 */
.theme-dropdown {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.08);
  overflow: hidden;
  min-width: 160px;
  max-width: 180px;
}



.theme-options {
  padding: 6px 0;
}

.theme-option {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f5f5f5;
}

.theme-option:last-child {
  border-bottom: none;
}

.theme-option:hover {
  background: rgba(24, 144, 255, 0.05);
}

.theme-option.active {
  background: rgba(24, 144, 255, 0.08);
  border-left: 2px solid var(--primary-color, #1890ff);
}

.theme-option-icon {
  font-size: 16px;
  margin-right: 8px;
  width: 20px;
  text-align: center;
}

.theme-option-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  flex: 1;
}

.theme-option-check {
  color: var(--primary-color, #1890ff);
  font-size: 14px;
  margin-left: 4px;
}

.theme-dropdown-footer {
  padding: 8px 12px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
  background: #fafafa;
}

.reset-button {
  color: #666;
  font-size: 11px;
  padding: 0;
  height: auto;
  line-height: 1;
}

.reset-button:hover {
  color: var(--primary-color, #1890ff);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-dropdown {
    min-width: 140px;
  }
  
  .theme-option {
    padding: 6px 10px;
  }
  
  .theme-option-icon {
    font-size: 14px;
    margin-right: 6px;
  }
  
  .theme-option-name {
    font-size: 12px;
  }
}
</style>
