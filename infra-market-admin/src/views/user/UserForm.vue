<template>
  <div class="user-form">
    <a-card :title="isEdit ? '编辑用户' : '创建用户'">
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
        @finish="handleSubmit"
      >
        <a-form-item label="用户名" name="username">
          <a-input
            v-model:value="form.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </a-form-item>
        
        <a-form-item
          v-if="!isEdit"
          label="密码"
          name="password"
        >
          <a-input-password
            v-model:value="form.password"
            placeholder="请输入密码"
          />
        </a-form-item>
        
        <a-form-item label="邮箱" name="email">
          <a-input
            v-model:value="form.email"
            placeholder="请输入邮箱"
          />
        </a-form-item>
        
        <a-form-item label="手机号" name="phone">
          <a-input
            v-model:value="form.phone"
            placeholder="请输入手机号"
          />
        </a-form-item>
        
        <a-form-item label="角色" name="roleIds">
          <a-select
            v-model:value="form.roleIds"
            mode="multiple"
            placeholder="请选择角色"
            :options="roleOptions"
            :loading="roleLoading"
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
import { userApi } from '@/api/user'
import { roleApi } from '@/api/role'
import type { UserForm, Role } from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref()

const loading = ref(false)
const roleLoading = ref(false)
const roleOptions = ref<{ label: string; value: number }[]>([])

const isEdit = computed(() => !!route.params.id)

const form = reactive<UserForm>({
  username: '',
  email: '',
  phone: '',
  password: '',
  roleIds: [],
})

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: !isEdit.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  roleIds: [
    { required: true, message: '请选择角色', trigger: 'change' },
  ],
}))

// 获取角色列表
const fetchRoles = async () => {
  roleLoading.value = true
  try {
    const response = await roleApi.getAllRoles()
    roleOptions.value = response.data.map((role: Role) => ({
      label: role.name,
      value: role.id,
    }))
  } catch (error) {
    message.error('获取角色列表失败')
  } finally {
    roleLoading.value = false
  }
}

// 获取用户详情
const fetchUser = async (id: number) => {
  try {
    const response = await userApi.getUser(id)
    const user = response.data
    
    form.username = user.username
    form.email = user.email || ''
    form.phone = user.phone || ''
    // 设置用户已有的角色ID列表
    form.roleIds = user.roleIds || []
  } catch (error) {
    message.error('获取用户信息失败')
    router.push('/system/users')
  }
}

// 提交表单
const handleSubmit = async () => {
  loading.value = true
  try {
    if (isEdit.value) {
      await userApi.updateUser(Number(route.params.id), form)
      message.success('用户更新成功')
    } else {
      await userApi.createUser(form)
      message.success('用户创建成功')
    }
    router.push('/system/users')
  } catch (error) {
    message.error(isEdit.value ? '用户更新失败' : '用户创建失败')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/system/users')
}

onMounted(async () => {
  await fetchRoles()
  
  if (isEdit.value) {
    await fetchUser(Number(route.params.id))
  }
})
</script>

<style scoped>
.user-form {
  min-height: 100%;
  background: #f0f2f5;
  padding: 24px;
}
</style>
