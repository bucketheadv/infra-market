<template>
  <div class="component-renderer">
    <!-- 单个组件 -->
    <div v-if="!getIsArray()" class="component-single">
      <a-card size="small" class="component-card">
        <template #title>
          <span>{{ field.label || field.name }}</span>
        </template>
        <div v-if="component">
          <a-form-item
            v-for="compField in sortedComponentFields"
            :key="compField.name || ''"
            :label="compField.label || compField.name"
            :name="getFieldName(compField.name || '')"
            :rules="getFieldRules(compField)"
          >
            <FieldRenderer
              :field="compField"
              :value="getFieldValue(compField.name || '')"
              :component-cache="componentCache"
              @update:value="(val: any) => updateFieldValue(compField.name || '', val)"
            />
            <div v-if="compField.description" class="form-help-text">
              {{ compField.description }}
            </div>
          </a-form-item>
        </div>
        <div v-else class="component-loading">
          加载组件中...
        </div>
      </a-card>
    </div>
    
    <!-- 数组组件 -->
    <div v-else class="component-array">
      <div class="component-array-header">
        <span class="component-array-title">{{ field.label || field.name }}</span>
        <a-button
          v-if="getAllowDynamic()"
          type="primary"
          size="small"
          :icon="h(PlusOutlined)"
          @click="handleAddItem"
        >
          添加
        </a-button>
      </div>
      <div v-if="component">
        <a-card
          v-for="(_, index) in arrayValue"
          :key="index"
          size="small"
          class="component-card component-array-item"
        >
          <template #title>
            <span>{{ field.label || field.name }} #{{ index + 1 }}</span>
          </template>
          <template #extra>
            <a-button
              v-if="getAllowDynamic()"
              type="text"
              danger
              size="small"
              :icon="h(DeleteOutlined)"
              @click="handleRemoveItem(index)"
            >
              删除
            </a-button>
          </template>
          <a-form-item
            v-for="compField in sortedComponentFields"
            :key="compField.name || ''"
            :label="compField.label || compField.name"
            :name="getArrayFieldName(index, compField.name || '')"
            :rules="getFieldRules(compField)"
          >
            <FieldRenderer
              :field="compField"
              :value="getArrayFieldValue(index, compField.name || '')"
              :component-cache="componentCache"
              @update:value="(val: any) => updateArrayFieldValue(index, compField.name || '', val)"
            />
            <div v-if="compField.description" class="form-help-text">
              {{ compField.description }}
            </div>
          </a-form-item>
        </a-card>
        <a-empty v-if="arrayValue.length === 0" description="暂无数据，请添加" />
      </div>
      <div v-else class="component-loading">
        加载组件中...
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, watch, inject, provide } from 'vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { ActivityTemplateField } from '@/api/activityTemplate'
import type { ActivityComponent, ActivityComponentField } from '@/api/activityComponent'
import FieldRenderer from './FieldRenderer.vue'

interface Props {
  field: ActivityTemplateField | ActivityComponentField
  value: any
  componentCache: Map<number, ActivityComponent>
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: any]
}>()

// 注入父级的字段路径前缀（用于嵌套组件）
const injectedPath = inject<(() => (string | number)[]) | (string | number)[]>('componentFieldPath', [])
const parentFieldPath = computed(() => {
  if (typeof injectedPath === 'function') {
    return injectedPath()
  }
  return injectedPath as (string | number)[]
})

// 计算当前字段的完整路径前缀
const currentFieldPath = computed(() => {
  // 如果是顶层组件（parentFieldPath 为空），使用 configData 作为前缀
  if (parentFieldPath.value.length === 0) {
    return ['configData', props.field.name || '']
  }
  // 如果是嵌套组件，追加当前字段名
  return [...parentFieldPath.value, props.field.name || '']
})

// 提供当前路径给子组件（提供 computed 的 getter 函数）
provide('componentFieldPath', () => currentFieldPath.value)

const component = computed(() => {
  if (!props.field.componentId) {
    return null
  }
  return props.componentCache.get(props.field.componentId) || null
})

