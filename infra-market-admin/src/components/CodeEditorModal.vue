<template>
  <a-modal
    v-model:open="visible"
    title="代码编辑器"
    width="90%"
    :footer="null"
    @cancel="handleCancel"
    class="code-editor-modal"
  >
    <div class="modal-content">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <a-select
            v-model:value="selectedLanguage"
            style="width: 140px"
            @change="handleLanguageChange"
          >
            <a-select-option value="text">Text</a-select-option>
            <a-select-option value="json">JSON</a-select-option>
            <a-select-option value="xml">XML</a-select-option>
            <a-select-option value="html">HTML</a-select-option>
            <a-select-option value="css">CSS</a-select-option>
            <a-select-option value="javascript">JavaScript</a-select-option>
            <a-select-option value="typescript">TypeScript</a-select-option>
            <a-select-option value="java">Java</a-select-option>
            <a-select-option value="kotlin">Kotlin</a-select-option>
            <a-select-option value="sql">SQL</a-select-option>
            <a-select-option value="yaml">YAML</a-select-option>
          </a-select>
          
          <a-button-group style="margin-left: 12px">
            <a-button @click="formatCode" :disabled="!canFormat">
              <template #icon>
                <span>🎨</span>
              </template>
              格式化
            </a-button>
            <a-button @click="clearCode">
              <template #icon>
                <span>🗑️</span>
              </template>
              清空
            </a-button>
          </a-button-group>
        </div>
        
        <div class="toolbar-right">
          <a-button @click="handleCancel">取消</a-button>
          <a-button type="primary" @click="handleConfirm">确定</a-button>
        </div>
      </div>
      
      <!-- 代码编辑器 -->
      <div class="editor-container">
        <CodeEditor
          ref="editorRef"
          v-model="editorValue"
          :language="selectedLanguage"
          :height="'100%'"
          :options="editorOptions"
        />
      </div>
      
      <!-- 状态栏 -->
      <div class="status-bar">
        <div class="status-left">
          <span class="status-item">
            <span class="status-icon">🌐</span>
            {{ getLanguageLabel(selectedLanguage) }}
          </span>
          <span class="status-item">
            <span class="status-icon">📄</span>
            {{ lineCount }} 行
          </span>
          <span class="status-item">
            <span class="status-icon">🔤</span>
            {{ characterCount }} 字符
          </span>
        </div>
        <div class="status-right">
          <span class="status-item">
            <span class="status-icon">💡</span>
            支持语法高亮和格式化
          </span>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import CodeEditor from './CodeEditor.vue'
import * as prettier from 'prettier/standalone'
import * as prettierParserBabel from 'prettier/parser-babel'
import * as prettierParserHtml from 'prettier/parser-html'
import * as prettierParserCss from 'prettier/parser-postcss'
import * as jsBeautify from 'js-beautify'
import * as sqlFormatter from 'sql-formatter'
import xmlFormatter from 'xml-formatter'

// Props
interface Props {
  open: boolean
  value: string
  language?: string
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  language: 'json',
  placeholder: '请输入代码...'
})

// Emits
const emit = defineEmits<{
  'update:open': [value: boolean]
  'update:value': [value: string]
  'confirm': [value: string]
  'cancel': []
  'language-change': [language: string]
}>()

// 响应式数据
const visible = ref(props.open)
const editorValue = ref(props.value)
const selectedLanguage = ref(props.language)
const editorRef = ref()
const originalValue = ref(props.value) // 保存原始值，用于取消时恢复

// 计算属性
const lineCount = computed(() => {
  return editorValue.value.split('\n').length
})

const characterCount = computed(() => {
  return editorValue.value.length
})

const canFormat = computed(() => {
  return ['json', 'java', 'javascript', 'typescript', 'kotlin', 'html', 'css', 'xml', 'yaml', 'sql'].includes(selectedLanguage.value)
})

// 编辑器配置（避免每次渲染都创建新对象）
const editorOptions = {
  minimap: { enabled: true },
  scrollBeyondLastLine: false,
  wordWrap: 'on',
  lineNumbers: 'on',
  folding: true,
  fontSize: 10,
  fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
  lineHeight: 16,
  automaticLayout: true
}

