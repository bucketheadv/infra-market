<template>
  <div class="interface-execute-page">
    <div class="form-header">
      <div class="header-content">
        <div class="header-icon">
          <PlayCircleOutlined />
        </div>
        <div class="header-text">
          <div class="header-title">接口执行</div>
          <div class="header-subtitle">{{ interfaceData?.name || '接口测试' }}</div>
        </div>
        <div class="header-actions">
          <ThemeButton 
            variant="secondary"
            size="small"
            :icon="EditOutlined"
            @click="handleEdit"
            class="edit-btn"
          >
            编辑接口
          </ThemeButton>
        </div>
      </div>
    </div>

    <div class="form-content">
      <a-card class="form-card" :bordered="false">
        <!-- 基本信息区域 -->
        <div class="form-section">
          <div class="section-header">
            <div class="section-icon">
              <span>📋</span>
            </div>
            <div class="section-title">接口基本信息</div>
          </div>
          
          <div v-if="loading" class="loading-container">
            <a-spin size="large" />
          </div>
          <div v-else-if="interfaceData" class="interface-info">
            <a-descriptions :column="2" :bordered="false" size="small">
              <a-descriptions-item label="接口名称">
                <span class="info-value">{{ interfaceData.name }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="请求方法">
                <a-tag :color="getMethodColor(interfaceData.method)" class="method-tag">
                  {{ interfaceData.method }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="请求URL" :span="2">
                <code class="url-text">{{ interfaceData.url }}</code>
              </a-descriptions-item>
              <a-descriptions-item label="接口描述" :span="2">
                <span class="description-text">{{ interfaceData.description || '暂无描述' }}</span>
              </a-descriptions-item>
              <a-descriptions-item v-if="interfaceData.postType" label="POST类型">
                <a-tag color="blue" class="post-type-tag">{{ getPostTypeLabel(interfaceData.postType) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item v-if="interfaceData.environment" label="接口环境">
                <a-tag :color="getTagColor(interfaceData.environment)" class="tag-tag">{{ getTagLabel(interfaceData.environment) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="interfaceData.status === 1 ? 'green' : 'red'" class="status-tag">
                  {{ interfaceData.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item v-if="interfaceData.timeout" label="超时时间">
                <span class="timeout-text">{{ formatTimeout(interfaceData.timeout) }}</span>
              </a-descriptions-item>
              <a-descriptions-item v-if="interfaceData.valuePath" label="取值路径">
                <code class="value-path-text">{{ interfaceData.valuePath }}</code>
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                <span class="time-text">{{ formatDateTime(interfaceData.createTime) }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                <span class="time-text">{{ formatDateTime(interfaceData.updateTime) }}</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </div>

        <!-- 主要内容区域 -->
        <a-tabs v-model:activeKey="mainActiveTab" class="main-tabs">
          <!-- 接口执行标签页 -->
          <a-tab-pane key="execute">
            <template #tab>
              <span class="tab-content">
                <span class="tab-icon">🚀</span>
                <span>接口执行</span>
              </span>
            </template>
            <a-row :gutter="24" class="content-row">
              <!-- 左侧：参数配置 -->
              <a-col :span="14">
                <div class="form-section">
                  <div class="section-header">
                    <div class="section-icon">
                      <span>⚙️</span>
                    </div>
                    <div class="section-title">参数配置</div>
                  </div>
                  <div v-if="loading" class="loading-container">
                    <a-spin size="large" />
                  </div>
                  <div v-else>
                    <a-form ref="formRef" :model="executeForm" layout="vertical">
                      <!-- URL参数 -->
                      <div v-if="urlParams.length > 0" class="param-group">
                        <h4>URL参数</h4>
                        <a-row v-for="param in urlParams" :key="param.name" class="param-row" :gutter="[6, 0]">
                          <a-col :span="6">
                            <label class="param-label">
                              {{ getParamDisplayName(param) }}
                              <span v-if="param.required" class="required">*</span>
                              <a-tooltip v-if="param.description" :title="param.description" placement="top">
                                <QuestionCircleOutlined class="help-icon" />
                              </a-tooltip>
                            </label>
                          </a-col>
                          <a-col :span="18">
                            <a-form-item
                              :name="['params', param.name]"
                              :rules="param.required ? [{ required: true, message: `请输入${getParamDisplayName(param)}` }] : []"
                            >
                              <!-- 代码编辑器弹窗按钮 -->
                              <div v-if="param.inputType === 'CODE'" class="code-editor-input">
                                <a-input
                                  :value="getCodePreview(executeForm.params[param.name])"
                                  :placeholder="`请输入${getParamDisplayName(param)}`"
                                  :disabled="!param.changeable"
                                  readonly
                                  class="code-preview-input"
                                  @click="!param.changeable || openCodeEditor(param, 'params')"
                                >
                                  <template #suffix>
                                    <ThemeButton
                                      v-if="param.changeable"
                                      variant="secondary"
                                      size="small"
                                      @click.stop="openCodeEditor(param, 'params')"
                                    >
                                      <template #icon>
                                        <span>📝</span>
                                      </template>
                                      编辑
                                    </ThemeButton>
                                  </template>
                                </a-input>
                              </div>
                              <!-- 其他输入组件 -->
                              <component
                                v-else
                                :is="getInputComponent(param)"
                                v-bind="getInputBindings(param, 'params')"
                                :placeholder="`请输入${getParamDisplayName(param)}`"
                                :options="getSelectOptions(param)"
                                :disabled="!param.changeable"
                                :required="param.required"
                              />
                            </a-form-item>
                          </a-col>
                        </a-row>
                      </div>

                      <!-- Header参数 -->
                      <div v-if="headerParams.length > 0" class="param-group">
                        <h4>Header参数</h4>
                        <a-row v-for="param in headerParams" :key="param.name" class="param-row" :gutter="[6, 0]">
                          <a-col :span="6">
                            <label class="param-label">
                              {{ getParamDisplayName(param) }}
                              <span v-if="param.required" class="required">*</span>
                              <a-tooltip v-if="param.description" :title="param.description" placement="top">
                                <QuestionCircleOutlined class="help-icon" />
                              </a-tooltip>
                            </label>
                          </a-col>
                          <a-col :span="18">
                            <a-form-item
                              :name="['headers', param.name]"
                              :rules="param.required ? [{ required: true, message: `请输入${getParamDisplayName(param)}` }] : []"
                            >
                              <!-- 代码编辑器弹窗按钮 -->
                              <div v-if="param.inputType === 'CODE'" class="code-editor-input">
                                <a-input
                                  :value="getCodePreview(executeForm.headers[param.name])"
                                  :placeholder="`请输入${getParamDisplayName(param)}`"
                                  :disabled="!param.changeable"
                                  readonly
                                  class="code-preview-input"
                                  @click="!param.changeable || openCodeEditor(param, 'headers')"
                                >
                                  <template #suffix>
                                    <ThemeButton
                                      v-if="param.changeable"
                                      variant="secondary"
                                      size="small"
                                      @click.stop="openCodeEditor(param, 'headers')"
                                    >
                                      <template #icon>
                                        <span>📝</span>
                                      </template>
                                      编辑
                                    </ThemeButton>
                                  </template>
                                </a-input>
                              </div>
                              <!-- 其他输入组件 -->
                              <component
                                v-else
                                :is="getInputComponent(param)"
                                v-bind="getInputBindings(param, 'headers')"
                                :placeholder="`请输入${getParamDisplayName(param)}`"
                                :options="getSelectOptions(param)"
                                :disabled="!param.changeable"
                                :required="param.required"
                              />
                            </a-form-item>
                          </a-col>
                        </a-row>
                      </div>

                      <!-- Body参数 -->
                      <div v-if="bodyParams.length > 0 && interfaceData?.method !== 'GET'" class="param-group">
                        <h4>Body参数</h4>
                        <a-row v-for="param in bodyParams" :key="param.name" class="param-row" :gutter="[6, 0]">
                          <a-col :span="6">
                            <label class="param-label">
                              {{ getParamDisplayName(param) }}
                              <span v-if="param.required" class="required">*</span>
                              <a-tooltip v-if="param.description" :title="param.description" placement="top">
                                <QuestionCircleOutlined class="help-icon" />
                              </a-tooltip>
                            </label>
                          </a-col>
                          <a-col :span="18">
                            <a-form-item
                              :name="['bodyParams', param.name]"
                              :rules="param.required ? [{ required: true, message: `请输入${getParamDisplayName(param)}` }] : []"
                            >
                              <!-- 代码编辑器弹窗按钮 -->
                              <div v-if="param.inputType === 'CODE'" class="code-editor-input">
                                <a-input
                                  :value="getCodePreview(executeForm.bodyParams[param.name])"
                                  :placeholder="`请输入${getParamDisplayName(param)}`"
                                  :disabled="!param.changeable"
                                  readonly
                                  class="code-preview-input"
                                  @click="!param.changeable || openCodeEditor(param, 'bodyParams')"
                                >
                                  <template #suffix>
                                    <ThemeButton
                                      v-if="param.changeable"
                                      variant="secondary"
                                      size="small"
                                      @click.stop="openCodeEditor(param, 'bodyParams')"
                                    >
                                      <template #icon>
                                        <span>📝</span>
                                      </template>
                                      编辑
                                    </ThemeButton>
                                  </template>
                                </a-input>
                              </div>
                              <!-- 其他输入组件 -->
                              <component
                                v-else
                                :is="getInputComponent(param)"
                                v-bind="getInputBindings(param, 'bodyParams')"
                                :placeholder="`请输入${getParamDisplayName(param)}`"
                                :options="getSelectOptions(param)"
                                :disabled="!param.changeable"
                                :required="param.required"
                              />
                            </a-form-item>
                          </a-col>
                        </a-row>
                      </div>


                      <div v-if="urlParams.length === 0 && headerParams.length === 0 && bodyParams.length === 0" class="no-params">
                        <a-empty description="该接口无需配置参数" />
                      </div>

                    </a-form>
                  </div>
                </div>
              </a-col>

              <!-- 右侧：执行结果 -->
              <a-col :span="10">
                <div class="form-section">
                  <div class="section-header">
                    <div class="section-icon">
                      <span>📊</span>
                    </div>
                    <div class="section-title">执行结果</div>
                  </div>
                  <div v-if="executing" class="executing-container">
                    <a-spin size="large" />
                    <div class="executing-text">正在执行接口...</div>
                    <div v-if="timeoutCountdown > 0" class="timeout-countdown">
                      <a-alert 
                        :message="`预计剩余时间: ${timeoutCountdown}秒`"
                        :type="getCountdownAlertType()" 
                        show-icon
                        :closable="false"
                        class="countdown-alert"
                      />
                    </div>
                  </div>
                  <div v-else-if="!executeResult" class="no-result">
                    <a-empty description="点击执行按钮开始测试接口" />
                  </div>
                  <div v-else>
                    <a-tabs v-model:activeKey="activeTab">
                      <a-tab-pane key="response" tab="响应内容">
                        <div class="response-container">
                          <div class="response-header">
                            <a-tag :color="executeResult.success ? 'green' : 'red'">
                              {{ executeResult.status }}
                            </a-tag>
                            <span class="response-time">
                              响应时间: {{ executeResult.responseTime }}ms
                            </span>
                          </div>
                          <!-- 提取值显示 -->
                          <div v-if="executeResult.extractedValue" class="extracted-value-section">
                            <h4>提取的值</h4>
                            <div class="extracted-value-content">
                              <CodeEditor
                                :model-value="formatResponseBody(executeResult.extractedValue)"
                                :readonly="true"
                                :height="400"
                                :language="detectResponseLanguage(executeResult.extractedValue)"
                                :options="{
                                  minimap: { enabled: false },
                                  scrollBeyondLastLine: false,
                                  wordWrap: 'on',
                                  lineNumbers: 'on',
                                  folding: false,
                                  fontSize: 10,
                                  fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                                  lineHeight: 16,
                                  readOnly: true
                                }"
                              />
                            </div>
                          </div>
                          <div class="response-body">
                            <div class="response-body-header" @click="executeResult.extractedValue ? toggleResponseBody() : null" :class="{ 'clickable': executeResult.extractedValue }">
                              <h4>响应体</h4>
                              <div class="response-header-right">
                                <span class="response-size">{{ executeResult.body?.length || 0 }} 字符</span>
                                <a-button 
                                  v-if="executeResult.extractedValue"
                                  type="text" 
                                  size="small" 
                                  class="collapse-btn"
                                  :icon="responseBodyCollapsed ? h(DownOutlined) : h(UpOutlined)"
                                />
                              </div>
                            </div>
                            <div v-show="!executeResult.extractedValue || !responseBodyCollapsed" class="response-body-content">
                              <CodeEditor
                                :model-value="formatResponseBody(executeResult.body)"
                                :readonly="true"
                                :height="400"
                                :language="detectResponseLanguage(executeResult.body)"
                                :options="{
                                  minimap: { enabled: true },
                                  scrollBeyondLastLine: false,
                                  wordWrap: 'on',
                                  lineNumbers: 'on',
                                  folding: true,
                                  fontSize: 10,
                                  fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                                  lineHeight: 16,
                                  readOnly: true,
                                  theme: 'vs-light',
                                  renderLineHighlight: 'gutter',
                                  cursorStyle: 'line',
                                  selectOnLineNumbers: true,
                                  roundedSelection: false,
                                  scrollbar: {
                                    vertical: 'auto',
                                    horizontal: 'auto',
                                    verticalScrollbarSize: 10,
                                    horizontalScrollbarSize: 10
                                  },
                                  padding: { top: 12, bottom: 12 },
                                  contextmenu: true,
                                  mouseWheelZoom: true,
                                  smoothScrolling: true
                                }"
                              />
                            </div>
                          </div>
                        </div>
                      </a-tab-pane>
                      <a-tab-pane key="headers" tab="响应头">
                        <div class="headers-container">
                          <pre>{{ formatHeaders(executeResult.headers) }}</pre>
                        </div>
                      </a-tab-pane>
                      <a-tab-pane v-if="executeResult.error" key="error" tab="错误信息">
                        <div class="error-container">
                          <pre>{{ executeResult.error }}</pre>
                        </div>
                      </a-tab-pane>
                    </a-tabs>
                  </div>
                </div>
              </a-col>
            </a-row>
          </a-tab-pane>

          <!-- 执行记录标签页 -->
          <a-tab-pane key="records">
            <template #tab>
              <span class="tab-content">
                <span class="tab-icon">📋</span>
                <span>执行记录</span>
              </span>
            </template>
            <div class="form-section">
              <div class="section-header">
                <div class="section-icon">
                  <span>📋</span>
                </div>
                <div class="section-title">执行记录</div>
                <div class="section-actions">
                  <ThemeButton 
                    variant="secondary" 
                    size="small"
                    :icon="ReloadOutlined"
                    @click="loadExecutionRecords"
                    :loading="recordsLoading"
                  >
                    刷新
                  </ThemeButton>
                </div>
              </div>
              
              <div class="execution-records-container">
                <div v-if="recordsLoading" class="loading-container">
                  <a-spin size="large" />
                </div>
                <div v-else-if="executionRecords.length === 0" class="no-records">
                  <a-empty description="暂无执行记录" />
                </div>
                <div v-else>
                  <a-table
                    :columns="recordColumns"
                    :data-source="executionRecords"
                    :pagination="recordPagination"
                    :loading="recordsLoading"
                    size="small"
                    :scroll="{ x: 800 }"
                    @change="handleRecordTableChange"
                    class="execution-records-table"
                    :locale="{
                      emptyText: '暂无数据',
                      filterConfirm: '确定',
                      filterReset: '重置',
                      filterEmptyText: '无筛选项',
                      selectAll: '全选',
                      selectInvert: '反选',
                      sortTitle: '排序',
                      expand: '展开行',
                      collapse: '收起行'
                    }"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'success'">
                        <a-tag :color="record.success ? 'green' : 'red'">
                          {{ record.success ? '成功' : '失败' }}
                        </a-tag>
                      </template>
                      <template v-else-if="column.key === 'executionTime'">
                        <span v-if="record.executionTime">{{ record.executionTime }}ms</span>
                        <span v-else class="text-muted">-</span>
                      </template>
                      <template v-else-if="column.key === 'responseStatus'">
                        <a-tag v-if="record.responseStatus" :color="getStatusColor(record.responseStatus)">
                          {{ record.responseStatus }}
                        </a-tag>
                        <span v-else class="text-muted">-</span>
                      </template>
                      <template v-else-if="column.key === 'createTime'">
                        {{ record.createTime || '-' }}
                      </template>
                      <template v-else-if="column.key === 'action'">
                        <a-space size="small">
                          <ThemeButton 
                            variant="secondary"
                            size="small"
                            @click="viewRecordDetail(record)"
                            class="detail-btn"
                          >
                            <template #icon>
                              <EyeOutlined />
                            </template>
                            查看详情
                          </ThemeButton>
                        </a-space>
                      </template>
                    </template>
                  </a-table>
                </div>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>

        <!-- 操作按钮区域 -->
        <div class="form-actions">
          <a-space size="small">
            <a-tag v-if="interfaceData?.environment" :color="getTagColor(interfaceData.environment)" class="interface-tag">
              {{ getTagLabel(interfaceData.environment) }}
            </a-tag>
            <ThemeButton 
              variant="primary" 
              size="small"
              :icon="PlayCircleOutlined"
              :loading="executing"
              :disabled="executing || interfaceData?.status !== 1"
              @click="handleExecute"
              class="submit-btn"
            >
              {{ executing ? '执行中...' : (interfaceData?.status !== 1 ? '接口已禁用' : '执行接口') }}
            </ThemeButton>
            <ThemeButton 
              variant="secondary"
              size="small"
              :icon="CloseOutlined"
              @click="handleBack"
              class="cancel-btn"
            >
              返回
            </ThemeButton>
          </a-space>
        </div>
      </a-card>
    </div>

    <!-- 代码编辑器弹窗 -->
    <CodeEditorModal
      v-model:visible="codeEditorVisible"
      v-model:value="tempCodeValue"
      :language="getCodeLanguage()"
      :placeholder="getCodePlaceholder()"
      @confirm="handleCodeConfirm"
      @cancel="handleCodeCancel"
    />

    <!-- 执行记录详情弹窗 -->
    <a-modal
      v-model:open="recordDetailVisible"
      title="执行记录详情"
      width="80%"
      :footer="null"
      class="record-detail-modal"
    >
      <div v-if="selectedRecord" class="record-detail-content">
        <a-descriptions :column="2" :bordered="true" size="small">
          <a-descriptions-item label="执行ID">
            {{ selectedRecord.id }}
          </a-descriptions-item>
          <a-descriptions-item label="执行人">
            {{ selectedRecord.executorName }}
          </a-descriptions-item>
          <a-descriptions-item label="执行状态">
            <a-tag :color="selectedRecord.success ? 'green' : 'red'">
              {{ selectedRecord.success ? '成功' : '失败' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="响应状态码">
            <a-tag v-if="selectedRecord.responseStatus" :color="getStatusColor(selectedRecord.responseStatus)">
              {{ selectedRecord.responseStatus }}
            </a-tag>
            <span v-else class="text-muted">-</span>
          </a-descriptions-item>
          <a-descriptions-item label="执行时间">
            <span v-if="selectedRecord.executionTime">{{ selectedRecord.executionTime }}ms</span>
            <span v-else class="text-muted">-</span>
          </a-descriptions-item>
          <a-descriptions-item label="客户端IP">
            {{ selectedRecord.clientIp || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="执行时间" :span="2">
            {{ selectedRecord.createTime || '暂无' }}
          </a-descriptions-item>
          <a-descriptions-item v-if="selectedRecord.errorMessage" label="错误信息" :span="2">
            <div class="error-message">{{ selectedRecord.errorMessage }}</div>
          </a-descriptions-item>
        </a-descriptions>

        <a-tabs v-model:activeKey="detailActiveTab" class="detail-tabs">
          <a-tab-pane key="request" tab="请求参数">
            <div class="detail-content">
              <!-- URL参数 -->
              <div v-if="urlParams.length > 0" class="param-section">
                <h4>URL参数</h4>
                <div class="record-params-container">
                  <a-row v-for="param in urlParams" :key="param.name" class="record-param-row" :gutter="[6, 0]">
                    <a-col :span="6">
                      <label class="record-param-label">
                        {{ getParamDisplayName(param) }}
                        <span v-if="param.required" class="required">*</span>
                        <a-tooltip v-if="param.description" :title="param.description" placement="top">
                          <QuestionCircleOutlined class="help-icon" />
                        </a-tooltip>
                      </label>
                    </a-col>
                    <a-col :span="18">
                      <div class="record-param-value">
                        <div v-if="param.inputType === 'CODE'" class="code-value-display">
                          <CodeEditor
                            :model-value="getRecordParamValue(selectedRecord?.requestParams, param.name)"
                            :readonly="true"
                            :height="150"
                            :language="getCodeLanguageForParam(param)"
                            :options="{
                              fontSize: 12,
                              fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                              lineHeight: 18,
                              minimap: { enabled: false },
                              readOnly: true
                            }"
                          />
                        </div>
                        <a-input
                          v-else
                          :value="getRecordParamDisplayValue(selectedRecord?.requestParams, param)"
                          readonly
                          :placeholder="`无${getParamDisplayName(param)}`"
                          class="readonly-input"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </div>
              
              <!-- Header参数 -->
              <div v-if="headerParams.length > 0" class="param-section">
                <h4>请求头</h4>
                <div class="record-params-container">
                  <a-row v-for="param in headerParams" :key="param.name" class="record-param-row" :gutter="[6, 0]">
                    <a-col :span="6">
                      <label class="record-param-label">
                        {{ getParamDisplayName(param) }}
                        <span v-if="param.required" class="required">*</span>
                        <a-tooltip v-if="param.description" :title="param.description" placement="top">
                          <QuestionCircleOutlined class="help-icon" />
                        </a-tooltip>
                      </label>
                    </a-col>
                    <a-col :span="18">
                      <div class="record-param-value">
                        <div v-if="param.inputType === 'CODE'" class="code-value-display">
                          <CodeEditor
                            :model-value="getRecordParamValue(selectedRecord?.requestHeaders, param.name)"
                            :readonly="true"
                            :height="150"
                            :language="getCodeLanguageForParam(param)"
                            :options="{
                              fontSize: 12,
                              fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                              lineHeight: 18,
                              minimap: { enabled: false },
                              readOnly: true
                            }"
                          />
                        </div>
                        <a-input
                          v-else
                          :value="getRecordParamDisplayValue(selectedRecord?.requestHeaders, param)"
                          readonly
                          :placeholder="`无${getParamDisplayName(param)}`"
                          class="readonly-input"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </div>
              
              <!-- Body参数 -->
              <div v-if="bodyParams.length > 0 && interfaceData?.method !== 'GET'" class="param-section">
                <h4>请求体</h4>
                <div class="record-params-container">
                  <a-row v-for="param in bodyParams" :key="param.name" class="record-param-row" :gutter="[6, 0]">
                    <a-col :span="6">
                      <label class="record-param-label">
                        {{ getParamDisplayName(param) }}
                        <span v-if="param.required" class="required">*</span>
                        <a-tooltip v-if="param.description" :title="param.description" placement="top">
                          <QuestionCircleOutlined class="help-icon" />
                        </a-tooltip>
                      </label>
                    </a-col>
                    <a-col :span="18">
                      <div class="record-param-value">
                        <div v-if="param.inputType === 'CODE'" class="code-value-display">
                          <CodeEditor
                            :model-value="getRecordParamValue(selectedRecord?.requestBody, param.name)"
                            :readonly="true"
                            :height="150"
                            :language="getCodeLanguageForParam(param)"
                            :options="{
                              fontSize: 12,
                              fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                              lineHeight: 18,
                              minimap: { enabled: false },
                              readOnly: true
                            }"
                          />
                        </div>
                        <a-input
                          v-else
                          :value="getRecordParamDisplayValue(selectedRecord?.requestBody, param)"
                          readonly
                          :placeholder="`无${getParamDisplayName(param)}`"
                          class="readonly-input"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </div>
              
              <div v-if="urlParams.length === 0 && headerParams.length === 0 && bodyParams.length === 0" class="no-content">
                <a-empty description="该接口未定义任何参数" />
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane key="response" tab="响应内容">
            <div class="detail-content">
              <div v-if="selectedRecord.responseHeaders" class="param-section">
                <h4>响应头</h4>
                <CodeEditor
                  :model-value="formatJson(selectedRecord.responseHeaders)"
                  :readonly="true"
                  :height="200"
                  language="json"
                  :options="{
                    fontSize: 12,
                    fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                    lineHeight: 18
                  }"
                />
              </div>
              <div v-if="selectedRecord.responseBody" class="param-section">
                <h4>响应体</h4>
                <CodeEditor
                  :model-value="formatJson(selectedRecord.responseBody)"
                  :readonly="true"
                  :height="300"
                  :language="detectResponseLanguage(selectedRecord.responseBody)"
                  :options="{
                    fontSize: 12,
                    fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
                    lineHeight: 18
                  }"
                />
              </div>
              <div v-if="!selectedRecord.responseHeaders && !selectedRecord.responseBody" class="no-content">
                <a-empty description="无响应内容" />
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { PlayCircleOutlined, CloseOutlined, QuestionCircleOutlined, ReloadOutlined, EyeOutlined, EditOutlined, DownOutlined, UpOutlined } from '@ant-design/icons-vue'
import { interfaceApi, executionRecordApi, POST_TYPES, TAGS, type ApiInterface, type ApiParam, type ApiExecuteRequest, type ApiExecuteResponse, type ApiInterfaceExecutionRecord, type ApiInterfaceExecutionRecordQuery } from '@/api/interface'
import ThemeButton from '@/components/ThemeButton.vue'
import CodeEditor from '@/components/CodeEditor.vue'
import CodeEditorModal from '@/components/CodeEditorModal.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const formRef = ref()
const loading = ref(false)
const executing = ref(false)
const interfaceData = ref<ApiInterface | null>(null)
const executeResult = ref<ApiExecuteResponse | null>(null)
const activeTab = ref('response')
const mainActiveTab = ref('execute')
const responseBodyCollapsed = ref(true) // 响应体默认收起

// 超时倒计时相关
const timeoutCountdown = ref(0)
const countdownTimer = ref<NodeJS.Timeout | null>(null)
const totalTimeout = ref(0)
const warningShown = ref(false)

// 执行记录相关
const recordsLoading = ref(false)
const executionRecords = ref<ApiInterfaceExecutionRecord[]>([])
const recordDetailVisible = ref(false)
const selectedRecord = ref<ApiInterfaceExecutionRecord | null>(null)
const detailActiveTab = ref('request')

// 代码编辑器弹窗相关
const codeEditorVisible = ref(false)
const tempCodeValue = ref('')
const currentCodeParam = ref<{ param: ApiParam, type: 'params' | 'headers' | 'bodyParams' } | null>(null)

const executeForm = reactive({
  params: {} as Record<string, any>,
  headers: {} as Record<string, any>,
  bodyParams: {} as Record<string, any>
})

// 执行记录表格配置
const recordColumns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    sorter: true
  },
  {
    title: '执行人',
    dataIndex: 'executorName',
    key: 'executorName',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'success',
    key: 'success',
    width: 80
  },
  {
    title: '响应状态',
    dataIndex: 'responseStatus',
    key: 'responseStatus',
    width: 100
  },
  {
    title: '执行耗时',
    dataIndex: 'executionTime',
    key: 'executionTime',
    width: 100,
    sorter: true
  },
  {
    title: '执行时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
    sorter: true
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right'
  }
]

// 分页配置
const recordPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100'],
  showSizeChange: true,
  size: 'small',
  locale: {
    items_per_page: '条/页',
    jump_to: '跳至',
    jump_to_confirm: '确定',
    page: '页',
    prev_page: '上一页',
    next_page: '下一页',
    prev_5: '向前 5 页',
    next_5: '向后 5 页',
    prev_3: '向前 3 页',
    next_3: '向后 3 页',
  }
})

// 获取接口ID
const interfaceId = route.params.id as string

// 计算属性
const urlParams = computed(() => interfaceData.value?.urlParams || [])
const headerParams = computed(() => interfaceData.value?.headerParams || [])
const bodyParams = computed(() => interfaceData.value?.bodyParams || [])

// 初始化数据
onMounted(async () => {
  if (interfaceId) {
    await loadInterfaceData()
    await loadExecutionRecords()
  } else {
    message.error('接口ID不存在')
    router.back()
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopCountdown()
})

// 加载接口数据
const loadInterfaceData = async () => {
  try {
    loading.value = true
    const response = await interfaceApi.getById(Number(interfaceId))
    interfaceData.value = response.data
    
    // 初始化表单数据
    initializeFormData()
  } catch (error) {
    console.error('加载接口数据失败:', error)
    message.error('加载接口数据失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 初始化表单数据
const initializeFormData = () => {
  if (!interfaceData.value) return
  
  // 初始化URL参数
  interfaceData.value.urlParams?.forEach(param => {
    if (param.defaultValue !== undefined) {
      // 多选下拉框的默认值应该是数组
      if (param.inputType === 'MULTI_SELECT') {
        executeForm.params[param.name] = Array.isArray(param.defaultValue) ? param.defaultValue : []
      } else {
        executeForm.params[param.name] = param.defaultValue
      }
    }
  })
  
  // 初始化Header参数
  interfaceData.value.headerParams?.forEach(param => {
    if (param.defaultValue !== undefined) {
      // 多选下拉框的默认值应该是数组
      if (param.inputType === 'MULTI_SELECT') {
        executeForm.headers[param.name] = Array.isArray(param.defaultValue) ? param.defaultValue : []
      } else {
        executeForm.headers[param.name] = param.defaultValue
      }
    }
  })
  
  // 初始化Body参数
  interfaceData.value.bodyParams?.forEach(param => {
    if (param.defaultValue !== undefined) {
      // 多选下拉框的默认值应该是数组
      if (param.inputType === 'MULTI_SELECT') {
        executeForm.bodyParams[param.name] = Array.isArray(param.defaultValue) ? param.defaultValue : []
      } else {
        executeForm.bodyParams[param.name] = param.defaultValue
      }
    }
  })
  
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 跳转到编辑页
const handleEdit = () => {
  if (interfaceId) {
    router.push(`/tools/interface/${interfaceId}/edit`)
  } else {
    message.error('接口ID不存在')
  }
}

// 显示生产环境确认弹窗
const showProductionConfirm = (): Promise<boolean> => {
  return new Promise((resolve) => {
    Modal.confirm({
      title: '⚠️ 生产环境接口执行确认',
      content: h('div', { style: 'line-height: 1.6;' }, [
        h('div', { 
          style: 'background: #fff2f0; border: 1px solid #ffccc7; border-radius: 6px; padding: 12px; margin-bottom: 16px;' 
        }, [
          h('p', { 
            style: 'margin: 0 0 8px 0; color: #ff4d4f; font-weight: 600; font-size: 14px;' 
          }, '⚠️ 您即将执行生产环境的接口！'),
          h('p', { 
            style: 'margin: 0; color: #d4380d; font-size: 13px;' 
          }, '此操作可能对生产环境造成影响，请谨慎操作。')
        ]),
        h('div', { style: 'margin-bottom: 12px;' }, [
          h('p', { 
            style: 'margin: 0 0 8px 0; font-weight: 500; color: #262626;' 
          }, '接口信息：'),
          h('div', { style: 'background: #fafafa; padding: 8px 12px; border-radius: 4px; font-size: 13px;' }, [
            h('div', { style: 'margin-bottom: 4px;' }, `接口名称：${interfaceData.value?.name || '未知'}`),
            h('div', { style: 'margin-bottom: 4px;' }, `请求方法：${interfaceData.value?.method || '未知'}`),
            h('div', { style: 'word-break: break-all;' }, `请求URL：${interfaceData.value?.url || '未知'}`)
          ])
        ]),
        h('p', { 
          style: 'margin: 0; color: #ff4d4f; font-weight: 500; text-align: center;' 
        }, '请确认您了解执行此接口可能对生产环境造成的影响！')
      ]),
      okText: '确认执行',
      cancelText: '取消',
      okType: 'danger',
      centered: true,
      width: 520,
      onOk() {
        resolve(true)
      },
      onCancel() {
        resolve(false)
      }
    })
  })
}

// 开始倒计时
const startCountdown = (timeout: number) => {
  timeoutCountdown.value = timeout
  totalTimeout.value = timeout
  warningShown.value = false
  
  countdownTimer.value = setInterval(() => {
    timeoutCountdown.value--
    
    // 检查是否需要显示警告（剩余时间少于10秒或少于总时间的20%）
    const shouldShowWarning = timeoutCountdown.value <= 10 || 
                             timeoutCountdown.value <= Math.ceil(totalTimeout.value * 0.2)
    
    if (shouldShowWarning && !warningShown.value) {
      warningShown.value = true
      message.warning({
        content: `接口执行即将超时，剩余时间：${timeoutCountdown.value}秒`,
        duration: 3
      })
    }
    
    if (timeoutCountdown.value <= 0) {
      clearInterval(countdownTimer.value!)
      countdownTimer.value = null
    }
  }, 1000)
}

// 停止倒计时
const stopCountdown = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
  timeoutCountdown.value = 0
  totalTimeout.value = 0
  warningShown.value = false
}

// 获取倒计时警告类型
const getCountdownAlertType = () => {
  if (timeoutCountdown.value <= 5) {
    return 'error'
  } else if (timeoutCountdown.value <= 10 || timeoutCountdown.value <= Math.ceil(totalTimeout.value * 0.2)) {
    return 'warning'
  } else {
    return 'info'
  }
}

// 切换响应体展开/收起状态
const toggleResponseBody = () => {
  responseBodyCollapsed.value = !responseBodyCollapsed.value
}

// 执行接口
const handleExecute = async () => {
  if (!interfaceData.value) return
  
  // 检查接口状态
  if (interfaceData.value.status !== 1) {
    message.error('接口已禁用，无法执行')
    return
  }
  
  // 检查是否为生产环境接口，如果是则显示确认弹窗
  if (interfaceData.value.environment === 'PRODUCTION') {
    const confirmed = await showProductionConfirm()
    if (!confirmed) {
      return
    }
  }
  
  try {
    executing.value = true
    executeResult.value = null
    
    // 启动倒计时
    const timeout = interfaceData.value.timeout || 60
    startCountdown(timeout)
    
    const request: ApiExecuteRequest = {
      interfaceId: interfaceData.value.id!,
      headers: executeForm.headers,
      urlParams: executeForm.params,
      bodyParams: executeForm.bodyParams,
      timeout: interfaceData.value.timeout
    }
    
    const response = await interfaceApi.execute(request)
    
    // 检查响应是否成功
    if (response.data.success === false) {
      // 接口执行失败，显示错误信息
      executeResult.value = response.data
      activeTab.value = 'error'
      message.error(response.data.error || '接口执行失败')
    } else {
      // 接口执行成功
      executeResult.value = response.data
      activeTab.value = 'response'
      message.success('接口执行成功')
    }
  } catch (error: any) {
    console.error('接口执行失败:', error)
    
    // 检查是否为超时错误
    const isTimeoutError = error.code === 'ECONNABORTED' || 
                          error.message?.includes('timeout') || 
                          error.message?.includes('请求超时')
    
    if (isTimeoutError) {
      // 超时错误处理
      const timeout = interfaceData.value?.timeout || 60
      message.error({
        content: `接口执行超时（${timeout}秒），请检查网络连接或增加超时时间`,
        duration: 6
      })
      
      // 创建超时错误结果
      executeResult.value = {
        status: 0,
        headers: {},
        body: '',
        responseTime: timeout * 1000,
        success: false,
        error: `请求超时，超过${timeout}秒未响应`
      }
      activeTab.value = 'error'
    } else {
      // 其他错误处理
      let errorMessage = '接口执行失败'
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message
      } else if (error.response?.data?.data) {
        errorMessage = error.response.data.data
      } else if (error.message) {
        errorMessage = error.message
      }
      
      message.error(errorMessage)
      
      // 创建错误结果
      executeResult.value = {
        status: error.response?.status || 0,
        headers: error.response?.headers || {},
        body: error.response?.data || '',
        responseTime: 0,
        success: false,
        error: errorMessage
      }
      activeTab.value = 'error'
    }
  } finally {
    executing.value = false
    stopCountdown()
  }
}

// 获取输入组件
const getInputComponent = (param: ApiParam) => {
  switch (param.inputType) {
    case 'SELECT':
      return 'a-select'
    case 'MULTI_SELECT':
      return 'a-select'
    case 'TEXTAREA':
      return 'a-textarea'
    case 'CODE':
      return 'a-textarea'
    case 'NUMBER':
      return 'a-input-number'
    case 'DATE':
      return 'a-date-picker'
    case 'DATETIME':
      return 'a-date-picker'
    default:
      return 'a-input'
  }
}

// 获取输入组件绑定属性
const getInputBindings = (param: ApiParam, type: 'params' | 'headers' | 'bodyParams') => {
  const baseProps = {
    ...getDatePickerProps(param),
    ...getCodeEditorProps(param)
  }
  
  // 为SELECT和MULTI_SELECT类型添加属性
  let selectProps = {}
  if (param.inputType === 'SELECT' && !param.required) {
    selectProps = { allowClear: true }
  } else if (param.inputType === 'MULTI_SELECT') {
    // 多选下拉框：非必填且有值时才显示清空按钮
    const hasValue = executeForm[type][param.name] && 
      Array.isArray(executeForm[type][param.name]) && 
      executeForm[type][param.name].length > 0
    selectProps = { 
      mode: 'multiple',
      allowClear: !param.required && hasValue
    }
  }
  
  // 根据组件类型选择不同的 v-model 绑定方式
  if (param.inputType === 'CODE') {
    return {
      ...baseProps,
      ...selectProps,
      modelValue: executeForm[type][param.name] || '',
      'onUpdate:modelValue': (value: string) => {
        executeForm[type][param.name] = value
      }
    }
  } else {
    return {
      ...baseProps,
      ...selectProps,
      value: executeForm[type][param.name],
      'onUpdate:value': (value: any) => {
        executeForm[type][param.name] = value
      }
    }
  }
}

// 获取日期选择器属性
const getDatePickerProps = (param: ApiParam) => {
  if (param.inputType === 'DATE') {
    return {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD'
    }
  } else if (param.inputType === 'DATETIME') {
    return {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss'
    }
  }
  return {}
}

// 获取代码编辑器属性
const getCodeEditorProps = (param: ApiParam) => {
  if (param.inputType === 'CODE') {
    return {
      height: 200,
      options: {
        minimap: { enabled: true },
        scrollBeyondLastLine: false,
        wordWrap: 'on' as const,
        lineNumbers: 'on' as const,
        folding: true,
        fontSize: 16,
        fontFamily: 'Monaco, Menlo, Ubuntu Mono, monospace',
        lineHeight: 24,
        readOnly: false
      }
    }
  }
  return {}
}

// 格式化响应体
const formatResponseBody = (body: string) => {
  try {
    const parsed = JSON.parse(body)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return body
  }
}

// 格式化响应头
const formatHeaders = (headers: Record<string, string>) => {
  return Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n')
}

// 检测响应语言
const detectResponseLanguage = (body: string): string => {
  if (!body || body.trim() === '') {
    return 'text'
  }

  const trimmedBody = body.trim()
  
  // JSON检测
  if (trimmedBody.startsWith('{') && trimmedBody.endsWith('}') ||
      trimmedBody.startsWith('[') && trimmedBody.endsWith(']')) {
    try {
      JSON.parse(trimmedBody)
      return 'json'
    } catch {
      // 不是有效的JSON，继续检测其他格式
    }
  }
  
  // XML检测
  if (trimmedBody.startsWith('<') && trimmedBody.endsWith('>')) {
    return 'xml'
  }
  
  // HTML检测
  if (trimmedBody.includes('<html') || trimmedBody.includes('<div') || trimmedBody.includes('<p')) {
    return 'html'
  }
  
  // CSS检测
  if (trimmedBody.includes('{') && trimmedBody.includes('}') && trimmedBody.includes(':')) {
    return 'css'
  }
  
  // JavaScript检测
  if (trimmedBody.includes('function') || trimmedBody.includes('=>') || trimmedBody.includes('const ')) {
    return 'javascript'
  }
  
  // SQL检测
  if (trimmedBody.toUpperCase().includes('SELECT') || trimmedBody.toUpperCase().includes('INSERT') || 
      trimmedBody.toUpperCase().includes('UPDATE') || trimmedBody.toUpperCase().includes('DELETE')) {
    return 'sql'
  }
  
  // YAML检测
  if (trimmedBody.includes(':') && trimmedBody.includes('\n') && !trimmedBody.includes('{')) {
    return 'yaml'
  }
  
  // 默认返回文本
  return 'text'
}

// 代码编辑器相关方法
const getCodePreview = (value: string | undefined): string => {
  if (!value || value.trim() === '') {
    return ''
  }
  
  // 如果内容太长，显示前50个字符
  if (value.length > 50) {
    return value.substring(0, 50) + '...'
  }
  
  return value
}

// 获取参数显示名称
const getParamDisplayName = (param: ApiParam): string => {
  if (param.chineseName) {
    return `${param.chineseName}（${param.name}）`
  }
  return param.name
}

// 获取下拉选项
const getSelectOptions = (param: ApiParam) => {
  if ((param.inputType === 'SELECT' || param.inputType === 'MULTI_SELECT') && param.options) {
    return param.options.map(option => ({
      label: option.label || option.value,
      value: option.value
    }))
  }
  return []
}

// 数据类型到代码编辑器语言的映射
const getDataTypeToLanguageMapping = (): Record<string, string> => {
  return {
    // 传统数据类型
    'STRING': 'text',
    'INTEGER': 'text',
    'LONG': 'text', 
    'DOUBLE': 'text',
    'BOOLEAN': 'text',
    'DATE': 'text',
    'DATETIME': 'text',
    'JSON': 'json',
    'ARRAY': 'json',
    // 编程语言类型
    'TEXT': 'text',
    'XML': 'xml',
    'HTML': 'html',
    'CSS': 'css',
    'JAVASCRIPT': 'javascript',
    'TYPESCRIPT': 'typescript',
    'JAVA': 'java',
    'KOTLIN': 'kotlin',
    'SQL': 'sql',
    'YAML': 'yaml'
  }
}

const getCodeLanguage = (): string => {
  if (!currentCodeParam.value?.param.dataType) {
    return 'json' // 默认使用JSON
  }
  
  const dataType = currentCodeParam.value.param.dataType
  const languageMapping = getDataTypeToLanguageMapping()
  const language = languageMapping[dataType] || 'json'
  
  return language
}

const getCodePlaceholder = (): string => {
  if (currentCodeParam.value) {
    return `请输入${getParamDisplayName(currentCodeParam.value.param)}...`
  }
  return '请输入代码...'
}

const openCodeEditor = (param: ApiParam, type: 'params' | 'headers' | 'bodyParams') => {
  currentCodeParam.value = { param, type }
  tempCodeValue.value = executeForm[type][param.name] || ''
  codeEditorVisible.value = true
}

const handleCodeConfirm = (value: string) => {
  if (currentCodeParam.value) {
    executeForm[currentCodeParam.value.type][currentCodeParam.value.param.name] = value
  }
  codeEditorVisible.value = false
  currentCodeParam.value = null
}

const handleCodeCancel = () => {
  codeEditorVisible.value = false
  currentCodeParam.value = null
}

// 获取请求方法颜色
const getMethodColor = (method: string) => {
  const colors: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple',
    HEAD: 'cyan',
    OPTIONS: 'geekblue'
  }
  return colors[method] || 'default'
}

// 获取POST类型标签
const getPostTypeLabel = (postType: string) => {
  const type = POST_TYPES.find(t => t.value === postType)
  return type ? type.label : postType
}

// 获取标签标签
const getTagLabel = (tag: string) => {
  const tagInfo = TAGS.find(t => t.value === tag)
  return tagInfo ? tagInfo.label : tag
}

// 获取标签颜色
const getTagColor = (tag: string) => {
  return tag === 'TEST' ? 'blue' : 'green'
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date | undefined): string => {
  if (!dateTime) return '暂无'
  
  // 如果后端返回的是格式化的字符串，直接返回
  if (typeof dateTime === 'string' && dateTime.includes('-') && dateTime.includes(':')) {
    return dateTime
  }
  
  // 如果是 Date 对象或其他格式，进行转换
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化超时时间
const formatTimeout = (timeout: number): string => {
  if (timeout < 60) {
    return `${timeout}秒`
  } else if (timeout < 3600) {
    const minutes = Math.floor(timeout / 60)
    const seconds = timeout % 60
    if (seconds === 0) {
      return `${minutes}分钟`
    } else {
      return `${minutes}分${seconds}秒`
    }
  } else {
    const hours = Math.floor(timeout / 3600)
    const minutes = Math.floor((timeout % 3600) / 60)
    const seconds = timeout % 60
    if (minutes === 0 && seconds === 0) {
      return `${hours}小时`
    } else if (seconds === 0) {
      return `${hours}小时${minutes}分钟`
    } else {
      return `${hours}小时${minutes}分${seconds}秒`
    }
  }
}


// 执行记录相关方法
const loadExecutionRecords = async () => {
  if (!interfaceId) return
  
  try {
    recordsLoading.value = true
    const query: ApiInterfaceExecutionRecordQuery = {
      interfaceId: Number(interfaceId),
      page: recordPagination.current,
      size: recordPagination.pageSize
    }
    
    const response = await executionRecordApi.getList(query)
    executionRecords.value = response.data?.records || []
    
    // 使用API返回的分页信息更新total
    recordPagination.total = response.data?.total || 0
  } catch (error) {
    console.error('加载执行记录失败:', error)
    message.error('加载执行记录失败')
  } finally {
    recordsLoading.value = false
  }
}

// 表格变化处理
const handleRecordTableChange = (pagination: any) => {
  recordPagination.current = pagination.current
  recordPagination.pageSize = pagination.pageSize
  loadExecutionRecords()
}

// 查看执行记录详情
const viewRecordDetail = (record: ApiInterfaceExecutionRecord) => {
  selectedRecord.value = record
  recordDetailVisible.value = true
  detailActiveTab.value = 'request'
}

// 格式化JSON
const formatJson = (jsonString: string | undefined): string => {
  if (!jsonString) return ''
  
  try {
    const parsed = JSON.parse(jsonString)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return jsonString
  }
}

// 获取状态码颜色
const getStatusColor = (status: number): string => {
  if (status >= 200 && status < 300) return 'green'
  if (status >= 300 && status < 400) return 'blue'
  if (status >= 400 && status < 500) return 'orange'
  if (status >= 500) return 'red'
  return 'default'
}

// 从JSON字符串中获取参数值
const getRecordParamValue = (jsonString: string | undefined, paramName: string): string => {
  if (!jsonString) return ''
  
  try {
    const params = JSON.parse(jsonString)
    const value = params[paramName]
    
    if (value === undefined || value === null) {
      return ''
    }
    
    // 如果是对象或数组,格式化为JSON字符串
    if (typeof value === 'object') {
      return JSON.stringify(value, null, 2)
    }
    
    return String(value)
  } catch (error) {
    console.error('解析参数JSON失败:', error)
    return ''
  }
}

// 获取参数的显示值(用于只读输入框)
const getRecordParamDisplayValue = (jsonString: string | undefined, param: ApiParam): string => {
  if (!jsonString) return ''
  
  try {
    const params = JSON.parse(jsonString)
    const value = params[param.name]
    
    if (value === undefined || value === null) {
      return ''
    }
    
    // 多选下拉框显示
    if (param.inputType === 'MULTI_SELECT' && Array.isArray(value)) {
      // 尝试从选项中获取标签
      if (param.options && param.options.length > 0) {
        const labels = value.map(v => {
          const option = param.options?.find(opt => opt.value === v)
          return option?.label || v
        })
        return labels.join(', ')
      }
      return value.join(', ')
    }
    
    // 单选下拉框显示
    if (param.inputType === 'SELECT' && param.options && param.options.length > 0) {
      const option = param.options.find(opt => opt.value === value)
      return option?.label || String(value)
    }
    
    // 对象或数组显示为JSON字符串
    if (typeof value === 'object') {
      return JSON.stringify(value)
    }
    
    return String(value)
  } catch (error) {
    console.error('解析参数JSON失败:', error)
    return ''
  }
}

// 根据参数的dataType获取代码编辑器的语言
const getCodeLanguageForParam = (param: ApiParam): string => {
  const languageMapping = getDataTypeToLanguageMapping()
  return languageMapping[param.dataType] || 'text'
}
</script>

<style scoped>
.interface-execute-page {
  min-height: 100%;
  background: #f0f2f5;
  padding: 0;
}

.form-header {
  margin-bottom: 8px;
  padding: 0 12px;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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
  padding: 0 12px 16px;
}

.form-card {
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

/* 表单区域样式 */
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
  border-radius: 4px;
  background: linear-gradient(135deg, var(--primary-color, #1890ff), var(--secondary-color, #40a9ff));
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

/* 操作按钮样式 */
.form-actions {
  padding: 12px 0 0 0;
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.interface-tag {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  margin-right: 8px;
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

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.no-result {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.executing-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 20px;
}

.executing-text {
  margin-top: 16px;
  font-size: 14px;
  color: #0369a1;
  font-weight: 500;
  text-align: center;
}

.timeout-countdown {
  margin-top: 12px;
  width: 100%;
  max-width: 300px;
}

.countdown-alert {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.countdown-alert.ant-alert-info {
  border: 1px solid #91d5ff;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
}

.countdown-alert.ant-alert-info .ant-alert-message {
  font-weight: 500;
  color: #1890ff;
}

.countdown-alert.ant-alert-warning {
  border: 1px solid #ffd591;
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
}

.countdown-alert.ant-alert-warning .ant-alert-message {
  font-weight: 600;
  color: #fa8c16;
}

.countdown-alert.ant-alert-error {
  border: 1px solid #ffccc7;
  background: linear-gradient(135deg, #fff2f0 0%, #fff1f0 100%);
  animation: pulse 1s infinite;
}

.countdown-alert.ant-alert-error .ant-alert-message {
  font-weight: 600;
  color: #ff4d4f;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

.param-group {
  margin-bottom: 12px;
}

.param-group h4 {
  margin-bottom: 8px;
  color: #1890ff;
  font-weight: 600;
  font-size: 13px;
}

.param-row {
  margin-bottom: 8px;
}

.param-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
  font-weight: 500;
  font-size: 12px;
  color: #333;
}

.required {
  color: #ff4d4f;
  margin-left: 4px;
}

.help-icon {
  color: #8c8c8c;
  font-size: 12px;
  cursor: help;
  transition: color 0.2s ease;
}

.help-icon:hover {
  color: #1890ff;
}

.no-params {
  text-align: center;
  padding: 40px 0;
}

.response-container {
  height: 100%;
}

.extracted-value-section {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 2px solid #52c41a;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.15);
  overflow: hidden;
  position: relative;
}

.extracted-value-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 50%, #52c41a 100%);
}

.extracted-value-section h4 {
  margin: 0;
  padding: 16px 16px 8px 16px;
  color: #389e0d;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.extracted-value-section h4::before {
  content: '✨';
  font-size: 16px;
}

.extracted-value-content {
  background: #fff;
  margin: 0 12px 12px 12px;
  border-radius: 8px;
  border: 1px solid #b7eb8f;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.response-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.response-time {
  font-size: 12px;
  color: #666;
}

.response-body {
  margin-top: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid #1890ff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15);
  overflow: hidden;
  position: relative;
}

.response-body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #1890ff 0%, #40a9ff 50%, #1890ff 100%);
}

.response-body-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 8px 16px;
  background: transparent;
  border-bottom: 1px solid rgba(24, 144, 255, 0.1);
  transition: background-color 0.2s ease;
}

.response-body-header.clickable {
  cursor: pointer;
}

.response-body-header.clickable:hover {
  background: rgba(24, 144, 255, 0.05);
}

.response-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.response-body-header h4 {
  margin: 0;
  color: #0050b3;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.response-body-header h4::before {
  content: '📄';
  font-size: 16px;
}

.response-size {
  font-size: 12px;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 4px 12px;
  border-radius: 16px;
  border: 1px solid rgba(24, 144, 255, 0.2);
  font-weight: 500;
}

.response-body-content {
  background: #fff;
  margin: 0 12px 12px 12px;
  border-radius: 8px;
  border: 1px solid rgba(24, 144, 255, 0.1);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.collapse-btn {
  color: #1890ff !important;
  border: none !important;
  box-shadow: none !important;
  padding: 4px !important;
  min-width: 24px !important;
  height: 24px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.2s ease !important;
}

.collapse-btn:hover {
  background: rgba(24, 144, 255, 0.1) !important;
  color: #0050b3 !important;
}

.headers-container,
.error-container {
  padding: 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
  max-height: 300px;
  overflow-y: auto;
}

.headers-container pre,
.error-container pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 代码编辑器输入样式 */
.code-editor-input {
  width: 100%;
}

.code-preview-input {
  cursor: pointer;
}

.code-preview-input :deep(.ant-input) {
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  color: #666;
  font-family: Monaco, Consolas, monospace;
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease;
  height: 32px;
  padding: 4px 8px;
}

.code-preview-input :deep(.ant-input):hover {
  border-color: #40a9ff;
}

.code-preview-input :deep(.ant-input):focus {
  border-color: #40a9ff;
  box-shadow: none;
  outline: none;
}

.code-preview-input :deep(.ant-input-suffix) {
  padding-right: 4px;
}

.code-preview-input :deep(.ant-input-suffix .ant-btn) {
  border-radius: 3px;
  font-size: 11px;
  height: 24px;
  padding: 0 8px;
  line-height: 22px;
}

/* 基本信息样式 */
.info-card {
  margin: 0 0 24px 0;
  margin-right: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.content-row {
  margin: 0;
}

/* 主标签页样式 */
.main-tabs {
  margin-top: 16px;
}

.main-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 12px;
  background: #fff;
  border-radius: 6px;
  padding: 0 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.main-tabs :deep(.ant-tabs-nav::before) {
  border-bottom: 1px solid #f0f0f0;
}

.main-tabs :deep(.ant-tabs-tab) {
  padding: 8px 8px;
  font-weight: 500;
  font-size: 13px;
  color: #666;
  border: none;
  background: transparent;
  border-radius: 4px;
  margin-right: 0px;
  transition: all 0.2s ease;
}

.main-tabs :deep(.ant-tabs-tab:hover) {
  color: var(--primary-color, #1890ff);
  background: rgba(24, 144, 255, 0.06);
}

.main-tabs :deep(.ant-tabs-tab-active) {
  color: var(--primary-color, #1890ff);
  background: rgba(24, 144, 255, 0.1);
  font-weight: 600;
}

.main-tabs :deep(.ant-tabs-ink-bar) {
  background: var(--primary-color, #1890ff);
  height: 2px;
}

.main-tabs :deep(.ant-tabs-content-holder) {
  padding: 0;
}

.main-tabs :deep(.ant-tabs-tabpane) {
  padding: 0;
}

/* 标签页内容样式 */
.tab-content {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.tab-icon {
  font-size: 13px;
  opacity: 0.8;
}

.main-tabs :deep(.ant-tabs-tab-active .tab-icon) {
  opacity: 1;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1890ff;
}

.title-icon {
  font-size: 16px;
}

.interface-info {
  padding: 8px 0;
}

.info-value {
  font-weight: 500;
  color: #333;
}

.url-text {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  padding: 6px 12px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #1890ff;
  word-break: break-all;
  border: 1px solid #d6e4ff;
}

.description-text {
  color: #666;
  line-height: 1.5;
}

.method-tag,
.post-type-tag,
.status-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 2px 8px;
}

.time-text {
  color: #999;
  font-size: 13px;
}

.timeout-text {
  color: #1890ff;
  font-size: 13px;
  font-weight: 500;
}

.value-path-text {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 11px;
  color: #fa8c16;
  word-break: break-all;
  border: 1px solid #ffd591;
  font-weight: 500;
}


:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: #666;
  background: #fafafa;
  padding: 8px 12px;
  border-radius: 6px;
  margin-right: 12px;
  min-width: 80px;
}

:deep(.ant-descriptions-item-content) {
  color: #333;
  padding: 8px 0;
}

:deep(.ant-descriptions-item) {
  margin-bottom: 8px;
  padding: 4px 0;
}

/* 执行记录相关样式 */
.section-actions {
  margin-left: auto;
}

.execution-records-container {
  margin-top: 16px;
}

.execution-records-table {
  margin-top: 16px;
}

.execution-records-table :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  font-size: 12px;
}

.execution-records-table :deep(.ant-table-tbody > tr > td) {
  font-size: 12px;
  padding: 8px 12px;
}

.text-muted {
  color: #999;
}

.no-records {
  text-align: center;
  padding: 40px 0;
}

/* 执行记录详情弹窗样式 */
.record-detail-modal :deep(.ant-modal-header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #f0f0f0;
}

.record-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-tabs {
  margin-top: 16px;
}

.detail-content {
  padding: 16px 0;
}

.param-section {
  margin-bottom: 16px;
}

.param-section h4 {
  margin-bottom: 8px;
  color: #1890ff;
  font-weight: 600;
  font-size: 13px;
}

.no-content {
  text-align: center;
  padding: 40px 0;
}

.error-message {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  padding: 12px;
  color: #ff4d4f;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

/* 查看详情按钮样式 */
.detail-btn {
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 12px;
  padding: 4px 8px;
  height: auto;
  min-width: 80px;
}

.detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-btn :deep(.anticon) {
  font-size: 12px;
  margin-right: 4px;
}

/* 编辑按钮样式 */
.edit-btn.theme-button.theme-button--secondary {
  border-radius: 4px !important;
  transition: all 0.2s ease !important;
  font-size: 13px !important;
  font-weight: 500 !important;
  padding: 4px 12px !important;
  height: 28px !important;
  min-width: 90px !important;
  background: #f0f9ff !important;
  border: 1px solid #bae6fd !important;
  color: #0369a1 !important;
  backdrop-filter: none !important;
}

.edit-btn.theme-button.theme-button--secondary:hover:not(.theme-button--disabled) {
  transform: translateY(-1px) !important;
  box-shadow: 0 2px 8px rgba(3, 105, 161, 0.15) !important;
  background: #e0f2fe !important;
  border-color: #7dd3fc !important;
  color: #0c4a6e !important;
}

.edit-btn.theme-button.theme-button--secondary:active,
.edit-btn.theme-button.theme-button--secondary:focus,
.edit-btn.theme-button.theme-button--secondary:focus-visible {
  transform: translateY(0) !important;
  box-shadow: 0 0 0 2px rgba(3, 105, 161, 0.2) !important;
  background: #e0f2fe !important;
  border-color: #0369a1 !important;
  color: #0c4a6e !important;
  outline: none !important;
}

.edit-btn :deep(.theme-button__icon) {
  font-size: 12px !important;
  margin-right: 4px !important;
}

/* 执行记录参数显示样式 */
.record-params-container {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e8e8e8;
}

.record-param-row {
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-param-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.record-param-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  font-size: 12px;
  color: #333;
  padding-top: 4px;
}

.record-param-value {
  width: 100%;
}

.readonly-input {
  background: #fff !important;
  border: 1px solid #d9d9d9 !important;
  border-radius: 4px;
  color: #595959 !important;
  font-size: 13px;
  cursor: default;
}

.readonly-input:hover,
.readonly-input:focus {
  border-color: #d9d9d9 !important;
  box-shadow: none !important;
}

.readonly-input :deep(.ant-input) {
  background: #fff !important;
  color: #595959 !important;
  cursor: default;
}

.code-value-display {
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}
</style>
