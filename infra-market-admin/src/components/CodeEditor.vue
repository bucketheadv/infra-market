<template>
  <div 
    ref="editorContainer" 
    class="code-editor-container" 
    :style="{ height: typeof props.height === 'number' ? props.height + 'px' : props.height }"
  ></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, shallowRef } from 'vue'
import * as monaco from 'monaco-editor'
import type { editor } from 'monaco-editor'

// Props
interface Props {
  modelValue: string
  language?: string
  theme?: 'light' | 'dark' | 'auto'
  height?: string | number
  width?: string | number
  readonly?: boolean
  placeholder?: string
  options?: any
}

const props = withDefaults(defineProps<Props>(), {
  language: 'json',
  theme: 'light',
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
const editorInstance = shallowRef<editor.IStandaloneCodeEditor>()

// 定义自定义主题（类似 GitHub/Atom 风格）
const defineCustomTheme = () => {
  monaco.editor.defineTheme('github-light', {
    base: 'vs',
    inherit: true,
    rules: [
      // JSON 专用配色
      { token: 'key', foreground: '0451a5' },
      { token: 'string.key.json', foreground: '0451a5' },
      { token: 'string', foreground: 'a31515' },
      { token: 'string.value.json', foreground: 'a31515' },
      { token: 'number', foreground: '098658' },
      { token: 'number.json', foreground: '098658' },
      { token: 'keyword.json', foreground: '0000ff' },
      { token: 'delimiter.bracket', foreground: '000000' },
      { token: 'delimiter.square', foreground: '000000' },
      { token: 'delimiter.brace', foreground: '000000' },
      { token: 'delimiter.comma', foreground: '000000' },
      { token: 'delimiter.colon', foreground: '000000' },
      
      // 代码通用配色
      { token: 'keyword', foreground: 'd73a49' },
      { token: 'identifier', foreground: '24292e' },
      { token: 'variable', foreground: '24292e' },
      { token: 'variable.name', foreground: '24292e' },
      { token: 'type.identifier', foreground: '6f42c1' },
      { token: 'function', foreground: '6f42c1' },
      { token: 'constant', foreground: '005cc5' },
      { token: 'annotation', foreground: '005cc5' },
      { token: 'operator', foreground: 'd73a49' },
      { token: 'regexp', foreground: '032f62' },
      { token: 'comment', foreground: '6a737d', fontStyle: 'italic' },
      { token: 'tag', foreground: '22863a' },
      { token: 'attribute.name', foreground: '6f42c1' },
      { token: 'attribute.value', foreground: 'a31515' },
    ],
    colors: {
      'editor.background': '#ffffff',
      'editor.foreground': '#24292e',
      'editor.lineHighlightBackground': '#f6f8fa',
      'editorLineNumber.foreground': '#959da5',
      'editorLineNumber.activeForeground': '#24292e',
      'editor.selectionBackground': '#add6ff88',
      'editor.inactiveSelectionBackground': '#add6ff44',
      'editor.selectionHighlightBackground': '#fff59d44',
      'editor.findMatchBackground': '#fff59d88',
      'editor.findMatchHighlightBackground': '#fff59d44',
      'editorCursor.foreground': '#0969da',
      'editorBracketMatch.background': '#c8e1ff44',
      'editorBracketMatch.border': '#54aeff66',
      'editorGutter.background': '#fafafa',
      'editorGutter.border': '#e8e8e8'
    }
  })

  monaco.editor.defineTheme('github-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'key', foreground: '79b8ff' },
      { token: 'string.key.json', foreground: '79b8ff' },
      { token: 'string', foreground: '9ecbff' },
      { token: 'string.value.json', foreground: '9ecbff' },
      { token: 'number', foreground: '79c0ff' },
      { token: 'number.json', foreground: '79c0ff' },
      { token: 'keyword.json', foreground: '79c0ff' },
      { token: 'keyword', foreground: 'ff7b72' },
      { token: 'comment', foreground: '8b949e', fontStyle: 'italic' },
      { token: 'function', foreground: 'd2a8ff' },
      { token: 'type.identifier', foreground: 'd2a8ff' },
      { token: 'constant', foreground: '79c0ff' },
    ],
    colors: {
      'editor.background': '#1e1e1e',
      'editor.foreground': '#e6edf3',
      'editor.lineHighlightBackground': '#1f2937',
      'editorLineNumber.foreground': '#6e7681',
      'editorLineNumber.activeForeground': '#e6edf3',
      'editor.selectionBackground': '#3b5070',
      'editorCursor.foreground': '#58a6ff',
    }
  })
}

