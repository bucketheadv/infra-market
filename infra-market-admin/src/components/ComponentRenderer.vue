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
          v-for="(item, index) in arrayValue"
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
import { computed, h, watch, nextTick } from 'vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { ActivityTemplateField } from '@/api/activityTemplate'
import type { ActivityComponent, ActivityComponentField } from '@/api/activityComponent'
import FieldRenderer from './FieldRenderer.vue'

interface Props {
  field: ActivityTemplateField
  value: any
  componentCache: Map<number, ActivityComponent>
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: any]
}>()

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
  const newValue = { ...props.value }
  newValue[fieldName] = val
  emit('update:value', newValue)
  // 确保数据已更新后再触发验证
  nextTick(() => {
    // 验证会在数据更新后自动触发
  })
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
  return ['configData', props.field.name, fieldName]
}

const getArrayFieldName = (index: number, fieldName: string) => {
  return ['configData', props.field.name, index, fieldName]
}

const getFieldRules = (field: ActivityComponentField) => {
  const rules: any[] = []
  
  if (field.required) {
    // 对于日期时间选择器，使用 'change' 触发器；其他使用 'blur'
    const trigger = field.inputType === 'DATE' || field.inputType === 'DATETIME' ? ['change', 'blur'] : 'blur'
    rules.push({
      required: true,
      message: `请输入${field.label || field.name}`,
      trigger: trigger,
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
        // 其他情况都认为有效（包括有效的日期时间字符串）
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
