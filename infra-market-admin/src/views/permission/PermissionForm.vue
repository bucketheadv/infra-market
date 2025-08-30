<template>
  <div class="permission-form">
    <a-card :title="isEdit ? '编辑权限' : '创建权限'">
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
        @finish="handleSubmit"
      >
        <a-form-item label="权限名称" name="name">
          <a-input
            v-model:value="form.name"
            placeholder="请输入权限名称"
          />
        </a-form-item>
        
        <a-form-item label="权限编码" name="code">
          <a-input
            v-model:value="form.code"
            placeholder="请输入权限编码"
            :disabled="isEdit"
          />
        </a-form-item>
        
        <a-form-item label="权限类型" name="type">
          <a-select
            v-model:value="form.type"
            placeholder="请选择权限类型"
          >
            <a-select-option value="menu">菜单</a-select-option>
            <a-select-option value="button">按钮</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="父级权限" name="parentId">
          <a-tree-select
            v-model:value="form.parentId"
            :tree-data="permissionTree"
            :loading="permissionLoading"
            placeholder="请选择父级权限"
            allow-clear
            :field-names="{
              children: 'children',
              label: 'name',
              value: 'id',
            }"
          />
        </a-form-item>
        
        <a-form-item label="路径" name="path">
          <a-input
            v-model:value="form.path"
            placeholder="请输入路径"
          />
        </a-form-item>
        
        <a-form-item label="图标" name="icon">
          <a-select
            v-model:value="form.icon"
            placeholder="请选择图标"
            allow-clear
            show-search
            :filter-option="filterIconOption"
          >
            <a-select-option
              v-for="icon in iconOptions"
              :key="icon.value"
              :value="icon.value"
            >
              <span style="margin-right: 8px;">
                <component :is="icon.component" />
              </span>
              {{ icon.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="排序" name="sort">
          <a-input-number
            v-model:value="form.sort"
            placeholder="请输入排序值"
            :min="0"
            :max="999"
            style="width: 100%;"
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
import { permissionApi } from '@/api/permission'
import { useAuthStore } from '@/stores/auth'
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
  ArrowLeftOutlined,
  ArrowRightOutlined,
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
  } catch (error) {
    message.error('获取权限树失败')
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
  } catch (error) {
    message.error('获取权限信息失败')
    router.push('/permissions')
  }
}

// 提交表单
const handleSubmit = async () => {
  loading.value = true
  try {
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
    router.push('/permissions')
  } catch (error) {
    message.error(isEdit.value ? '权限更新失败' : '权限创建失败')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/permissions')
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
  padding: 24px;
}
</style>