// 代码类型检测函数
const detectCodeType = (code: string): string => {
  if (!code || code.trim() === '') {
    return props.language || 'json'
  }
  
  const trimmedCode = code.trim()
  
  // JSON检测
  if (trimmedCode.startsWith('{') && trimmedCode.endsWith('}') || 
      trimmedCode.startsWith('[') && trimmedCode.endsWith(']')) {
    try {
      JSON.parse(trimmedCode)
      return 'json'
    } catch (e) {
      // 可能是格式错误的JSON，但仍然返回json
    }
  }
  
  // XML检测
  if (trimmedCode.startsWith('<') && trimmedCode.includes('>')) {
    return 'xml'
  }
  
  // SQL检测
  const sqlKeywords = ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'CREATE', 'DROP', 'ALTER', 'FROM', 'WHERE', 'JOIN']
  const upperCode = trimmedCode.toUpperCase()
  if (sqlKeywords.some(keyword => upperCode.includes(keyword))) {
    return 'sql'
  }
  
  // YAML检测
  if (trimmedCode.includes(':') && (trimmedCode.includes('-') || trimmedCode.includes('|'))) {
    return 'yaml'
  }
  
  // HTML检测
  if (trimmedCode.includes('<html') || trimmedCode.includes('<div') || trimmedCode.includes('<p>')) {
    return 'html'
  }
  
  // CSS检测
  if (trimmedCode.includes('{') && trimmedCode.includes('}') && 
      (trimmedCode.includes('color:') || trimmedCode.includes('margin:') || trimmedCode.includes('padding:'))) {
    return 'css'
  }
  
  // JavaScript检测
  if (trimmedCode.includes('function') || trimmedCode.includes('const ') || trimmedCode.includes('let ') || 
      trimmedCode.includes('var ') || trimmedCode.includes('=>')) {
    return 'javascript'
  }
  
  // TypeScript检测
  if (trimmedCode.includes('interface ') || trimmedCode.includes('type ') || 
      trimmedCode.includes(': string') || trimmedCode.includes(': number')) {
    return 'typescript'
  }
  
  // Java检测
  if (trimmedCode.includes('public class') || trimmedCode.includes('private ') || 
      trimmedCode.includes('System.out.println')) {
    return 'java'
  }
  
  // Kotlin检测
  if (trimmedCode.includes('fun ') || trimmedCode.includes('val ') || 
      trimmedCode.includes('var ') && trimmedCode.includes(':')) {
    return 'kotlin'
  }
  
  // 默认返回传入的语言或json
  return props.language || 'json'
}

// 监听props变化
watch(() => props.open, (newVal) => {
  visible.value = newVal
  if (newVal) {
    // 弹窗打开时，保存原始值并设置编辑器值
    originalValue.value = props.value
    editorValue.value = props.value
    // 优先使用传入的语言，如果没有则自动检测代码类型
    if (props.language) {
      selectedLanguage.value = props.language
    } else {
      const detectedLanguage = detectCodeType(props.value)
      selectedLanguage.value = detectedLanguage
    }
    
    // 弹窗打开后，延迟更新编辑器布局
    nextTick(() => {
      setTimeout(() => {
        const editor = editorRef.value?.getEditor()
        if (editor) {
          editor.requestMeasure()
        }
      }, 100)
    })
  }
})

watch(visible, (newVal) => {
  emit('update:open', newVal)
})

// 只在弹窗关闭时才响应外部value的变化
watch(() => props.value, (newVal) => {
  if (!visible.value) {
    editorValue.value = newVal
  }
})

// 方法
const getLanguageLabel = (lang: string): string => {
  const labels: Record<string, string> = {
    json: 'JSON',
    java: 'Java',
    javascript: 'JavaScript',
    typescript: 'TypeScript',
    kotlin: 'Kotlin',
    html: 'HTML',
    css: 'CSS',
    xml: 'XML',
    sql: 'SQL',
    yaml: 'YAML',
    text: 'Text'
  }
  return labels[lang] || lang.toUpperCase()
}

const handleLanguageChange = () => {
  // 语言改变时，触发语言变化事件
  emit('language-change', selectedLanguage.value)
}

