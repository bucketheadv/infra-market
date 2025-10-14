<template>
  <div 
    ref="editorContainer" 
    class="code-editor-container" 
    :style="{ height: typeof props.height === 'number' ? props.height + 'px' : props.height }"
  ></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, shallowRef } from 'vue'
import { EditorView } from 'codemirror'
import { EditorState, type Extension } from '@codemirror/state'
import { javascript } from '@codemirror/lang-javascript'
import { json } from '@codemirror/lang-json'
import { xml } from '@codemirror/lang-xml'
import { sql } from '@codemirror/lang-sql'
import { html } from '@codemirror/lang-html'
import { css } from '@codemirror/lang-css'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'
import { keymap, lineNumbers, highlightActiveLineGutter, highlightSpecialChars, drawSelection, rectangularSelection, highlightActiveLine } from '@codemirror/view'
import { indentWithTab, defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { indentOnInput, bracketMatching, syntaxHighlighting, HighlightStyle } from '@codemirror/language'
import { tags as t } from '@lezer/highlight'
import { searchKeymap, highlightSelectionMatches } from '@codemirror/search'
import { closeBrackets, closeBracketsKeymap } from '@codemirror/autocomplete'

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
const editorView = shallowRef<EditorView>()

// 自定义语法高亮配色方案（类似 GitHub/Atom 风格，优化JSON显示）
const customHighlightStyle = HighlightStyle.define([
  // JSON 专用配色
  { tag: t.propertyName, color: '#0451a5' },                          // JSON 键名：深蓝色
  { tag: [t.string], color: '#a31515' },                              // 字符串值：红色
  { tag: [t.number], color: '#098658' },                              // 数字：绿色
  { tag: [t.bool, t.null], color: '#0000ff' },                        // 布尔值/null：蓝色
  { tag: [t.bracket, t.brace, t.squareBracket], color: '#000000' },   // 括号：黑色
  { tag: [t.separator], color: '#000000' },                           // 分隔符（逗号冒号）：黑色
  
  // 代码通用配色
  { tag: t.keyword, color: '#d73a49' },                              // 关键字：玫瑰红
  { tag: [t.name, t.deleted, t.character, t.macroName], color: '#24292e' }, // 变量名：深灰
  { tag: [t.function(t.variableName)], color: '#6f42c1' },           // 函数名：紫色
  { tag: [t.labelName], color: '#22863a' },                          // 标签：绿色
  { tag: [t.color, t.constant(t.name), t.standard(t.name)], color: '#005cc5' }, // 常量：蓝色
  { tag: [t.definition(t.name)], color: '#24292e' },                 // 定义：深灰
  { tag: [t.typeName, t.className], color: '#6f42c1' },              // 类型/类名：紫色
  { tag: [t.changed, t.annotation], color: '#005cc5' },              // 注解：蓝色
  { tag: [t.modifier, t.self, t.namespace], color: '#d73a49' },      // 修饰符：玫瑰红
  { tag: [t.operator, t.operatorKeyword], color: '#d73a49' },        // 操作符：玫瑰红
  { tag: [t.url, t.escape, t.regexp, t.link], color: '#032f62' },    // URL/正则：深蓝
  { tag: [t.meta, t.comment], color: '#6a737d', fontStyle: 'italic' }, // 注释：灰色斜体
  { tag: t.strong, fontWeight: 'bold', color: '#24292e' },            // 加粗
  { tag: t.emphasis, fontStyle: 'italic' },                          // 斜体
  { tag: t.strikethrough, textDecoration: 'line-through' },           // 删除线
  { tag: t.heading, fontWeight: 'bold', color: '#005cc5' },           // 标题：蓝色加粗
  { tag: [t.atom, t.special(t.variableName)], color: '#005cc5' },    // 特殊变量：蓝色
  { tag: [t.processingInstruction, t.inserted], color: '#032f62' },  // 处理指令：深蓝
  { tag: [t.special(t.string)], color: '#22863a' },                   // 特殊字符串：绿色
  { tag: t.invalid, color: '#cb2431' }                                // 无效：红色
])

// 获取语言扩展
const getLanguageExtension = (lang: string): Extension => {
  const langMap: Record<string, () => Extension> = {
    javascript: () => javascript(),
    typescript: () => javascript({ typescript: true }),
    json: () => json(),
    xml: () => xml(),
    sql: () => sql(),
    html: () => html(),
    css: () => css(),
    java: () => java(),
    kotlin: () => java(), // 暂时使用Java高亮
    text: () => [],
    yaml: () => []
  }
  
  const extension = langMap[lang.toLowerCase()]
  return extension ? extension() : json()
}

// 获取主题
const getTheme = (): Extension => {
  if (props.theme === 'dark') {
    return oneDark
  } else if (props.theme === 'auto') {
    const isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    return isDark ? oneDark : []
  }
  return []
}

// 获取只读扩展
const getReadOnlyExtension = (): Extension => {
  if (props.readonly) {
    return EditorView.editable.of(false)
  }
  return []
}

// 获取基础编辑器扩展（不包括语言扩展）
const getBasicExtensions = (): Extension[] => {
  return [
    lineNumbers(),
    highlightActiveLineGutter(),
    highlightSpecialChars(),
    history(),
    drawSelection(),
    EditorState.allowMultipleSelections.of(true),
    indentOnInput(),
    bracketMatching(),
    closeBrackets(),
    rectangularSelection(),
    highlightActiveLine(),
    highlightSelectionMatches(),
    syntaxHighlighting(customHighlightStyle, { fallback: true }), // 使用自定义语法高亮
    keymap.of([
      ...closeBracketsKeymap,
      ...defaultKeymap,
      ...searchKeymap,
      ...historyKeymap,
      indentWithTab
    ])
  ]
}

// 创建编辑器
const createEditor = () => {
  if (!editorContainer.value) return

  const languageExt = getLanguageExtension(props.language)
  
  const extensions: Extension[] = [
    ...getBasicExtensions(),
    languageExt, // 语言扩展，提供语法高亮
    getTheme(),
    getReadOnlyExtension(),
    EditorView.lineWrapping,
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        const value = update.state.doc.toString()
        emit('update:modelValue', value)
        emit('change', value)
      }
    }),
    EditorView.domEventHandlers({
      focus: () => {
        emit('focus')
      },
      blur: () => {
        emit('blur')
      }
    })
  ]

  // 应用自定义选项和字体样式
  const fontSize = props.options.fontSize || 12 // 默认12px
  const fontFamily = props.options.fontFamily || '"Ubuntu Mono", "Courier New", Courier, Monaco, Consolas, monospace'
  
  extensions.push(EditorView.theme({
    '&': { 
      fontSize: `${fontSize}px`,
      lineHeight: '1.6'
    },
    '.cm-content, .cm-line': { 
      fontFamily: fontFamily,
      fontSize: `${fontSize}px`,
      lineHeight: '1.6'
    },
    '.cm-gutters': {
      fontSize: `${fontSize}px`,
      backgroundColor: '#fafafa',
      borderRight: '1px solid #e8e8e8'
    },
    // 改进配色
    '.cm-activeLine': {
      backgroundColor: '#f0f9ff'
    },
    '.cm-activeLineGutter': {
      backgroundColor: '#e6f4ff'
    },
    '.cm-selectionBackground, ::selection': {
      backgroundColor: '#b3d9ff !important'
    },
    '.cm-focused .cm-selectionBackground, .cm-focused ::selection': {
      backgroundColor: '#b3d9ff !important'
    }
  }))

  const state = EditorState.create({
    doc: props.modelValue || '',
    extensions
  })

  editorView.value = new EditorView({
    state,
    parent: editorContainer.value
  })
}

