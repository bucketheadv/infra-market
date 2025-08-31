<template>
  <div class="user-form-container">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <UserOutlined />
        </div>
        <div class="header-text">
          <h1 class="header-title">{{ isEdit ? '编辑用户' : '创建用户' }}</h1>
          <p class="header-subtitle">{{ isEdit ? '修改用户信息和权限设置' : '创建新的系统用户账户' }}</p>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 20 }"
          @finish="handleSubmit"
          class="user-form"
        >
          <!-- 基本信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <IdcardOutlined />
              </div>
              <div class="section-title">基本信息</div>
            </div>
            
            <a-row :gutter="16">
              <a-col :xs="24" :lg="12">
                <a-form-item label="用户名" name="username">
                  <a-input
                    v-model:value="form.username"
                    placeholder="请输入用户名"
                    :disabled="isEdit"
                    size="large"
                    class="form-input"
                  >
                    <template #prefix>
                      <UserOutlined class="input-icon" />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
              
              <a-col :xs="24" :lg="12">
                <a-form-item
                  v-if="!isEdit"
                  label="密码"
                  name="password"
                >
                  <a-input-password
                    v-model:value="form.password"
                    placeholder="请输入密码"
                    size="large"
                    class="form-input"
                  >
                    <template #prefix>
                      <LockOutlined class="input-icon" />
                    </template>
                  </a-input-password>
                </a-form-item>
              </a-col>
            </a-row>
          </div>

          <!-- 联系信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <ContactsOutlined />
              </div>
              <div class="section-title">联系信息</div>
            </div>
            
            <a-row :gutter="16">
              <a-col :xs="24" :lg="12">
                <a-form-item label="邮箱" name="email">
                  <a-input
                    v-model:value="form.email"
                    placeholder="请输入邮箱地址"
                    size="large"
                    class="form-input"
                  >
                    <template #prefix>
                      <MailOutlined class="input-icon" />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
              
              <a-col :xs="24" :lg="12">
                <a-form-item label="手机号" name="phone">
                  <a-input
                    v-model:value="form.phone"
                    placeholder="请输入手机号码"
                    size="large"
                    class="form-input"
                  >
                    <template #prefix>
                      <PhoneOutlined class="input-icon" />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>
          </div>

          <!-- 权限设置区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <SafetyCertificateOutlined />
              </div>
              <div class="section-title">权限设置</div>
            </div>
            
            <a-form-item label="角色分配" name="roleIds">
              <a-select
                v-model:value="form.roleIds"
                mode="multiple"
                placeholder="请选择用户角色"
                :options="roleOptions"
                :loading="roleLoading"
                size="large"
                class="form-select"
                :max-tag-count="3"
                :max-tag-placeholder="(omittedValues: any[]) => `+${omittedValues.length}个角色`"
              >
                <template #suffixIcon>
                  <TeamOutlined class="select-icon" />
                </template>
              </a-select>
              <div class="form-help">
                <InfoCircleOutlined />
                <span>选择角色将决定用户可访问的功能模块</span>
              </div>
            </a-form-item>
          </div>

          <!-- 操作按钮区域 -->
          <div class="form-actions">
            <a-space size="large">
              <a-button 
                type="primary" 
                html-type="submit" 
                :loading="loading"
                size="large"
                class="submit-btn"
              >
                <template #icon>
                  <CheckOutlined />
                </template>
                {{ isEdit ? '更新用户' : '创建用户' }}
              </a-button>
              <a-button 
                @click="handleCancel"
                size="large"
                class="cancel-btn"
              >
                <template #icon>
                  <CloseOutlined />
                </template>
                取消
              </a-button>
            </a-space>
          </div>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  UserOutlined, 
  LockOutlined, 
  MailOutlined, 
  PhoneOutlined, 
  TeamOutlined,
  IdcardOutlined,
  ContactsOutlined,
  SafetyCertificateOutlined,
  InfoCircleOutlined,
  CheckOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
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
.user-form-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 16px;
}

.form-header {
  margin-bottom: 16px;
}

.header-content {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 4px;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.header-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  box-shadow: 0 1px 3px rgba(24, 144, 255, 0.2);
}

.header-icon :deep(.anticon) {
  font-size: 14px;
  color: white;
}

.header-text {
  flex: 1;
}