const formatCode = async () => {
  if (!canFormat.value) return
  
  try {
    let formatted = editorValue.value
    
    switch (selectedLanguage.value) {
      case 'json':
        try {
          const parsed = JSON.parse(editorValue.value)
          formatted = JSON.stringify(parsed, null, 2)
        } catch (parseError) {
          message.error('JSON格式错误，无法格式化')
          return
        }
        break
        
      case 'javascript':
      case 'typescript':
        try {
          formatted = await prettier.format(editorValue.value, {
            parser: 'babel',
            plugins: [prettierParserBabel],
            semi: true,
            singleQuote: true,
            tabWidth: 4,
            trailingComma: 'es5'
          })
        } catch (error) {
          // 如果Prettier失败，使用js-beautify作为备用
          formatted = jsBeautify.js(editorValue.value, {
            indent_size: 4,
            indent_char: ' ',
            max_preserve_newlines: 2,
            preserve_newlines: true,
            keep_array_indentation: false,
            break_chained_methods: false,
            indent_scripts: 'normal',
            brace_style: 'collapse',
            space_before_conditional: true,
            unescape_strings: false,
            jslint_happy: false,
            end_with_newline: true,
            wrap_line_length: 0,
            indent_inner_html: false,
            comma_first: false,
            e4x: false,
            indent_empty_lines: false
          })
        }
        break
        
      case 'html':
        try {
          formatted = await prettier.format(editorValue.value, {
            parser: 'html',
            plugins: [prettierParserHtml],
            tabWidth: 4,
            printWidth: 80,
            htmlWhitespaceSensitivity: 'css'
          })
        } catch (error) {
          // 如果Prettier失败，使用js-beautify作为备用
          formatted = jsBeautify.html(editorValue.value, {
            indent_size: 4,
            indent_char: ' ',
            max_preserve_newlines: 2,
            preserve_newlines: true,
            keep_array_indentation: false,
            break_chained_methods: false,
            indent_scripts: 'normal',
            brace_style: 'collapse',
            space_before_conditional: true,
            unescape_strings: false,
            jslint_happy: false,
            end_with_newline: true,
            wrap_line_length: 0,
            indent_inner_html: false,
            comma_first: false,
            e4x: false,
            indent_empty_lines: false
          })
        }
        break
        
      case 'css':
        try {
          formatted = await prettier.format(editorValue.value, {
            parser: 'css',
            plugins: [prettierParserCss],
            tabWidth: 4,
            printWidth: 80
          })
        } catch (error) {
          // 如果Prettier失败，使用js-beautify作为备用
          formatted = jsBeautify.css(editorValue.value, {
            indent_size: 4,
            indent_char: ' ',
            max_preserve_newlines: 2,
            preserve_newlines: true,
            keep_array_indentation: false,
            break_chained_methods: false,
            indent_scripts: 'normal',
            brace_style: 'collapse',
            space_before_conditional: true,
            unescape_strings: false,
            jslint_happy: false,
            end_with_newline: true,
            wrap_line_length: 0,
            indent_inner_html: false,
            comma_first: false,
            e4x: false,
            indent_empty_lines: false
          })
        }
        break
        
      case 'xml':
        try {
          formatted = xmlFormatter(editorValue.value, {
            indentation: '    ',
            filter: (node: any) => node.type !== 'Comment',
            collapseContent: true,
            lineSeparator: '\n'
          })
        } catch (error) {
          message.error('XML格式错误，无法格式化')
          return
        }
        break
        
      case 'yaml':
        try {
          formatted = await prettier.format(editorValue.value, {
            parser: 'yaml',
            tabWidth: 4,
            printWidth: 80
          })
        } catch (error) {
          message.error('YAML格式错误，无法格式化')
          return
        }
        break
        
      case 'java':
      case 'kotlin':
        // Java和Kotlin使用js-beautify的JavaScript格式化器作为近似
        formatted = jsBeautify.js(editorValue.value, {
          indent_size: 4,
          indent_char: ' ',
          max_preserve_newlines: 2,
          preserve_newlines: true,
          keep_array_indentation: false,
          break_chained_methods: false,
          indent_scripts: 'normal',
          brace_style: 'collapse',
          space_before_conditional: true,
          unescape_strings: false,
          jslint_happy: false,
          end_with_newline: true,
          wrap_line_length: 0,
          indent_inner_html: false,
          comma_first: false,
          e4x: false,
          indent_empty_lines: false
        })
        break
        
      case 'sql':
        try {
          formatted = sqlFormatter.format(editorValue.value, {
            language: 'sql',
            tabWidth: 4,
            useTabs: false,
            keywordCase: 'upper',
            functionCase: 'upper',
            dataTypeCase: 'upper',
            linesBetweenQueries: 2
          })
        } catch (error) {
          message.error('SQL格式错误，无法格式化')
          return
        }
        break
        
      default:
        message.info('当前语言暂不支持格式化')
        return
    }
    
    editorValue.value = formatted
    message.success('格式化完成')
  } catch (error) {
    console.error('格式化失败:', error)
    message.error('格式化失败，请检查代码语法')
  }
}

const clearCode = () => {
  editorValue.value = ''
}


const handleConfirm = () => {
  emit('update:value', editorValue.value)
  emit('confirm', editorValue.value)
  visible.value = false
}

const handleCancel = () => {
  // 取消时只关闭弹窗，不更新值（下次打开时会自动从props.value恢复）
  emit('cancel')
  visible.value = false
}

// 暴露方法给父组件
defineExpose({
  focus: () => {
    nextTick(() => {
      editorRef.value?.focus()
    })
  }
})
</script>

<style scoped>
.code-editor-modal :deep(.ant-modal-body) {
  padding: 0;
}

.modal-content {
  display: flex;
  flex-direction: column;
  height: 75vh;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px 8px 0 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.editor-container {
  flex: 1;
  padding: 20px 24px;
  background: #ffffff;
  border-radius: 0 0 8px 8px;
  min-height: 0; /* 确保flex子元素可以收缩 */
  display: flex;
  flex-direction: column;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-top: 1px solid #e8e8e8;
  font-size: 13px;
  color: #495057;
  font-weight: 500;
  border-radius: 0 0 8px 8px;
}

.status-left,
.status-right {
  display: flex;
  align-items: center;
}

.status-item {
  display: flex;
  align-items: center;
  margin-right: 16px;
}

.status-item:last-child {
  margin-right: 0;
}

.status-icon {
  margin-right: 6px;
  font-size: 14px;
}

.status-item:last-child {
  margin-right: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .code-editor-modal :deep(.ant-modal) {
    width: 95% !important;
    max-width: none;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .toolbar-left {
    justify-content: center;
  }
  
  .toolbar-right {
    justify-content: center;
  }
  
  .status-bar {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .status-item {
    margin-right: 8px;
  }
}
</style>
