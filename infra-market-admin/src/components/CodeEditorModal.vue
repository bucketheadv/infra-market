<template>
  <a-modal
    v-model:visible="visible"
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
            <a-select-option value="json">JSON</a-select-option>
            <a-select-option value="java">Java</a-select-option>
            <a-select-option value="javascript">JavaScript</a-select-option>
            <a-select-option value="typescript">TypeScript</a-select-option>
            <a-select-option value="kotlin">Kotlin</a-select-option>
            <a-select-option value="html">HTML</a-select-option>
            <a-select-option value="css">CSS</a-select-option>
            <a-select-option value="xml">XML</a-select-option>
            <a-select-option value="sql">SQL</a-select-option>
            <a-select-option value="yaml">YAML</a-select-option>
            <a-select-option value="text">Text</a-select-option>
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
          :height="250"
          :options="{
            minimap: { enabled: true },
            scrollBeyondLastLine: false,
            wordWrap: 'on',
            lineNumbers: 'on',
            folding: true,
            fontSize: 10,
            fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
            lineHeight: 16,
            automaticLayout: true
          }"
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
import CodeEditor from './CodeEditor.vue'

// Props
interface Props {
  visible: boolean
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
  'update:visible': [value: boolean]
  'update:value': [value: string]
  'confirm': [value: string]
  'cancel': []
}>()

// 响应式数据
const visible = ref(props.visible)
const editorValue = ref(props.value)
const selectedLanguage = ref(props.language)
const editorRef = ref()

// 计算属性
const lineCount = computed(() => {
  return editorValue.value.split('\n').length
})

const characterCount = computed(() => {
  return editorValue.value.length
})

const canFormat = computed(() => {
  return ['json', 'java', 'javascript', 'typescript', 'kotlin', 'html', 'css'].includes(selectedLanguage.value)
})

// 监听props变化
watch(() => props.visible, (newVal) => {
  visible.value = newVal
  if (newVal) {
    editorValue.value = props.value
    selectedLanguage.value = props.language
  }
})

watch(visible, (newVal) => {
  emit('update:visible', newVal)
})

watch(() => props.value, (newVal) => {
  if (visible.value) {
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
  // 语言改变时，可以在这里添加额外的逻辑
}

const formatCode = () => {
  if (!canFormat.value) return
  
  try {
    let formatted = editorValue.value
    
    switch (selectedLanguage.value) {
      case 'json':
        const parsed = JSON.parse(editorValue.value)
        formatted = JSON.stringify(parsed, null, 2)
        break
      case 'javascript':
      case 'typescript':
        // 这里可以集成 Prettier 或其他格式化工具
        // 暂时使用简单的缩进格式化
        formatted = editorValue.value
          .split('\n')
          .map(line => line.trim())
          .join('\n')
        break
      case 'html':
      case 'css':
        // 这里可以集成 HTML/CSS 格式化工具
        formatted = editorValue.value
        break
    }
    
    editorValue.value = formatted
  } catch (error) {
    console.error('格式化失败:', error)
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
  height: 60vh;
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
