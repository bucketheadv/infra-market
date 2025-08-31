<template>
  <div class="change-password-container">
    <div class="change-password-wrapper">
      <div class="change-password-header">
        <div class="header-icon">
          <KeyOutlined />
        </div>
        <h1 class="header-title">修改密码</h1>
        <p class="header-subtitle">为了您的账户安全，请设置一个强密码</p>
      </div>
      
      <a-card :bordered="false" class="change-password-card">
        <a-form
          :model="formData"
          :rules="rules"
          ref="formRef"
          layout="vertical"
          @finish="handleSubmit"
          class="password-form"
        >
          <a-form-item label="原密码" name="oldPassword">
            <a-input-password
              v-model:value="formData.oldPassword"
              placeholder="请输入原密码"
              size="large"
              class="password-input"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item label="新密码" name="newPassword">
            <a-input-password
              v-model:value="formData.newPassword"
              placeholder="请输入新密码"
              size="large"
              class="password-input"
            >
              <template #prefix>
                <KeyOutlined class="input-icon" />
              </template>
            </a-input-password>
            <div class="password-tips">
              <span class="tip-text">密码长度至少6位，建议包含字母、数字和特殊字符</span>
            </div>
          </a-form-item>

          <a-form-item label="确认新密码" name="confirmPassword">
            <a-input-password
              v-model:value="formData.confirmPassword"
              placeholder="请再次输入新密码"
              size="large"
              class="password-input"
            >
              <template #prefix>
                <CheckCircleOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item class="submit-item">
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="submit-btn"
            >
              <template #icon>
                <SaveOutlined />
              </template>
              确认修改
            </a-button>
          </a-form-item>
          
          <div class="form-footer">
            <a-button 
              type="link" 
              @click="goBack"
              class="back-btn"
            >
              <ArrowLeftOutlined />
              返回
            </a-button>
          </div>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  KeyOutlined, 
  LockOutlined, 
  CheckCircleOutlined, 
  SaveOutlined,
  ArrowLeftOutlined 
} from '@ant-design/icons-vue'
import { authApi } from '@/api/auth'
import type { FormInstance } from 'ant-design-vue'

const router = useRouter()
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

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.change-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #f0f2f5 0%, #e6f7ff 100%);
  padding: 24px;
  position: relative;
}

.change-password-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(24,144,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(24,144,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(24,144,255,0.1)"/><circle cx="10" cy="60" r="0.5" fill="rgba(24,144,255,0.1)"/><circle cx="90" cy="40" r="0.5" fill="rgba(24,144,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.2;
}

.change-password-wrapper {
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 1;
}

.change-password-header {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
  border: 3px solid rgba(255, 255, 255, 0.8);
}

.header-icon :deep(.anticon) {
  font-size: 36px;
  color: white;
}

.header-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #333;
}

.header-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 400;
}

.change-password-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.change-password-card :deep(.ant-card-body) {
  padding: 40px;
}

.password-form {
  margin-top: 0;
}

.password-form :deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  margin-bottom: 8px;
}

.password-input {
  border-radius: 8px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  background: #fafafa;
}

.password-input:hover {
  border-color: #1890ff;
  background: white;
}

.password-input:focus,
.password-input:focus-within {
  border-color: #1890ff;
  background: white;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.input-icon {
  color: #999;
  font-size: 16px;
}

.password-tips {
  margin-top: 8px;
}

.tip-text {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.submit-item {
  margin-top: 32px;
  margin-bottom: 16px;
}

.submit-btn {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
  background: linear-gradient(135deg, #096dd9 0%, #1890ff 100%);
}

.submit-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.back-btn {
  color: #666;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .change-password-container {
    padding: 16px;
  }
  
  .change-password-wrapper {
    max-width: 100%;
  }
  
  .header-icon {
    width: 60px;
    height: 60px;
    margin-bottom: 16px;
  }
  
  .header-icon :deep(.anticon) {
    font-size: 28px;
  }
  
  .header-title {
    font-size: 24px;
  }
  
  .header-subtitle {
    font-size: 14px;
  }
  
  .change-password-card :deep(.ant-card-body) {
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .change-password-card :deep(.ant-card-body) {
    padding: 20px;
  }
  
  .header-title {
    font-size: 20px;
  }
  
  .submit-btn {
    height: 44px;
    font-size: 14px;
  }
}
</style>
