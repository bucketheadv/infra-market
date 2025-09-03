<template>
  <div class="change-password-form">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <KeyOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">修改密码</div>
          <div class="header-subtitle">为了您的账户安全，请设置一个强密码</div>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          :label-col="{ span: 2 }"
          :wrapper-col="{ span: 22 }"
          class="password-form-content"
          size="small"
          layout="horizontal"
        >
          <!-- 密码信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <LockOutlined />
              </div>
              <div class="section-title">密码信息</div>
              <div class="section-subtitle">输入原密码并设置新的安全密码</div>
            </div>
            
            <a-form-item label="原密码" name="oldPassword">
              <a-input-password
                v-model:value="formData.oldPassword"
                placeholder="请输入原密码"
                size="middle"
                class="form-input"
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
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <KeyOutlined class="input-icon" />
                </template>
              </a-input-password>
              <div class="password-tips">
                <span class="tip-text">密码长度至少6位，建议包含字母、数字和特殊字符</span>
              </div>
            </a-form-item>
            
            <a-form-item label="确认密码" name="confirmPassword">
              <a-input-password
                v-model:value="formData.confirmPassword"
                placeholder="请再次输入新密码"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <CheckCircleOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
          </div>

          <!-- 操作按钮区域 -->
          <div class="form-actions">
            <a-space size="small">
              <ThemeButton 
                variant="primary" 
                size="small"
                :icon="SaveOutlined"
                :disabled="loading"
                @click="handleSubmit"
                class="submit-btn"
              >
                确认修改
              </ThemeButton>
              <ThemeButton 
                variant="secondary"
                size="small"
                :icon="ArrowLeftOutlined"
                @click="goBack"
                class="cancel-btn"
              >
                返回
              </ThemeButton>
            </a-space>
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
  ArrowLeftOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import { authApi } from '@/api/auth'
import ThemeButton from '@/components/ThemeButton.vue'
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
  if (!value) {
    return Promise.resolve()
  }
  if (value !== formData.newPassword) {
    throw new Error('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { max: 20, message: '密码长度不能超过20位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { max: 20, message: '密码长度不能超过20位', trigger: 'blur' },
    { 
      validator: (rule: any, value: string) => {
        if (value && value === formData.oldPassword) {
          throw new Error('新密码不能与原密码相同')
        }
        return Promise.resolve()
      }, 
      trigger: 'blur' 
    },
    {
      validator: (rule: any, value: string) => {
        if (!value) return Promise.resolve()
        
        // 检查密码强度
        const hasLetter = /[a-zA-Z]/.test(value)
        const hasNumber = /\d/.test(value)
        const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)
        
        if (!hasLetter && !hasNumber && !hasSpecial) {
          throw new Error('密码必须包含字母、数字或特殊字符')
        }
        
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value?.validate()
    
    loading.value = true
    
    await authApi.changePassword({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword
    })
    
    message.success('密码修改成功')
    
    // 清空表单
    formRef.value?.resetFields()
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单验证失败
      message.error('请填写完整的表单信息')
    } else {
      message.error(error.message || '密码修改失败')
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.change-password-form {
  min-height: 100%;
  background: #f0f2f5;
  padding: 0;
}

.form-header {
  margin-bottom: 8px;
  padding: 0 16px;
  margin-top: 16px;
}

.header-content {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 6px;
  padding: 12px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.header-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary-color, #1890ff), var(--secondary-color, #40a9ff));
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 2px 6px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.header-icon :deep(.anticon) {
  font-size: 18px;
  color: white;
}

.header-text {
  flex: 1;
}

.header-title {
  margin: 0 0 2px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.2;
}

.header-subtitle {
  margin: 0;
  font-size: 11px;
  color: #666;
  line-height: 1.2;
}

.form-content {
  padding: 0 16px 16px;
}

.form-card {
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.password-form-content {
  padding: 12px 0;
}

/* 调整表单标签的对齐方式 */
.password-form-content :deep(.ant-form-item-label) {
  text-align: right;
  padding-right: 6px;
  line-height: 32px;
}

/* 确保所有标签垂直对齐 */
.password-form-content :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.password-form-content :deep(.ant-form-item-label > label) {
  height: 32px;
  line-height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 13px;
  white-space: nowrap;
}

/* 统一所有表单项的标签对齐 */
.password-form-content :deep(.ant-form-item-label) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
}

/* 确保输入框区域对齐 */
.password-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: flex-start;
  min-height: 32px;
}

/* 优化标签列宽度，减少占用空间 */
.password-form-content :deep(.ant-col-2) {
  flex: 0 0 8.333333%;
  max-width: 8.333333%;
}

/* 确保输入框有足够空间 */
.password-form-content :deep(.ant-col-22) {
  flex: 0 0 91.666667%;
  max-width: 91.666667%;
}

/* 确保所有输入框完美对齐 */
.password-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: center;
}

.password-form-content :deep(.ant-form-item-control-input) {
  width: 100%;
}

.password-form-content :deep(.ant-form-item-control-input-content) {
  width: 100%;
}

.form-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.section-icon {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, var(--primary-color, #1890ff), var(--secondary-color, #40a9ff));
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
  box-shadow: 0 1px 3px var(--shadow-color, rgba(24, 144, 255, 0.12));
}

.section-icon :deep(.anticon) {
  font-size: 10px;
  color: white;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  margin-right: 6px;
}

.section-subtitle {
  font-size: 10px;
  color: #666;
  font-weight: 400;
}

.form-input {
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 13px;
  width: 100%;
  min-width: 300px;
}

.form-input:hover {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.1));
}

.form-input:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.input-icon {
  color: #bfbfbf;
  font-size: 12px;
}

.password-tips {
  margin-top: 4px;
}

.tip-text {
  font-size: 11px;
  color: #666;
  line-height: 1.3;
}

.form-actions {
  padding: 12px 0 0 0;
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.submit-btn {
  border-radius: 4px;
  transition: all 0.2s ease;
  min-width: 100px;
  font-size: 13px;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px var(--shadow-color, rgba(24, 144, 255, 0.2));
}

.cancel-btn {
  border-radius: 4px;
  transition: all 0.2s ease;
  min-width: 80px;
  font-size: 13px;
}

.cancel-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-content {
    padding: 0 16px 16px;
  }
  
  .form-header {
    padding: 0 16px;
    margin-top: 12px;
  }
  
  .header-content {
    padding: 14px 18px;
  }
  
  .header-icon {
    width: 32px;
    height: 32px;
    margin-right: 10px;
  }
  
  .header-title {
    font-size: 15px;
  }
  
  .header-subtitle {
    font-size: 11px;
  }
  
  .password-form-content {
    padding: 14px 0;
  }
  
  .form-section {
    margin-bottom: 18px;
  }
  
  .section-header {
    margin-bottom: 14px;
  }
}

@media (max-width: 480px) {
  .form-content {
    padding: 0 12px 12px;
  }
  
  .form-header {
    padding: 0 12px;
    margin-top: 10px;
  }
  
  .header-content {
    padding: 12px 16px;
  }
  
  .header-icon {
    width: 28px;
    height: 28px;
    margin-right: 8px;
  }
  
  .header-title {
    font-size: 14px;
  }
  
  .header-subtitle {
    font-size: 10px;
  }
  
  .password-form-content {
    padding: 12px 0;
  }
  
  .form-section {
    margin-bottom: 16px;
  }
}
</style>
