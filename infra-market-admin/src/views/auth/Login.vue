<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      <div class="bg-circle bg-circle-4"></div>
      <div class="bg-circle bg-circle-5"></div>
      <div class="bg-circle bg-circle-6"></div>
    </div>
    
    <div class="login-content">
      <div class="login-form-wrapper">
        <div class="login-header">
          <div class="logo-container">
            <div class="logo-icon">
              <ShopOutlined />
            </div>
            <div class="logo-text">
              <h1 class="login-title">基础设施市场</h1>
              <p class="login-subtitle">Infrastructure Market</p>
            </div>
          </div>
        </div>
        
        <div class="login-body">
          <div class="welcome-text">
            <h2>欢迎回来</h2>
            <p>请登录您的账户以继续</p>
          </div>
          
          <a-form
            :model="form"
            :rules="rules"
            @finish="handleLogin"
            class="login-form"
            layout="vertical"
          >
            <a-form-item name="username" label="用户名">
              <a-input
                v-model:value="form.username"
                placeholder="请输入用户名"
                class="form-input"
                size="large"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item name="password" label="密码">
              <a-input-password
                v-model:value="form.password"
                placeholder="请输入密码"
                class="form-input"
                size="large"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
            
            <a-form-item class="submit-item">
              <a-button
                type="primary"
                html-type="submit"
                class="login-btn"
                size="large"
                :loading="loading"
              >
                <LoginOutlined />
                登录
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, ShopOutlined, LoginOutlined } from '@ant-design/icons-vue'
import type { LoginForm } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive<LoginForm>({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  loading.value = true
  
  try {
    await authStore.login(form)
    message.success('登录成功')
    router.push('/')
  } catch (error: any) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #e6fffa 0%, #f0fff4 25%, #f0f9ff 50%, #f0fff4 75%, #e6fffa 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 背景装饰元素 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.bg-circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 5%;
  animation-delay: 0s;
  background: rgba(16, 185, 129, 0.08);
}

.bg-circle-2 {
  width: 160px;
  height: 160px;
  top: 70%;
  right: 8%;
  animation-delay: 1s;
  background: rgba(5, 150, 105, 0.1);
}

.bg-circle-3 {
  width: 100px;
  height: 100px;
  top: 20%;
  right: 20%;
  animation-delay: 2s;
  background: rgba(16, 185, 129, 0.07);
}

.bg-circle-4 {
  width: 130px;
  height: 130px;
  bottom: 20%;
  left: 10%;
  animation-delay: 3s;
  background: rgba(4, 120, 87, 0.09);
}

.bg-circle-5 {
  width: 80px;
  height: 80px;
  top: 80%;
  left: 50%;
  animation-delay: 4s;
  background: rgba(16, 185, 129, 0.08);
}

.bg-circle-6 {
  width: 140px;
  height: 140px;
  top: 40%;
  left: 75%;
  animation-delay: 5s;
  background: rgba(5, 150, 105, 0.07);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-content {
  position: relative;
  z-index: 2;
  width: 100%;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.login-form-wrapper {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  width: 100%;
  max-width: 480px;
}

.login-header {
  text-align: center;
  padding: 36px 36px 28px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

.login-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="10" cy="60" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="90" cy="40" r="0.5" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.logo-icon :deep(.anticon) {
  font-size: 20px;
  color: white;
}

.logo-text {
  text-align: left;
}

.login-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.login-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 400;
}

.login-body {
  padding: 36px;
}

.welcome-text {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-text h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.welcome-text p {
  font-size: 15px;
  color: #6b7280;
  margin-bottom: 0;
}

.login-card {
  background: transparent;
  border: none;
  box-shadow: none;
}

.login-card :deep(.ant-card-body) {
  padding: 28px;
}

.login-form {
  margin-top: 0;
}

.login-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
  margin-bottom: 8px;
}

.form-input {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  background: #fff;
  font-size: 14px;
  height: 44px;
}

.form-input:hover {
  border-color: #10b981;
  background: #fff;
}

.form-input:focus,
.form-input:focus-within {
  border-color: #059669;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.input-icon {
  color: #9ca3af;
  font-size: 14px;
}

.submit-item {
  margin-top: 32px;
  margin-bottom: 0;
}

.login-btn {
  height: 48px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  width: 100%;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.35);
  transform: translateY(-1px);
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:active {
  background: linear-gradient(135deg, #047857 0%, #065f46 100%);
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-content {
    max-width: 100%;
    padding: 16px;
  }
  
  .login-header {
    padding: 24px 24px 18px;
  }
  
  .logo-icon {
    width: 48px;
    height: 48px;
    margin-right: 8px;
  }
  
  .logo-icon :deep(.anticon) {
    font-size: 20px;
  }
  
  .login-title {
    font-size: 20px;
  }
  
  .login-subtitle {
    font-size: 12px;
  }
  
  .login-card :deep(.ant-card-body) {
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .login-content {
    padding: 12px;
  }
  
  .login-header {
    padding: 20px 20px 16px;
  }
  
  .login-card :deep(.ant-card-body) {
    padding: 20px;
  }
  
  .login-btn {
    height: 40px;
    font-size: 14px;
  }
}
</style>
