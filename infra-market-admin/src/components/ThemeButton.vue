<template>
  <button
    :class="[
      'theme-button',
      `theme-button--${variant}`,
      `theme-button--${size}`,
      { 'theme-button--disabled': disabled }
    ]"
    :disabled="disabled"
    @click="handleClick"
  >
    <span v-if="icon" class="theme-button__icon">
      <component :is="icon" />
    </span>
    <span v-if="$slots.default" class="theme-button__content">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

interface Props {
  variant?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'ghost'
  size?: 'small' | 'medium' | 'large'
  disabled?: boolean
  icon?: any
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'medium',
  disabled: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.currentTheme)

const handleClick = (event: MouseEvent) => {
  if (!props.disabled) {
    emit('click', event)
  }
}
</script>

<style scoped>
.theme-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 6px;
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
  user-select: none;
  outline: none;
}

.theme-button:focus {
  outline: none;
}

.theme-button--disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 尺寸变体 */
.theme-button--small {
  padding: 4px 10px;
  font-size: 12px;
  min-height: 24px;
}

.theme-button--medium {
  padding: 6px 14px;
  font-size: 13px;
  min-height: 30px;
}

.theme-button--large {
  padding: 8px 18px;
  font-size: 14px;
  min-height: 36px;
}

/* 主要变体 - 使用主题色彩 */
.theme-button--primary {
  background: var(--primary-color, #1890ff);
  color: white;
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.theme-button--primary:hover:not(.theme-button--disabled) {
  background: var(--secondary-color, #40a9ff);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--shadow-color, rgba(24, 144, 255, 0.3));
}

.theme-button--primary:active:not(.theme-button--disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 4px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

/* 次要变体 */
.theme-button--secondary {
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary-color, #1890ff);
  backdrop-filter: blur(10px);
}

.theme-button--secondary:hover:not(.theme-button--disabled) {
  background: var(--primary-color, #1890ff);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--shadow-color, rgba(24, 144, 255, 0.3));
}

/* 成功变体 */
.theme-button--success {
  background: #52c41a;
  color: white;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.2);
}

.theme-button--success:hover:not(.theme-button--disabled) {
  background: #73d13d;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(82, 196, 26, 0.3);
}

/* 警告变体 */
.theme-button--warning {
  background: #fa8c16;
  color: white;
  box-shadow: 0 2px 4px rgba(250, 140, 22, 0.2);
}

.theme-button--warning:hover:not(.theme-button--disabled) {
  background: #ffa940;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(250, 140, 22, 0.3);
}

/* 危险变体 */
.theme-button--danger {
  background: #f5222d;
  color: white;
  box-shadow: 0 2px 4px rgba(245, 34, 45, 0.2);
}

.theme-button--danger:hover:not(.theme-button--disabled) {
  background: #ff4d4f;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(245, 34, 45, 0.3);
}

/* 幽灵变体 */
.theme-button--ghost {
  background: transparent;
  color: var(--primary-color, #1890ff);
}

.theme-button--ghost:hover:not(.theme-button--disabled) {
  background: var(--primary-color, #1890ff);
  color: white;
  transform: translateY(-1px);
}

/* 图标样式 */
.theme-button__icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-button--small .theme-button__icon {
  font-size: 12px;
}

.theme-button--medium .theme-button__icon {
  font-size: 14px;
}

.theme-button--large .theme-button__icon {
  font-size: 16px;
}

/* 内容样式 */
.theme-button__content {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 主题特定样式 */
:root.theme-default .theme-button--primary {
  background: #1890ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

:root.theme-default .theme-button--primary:hover:not(.theme-button--disabled) {
  background: #40a9ff;
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

:root.theme-dark .theme-button--primary {
  background: #722ed1;
  box-shadow: 0 2px 4px rgba(114, 46, 209, 0.2);
}

:root.theme-dark .theme-button--primary:hover:not(.theme-button--disabled) {
  background: #9254de;
  box-shadow: 0 4px 8px rgba(114, 46, 209, 0.3);
}

:root.theme-green .theme-button--primary {
  background: #52c41a;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.2);
}

:root.theme-green .theme-button--primary:hover:not(.theme-button--disabled) {
  background: #73d13d;
  box-shadow: 0 4px 8px rgba(82, 196, 26, 0.3);
}

:root.theme-orange .theme-button--primary {
  background: #fa8c16;
  box-shadow: 0 2px 4px rgba(250, 140, 22, 0.2);
}

:root.theme-orange .theme-button--primary:hover:not(.theme-button--disabled) {
  background: #ffa940;
  box-shadow: 0 4px 8px rgba(250, 140, 22, 0.3);
}

:root.theme-red .theme-button--primary {
  background: #f5222d;
  box-shadow: 0 2px 4px rgba(245, 34, 45, 0.2);
}

:root.theme-red .theme-button--primary:hover:not(.theme-button--disabled) {
  background: #ff4d4f;
  box-shadow: 0 4px 8px rgba(245, 34, 45, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-button--medium {
    padding: 5px 12px;
    font-size: 12px;
    min-height: 28px;
  }
  
  .theme-button--large {
    padding: 7px 16px;
    font-size: 13px;
    min-height: 32px;
  }
}
</style>
