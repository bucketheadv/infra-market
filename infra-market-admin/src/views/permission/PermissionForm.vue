<template>
  <div class="permission-form">
    <a-card :title="isEdit ? '编辑权限' : '创建权限'">
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
        @finish="handleSubmit"
      >
        <a-form-item label="权限名称" name="name">
          <a-input
            v-model:value="form.name"
            placeholder="请输入权限名称"
          />
        </a-form-item>
        
        <a-form-item label="权限编码" name="code">
          <a-input
            v-model:value="form.code"
            placeholder="请输入权限编码"
            :disabled="isEdit"
          />
        </a-form-item>
        
        <a-form-item label="权限类型" name="type">
          <a-select
            v-model:value="form.type"
            placeholder="请选择权限类型"
          >
            <a-select-option value="menu">菜单</a-select-option>
            <a-select-option value="button">按钮</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="父级权限" name="parentId">
          <a-tree-select
            v-model:value="form.parentId"
            :tree-data="permissionTree"
            :loading="permissionLoading"
            placeholder="请选择父级权限"
            allow-clear
            :field-names="{
              children: 'children',
              label: 'name',
              value: 'id',
            }"
          />
        </a-form-item>
        
        <a-form-item label="路径" name="path">
          <a-input
            v-model:value="form.path"
            placeholder="请输入路径"
          />
        </a-form-item>
        
        <a-form-item label="图标" name="icon">
          <a-input
            v-model:value="form.icon"
            placeholder="请输入图标名称"
          />
        </a-form-item>
        
        <a-form-item label="排序" name="sort">
          <a-input-number
            v-model:value="form.sort"
            placeholder="请输入排序值"
            :min="0"
            :max="999"
            style="width: 100%;"
          />
        </a-form-item>
        
        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              {{ isEdit ? '更新' : '创建' }}
            </a-button>
            <a-button @click="handleCancel">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { permissionApi } from '@/api/permission'
import type { PermissionForm, Permission } from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref()

const loading = ref(false)
const permissionLoading = ref(false)
const permissionTree = ref<Permission[]>([])

const isEdit = computed(() => !!route.params.id)

const form = reactive<PermissionForm>({
  name: '',
  code: '',
  type: 'menu',
  parentId: undefined,
  path: '',
  icon: '',
  sort: 0,
})

const rules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[a-z:]+$/, message: '权限编码只能包含小写字母和冒号', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' },
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
  ],
}

// 获取权限树
const fetchPermissionTree = async () => {
  permissionLoading.value = true
  try {
    const response = await permissionApi.getPermissionTree()
    permissionTree.value = response.data
  } catch (error) {
    message.error('获取权限树失败')
  } finally {
    permissionLoading.value = false
  }
}

// 获取权限详情
const fetchPermission = async (id: number) => {
  try {
    const response = await permissionApi.getPermission(id)
    const permission = response.data
    
    form.name = permission.name
    form.code = permission.code
    form.type = permission.type
    form.parentId = permission.parentId
    form.path = permission.path || ''
    form.icon = permission.icon || ''
    form.sort = permission.sort
  } catch (error) {
    message.error('获取权限信息失败')
    router.push('/permissions')
  }
}

// 提交表单
const handleSubmit = async () => {
  loading.value = true
  try {
    if (isEdit.value) {
      await permissionApi.updatePermission(Number(route.params.id), form)
      message.success('权限更新成功')
    } else {
      await permissionApi.createPermission(form)
      message.success('权限创建成功')
    }
    router.push('/permissions')
  } catch (error) {
    message.error(isEdit.value ? '权限更新失败' : '权限创建失败')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/permissions')
}

onMounted(async () => {
  await fetchPermissionTree()
  
  if (isEdit.value) {
    await fetchPermission(Number(route.params.id))
  }
})
</script>

<style scoped>
.permission-form {
  padding: 24px;
}
</style>