// 获取语言映射
const getMonacoLanguage = (lang: string): string => {
  const langMap: Record<string, string> = {
    javascript: 'javascript',
    typescript: 'typescript',
    json: 'json',
    xml: 'xml',
    sql: 'sql',
    html: 'html',
    css: 'css',
    java: 'java',
    kotlin: 'kotlin',
    text: 'plaintext',
    yaml: 'yaml'
  }
  
  return langMap[lang.toLowerCase()] || 'json'
}

// 获取主题
const getTheme = (): string => {
  if (props.theme === 'dark') {
    return 'github-dark'
  } else if (props.theme === 'auto') {
    const isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    return isDark ? 'github-dark' : 'github-light'
  }
  return 'github-light'
}

// 等待字体加载完成
const waitForFonts = async (): Promise<void> => {
  try {
    // 等待所有字体加载完成
    await document.fonts.ready
    
    // 额外检查 Intel One Mono 字体是否可用
    const fonts = document.fonts
    let intelOneMonoLoaded = false
    fonts.forEach((font) => {
      if (font.family.includes('Intel One Mono')) {
        intelOneMonoLoaded = true
      }
    })
    
    // 如果字体还没加载，等待一下
    if (!intelOneMonoLoaded) {
      await new Promise(resolve => setTimeout(resolve, 200))
    }
  } catch (error) {
    console.warn('Failed to load Intel One Mono font:', error)
  }
}

// 创建编辑器
const createEditor = async () => {
  if (!editorContainer.value) return

  // 等待字体加载完成
  await waitForFonts()

  // 定义自定义主题
  defineCustomTheme()

  // 应用自定义选项和字体样式 - 使用 Intel One Mono 作为默认字体
  const fontSize = props.options.fontSize || 12
  const fontFamily = props.options.fontFamily || '"Intel One Mono", "SF Mono", Monaco, Menlo, "Courier New", Courier, Consolas, monospace'

  const editorOptions: editor.IStandaloneEditorConstructionOptions = {
    value: props.modelValue || '',
    language: getMonacoLanguage(props.language),
    theme: getTheme(),
    readOnly: props.readonly,
    fontSize: fontSize,
    fontFamily: fontFamily,
    fontLigatures: false,
    lineHeight: 1.6 * fontSize,
    tabSize: 4,
    insertSpaces: true,
    automaticLayout: true,
    disableMonospaceOptimizations: true, // 禁用等宽字体优化，强制重新测量非等宽字体
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    lineNumbers: 'on',
    glyphMargin: false,
    folding: true,
    renderLineHighlight: 'all',
    selectOnLineNumbers: true,
    matchBrackets: 'always',
    autoClosingBrackets: 'always',
    autoClosingQuotes: 'always',
    formatOnPaste: true,
    formatOnType: true,
    smoothScrolling: true,
    cursorBlinking: 'smooth',
    cursorSmoothCaretAnimation: 'on',
    bracketPairColorization: {
      enabled: true
    },
    ...props.options
  }

  editorInstance.value = monaco.editor.create(editorContainer.value, editorOptions)

  // 字体加载完成后，强制重新测量布局
  await nextTick()
  editorInstance.value.layout()

  // 监听内容变化
  editorInstance.value.onDidChangeModelContent(() => {
    const value = editorInstance.value?.getValue() || ''
    emit('update:modelValue', value)
    emit('change', value)
  })

  // 监听焦点事件
  editorInstance.value.onDidFocusEditorText(() => {
    emit('focus')
  })

  editorInstance.value.onDidBlurEditorText(() => {
    emit('blur')
  })
}

