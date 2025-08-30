<template>
  <div class="change-password-container">
    <a-card title="修改密码" :bordered="false" class="change-password-card">
      <a-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        layout="vertical"
        @finish="handleSubmit"
      >
        <a-form-item label="原密码" name="oldPassword">
          <a-input-password
            v-model:value="formData.oldPassword"
            placeholder="请输入原密码"
            size="large"
          />
        </a-form-item>

        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="formData.newPassword"
            placeholder="请输入新密码"
            size="large"
          />
        </a-form-item>

        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password
            v-model:value="formData.confirmPassword"
            placeholder="请再次输入新密码"
            size="large"
          />
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
          >
            确认修改
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { authApi } from '@/api/auth'
import type { FormInstance } from 'ant-design-vue'

const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formData.newPassword) {
    throw new Error('两次输入的密码不一致')
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    loading.value = true
    
    await authApi.changePassword({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword
    })
    
    message.success('密码修改成功')
    
    // 清空表单
    formRef.value?.resetFields()
  } catch (error: any) {
    message.error(error.message || '密码修改失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.change-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  background-color: #f0f2f5;
  padding: 24px;
}

.change-password-card {
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.change-password-card :deep(.ant-card-head-title) {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}
</style>
