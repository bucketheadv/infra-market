<template>
  <div class="permission-form">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <SafetyCertificateOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">{{ isEdit ? '编辑权限' : '创建权限' }}</div>
          <div class="header-subtitle">{{ isEdit ? '修改权限信息和配置' : '创建新的权限并配置属性' }}</div>
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
          class="permission-form-content"
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
            
            <a-form-item label="权限名称" name="name">
              <a-input
                v-model:value="form.name"
                placeholder="请输入权限名称"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item label="权限编码" name="code">
              <a-input
                v-model:value="form.code"
                placeholder="请输入权限编码"
                :disabled="isEdit"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <KeyOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item label="权限类型" name="type">
              <a-select
                v-model:value="form.type"
                placeholder="请选择权限类型"
                size="middle"
                class="form-select"
              >
                <a-select-option value="menu">
                  <div class="select-option-content">
                    <MenuOutlined class="option-icon" />
                    <span>菜单权限</span>
                  </div>
                </a-select-option>
                <a-select-option value="button">
                  <div class="select-option-content">
                    <AppstoreOutlined class="option-icon" />
                    <span>按钮权限</span>
                  </div>
                </a-select-option>
              </a-select>
            </a-form-item>
          </div>

          <!-- 层级关系区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <ClusterOutlined />
              </div>
              <div class="section-title">层级关系</div>
              <div class="section-subtitle">设置权限的层级结构和访问路径</div>
            </div>
            
            <a-form-item label="父级权限" name="parentId">
              <a-tree-select
                v-model:value="form.parentId"
                :tree-data="permissionTree"
                :loading="permissionLoading"
                placeholder="请选择父级权限（可选）"
                allow-clear
                size="middle"
                class="form-tree-select"
                :field-names="{
                  children: 'children',
                  label: 'name',
                  value: 'id',
                }"
                :dropdown-style="{ maxHeight: '300px', overflow: 'auto' }"
              >
                <template #suffixIcon>
                  <FolderOutlined />
                </template>
              </a-tree-select>
            </a-form-item>
            
            <a-form-item label="访问路径" name="path">
              <a-input
                v-model:value="form.path"
                placeholder="请输入访问路径，如：/system/users"
                size="middle"
                class="form-input"
              >
                <template #prefix>
                  <LinkOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
          </div>

          <!-- 显示设置区域 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <EyeOutlined />
              </div>
              <div class="section-title">显示设置</div>
              <div class="section-subtitle">配置权限的显示样式和排序</div>
            </div>
            
            <a-form-item label="图标" name="icon">
              <a-select
                v-model:value="form.icon"
                placeholder="请选择图标（可选）"
                allow-clear
                show-search
                size="middle"
                class="form-select icon-select"
                :filter-option="filterIconOption"
                :dropdown-style="{ maxHeight: '300px', overflow: 'auto' }"
              >
                <a-select-option
                  v-for="icon in iconOptions"
                  :key="icon.value"
                  :value="icon.value"
                >
                  <div class="icon-option">
                    <component :is="icon.component" class="icon-display" />
                    <span class="icon-label">{{ icon.label }}</span>
                  </div>
                </a-select-option>
              </a-select>
            </a-form-item>
            
            <a-form-item label="排序权重" name="sort">
              <a-input-number
                v-model:value="form.sort"
                placeholder="请输入排序值，数字越小排序越靠前"
                :min="0"
                :max="999"
                size="middle"
                class="form-input-number"
                style="width: 100%;"
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
                {{ isEdit ? '更新权限' : '创建权限' }}
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
import { permissionApi } from '@/api/permission'
import { useAuthStore } from '@/stores/auth'
import ThemeButton from '@/components/ThemeButton.vue'
import type { PermissionForm, Permission } from '@/types'
import {
  SettingOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  CloudServerOutlined,
  DesktopOutlined,
  ApiOutlined,
  ShoppingOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  DashboardOutlined,
  MenuOutlined,
  HomeOutlined,
  FolderOutlined,
  FileOutlined,
  DatabaseOutlined,
  KeyOutlined,
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined,
  UploadOutlined,
  ExportOutlined,
  ImportOutlined,
  PrinterOutlined,
  MailOutlined,
  PhoneOutlined,
  GlobalOutlined,
  EnvironmentOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  StarOutlined,
  HeartOutlined,
  LikeOutlined,
  DislikeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  QuestionCircleOutlined,
  WarningOutlined,
  StopOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  StepForwardOutlined,
  StepBackwardOutlined,
  FastForwardOutlined,
  FastBackwardOutlined,
  SoundOutlined,
  VideoCameraOutlined,
  CameraOutlined,
  PictureOutlined,
  FileImageOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileExcelOutlined,
  FilePptOutlined,
  FileZipOutlined,
  FileMarkdownOutlined,
  FileUnknownOutlined,
  LinkOutlined,
  ShareAltOutlined,
  SendOutlined,
  SaveOutlined,
  BookOutlined,
  ReadOutlined,
  ExperimentOutlined,
  BugOutlined,
  ToolOutlined,
  BuildOutlined,
  RocketOutlined,
  ThunderboltOutlined,
  FireOutlined,
  BulbOutlined,
  GiftOutlined,
  TrophyOutlined,
  CrownOutlined,
  FlagOutlined,
  BankOutlined,
  CarOutlined,
  CompassOutlined,
  PushpinOutlined,
  AimOutlined,
  ScanOutlined,
  QrcodeOutlined,
  BarcodeOutlined,
  CreditCardOutlined,
  WalletOutlined,
  DollarOutlined,
  EuroOutlined,
  PoundOutlined,
  ShoppingCartOutlined,
  TagsOutlined,
  TagOutlined,
  ContainerOutlined,
  BoxPlotOutlined,
  FundOutlined,
  PieChartOutlined,
  BarChartOutlined,
  LineChartOutlined,
  AreaChartOutlined,
  DotChartOutlined,
  RadarChartOutlined,
  HeatMapOutlined,
  FallOutlined,
  RiseOutlined,
  StockOutlined,
  AlertOutlined,
  AuditOutlined,
  BranchesOutlined,
  ClusterOutlined,
  ApartmentOutlined,
  PartitionOutlined,
  GatewayOutlined,
  NodeIndexOutlined,
  NodeCollapseOutlined,
  NodeExpandOutlined,
  InboxOutlined,
  SolutionOutlined,
  PropertySafetyOutlined,
  SafetyOutlined,
  InsuranceOutlined,
  ExceptionOutlined,
  CheckSquareOutlined,
  BorderOutlined,
  BorderInnerOutlined,
  BorderTopOutlined,
  BorderBottomOutlined,
  BorderLeftOutlined,
  BorderRightOutlined,
  BorderVerticleOutlined,
  BorderHorizontalOutlined,
  RadiusBottomleftOutlined,
  RadiusBottomrightOutlined,
  RadiusUpleftOutlined,
  RadiusUprightOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  ColumnHeightOutlined,
  ColumnWidthOutlined,
  MinusSquareOutlined,
  PlusSquareOutlined,
  CloseSquareOutlined,
  DownSquareOutlined,
  UpSquareOutlined,
  RightSquareOutlined,
  LeftSquareOutlined,
  PlaySquareOutlined,
  MinusCircleOutlined,
  PlusCircleOutlined,
  DownCircleOutlined,
  UpCircleOutlined,
  RightCircleOutlined,
  LeftCircleOutlined,
  MinusOutlined,
  CloseOutlined,
  CheckOutlined,
  DownOutlined,
  UpOutlined,
  RightOutlined,
  LeftOutlined,
  ArrowLeftOutlined,
  LoadingOutlined,
  Loading3QuartersOutlined,
  SyncOutlined,
  RedoOutlined,
  UndoOutlined,
  RotateLeftOutlined,
  RotateRightOutlined,
  SwapOutlined,
  SwapLeftOutlined,
  SwapRightOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  DoubleLeftOutlined,
  DoubleRightOutlined,
  VerticalLeftOutlined,
  VerticalRightOutlined,
  VerticalAlignTopOutlined,
  VerticalAlignMiddleOutlined,
  VerticalAlignBottomOutlined,
  AlignLeftOutlined,
  AlignCenterOutlined,
  AlignRightOutlined,
  BgColorsOutlined,
  FontColorsOutlined,
  FontSizeOutlined,
  BoldOutlined,
  ItalicOutlined,
  UnderlineOutlined,
  StrikethroughOutlined,
  HighlightOutlined,
  OrderedListOutlined,
  UnorderedListOutlined,
  NumberOutlined,
  QuestionOutlined,
  InfoOutlined,
  ExclamationOutlined,
  IdcardOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref()

const loading = ref(false)
const permissionLoading = ref(false)
const permissionTree = ref<Permission[]>([])

const isEdit = computed(() => !!route.params.id)

// 图标选项
const iconOptions = [
  { value: 'SettingOutlined', label: '设置', component: SettingOutlined },
  { value: 'UserOutlined', label: '用户', component: UserOutlined },
  { value: 'TeamOutlined', label: '团队', component: TeamOutlined },
  { value: 'SafetyCertificateOutlined', label: '安全证书', component: SafetyCertificateOutlined },
  { value: 'CloudServerOutlined', label: '云服务器', component: CloudServerOutlined },
  { value: 'DesktopOutlined', label: '桌面', component: DesktopOutlined },
  { value: 'ApiOutlined', label: 'API', component: ApiOutlined },
  { value: 'ShoppingOutlined', label: '购物', component: ShoppingOutlined },
  { value: 'AppstoreOutlined', label: '应用商店', component: AppstoreOutlined },
  { value: 'FileTextOutlined', label: '文件文本', component: FileTextOutlined },
  { value: 'DashboardOutlined', label: '仪表盘', component: DashboardOutlined },
  { value: 'MenuOutlined', label: '菜单', component: MenuOutlined },
  { value: 'HomeOutlined', label: '首页', component: HomeOutlined },
  { value: 'FolderOutlined', label: '文件夹', component: FolderOutlined },
  { value: 'FileOutlined', label: '文件', component: FileOutlined },
  { value: 'DatabaseOutlined', label: '数据库', component: DatabaseOutlined },
  { value: 'KeyOutlined', label: '钥匙', component: KeyOutlined },
  { value: 'LockOutlined', label: '锁定', component: LockOutlined },
  { value: 'UnlockOutlined', label: '解锁', component: UnlockOutlined },
  { value: 'EyeOutlined', label: '眼睛', component: EyeOutlined },
  { value: 'EditOutlined', label: '编辑', component: EditOutlined },
  { value: 'DeleteOutlined', label: '删除', component: DeleteOutlined },
  { value: 'PlusOutlined', label: '添加', component: PlusOutlined },
  { value: 'SearchOutlined', label: '搜索', component: SearchOutlined },
  { value: 'ReloadOutlined', label: '刷新', component: ReloadOutlined },
  { value: 'DownloadOutlined', label: '下载', component: DownloadOutlined },
  { value: 'UploadOutlined', label: '上传', component: UploadOutlined },
  { value: 'ExportOutlined', label: '导出', component: ExportOutlined },
  { value: 'ImportOutlined', label: '导入', component: ImportOutlined },
  { value: 'PrinterOutlined', label: '打印机', component: PrinterOutlined },
  { value: 'MailOutlined', label: '邮件', component: MailOutlined },
  { value: 'PhoneOutlined', label: '电话', component: PhoneOutlined },
  { value: 'GlobalOutlined', label: '全球', component: GlobalOutlined },
  { value: 'EnvironmentOutlined', label: '环境', component: EnvironmentOutlined },
  { value: 'CalendarOutlined', label: '日历', component: CalendarOutlined },
  { value: 'ClockCircleOutlined', label: '时钟', component: ClockCircleOutlined },
  { value: 'StarOutlined', label: '星星', component: StarOutlined },
  { value: 'HeartOutlined', label: '心形', component: HeartOutlined },
  { value: 'LikeOutlined', label: '点赞', component: LikeOutlined },
  { value: 'DislikeOutlined', label: '点踩', component: DislikeOutlined },
  { value: 'CheckCircleOutlined', label: '成功', component: CheckCircleOutlined },
  { value: 'CloseCircleOutlined', label: '关闭', component: CloseCircleOutlined },
  { value: 'ExclamationCircleOutlined', label: '感叹号', component: ExclamationCircleOutlined },
  { value: 'InfoCircleOutlined', label: '信息', component: InfoCircleOutlined },
  { value: 'QuestionCircleOutlined', label: '问号', component: QuestionCircleOutlined },
  { value: 'WarningOutlined', label: '警告', component: WarningOutlined },
  { value: 'StopOutlined', label: '停止', component: StopOutlined },
  { value: 'PlayCircleOutlined', label: '播放', component: PlayCircleOutlined },
  { value: 'PauseCircleOutlined', label: '暂停', component: PauseCircleOutlined },
  { value: 'StepForwardOutlined', label: '前进', component: StepForwardOutlined },
  { value: 'StepBackwardOutlined', label: '后退', component: StepBackwardOutlined },
  { value: 'FastForwardOutlined', label: '快进', component: FastForwardOutlined },
  { value: 'FastBackwardOutlined', label: '快退', component: FastBackwardOutlined },
  { value: 'SoundOutlined', label: '声音', component: SoundOutlined },
  { value: 'VideoCameraOutlined', label: '摄像头', component: VideoCameraOutlined },
  { value: 'CameraOutlined', label: '相机', component: CameraOutlined },
  { value: 'PictureOutlined', label: '图片', component: PictureOutlined },
  { value: 'FileImageOutlined', label: '图片文件', component: FileImageOutlined },
  { value: 'FilePdfOutlined', label: 'PDF文件', component: FilePdfOutlined },
  { value: 'FileWordOutlined', label: 'Word文件', component: FileWordOutlined },
  { value: 'FileExcelOutlined', label: 'Excel文件', component: FileExcelOutlined },
  { value: 'FilePptOutlined', label: 'PPT文件', component: FilePptOutlined },
  { value: 'FileZipOutlined', label: '压缩文件', component: FileZipOutlined },
  { value: 'FileMarkdownOutlined', label: 'Markdown文件', component: FileMarkdownOutlined },
  { value: 'FileUnknownOutlined', label: '未知文件', component: FileUnknownOutlined },
  { value: 'LinkOutlined', label: '链接', component: LinkOutlined },
  { value: 'ShareAltOutlined', label: '分享', component: ShareAltOutlined },
  { value: 'SendOutlined', label: '发送', component: SendOutlined },
  { value: 'SaveOutlined', label: '保存', component: SaveOutlined },
  { value: 'BookOutlined', label: '书籍', component: BookOutlined },
  { value: 'ReadOutlined', label: '阅读', component: ReadOutlined },
  { value: 'ExperimentOutlined', label: '实验', component: ExperimentOutlined },
  { value: 'BugOutlined', label: 'Bug', component: BugOutlined },
  { value: 'ToolOutlined', label: '工具', component: ToolOutlined },
  { value: 'BuildOutlined', label: '构建', component: BuildOutlined },
  { value: 'RocketOutlined', label: '火箭', component: RocketOutlined },
  { value: 'ThunderboltOutlined', label: '闪电', component: ThunderboltOutlined },
  { value: 'FireOutlined', label: '火焰', component: FireOutlined },
  { value: 'BulbOutlined', label: '灯泡', component: BulbOutlined },
  { value: 'GiftOutlined', label: '礼物', component: GiftOutlined },
  { value: 'TrophyOutlined', label: '奖杯', component: TrophyOutlined },
  { value: 'CrownOutlined', label: '皇冠', component: CrownOutlined },
  { value: 'FlagOutlined', label: '旗帜', component: FlagOutlined },
  { value: 'BankOutlined', label: '银行', component: BankOutlined },
  { value: 'CarOutlined', label: '汽车', component: CarOutlined },
  { value: 'CompassOutlined', label: '指南针', component: CompassOutlined },
  { value: 'PushpinOutlined', label: '图钉', component: PushpinOutlined },
  { value: 'AimOutlined', label: '瞄准', component: AimOutlined },
  { value: 'ScanOutlined', label: '扫描', component: ScanOutlined },
  { value: 'QrcodeOutlined', label: '二维码', component: QrcodeOutlined },
  { value: 'BarcodeOutlined', label: '条形码', component: BarcodeOutlined },
  { value: 'CreditCardOutlined', label: '信用卡', component: CreditCardOutlined },
  { value: 'WalletOutlined', label: '钱包', component: WalletOutlined },
  { value: 'DollarOutlined', label: '美元', component: DollarOutlined },
  { value: 'EuroOutlined', label: '欧元', component: EuroOutlined },
  { value: 'PoundOutlined', label: '英镑', component: PoundOutlined },
  { value: 'ShoppingCartOutlined', label: '购物车', component: ShoppingCartOutlined },
  { value: 'TagsOutlined', label: '标签', component: TagsOutlined },
  { value: 'TagOutlined', label: '单个标签', component: TagOutlined },
  { value: 'ContainerOutlined', label: '容器', component: ContainerOutlined },
  { value: 'BoxPlotOutlined', label: '箱线图', component: BoxPlotOutlined },
  { value: 'FundOutlined', label: '基金', component: FundOutlined },
  { value: 'PieChartOutlined', label: '饼图', component: PieChartOutlined },
  { value: 'BarChartOutlined', label: '柱状图', component: BarChartOutlined },
  { value: 'LineChartOutlined', label: '折线图', component: LineChartOutlined },
  { value: 'AreaChartOutlined', label: '面积图', component: AreaChartOutlined },
  { value: 'DotChartOutlined', label: '点图', component: DotChartOutlined },
  { value: 'RadarChartOutlined', label: '雷达图', component: RadarChartOutlined },
  { value: 'HeatMapOutlined', label: '热力图', component: HeatMapOutlined },
  { value: 'FallOutlined', label: '下降', component: FallOutlined },
  { value: 'RiseOutlined', label: '上升', component: RiseOutlined },
  { value: 'StockOutlined', label: '股票', component: StockOutlined },
  { value: 'AlertOutlined', label: '警报', component: AlertOutlined },
  { value: 'AuditOutlined', label: '审计', component: AuditOutlined },
  { value: 'BranchesOutlined', label: '分支', component: BranchesOutlined },
  { value: 'ClusterOutlined', label: '集群', component: ClusterOutlined },
  { value: 'ApartmentOutlined', label: '公寓', component: ApartmentOutlined },
  { value: 'PartitionOutlined', label: '分区', component: PartitionOutlined },
  { value: 'GatewayOutlined', label: '网关', component: GatewayOutlined },
  { value: 'NodeIndexOutlined', label: '节点索引', component: NodeIndexOutlined },
  { value: 'NodeCollapseOutlined', label: '节点折叠', component: NodeCollapseOutlined },
  { value: 'NodeExpandOutlined', label: '节点展开', component: NodeExpandOutlined },
  { value: 'InboxOutlined', label: '收件箱', component: InboxOutlined },
  { value: 'SolutionOutlined', label: '解决方案', component: SolutionOutlined },
  { value: 'PropertySafetyOutlined', label: '财产安全', component: PropertySafetyOutlined },
  { value: 'SafetyOutlined', label: '安全', component: SafetyOutlined },
  { value: 'InsuranceOutlined', label: '保险', component: InsuranceOutlined },
  { value: 'ExceptionOutlined', label: '异常', component: ExceptionOutlined },
]

// 图标过滤函数
const filterIconOption = (input: string, option: any) => {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

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
  } catch (error: any) {
    message.error(error.message || '获取权限树失败')
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
  } catch (error: any) {
    message.error(error.message || '获取权限信息失败')
    router.push('/system/permissions')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value?.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      await permissionApi.updatePermission(Number(route.params.id), form)
      message.success('权限更新成功')
      // 刷新用户权限数据和菜单
      await authStore.getCurrentUser()
      await authStore.getUserMenus()
    } else {
      await permissionApi.createPermission(form)
      message.success('权限创建成功')
    }
    router.push('/system/permissions')
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单验证失败
      message.error('请填写完整的表单信息')
    } else {
      message.error(error.message || (isEdit.value ? '权限更新失败' : '权限创建失败'))
    }
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/system/permissions')
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