// 更新编辑器内容
const updateEditorValue = (newValue: string) => {
  if (!editorInstance.value) return
  
  const currentValue = editorInstance.value.getValue()
  if (currentValue !== newValue) {
    editorInstance.value.setValue(newValue || '')
  }
}

// 更新语言
const updateLanguage = (lang: string) => {
  if (!editorInstance.value) return
  
  const model = editorInstance.value.getModel()
  if (model) {
    monaco.editor.setModelLanguage(model, getMonacoLanguage(lang))
  }
}

// 更新主题
const updateTheme = () => {
  monaco.editor.setTheme(getTheme())
}

// 更新只读状态
const updateReadOnly = (readonly: boolean) => {
  if (!editorInstance.value) return
  editorInstance.value.updateOptions({ readOnly: readonly })
}

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  updateEditorValue(newValue)
})

watch(() => props.language, (newLang) => {
  updateLanguage(newLang)
})

watch(() => props.theme, () => {
  updateTheme()
})

watch(() => props.readonly, (newReadOnly) => {
  updateReadOnly(newReadOnly)
})

watch(() => [props.options?.fontSize, props.options?.fontFamily], () => {
  if (!editorInstance.value) return
  
  const fontSize = props.options.fontSize || 12
  const fontFamily = props.options.fontFamily || '"Intel One Mono", "SF Mono", Monaco, Menlo, "Courier New", Courier, Consolas, monospace'
  
  editorInstance.value.updateOptions({
    fontSize: fontSize,
    fontFamily: fontFamily,
    fontLigatures: false,
    lineHeight: 1.6 * fontSize
  })
})

// 生命周期
onMounted(async () => {
  await nextTick()
  await createEditor()
})

onUnmounted(() => {
  editorInstance.value?.dispose()
  editorInstance.value = undefined
})

// 暴露方法给父组件
defineExpose({
  getEditor: () => editorInstance.value,
  getValue: () => editorInstance.value?.getValue() || '',
  setValue: (value: string) => updateEditorValue(value),
  focus: () => editorInstance.value?.focus(),
  blur: () => {
    const domNode = editorInstance.value?.getDomNode()
    if (domNode) {
      domNode.blur()
    }
  }
})
</script>

<style scoped>
/* 导入 Intel One Mono 字体 */
@font-face {
  font-family: 'Intel One Mono';
  src: url('https://cdn.jsdelivr.net/gh/intel/intel-one-mono@1.3.0/fonts/webfonts/IntelOneMono-Regular.woff2') format('woff2');
  font-weight: 400;
  font-style: normal;
  font-display: swap;
}
@font-face {
  font-family: 'Intel One Mono';
  src: url('https://cdn.jsdelivr.net/gh/intel/intel-one-mono@1.3.0/fonts/webfonts/IntelOneMono-Medium.woff2') format('woff2');
  font-weight: 500;
  font-style: normal;
  font-display: swap;
}
@font-face {
  font-family: 'Intel One Mono';
  src: url('https://cdn.jsdelivr.net/gh/intel/intel-one-mono@1.3.0/fonts/webfonts/IntelOneMono-Bold.woff2') format('woff2');
  font-weight: 700;
  font-style: normal;
  font-display: swap;
}

.code-editor-container {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-height: 200px;
  height: 100%;
}

.code-editor-container:hover {
  border-color: #40a9ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.code-editor-container:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* Monaco Editor 样式覆盖 - 确保字体应用 */
.code-editor-container :deep(.monaco-editor) {
  height: 100%;
}

.code-editor-container :deep(.monaco-editor .margin) {
  background-color: #fafafa;
}

/* 强制应用 Intel One Mono 字体到所有编辑器文本 */
.code-editor-container :deep(.monaco-editor .lines-content),
.code-editor-container :deep(.monaco-editor .view-lines),
.code-editor-container :deep(.monaco-editor .view-line),
.code-editor-container :deep(.monaco-editor .monaco-mouse-cursor-text) {
  font-family: "Intel One Mono", "SF Mono", Monaco, Menlo, "Courier New", Courier, Consolas, monospace !important;
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
