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
      { token: 'key', foreground: '0451a5', fontStyle: 'bold' },
      { token: 'string.key.json', foreground: '0451a5', fontStyle: 'bold' },
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
      { token: 'key', foreground: '79b8ff', fontStyle: 'bold' },
      { token: 'string.key.json', foreground: '79b8ff', fontStyle: 'bold' },
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

  // 监听粘贴事件
  editorInstance.value.onDidPaste((e) => {
    handlePaste(e)
  })

  // 添加双击选中引号内字符串的功能
  setupDoubleClickSelection()
}

// 查找引号的开始和结束位置
const findQuoteRange = (lineContent: string, column: number): { start: number, end: number } | null => {
  let start = -1
  let end = -1
  
  // 向前查找起始引号（从双击位置向左）
  let escapeCount = 0
  for (let i = column - 2; i >= 0; i--) {
    if (lineContent[i] === '\\') {
      escapeCount++
    } else {
      if (lineContent[i] === '"' && escapeCount % 2 === 0) {
        start = i
        break
      }
      escapeCount = 0
    }
  }
  
  // 如果没有找到起始引号，返回null
  if (start === -1) return null
  
  // 向后查找结束引号（从双击位置向右）
  escapeCount = 0
  for (let i = column - 1; i < lineContent.length; i++) {
    if (lineContent[i] === '\\') {
      escapeCount++
    } else {
      if (lineContent[i] === '"' && escapeCount % 2 === 0) {
        end = i
        break
      }
      escapeCount = 0
    }
  }
  
  // 如果没有找到结束引号，返回null
  if (end === -1) return null
  
  return { start, end }
}

// 设置双击选中引号内字符串的功能
const setupDoubleClickSelection = () => {
  if (!editorInstance.value) return
  
  const domNode = editorInstance.value.getDomNode()
  if (!domNode) return
  
  // 监听双击事件
  domNode.addEventListener('dblclick', (e: MouseEvent) => {
    if (!editorInstance.value) return
    
    const target = editorInstance.value.getTargetAtClientPoint(e.clientX, e.clientY)
    if (!target || !target.position) return
    
    const model = editorInstance.value.getModel()
    if (!model) return
    
    const position = target.position
    const lineContent = model.getLineContent(position.lineNumber)
    
    // 检查是否在引号内或双击的是引号本身
    const isInString = isInsideString(model, position)
    const charAtPosition = lineContent[position.column - 1]
    
    // 如果不在引号内且双击的不是引号，则返回
    if (!isInString && charAtPosition !== '"') return
    
    // 查找引号范围
    const range = findQuoteRange(lineContent, position.column)
    if (!range) return
    
    // 选中引号内的内容（不包括引号本身）
    // range.start和range.end是字符串索引（从0开始）
    // Monaco的列号从1开始，所以需要+1
    // 再+1跳过开始引号，end不需要再+1因为我们想选到end之前（不包括结束引号）
    const selection = new monaco.Selection(
      position.lineNumber,
      range.start + 2, // +1转换为列号，再+1跳过开始引号
      position.lineNumber,
      range.end + 1    // +1转换为列号，这样就不包括结束引号
    )
    
    editorInstance.value.setSelection(selection)
    editorInstance.value.revealPositionInCenter(position)
    
    // 阻止默认的双击选中行为
    e.preventDefault()
    e.stopPropagation()
  })
}

// 转义字符串中的特殊字符
const escapeString = (str: string): string => {
  return str
    .replace(/\\/g, '\\\\')  // 反斜杠
    .replace(/"/g, '\\"')     // 双引号
    .replace(/\n/g, '\\n')    // 换行符
    .replace(/\r/g, '\\r')    // 回车符
    .replace(/\t/g, '\\t')    // 制表符
}

// 检测光标是否在字符串内
const isInsideString = (model: monaco.editor.ITextModel, position: monaco.Position): boolean => {
  const lineContent = model.getLineContent(position.lineNumber)
  const beforeCursor = lineContent.substring(0, position.column - 1)
  
  // 统计双引号数量，如果为奇数则在字符串内
  let doubleQuoteCount = 0
  let inEscape = false
  
  for (let i = 0; i < beforeCursor.length; i++) {
    const char = beforeCursor[i]
    
    if (inEscape) {
      inEscape = false
      continue
    }
    
    if (char === '\\') {
      inEscape = true
      continue
    }
    
    if (char === '"') {
      doubleQuoteCount++
    }
  }
  
  return doubleQuoteCount % 2 === 1
}

// 处理粘贴事件
const handlePaste = (e: any) => {
  if (!editorInstance.value) return
  
  const model = editorInstance.value.getModel()
  if (!model) return
  
  const selection = editorInstance.value.getSelection()
  if (!selection) return
  
  // 获取粘贴的位置
  const position = selection.getStartPosition()
  
  // 检查是否在字符串内
  if (!isInsideString(model, position)) {
    return // 不在字符串内，不处理
  }
  
  // 获取粘贴的范围
  const range = e.range
  if (!range) return
  
  // 延迟处理，让粘贴操作先完成
  setTimeout(() => {
    if (!editorInstance.value) return
    const model = editorInstance.value.getModel()
    if (!model) return
    
    // 获取粘贴后的内容
    const pastedText = model.getValueInRange(range)
    
    // 检查是否需要转义（如果包含需要转义的字符）
    if (/["\\\\n\r\t]/.test(pastedText)) {
      const escapedText = escapeString(pastedText)
      
      // 替换粘贴的内容为转义后的内容
      editorInstance.value.executeEdits('paste', [{
        range: range,
        text: escapedText
      }])
      
      // 设置光标位置到转义后文本的末尾
      const newPosition = new monaco.Position(
        range.endLineNumber,
        range.startColumn + escapedText.length
      )
      editorInstance.value.setPosition(newPosition)
    }
  }, 10)
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

// 处理窗口大小变化
const handleResize = () => {
  if (editorInstance.value) {
    // 延迟执行以确保DOM更新完成
    setTimeout(() => {
      editorInstance.value?.layout()
    }, 100)
  }
}

// 生命周期
onMounted(async () => {
  await nextTick()
  await createEditor()
  
  // 添加窗口大小变化监听器
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 移除事件监听器
  window.removeEventListener('resize', handleResize)
  
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

/* Hover 提示框字体 */
:deep(.monaco-hover) {
  font-family: "Intel One Mono", "SF Mono", Monaco, Menlo, "Courier New", Courier, Consolas, monospace !important;
}

:deep(.monaco-hover .monaco-hover-content) {
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
