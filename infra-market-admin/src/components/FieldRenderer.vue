<template>
  <div class="field-renderer">
    <!-- 文本框 -->
    <a-input
      v-if="field.inputType === 'TEXT'"
      :value="value"
      :placeholder="field.placeholder || `请输入${field.label || field.name}`"
      size="middle"
      class="form-input"
      @update:value="handleUpdate"
    />
    
    <!-- 多行文本 -->
    <a-textarea
      v-else-if="field.inputType === 'TEXTAREA'"
      :value="value"
      :placeholder="field.placeholder || `请输入${field.label || field.name}`"
      :rows="4"
      size="middle"
      class="form-textarea"
      @update:value="handleUpdate"
    />
    
    <!-- 数字 -->
    <a-input-number
      v-else-if="field.inputType === 'NUMBER'"
      :value="value"
      :min="field.min as number"
      :max="field.max as number"
      :placeholder="field.placeholder || `请输入${field.label || field.name}`"
      size="middle"
      class="form-input"
      style="width: 100%"
      @update:value="handleUpdate"
    />
    
    <!-- 下拉框 -->
    <a-select
      v-else-if="field.inputType === 'SELECT'"
      :value="value"
      :placeholder="field.placeholder || `请选择${field.label || field.name}`"
      size="middle"
      class="form-input"
      :mode="field.multiple ? 'multiple' : undefined"
      @update:value="handleUpdate"
    >
      <a-select-option
        v-for="option in field.options"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label || option.value }}
      </a-select-option>
    </a-select>
    
    <!-- 多选下拉框 -->
    <a-select
      v-else-if="field.inputType === 'MULTI_SELECT'"
      :value="value"
      :placeholder="field.placeholder || `请选择${field.label || field.name}`"
      size="middle"
      class="form-input"
      mode="multiple"
      @update:value="handleUpdate"
    >
      <a-select-option
        v-for="option in field.options"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label || option.value }}
      </a-select-option>
    </a-select>
    
    <!-- 复选框 -->
    <a-checkbox
      v-else-if="field.inputType === 'CHECKBOX'"
      :checked="value"
      @update:checked="handleUpdate"
    >
      {{ field.label || field.name }}
    </a-checkbox>
    
    <!-- 日期 -->
    <a-date-picker
      v-else-if="field.inputType === 'DATE'"
      :value="value"
      :placeholder="field.placeholder || `请选择${field.label || field.name}`"
      size="middle"
      class="form-input"
      style="width: 100%"
      format="YYYY-MM-DD"
      value-format="YYYY-MM-DD"
      @update:value="(val: any) => handleUpdate(val)"
      @change="(val: any) => handleUpdate(val)"
    />
    
    <!-- 日期时间 -->
    <a-date-picker
      v-else-if="field.inputType === 'DATETIME'"
      :value="value"
      :placeholder="field.placeholder || `请选择${field.label || field.name}`"
      size="middle"
      class="form-input"
      style="width: 100%"
      show-time
      format="YYYY-MM-DD HH:mm:ss"
      value-format="YYYY-MM-DD HH:mm:ss"
      @update:value="(val: any) => handleUpdate(val)"
      @change="(val: any) => handleUpdate(val)"
    />
    
    <!-- 组件类型（递归） -->
    <ComponentRenderer
      v-else-if="field.type === 'COMPONENT'"
      :field="field as any"
      :value="value"
      :component-cache="componentCache"
      @update:value="handleUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import type { ActivityComponentField } from '@/api/activityComponent'
import type { ActivityComponent } from '@/api/activityComponent'
import ComponentRenderer from './ComponentRenderer.vue'

interface Props {
  field: ActivityComponentField
  value: any
  componentCache: Map<number, ActivityComponent>
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: any]
}>()

const handleUpdate = (val: any) => {
  emit('update:value', val)
}
</script>

<style scoped lang="css">
.field-renderer {
  width: 100%;
}
</style>