const sortedComponentFields = computed(() => {
  if (!component.value?.fields) {
    return []
  }
  return [...component.value.fields].sort((a, b) => (a.sort || 0) - (b.sort || 0))
})

const arrayValue = computed({
  get: () => {
    if (!Array.isArray(props.value)) {
      return []
    }
    return props.value
  },
  set: (val) => {
    emit('update:value', val)
  }
})


const getFieldValue = (fieldName: string) => {
  if (!props.value || typeof props.value !== 'object') {
    return undefined
  }
  return props.value[fieldName]
}

const getArrayFieldValue = (index: number, fieldName: string) => {
  if (!Array.isArray(arrayValue.value) || !arrayValue.value[index]) {
    return undefined
  }
  return arrayValue.value[index][fieldName]
}

const updateFieldValue = (fieldName: string, val: any) => {
  const newValue = { ...(props.value || {}) }
  newValue[fieldName] = val
  emit('update:value', newValue)
}

const updateArrayFieldValue = (index: number, fieldName: string, val: any) => {
  const newArray = [...arrayValue.value]
  if (!newArray[index]) {
    newArray[index] = {}
  }
  newArray[index] = { ...newArray[index], [fieldName]: val }
  arrayValue.value = newArray
}

const handleAddItem = () => {
  const newArray = [...arrayValue.value, {}]
  arrayValue.value = newArray
}

const handleRemoveItem = (index: number) => {
  const newArray = arrayValue.value.filter((_, i) => i !== index)
  arrayValue.value = newArray
}

const getFieldName = (fieldName: string) => {
  // 使用当前字段路径前缀，追加子字段名
  return [...currentFieldPath.value, fieldName]
}

const getArrayFieldName = (index: number, fieldName: string) => {
  // 使用当前字段路径前缀，追加索引和子字段名
  return [...currentFieldPath.value, index, fieldName]
}

const getFieldRules = (field: ActivityComponentField) => {
  const rules: any[] = []
  
  if (field.required) {
    // 所有字段都使用 'change' 和 'blur' 触发器，确保值变化时立即验证
    rules.push({
      required: true,
      message: `请输入${field.label || field.name}`,
      trigger: ['change', 'blur'],
      validator: (_rule: any, value: any) => {
        // 检查值是否为空
        if (value === null || value === undefined) {
          return Promise.reject(new Error(`请输入${field.label || field.name}`))
        }
        // 对于字符串类型，检查是否为空字符串
        if (typeof value === 'string' && value.trim() === '') {
          return Promise.reject(new Error(`请输入${field.label || field.name}`))
        }
        // 对于数组类型，检查是否为空数组
        if (Array.isArray(value) && value.length === 0) {
          return Promise.reject(new Error(`请输入${field.label || field.name}`))
        }
        // 其他情况都认为有效（包括有效的日期时间字符串、数字、布尔值等）
        return Promise.resolve()
      }
    })
  }
  
  return rules
}

// 兼容 array 和 isArray 两种字段名
const getIsArray = () => {
  const fieldAny = props.field as any
  return props.field.isArray ?? fieldAny.array ?? false
}

// 获取 allowDynamic 值
const getAllowDynamic = () => {
  return props.field.allowDynamic ?? false
}

// 初始化数组值
watch(() => props.value, (newVal) => {
  const isArray = getIsArray()
  if (isArray && !Array.isArray(newVal)) {
    emit('update:value', [])
  } else if (!isArray && (newVal === null || newVal === undefined)) {
    emit('update:value', {})
  }
}, { immediate: true })
</script>

<style scoped lang="css">
.component-renderer {
  width: 100%;
}

.component-single {
  width: 100%;
}

.component-array {
  width: 100%;
}

.component-array-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.component-array-title {
  font-weight: 500;
  font-size: 14px;
}

.component-card {
  margin-bottom: 12px;
}

.component-array-item {
  margin-bottom: 16px;
}

.component-loading {
  padding: 20px;
  text-align: center;
  color: #999;
}

.form-help-text {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
</style>
