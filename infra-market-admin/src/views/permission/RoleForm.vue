<template>
  <div class="role-form">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <TeamOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">{{ isEdit ? '编辑角色' : '创建角色' }}</div>
          <div class="header-subtitle">{{ isEdit ? '修改角色信息和权限配置' : '创建新的角色并配置权限' }}</div>
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
          class="role-form-content"
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
            
            <a-form-item label="角色名称" name="name">
              <a-input
                v-model:value="form.name"
                placeholder="请输入角色名称"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item label="角色编码" name="code">
              <a-input
                v-model:value="form.code"
                placeholder="请输入角色编码"
                :disabled="isEdit"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <KeyOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item label="角色描述" name="description">
              <a-textarea
                v-model:value="form.description"
                placeholder="请输入角色描述信息，帮助理解角色的用途和职责"
                :rows="4"
                class="form-textarea"
                show-count
                :maxlength="200"
              />
            </a-form-item>
          </div>

          <!-- 权限配置区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <SafetyCertificateOutlined />
              </div>
              <div class="section-title">权限配置</div>
              <div class="section-subtitle">选择该角色可以访问的功能模块和操作权限</div>
            </div>
            
            <a-form-item name="permissionIds" class="permission-tree-item">
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
                class="permission-tree"
                size="middle"
              />
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
              {{ isEdit ? '更新角色' : '创建角色' }}
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  TeamOutlined,
  IdcardOutlined,
  UserOutlined,
  KeyOutlined,
  SafetyCertificateOutlined,
  CheckOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
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

.role-form-content {
  padding: 12px 0;
}

/* 确保角色描述标签与其他标签对齐 */
.description-item :deep(.ant-form-item-label) {
  text-align: left;
  padding-left: 0;
  line-height: 32px;
  padding-top: 0;
}

/* 调整表单标签的对齐方式 */
.role-form-content :deep(.ant-form-item-label) {
  text-align: right;
  padding-right: 6px;
  line-height: 32px;
}

/* 确保所有标签垂直对齐 */
.role-form-content :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.role-form-content :deep(.ant-form-item-label > label) {
  height: 32px;
  line-height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 13px;
  white-space: nowrap;
}

/* 统一所有表单项的标签对齐 */
.role-form-content :deep(.ant-form-item-label) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
}

/* 确保输入框区域对齐 */
.role-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: flex-start;
  min-height: 32px;
}

/* 统一标签对齐方式 */
.role-form-content :deep(.ant-form-item-label) {
  text-align: left;
  padding-left: 0;
  padding-right: 0;
  margin-left: 0;
}

/* 输入框样式 */
.form-input {
  height: 32px;
  line-height: 32px;
  width: 100%;
  min-width: 300px;
}

.form-textarea {
  height: auto;
  min-height: 32px;
  line-height: 1.5;
  padding: 6px 11px;
  width: 100%;
  min-width: 300px;
}

/* 优化标签列宽度，减少占用空间 */
.role-form-content :deep(.ant-col-2) {
  flex: 0 0 8.333333%;
  max-width: 8.333333%;
}

/* 确保输入框有足够空间 */
.role-form-content :deep(.ant-col-22) {
  flex: 0 0 91.666667%;
  max-width: 91.666667%;
}

/* 确保所有输入框完美对齐 */
.role-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: center;
}

.role-form-content :deep(.ant-form-item-control-input) {
  width: 100%;
}

.role-form-content :deep(.ant-form-item-control-input-content) {
  width: 100%;
}

/* 调整标签位置，与系统管理标题x轴对齐 */
.role-form-content :deep(.ant-form-item-label) {
  padding-left: 0;
  margin-left: 0;
  text-align: left;
}

.role-form-content :deep(.ant-form-item-label > label) {
  padding-left: 0;
  margin-left: 0;
}

/* 调整输入框样式，让它们更长 */
.form-input {
  width: 100%;
  min-width: 200px;
}

.form-textarea {
  width: 100%;
  min-width: 100%;
}

/* 确保角色描述占满整行 */
.description-item :deep(.ant-form-item-control-input) {
  width: 100%;
}

.description-item :deep(.ant-form-item-control-input-content) {
  width: 100%;
}

/* 调整角色描述表单项的布局，让输入框与上一行对齐 */
.description-item :deep(.ant-form-item-control) {
  margin-left: 0;
  padding-left: 0;
  width: 100%;
}

.description-item :deep(.ant-form-item-control-input) {
  margin-left: 0;
  padding-left: 0;
  width: 100%;
}

.description-item :deep(.ant-form-item-control-input-content) {
  margin-left: 0;
  padding-left: 0;
  width: 100%;
}

/* 确保角色描述输入框与上一行完全对齐 */
.description-item :deep(.ant-form-item-control-input-content > .ant-input) {
  margin-left: 0;
  padding-left: 0;
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

.form-textarea {
  border-radius: 4px;
  transition: all 0.2s ease;
  resize: vertical;
  font-size: 13px;
  width: 100%;
}

.form-textarea:hover {
  border-color: var(--primary-color, #1890ff);
}

.form-textarea:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.permission-tree-item {
  margin-bottom: 0;
}

.permission-tree {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 8px;
  max-height: 250px;
  overflow-y: auto;
}

.permission-tree :deep(.ant-tree-node-content-wrapper) {
  border-radius: 3px;
  transition: all 0.15s ease;
  font-size: 12px;
  padding: 2px 4px;
}

.permission-tree :deep(.ant-tree-node-content-wrapper:hover) {
  background-color: rgba(24, 144, 255, 0.08);
}

.permission-tree :deep(.ant-tree-checkbox) {
  margin-right: 4px;
}

.permission-tree :deep(.ant-tree-title) {
  font-size: 12px;
}

.permission-tree :deep(.ant-tree-treenode) {
  padding: 1px 0;
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
  
  .role-form-content {
    padding: 14px 0;
  }
  
  .form-section {
    margin-bottom: 18px;
  }
  
  .section-header {
    margin-bottom: 14px;
  }
  
  .permission-tree {
    padding: 10px;
    max-height: 280px;
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
  
  .role-form-content {
    padding: 12px 0;
  }
  
  .form-section {
    margin-bottom: 16px;
  }
  
  .permission-tree {
    padding: 8px;
    max-height: 220px;
  }
}
</style>