.header-title {
  margin: 0 0 3px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.2;
}

.header-subtitle {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.3;
}

.form-content {
  max-width: 100%;
  margin: 0;
}

.form-card {
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  background: #fff;
}

.user-form {
  padding: 0;
}

/* 表单标签字体大小 */
.user-form :deep(.ant-form-item-label > label) {
  font-size: 12px;
  color: #333;
  font-weight: 500;
}

/* 表单验证错误信息字体大小 */
.user-form :deep(.ant-form-item-explain-error) {
  font-size: 11px;
}

.form-section {
  margin-bottom: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.form-section:hover {
  background: #f5f5f5;
  border-color: #d9d9d9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.section-icon {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  box-shadow: 0 1px 2px rgba(24, 144, 255, 0.2);
}

.section-icon :deep(.anticon) {
  font-size: 11px;
  color: white;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.form-input,
.form-select {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  font-size: 12px;
}

.form-input:hover,
.form-select:hover {
  border-color: #40a9ff;
}

.form-input:focus,
.form-select:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 输入框占位符文字大小 */
.form-input::placeholder,
.form-select::placeholder {
  font-size: 11px;
  color: #bfbfbf;
}

/* 下拉框样式调整 */
.form-select :deep(.ant-select-selector) {
  border-radius: 4px !important;
  border: 1px solid #d9d9d9 !important;
  box-shadow: none !important;
  font-size: 12px !important;
}

.form-select:hover :deep(.ant-select-selector) {
  border-color: #40a9ff !important;
}

.form-select:focus-within :deep(.ant-select-selector) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

/* 下拉框选项样式 */
.form-select :deep(.ant-select-dropdown) {
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.form-select :deep(.ant-select-item) {
  font-size: 12px;
  padding: 6px 10px;
}

.form-select :deep(.ant-select-item-option-selected) {
  background-color: #e6f7ff;
  color: #1890ff;
}

/* 下拉框标签样式 */
.form-select :deep(.ant-select-selection-item) {
  font-size: 12px !important;
}

.form-select :deep(.ant-select-selection-placeholder) {
  font-size: 11px !important;
  color: #bfbfbf !important;
}

.input-icon,
.select-icon {
  color: #bfbfbf;
  transition: color 0.3s ease;
  font-size: 11px;
}

.form-input:focus-within .input-icon,
.form-select:focus-within .select-icon {
  color: #1890ff;
  font-size: 11px;
}

.form-help {
  display: flex;
  align-items: center;
  margin-top: 4px;
  padding: 4px 8px;
  background: rgba(24, 144, 255, 0.05);
  border-radius: 3px;
  border-left: 2px solid #1890ff;
}

.form-help :deep(.anticon) {
  color: #1890ff;
  margin-right: 4px;
  font-size: 11px;
}

.form-help span {
  font-size: 11px;
  color: #666;
  line-height: 1.3;
}

.form-actions {
  text-align: center;
  padding: 16px 12px 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: 12px;
}

.submit-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 4px;
  font-weight: 500;
  background: #1890ff;
  border: none;
  box-shadow: 0 1px 3px rgba(24, 144, 255, 0.2);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: #40a9ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.cancel-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 4px;
  font-weight: 500;
  border: 1px solid #d9d9d9;
  color: #666;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  border-color: #40a9ff;
  color: #1890ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-form-container {
    padding: 16px;
  }
  
  .header-content {
    padding: 16px 20px;
    flex-direction: column;
    text-align: center;
  }
  
  .header-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .header-title {
    font-size: 14px;
  }
  
  .header-subtitle {
    font-size: 10px;
  }
  
  .form-section {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .section-header {
    flex-direction: column;
    text-align: center;
  }
  
  .section-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .form-actions {
    padding: 20px 16px 16px;
  }
  
  .submit-btn,
  .cancel-btn {
    width: 100%;
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .user-form-container {
    padding: 12px;
  }
  
  .header-content {
    padding: 12px 16px;
  }
  
  .header-icon {
    width: 32px;
    height: 32px;
  }
  
  .header-icon :deep(.anticon) {
    font-size: 14px;
  }
  
  .header-title {
    font-size: 13px;
  }
  
  .form-section {
    padding: 16px;
  }
  
  .form-actions {
    padding: 20px 16px 16px;
  }
}
</style>
