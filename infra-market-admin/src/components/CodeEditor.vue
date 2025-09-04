<template>
  <div class="code-editor-container" :style="{ height: typeof props.height === 'number' ? props.height + 'px' : props.height }">
    <div ref="editorContainer" class="monaco-editor"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'

// Props
interface Props {
  modelValue: string
  language?: string
  theme?: 'vs' | 'vs-dark' | 'hc-black' | 'auto'
  height?: string | number
  width?: string | number
  readonly?: boolean
  placeholder?: string
  options?: monaco.editor.IStandaloneEditorConstructionOptions
}

const props = withDefaults(defineProps<Props>(), {
  language: 'json',
  theme: 'auto',
  height: 200,
  width: '100%',
  readonly: false,
  placeholder: '',
  options: () => ({})
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string]
  'change': [value: string]
  'focus': []
  'blur': []
}>()

// 响应式数据
const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// 获取当前主题
const getCurrentTheme = (): string => {
  if (props.theme === 'auto') {
    // 检测系统主题
    const isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    return isDark ? 'vs-dark' : 'vs'
  }
  return props.theme
}

// 自动检测语言
const detectLanguage = (value: string): string => {
  if (!value || value.trim() === '') {
    return props.language
  }

  const trimmedValue = value.trim()
  
  // JSON检测
  if ((trimmedValue.startsWith('{') && trimmedValue.endsWith('}')) ||
      (trimmedValue.startsWith('[') && trimmedValue.endsWith(']'))) {
    try {
      JSON.parse(trimmedValue)
      return 'json'
    } catch {
      // 可能是格式错误的JSON，但仍然使用json语言
      return 'json'
    }
  }
  
  // XML检测
  if (trimmedValue.startsWith('<') && trimmedValue.includes('>')) {
    return 'xml'
  }
  
  // SQL检测
  if (trimmedValue.match(/^\s*(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP)\s+/i)) {
    return 'sql'
  }
  
  // Java检测
  if (trimmedValue.includes('public class') || trimmedValue.includes('private ') || 
      trimmedValue.includes('public static void main') || trimmedValue.includes('import java.')) {
    return 'java'
  }
  
  // Kotlin检测
  if (trimmedValue.includes('fun ') || trimmedValue.includes('val ') || 
      trimmedValue.includes('var ') || trimmedValue.includes('class ') && trimmedValue.includes(':')) {
    return 'kotlin'
  }
  
  // JavaScript检测
  if (trimmedValue.includes('function') || trimmedValue.includes('=>') || 
      trimmedValue.includes('const ') || trimmedValue.includes('let ') || 
      trimmedValue.includes('var ')) {
    return 'javascript'
  }
  
  // HTML检测
  if (trimmedValue.includes('<html') || trimmedValue.includes('<!DOCTYPE')) {
    return 'html'
  }
  
  // CSS检测
  if (trimmedValue.includes('{') && trimmedValue.includes('}') && 
      (trimmedValue.includes(':') || trimmedValue.includes(';'))) {
    return 'css'
  }
  
  // YAML检测
  if (trimmedValue.includes(':') && (trimmedValue.includes('-') || trimmedValue.includes('|'))) {
    return 'yaml'
  }
  
  // 默认返回原始语言
  return props.language
}