.permission-form-content {
  padding: 12px 0;
}

/* 调整表单标签的对齐方式 */
.permission-form-content :deep(.ant-form-item-label) {
  text-align: right;
  padding-right: 6px;
  line-height: 32px;
}

/* 确保所有标签垂直对齐 */
.permission-form-content :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.permission-form-content :deep(.ant-form-item-label > label) {
  height: 32px;
  line-height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 13px;
  white-space: nowrap;
}

/* 统一所有表单项的标签对齐 */
.permission-form-content :deep(.ant-form-item-label) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
}

/* 确保输入框区域对齐 */
.permission-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: flex-start;
  min-height: 32px;
}

/* 优化标签列宽度，减少占用空间 */
.permission-form-content :deep(.ant-col-2) {
  flex: 0 0 8.333333%;
  max-width: 8.333333%;
}

/* 确保输入框有足够空间 */
.permission-form-content :deep(.ant-col-22) {
  flex: 0 0 91.666667%;
  max-width: 91.666667%;
}

/* 确保所有输入框完美对齐 */
.permission-form-content :deep(.ant-form-item-control) {
  display: flex;
  align-items: center;
}

.permission-form-content :deep(.ant-form-item-control-input) {
  width: 100%;
}

.permission-form-content :deep(.ant-form-item-control-input-content) {
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
.form-select,
.form-tree-select,
.form-input-number {
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 13px;
  width: 100%;
  min-width: 300px;
}

.form-input:hover,
.form-select:hover,
.form-tree-select:hover,
.form-input-number:hover {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.1));
}

.form-input:focus,
.form-select:focus,
.form-tree-select:focus,
.form-input-number:focus {
  border-color: var(--primary-color, #1890ff);
  box-shadow: 0 0 0 1px var(--shadow-color, rgba(24, 144, 255, 0.15));
}

.input-icon {
  color: #bfbfbf;
  font-size: 12px;
}

.select-option-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.option-icon {
  color: var(--primary-color, #1890ff);
}

.icon-select {
  min-width: 180px;
}

.icon-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0;
}

.icon-display {
  font-size: 14px;
  color: var(--primary-color, #1890ff);
  width: 16px;
  text-align: center;
}

.icon-label {
  font-size: 13px;
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
  
  .permission-form-content {
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
  
  .permission-form-content {
    padding: 12px 0;
  }
  
  .form-section {
    margin-bottom: 16px;
  }
}
</style>