// 更新编辑器内容
const updateEditorValue = (newValue: string) => {
  if (!editorView.value) return
  
  const currentValue = editorView.value.state.doc.toString()
  if (currentValue !== newValue) {
    editorView.value.dispatch({
      changes: {
        from: 0,
        to: currentValue.length,
        insert: newValue || ''
      }
    })
  }
}

// 重新配置编辑器（通过重新创建）
const reconfigureEditor = () => {
  if (!editorView.value) return
  
  // 保存当前值
  const currentValue = editorView.value.state.doc.toString()
  
  // 销毁旧编辑器
  editorView.value.destroy()
  
  // 创建新编辑器（这会重新设置editorView.value）
  nextTick(() => {
    createEditor()
    
    // 恢复值
    nextTick(() => {
      if (currentValue && editorView.value) {
        updateEditorValue(currentValue)
      }
    })
  })
}

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  updateEditorValue(newValue)
})

watch(() => [props.language, props.theme, props.readonly], () => {
  reconfigureEditor()
})

// 单独监听 options 中的关键属性，避免深度监听导致频繁重建
watch(() => [props.options?.fontSize, props.options?.fontFamily], () => {
  reconfigureEditor()
})

watch(() => props.height, () => {
  nextTick(() => {
    editorView.value?.requestMeasure()
  })
})

// 生命周期
onMounted(async () => {
  await nextTick()
  createEditor()
})

onUnmounted(() => {
  editorView.value?.destroy()
  editorView.value = undefined
})

// 暴露方法给父组件
defineExpose({
  getEditor: () => editorView.value,
  getValue: () => editorView.value?.state.doc.toString() || '',
  setValue: (value: string) => updateEditorValue(value),
  focus: () => editorView.value?.focus(),
  blur: () => editorView.value?.contentDOM.blur()
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

/* CodeMirror样式覆盖 */
.code-editor-container :deep(.cm-editor) {
  height: 100%;
  font-size: 12px;
}

.code-editor-container :deep(.cm-scroller) {
  overflow: auto;
  font-family: "Ubuntu Mono", "Courier New", Courier, Monaco, Consolas, monospace;
  font-size: 12px;
}

.code-editor-container :deep(.cm-content) {
  padding: 8px 0;
  font-size: 12px;
  line-height: 1.6;
  font-family: "Ubuntu Mono", "Courier New", Courier, Monaco, Consolas, monospace;
}

.code-editor-container :deep(.cm-line) {
  padding: 0 8px;
  line-height: 1.6;
  font-size: 12px;
  font-family: "Ubuntu Mono", "Courier New", Courier, Monaco, Consolas, monospace;
}

.code-editor-container :deep(.cm-gutters) {
  background-color: #fafafa;
  border-right: 1px solid #e8e8e8;
  font-size: 12px;
  font-family: "Ubuntu Mono", "Courier New", Courier, Monaco, Consolas, monospace;
}

/* 改进的配色方案 */
.code-editor-container :deep(.cm-activeLine) {
  background-color: #f0f9ff;
}

.code-editor-container :deep(.cm-activeLineGutter) {
  background-color: #e6f4ff;
}

.code-editor-container :deep(.cm-selectionBackground) {
  background-color: #b3d9ff !important;
}

.code-editor-container :deep(.cm-focused .cm-selectionBackground) {
  background-color: #b3d9ff !important;
}

.code-editor-container :deep(.cm-cursor) {
  border-left: 2px solid #1890ff;
}

/* 暗色主题样式 */
.code-editor-container:has(.cm-theme-dark) {
  background: #1e1e1e;
  border-color: #434343;
}

.code-editor-container:has(.cm-theme-dark):hover {
  border-color: #40a9ff;
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