// 初始化编辑器
const initEditor = async () => {
  if (!editorContainer.value) return

  const currentTheme = getCurrentTheme()
  
  // 设置Monaco Editor主题
  monaco.editor.setTheme(currentTheme)

  // 创建编辑器实例
  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue || '',
    language: detectLanguage(props.modelValue || ''),
    theme: currentTheme,
    readOnly: props.readonly,
    automaticLayout: true,
    minimap: { enabled: true },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    lineNumbers: 'on',
    folding: true,
    lineDecorationsWidth: 10,
    lineNumbersMinChars: 3,
    renderLineHighlight: 'line',
    selectOnLineNumbers: true,
    roundedSelection: false,
    cursorStyle: 'line',
    cursorBlinking: 'blink',
    cursorWidth: 1,
    fontSize: 18,
    fontFamily: 'Monaco, Menlo, "Ubuntu Mono", monospace',
    lineHeight: 28,
    tabSize: 2,
    insertSpaces: true,
    detectIndentation: true,
    renderWhitespace: 'selection',
    renderControlCharacters: false,
    fontLigatures: true,
    bracketPairColorization: { enabled: true },
    guides: {
      bracketPairs: true,
      indentation: true
    },
    ...props.options
  })

  // 监听内容变化
  editor.onDidChangeModelContent(() => {
    const value = editor?.getValue() || ''
    emit('update:modelValue', value)
    emit('change', value)
  })

  // 监听焦点事件
  editor.onDidFocusEditorText(() => {
    emit('focus')
  })

  editor.onDidBlurEditorText(() => {
    emit('blur')
  })

  // 监听语言变化
  editor.onDidChangeModelLanguage(() => {
    const model = editor?.getModel()
    if (model) {
      const detectedLanguage = detectLanguage(model.getValue())
      if (detectedLanguage !== model.getLanguageId()) {
        monaco.editor.setModelLanguage(model, detectedLanguage)
      }
    }
  })

  // 设置占位符
  if (props.placeholder && !props.modelValue) {
    editor.setValue(props.placeholder)
    const model = editor.getModel()
    if (model) {
      const range = model.getFullModelRange()
      editor.setSelection(range)
    }
  }
}

// 更新编辑器值
const updateEditorValue = (newValue: string) => {
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue || '')
    
    // 自动检测并设置语言
    const detectedLanguage = detectLanguage(newValue)
    const model = editor.getModel()
    if (model && detectedLanguage !== model.getLanguageId()) {
      monaco.editor.setModelLanguage(model, detectedLanguage)
    }
  }
}

// 更新编辑器主题
const updateEditorTheme = (newTheme: string) => {
  if (editor) {
    const actualTheme = newTheme === 'auto' ? getCurrentTheme() : newTheme
    monaco.editor.setTheme(actualTheme)
  }
}

// 更新编辑器语言
const updateEditorLanguage = (newLanguage: string) => {
  if (editor) {
    const model = editor.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, newLanguage)
    }
  }
}

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  updateEditorValue(newValue)
})

watch(() => props.theme, (newTheme) => {
  updateEditorTheme(newTheme)
})

watch(() => props.language, (newLanguage) => {
  updateEditorLanguage(newLanguage)
})

watch(() => props.readonly, (newReadonly) => {
  if (editor) {
    editor.updateOptions({ readOnly: newReadonly })
  }
})

watch(() => props.height, () => {
  if (editor) {
    // 延迟执行布局更新，确保DOM已经更新
    nextTick(() => {
      editor?.layout()
    })
  }
})

// 系统主题变化监听
let mediaQuery: MediaQueryList | null = null

// 生命周期
onMounted(async () => {
  await nextTick()
  initEditor()
  
  // 监听系统主题变化（仅在auto模式下）
  if (props.theme === 'auto') {
    mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', () => {
      updateEditorTheme('auto')
    })
  }
})

onUnmounted(() => {
  if (editor) {
    editor.dispose()
    editor = null
  }
  
  // 清理主题监听器
  if (mediaQuery) {
    mediaQuery.removeEventListener('change', () => {
      updateEditorTheme('auto')
    })
    mediaQuery = null
  }
})

// 暴露方法给父组件
defineExpose({
  getEditor: () => editor,
  getValue: () => editor?.getValue() || '',
  setValue: (value: string) => updateEditorValue(value),
  focus: () => editor?.focus(),
  blur: () => editor?.getAction('editor.action.blur')?.run()
})
</script>

<style scoped>
.code-editor-container {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-height: 200px;
}

.code-editor-container:hover {
  border-color: #40a9ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.code-editor-container:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.monaco-editor {
  width: 100%;
  height: 100%;
}

/* 暗色主题样式 */
:global(.vs-dark) .code-editor-container {
  border-color: #434343;
  background: #1e1e1e;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

:global(.vs-dark) .code-editor-container:hover {
  border-color: #40a9ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

:global(.vs-dark) .code-editor-container:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 高对比度主题样式 */
:global(.hc-black) .code-editor-container {
  border-color: #ffffff;
  background: #000000;
  box-shadow: 0 1px 3px rgba(255, 255, 255, 0.1);
}

:global(.hc-black) .code-editor-container:hover {
  border-color: #40a9ff;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.2);
}

:global(.hc-black) .code-editor-container:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .code-editor-container {
    border-radius: 6px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.code-editor-container {
  animation: fadeIn 0.3s ease-out;
}
</style>
