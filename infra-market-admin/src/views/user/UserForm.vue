<template>
  <div class="user-form">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <UserOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">{{ isEdit ? '编辑用户' : '创建用户' }}</div>
          <div class="header-subtitle">{{ isEdit ? '修改用户信息和权限设置' : '创建新的系统用户账户' }}</div>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col="{ span: 2 }"
          :wrapper-col="{ span: 22 }"
          class="user-form-content"
          size="small"
          layout="horizontal"
        >
          <!-- 基本信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <IdcardOutlined />
              </div>
              <div class="section-title">基本信息</div>
            </div>
            
            <a-form-item label="用户名" name="username">
              <a-input
                v-model:value="form.username"
                placeholder="请输入用户名"
                :disabled="isEdit"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item
              v-if="!isEdit"
              label="密码"
              name="password"
            >
              <a-input-password
                v-model:value="form.password"
                placeholder="请输入密码"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
          </div>

          <!-- 联系信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <ContactsOutlined />
              </div>
              <div class="section-title">联系信息</div>
            </div>
            
            <a-form-item label="邮箱" name="email">
              <a-input
                v-model:value="form.email"
                placeholder="请输入邮箱地址"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <MailOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item label="手机号" name="phone">
              <a-input
                v-model:value="form.phone"
                placeholder="请输入手机号码"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <PhoneOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
          </div>

          <!-- 权限设置区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <SafetyCertificateOutlined />
              </div>
              <div class="section-title">权限设置</div>
              <div class="section-subtitle">选择用户角色，决定可访问的功能模块</div>
            </div>
            
            <a-form-item label="角色分配" name="roleIds">
              <a-select
                v-model:value="form.roleIds"
                mode="multiple"
                placeholder="请选择用户角色"
                :options="roleOptions"
                :loading="roleLoading"
                size="middle"
                class="form-select"
                :max-tag-count="3"
                :max-tag-placeholder="(omittedValues: any[]) => `+${omittedValues.length}个角色`"
              >
                <template #suffixIcon>
                  <TeamOutlined class="select-icon" />
                </template>
              </a-select>
            </a-form-item>
          </div>

          <!-- 操作按钮区域 -->
          <div class="form-actions">
            <a-space size="small">
              <ThemeButton 
                variant="primary" 
                size="small"
                :icon="CheckOutlined"
                :disabled="loading"
                @click="handleSubmit"
                class="submit-btn"
              >
                {{ isEdit ? '更新用户' : '创建用户' }}
              </ThemeButton>
              <ThemeButton 
                variant="secondary"
                size="small"
                :icon="CloseOutlined"
                @click="handleCancel"
                class="cancel-btn"
              >
                取消
              </ThemeButton>
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
import ThemeButton from '@/components/ThemeButton.vue'
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
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
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
  try {
    // 表单验证
    await formRef.value?.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      await userApi.updateUser(Number(route.params.id), form)
      message.success('用户更新成功')
    } else {
      await userApi.createUser(form)
      message.success('用户创建成功')
    }
    router.push('/system/users')
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单验证失败
      message.error('请填写完整的表单信息')
    } else {
      message.error(isEdit.value ? '用户更新失败' : '用户创建失败')
    }
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

.user-form-content {
  padding: 12px 0;
}

/* 调整表单标签的对齐方式 */
.user-form-content :deep(.ant-form-item-label) {
  text-align: right;
  padding-right: 6px;
  line-height: 32px;
}

/* 确保所有标签垂直对齐 */
.user-form-content :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.user-form-content :deep(.ant-form-item-label > label) {
  height: 32px;
  line-height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 13px;
  white-space: nowrap;
}

/* 统一所有表单项的标签对齐 */
.user-form-content :deep(.ant-form-item-label) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
}

/* 确保输入框区域对齐 */
.user-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: flex-start;
  min-height: 32px;
}

/* 优化标签列宽度，减少占用空间 */
.user-form-content :deep(.ant-col-2) {
  flex: 0 0 8.333333%;
  max-width: 8.333333%;
}

/* 确保输入框有足够空间 */
.user-form-content :deep(.ant-col-22) {
  flex: 0 0 91.666667%;
  max-width: 91.666667%;
}

/* 确保所有输入框完美对齐 */
.user-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: center;
}

.user-form-content :deep(.ant-form-item-control-input) {
  width: 100%;
}

.user-form-content :deep(.ant-form-item-control-input-content) {
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

.form-input,
.form-select {
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 13px;
  width: 100%;
  min-width: 300px;
}

.form-input:hover,
.form-select:hover {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.1));
}

.form-input:focus,
.form-select:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.input-icon,
.select-icon {
  color: #bfbfbf;
  font-size: 12px;
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
  
  .user-form-content {
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
  
  .user-form-content {
    padding: 12px 0;
  }
  
  .form-section {
    margin-bottom: 16px;
  }
}
</style>
