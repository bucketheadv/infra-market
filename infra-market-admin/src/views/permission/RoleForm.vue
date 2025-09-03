<template>
  <div class="role-form">
    <a-card :title="isEdit ? '编辑角色' : '创建角色'">
              <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 16 }"
        >
        <a-form-item label="角色名称" name="name">
          <a-input
            v-model:value="form.name"
            placeholder="请输入角色名称"
          />
        </a-form-item>
        
        <a-form-item label="角色编码" name="code">
          <a-input
            v-model:value="form.code"
            placeholder="请输入角色编码"
            :disabled="isEdit"
          />
        </a-form-item>
        
        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入角色描述"
            :rows="3"
          />
        </a-form-item>
        
        <a-form-item label="权限" name="permissionIds">
          <a-tree
            :key="treeKey"
            :checkedKeys="form.permissionIds"
            :tree-data="permissionTree"
            :loading="permissionLoading"
            checkable
            :defaultExpandAll="true"
            :field-names="{
              children: 'children',
              title: 'name',
              key: 'id',
            }"
            :checkStrictly="false"
            @check="handlePermissionCheck"
          />
        </a-form-item>
        
        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-space>
            <ThemeButton 
              variant="primary" 
              size="small"
              :disabled="loading"
              @click="handleSubmit"
            >
              {{ isEdit ? '更新' : '创建' }}
            </ThemeButton>
            <ThemeButton 
              variant="secondary"
              size="small"
              @click="handleCancel"
            >
              取消
            </ThemeButton>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { roleApi } from '@/api/role'
import { permissionApi } from '@/api/permission'
import ThemeButton from '@/components/ThemeButton.vue'
import type { RoleForm, Permission } from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref()

const loading = ref(false)
const permissionLoading = ref(false)
const permissionTree = ref<Permission[]>([])
const treeKey = ref(0) // 用于强制重新渲染树组件

const isEdit = computed(() => !!route.params.id)

const form = reactive<RoleForm>({
  name: '',
  code: '',
  description: '',
  permissionIds: [],
})

const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' },
  ],
  description: [
    { max: 200, message: '描述长度不能超过200个字符', trigger: 'blur' },
  ],
  permissionIds: [
    { required: true, message: '请选择权限', trigger: 'change' },
  ],
}

// 获取权限树
const fetchPermissionTree = async () => {
  permissionLoading.value = true
  try {
    const response = await permissionApi.getPermissionTree()
    permissionTree.value = response.data
  } catch (error: any) {
    message.error(error.message || '获取权限树失败')
  } finally {
    permissionLoading.value = false
  }
}

// 获取角色详情
const fetchRole = async (id: number) => {
  try {
    const response = await roleApi.getRole(id)
    const role = response.data
    
    form.name = role.name
    form.code = role.code
    form.description = role.description || ''
    // 设置角色已有的权限ID列表，确保类型为数字
    const permissionIds = (role.permissionIds || []).map(id => Number(id))
    form.permissionIds = permissionIds
    
    // 使用nextTick确保DOM更新后再设置权限ID
    await nextTick()
    form.permissionIds = permissionIds
    
    // 强制重新渲染树组件
    treeKey.value++
  } catch (error: any) {
    message.error(error.message || '获取角色信息失败')
    router.push('/system/roles')
  }
}

// 权限选择处理
const handlePermissionCheck = (checkedKeys: any) => {
  form.permissionIds = checkedKeys
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value?.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      await roleApi.updateRole(Number(route.params.id), form)
      message.success('角色更新成功')
    } else {
      await roleApi.createRole(form)
      message.success('角色创建成功')
    }
    router.push('/system/roles')
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单验证失败
      message.error('请填写完整的表单信息')
    } else {
      message.error(error.message || (isEdit.value ? '角色更新失败' : '角色创建失败'))
    }
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/system/roles')
}

onMounted(async () => {
  await fetchPermissionTree()
  
  if (isEdit.value) {
    await fetchRole(Number(route.params.id))
  }
})
</script>

<style scoped>
.role-form {
  min-height: 100%;
  background: #f0f2f5;
  padding: 24px;
}
</style>
