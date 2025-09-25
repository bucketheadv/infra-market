<template>
  <div class="image-processor">
    <a-card>
      <template #title>图片处理工具</template>
      
      <div class="content-container">
        <!-- 输入方式选择 -->
        <a-form :model="{ inputMethod }" class="input-methods">
          <a-form-item label="输入方式">
            <a-radio-group v-model:value="inputMethod">
              <a-radio-button value="upload">
                <UploadOutlined />
                文件上传
              </a-radio-button>
              <a-radio-button value="url">
                <LinkOutlined />
                图片地址
              </a-radio-button>
            </a-radio-group>
          </a-form-item>
        </a-form>

        <!-- 图片地址输入 -->
        <div v-if="inputMethod === 'url'" class="url-input-section">
          <a-form :model="{ imageUrl }">
            <a-form-item label="图片地址">
              <a-input
                v-model:value="imageUrl"
                placeholder="请输入图片地址，支持 http/https 链接"
                @pressEnter="loadImageFromUrl"
              >
                <template #addonAfter>
                  <a-button 
                    type="primary" 
                    :loading="loading"
                    @click="loadImageFromUrl"
                    :disabled="!imageUrl.trim()"
                  >
                    加载图片
                  </a-button>
                </template>
              </a-input>
            </a-form-item>
          </a-form>
          
          <!-- 示例图片链接 -->
          <a-form-item label="快速测试">
            <a-space wrap>
              <a-button 
                size="small" 
                @click="loadExampleImage('https://picsum.photos/800/600')"
                :loading="loading"
              >
                随机图片
              </a-button>
              <a-button 
                size="small" 
                @click="loadExampleImage('https://via.placeholder.com/800x600/1890ff/ffffff?text=Test+Image')"
                :loading="loading"
              >
                占位图片
              </a-button>
              <a-button 
                size="small" 
                @click="loadExampleImage('https://httpbin.org/image/png')"
                :loading="loading"
              >
                PNG图片
              </a-button>
              <a-button 
                size="small" 
                @click="loadExampleImage('https://httpbin.org/image/jpeg')"
                :loading="loading"
              >
                JPEG图片
              </a-button>
            </a-space>
          </a-form-item>
          
          <!-- 跨域说明 -->
          <a-alert
            message="提示：某些图片链接可能因跨域限制无法加载，建议使用示例链接或直接上传本地文件"
            type="info"
            show-icon
            :closable="false"
          />
        </div>

        <!-- 文件上传区域 -->
        <div v-if="inputMethod === 'upload'" class="upload-section">
          <a-form-item label="文件上传">
            <a-upload-dragger
              v-model:fileList="fileList"
              :beforeUpload="beforeUpload"
              :showUploadList="false"
              :accept="acceptedFormats"
              @drop="handleDrop"
              @dragover="handleDragOver"
              @dragleave="handleDragLeave"
            >
              <div class="upload-content" :class="{ 'drag-over': isDragOver }">
                <div class="upload-icon">
                  <InboxOutlined v-if="!isDragOver" />
                  <CloudUploadOutlined v-else />
                </div>
                <div class="upload-text">
                  <p class="upload-title">
                    {{ isDragOver ? '释放文件以开始上传' : '点击或拖拽文件到此区域上传' }}
                  </p>
                  <p class="upload-hint">
                    支持 JPG、PNG、GIF、WEBP、BMP、SVG、AVIF、HEIC、PAG 等格式
                  </p>
                  <div class="upload-note">
                    <ExclamationCircleOutlined />
                    <span>注意：PAG文件为动画格式，首次加载需要下载libpag包</span>
                  </div>
                </div>
              </div>
            </a-upload-dragger>
          </a-form-item>
        </div>

        <!-- 图片预览区域 -->
        <div v-if="currentImage" class="preview-section">
          <!-- 文件信息卡片 -->
          <div class="file-info-card">
            <div class="file-info-content">
              <div class="file-name">
                <span class="file-name-text">{{ currentImage.name }}</span>
                <span class="file-size">{{ formatFileSize(currentImage.size) }}</span>
              </div>
              <div class="file-details">
                <span class="file-format">{{ currentImage.type || '未知格式' }}</span>
                <span class="file-dimensions">{{ imageDimensions.width }} × {{ imageDimensions.height }} 像素</span>
              </div>
            </div>
            <div class="file-actions">
              <a-button @click="clearImage" danger size="small">
                <DeleteOutlined />
                清除
              </a-button>
            </div>
          </div>
          
          <div class="image-preview">
            <!-- PAG文件特殊处理 -->
            <div v-if="isPagFile(currentImage.name)" class="pag-preview">
              <div v-if="!pagPlayerLoaded" class="pag-loading">
                <div class="pag-icon">
                  <LoadingOutlined />
                </div>
                <div class="pag-info">
                  <h3>正在加载PAG播放器...</h3>
                  <p>首次加载libpag包可能需要几秒钟</p>
                </div>
              </div>
              <div v-else-if="pagPlayerError" class="pag-error">
                <div class="pag-icon">
                  <ExclamationCircleOutlined />
                </div>
                <div class="pag-info">
                  <h3>PAG播放器加载失败</h3>
                  <p>无法加载libpag包，请检查依赖安装</p>
                  <div class="pag-actions">
                    <a-button @click="retryLoadPagPlayer">
                      <ReloadOutlined />
                      重试加载
                    </a-button>
                    <a-button type="primary" @click="downloadPagFile">
                      <DownloadOutlined />
                      下载文件
                    </a-button>
                  </div>
                </div>
              </div>
              <div v-else class="pag-player-container">
                <div class="pag-player-header">
                  <h3>PAG动画预览</h3>
                  <div class="pag-controls">
                    <a-button size="small" @click="playPagAnimation" :disabled="!pagPlayer">
                      <PlayCircleOutlined />
                      播放
                    </a-button>
                    <a-button size="small" @click="pausePagAnimation" :disabled="!pagPlayer">
                      <PauseCircleOutlined />
                      暂停
                    </a-button>
                    <a-button size="small" @click="stopPagAnimation" :disabled="!pagPlayer">
                      <StopOutlined />
                      停止
                    </a-button>
                  </div>
                </div>
                <div class="pag-canvas-container">
                  <canvas 
                    ref="pagCanvas" 
                    class="pag-canvas"
                    style="display: block;"
                  ></canvas>
                </div>
                <div class="pag-actions">
                  <a-button type="primary" @click="downloadPagFile">
                    <DownloadOutlined />
                    下载文件
                  </a-button>
                  <a-button @click="openPagInfo">
                    <InfoCircleOutlined />
                    了解更多
                  </a-button>
                </div>
              </div>
            </div>
            <!-- 普通图片预览 -->
            <img
              v-else
              :src="currentImage.url"
              :alt="currentImage.name"
              class="preview-image"
              @load="onImageLoad"
              @error="onImageError"
            />
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="error-section">
          <a-alert
            :message="errorMessage"
            type="error"
            show-icon
            closable
            @close="errorMessage = ''"
            :description="getErrorDescription()"
          >
            <template #action v-if="imageUrl && errorMessage.includes('跨域')">
              <a-space>
                <a-button size="small" @click="copyImageUrl">
                  <CopyOutlined />
                  复制链接
                </a-button>
                <a-button size="small" @click="downloadImage">
                  <DownloadOutlined />
                  尝试下载
                </a-button>
                <a-button size="small" type="primary" @click="switchToUpload">
                  <UploadOutlined />
                  上传本地文件
                </a-button>
              </a-space>
            </template>
          </a-alert>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw } from 'vue'
import { message } from 'ant-design-vue'
import {
  LinkOutlined,
  UploadOutlined,
  InboxOutlined,
  CloudUploadOutlined,
  DeleteOutlined,
  DownloadOutlined,
  CopyOutlined,
  InfoCircleOutlined,
  ExclamationCircleOutlined,
  LoadingOutlined,
  ReloadOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import type { UploadFile } from 'ant-design-vue'

// 响应式数据
const inputMethod = ref<'url' | 'upload'>('upload')
const imageUrl = ref('')
const fileList = ref<UploadFile[]>([])
const currentImage = ref<{
  name: string
  url: string
  size: number
  type?: string
} | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const isDragOver = ref(false)
const imageDimensions = ref({ width: 0, height: 0 })

// PAG播放器相关
const pagCanvas = ref<HTMLCanvasElement | null>(null)
const pagPlayer = ref<any>(null)
const pagPlayerLoaded = ref(false)
const pagPlayerError = ref(false)
const libpag = ref<any>(null)

// 播放状态管理
const isPlaying = ref(false)

// 支持的图片格式
const acceptedFormats = computed(() => {
  return 'image/jpeg,image/jpg,image/png,image/gif,image/webp,image/bmp,image/svg+xml,image/avif,image/heic,image/heif,.pag'
})

// 从URL加载图片
const loadImageFromUrl = async () => {
  if (!imageUrl.value.trim()) {
    message.warning('请输入图片地址')
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    // 验证URL格式
    const url = new URL(imageUrl.value)
    if (!['http:', 'https:'].includes(url.protocol)) {
      throw new Error('只支持 http 和 https 协议的图片链接')
    }

    // 尝试多种方式加载图片
    await loadImageWithFallback(imageUrl.value)

    // 设置当前图片
    currentImage.value = {
      name: `图片_${Date.now()}`,
      url: imageUrl.value,
      size: 0, // URL图片无法获取确切大小
      type: getImageTypeFromUrl(imageUrl.value)
    }

    // 如果是PAG文件，加载libpag
    if (isPagFile(imageUrl.value)) {
      try {
        await loadLibPag()
        if (pagPlayerLoaded.value && !pagPlayer.value) {
          // 等待DOM更新后初始化播放器
          setTimeout(() => {
            initPagPlayer()
          }, 100)
        }
      } catch (error) {
        console.error('libpag加载失败:', error)
      }
    }

  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载图片失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

// 带降级处理的图片加载
const loadImageWithFallback = async (imageUrl: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    let attempts = 0
    const maxAttempts = 3
    
    const tryLoad = (useCORS: boolean = false) => {
      attempts++
      const img = new Image()
      
      if (useCORS) {
        img.crossOrigin = 'anonymous'
      }
      
      img.onload = () => {
        console.log(`图片加载成功 (尝试 ${attempts}, CORS: ${useCORS})`)
        resolve()
      }
      
      img.onerror = (error) => {
        console.log(`图片加载失败 (尝试 ${attempts}, CORS: ${useCORS})`, error)
        
        if (attempts < maxAttempts) {
          // 尝试不同的加载方式
          if (attempts === 1) {
            // 第二次尝试：使用CORS
            tryLoad(true)
          } else if (attempts === 2) {
            // 第三次尝试：不使用CORS，但添加时间戳避免缓存
            const urlWithTimestamp = imageUrl + (imageUrl.includes('?') ? '&' : '?') + '_t=' + Date.now()
            const img3 = new Image()
            img3.onload = () => {
              console.log('图片加载成功 (尝试 3, 带时间戳)')
              resolve()
            }
            img3.onerror = () => {
              console.log('图片加载失败 (尝试 3, 带时间戳)')
              reject(createDetailedError(imageUrl))
            }
            img3.src = urlWithTimestamp
          }
        } else {
          reject(createDetailedError(imageUrl))
        }
      }
      
      img.src = imageUrl
    }
    
    // 第一次尝试：不使用CORS
    tryLoad(false)
  })
}

// 创建详细的错误信息
const createDetailedError = (imageUrl: string): Error => {
  const domain = new URL(imageUrl).hostname
  
  // 检查是否是已知的支持跨域的服务
  const corsSupportedServices = [
    'picsum.photos',
    'via.placeholder.com', 
    'httpbin.org',
    'jsonplaceholder.typicode.com',
    'images.unsplash.com',
    'cdn.jsdelivr.net',
    'raw.githubusercontent.com'
  ]
  
  const isKnownCorsService = corsSupportedServices.some(service => domain.includes(service))
  
  if (isKnownCorsService) {
    return new Error(`图片服务 ${domain} 暂时不可用，请稍后重试或尝试其他图片链接。`)
  } else {
    return new Error(`图片加载失败！\n\n原因：${domain} 不支持跨域访问\n\n解决方案：\n1. 使用支持跨域的图片服务（如示例中的服务）\n2. 直接上传本地图片文件\n3. 将图片上传到支持跨域的图床服务\n\n推荐图床服务：\n- 七牛云、阿里云OSS\n- GitHub、Gitee\n- Imgur、Postimg`)
  }
}


// 文件上传前处理
const beforeUpload = async (file: File) => {
  // 检查文件类型
  if (!isValidImageType(file)) {
    message.error('不支持的文件格式')
    return false
  }

  // 检查文件大小 (限制为50MB)
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('文件大小不能超过50MB')
    return false
  }

  // 创建预览URL
  const url = URL.createObjectURL(file)
  currentImage.value = {
    name: file.name,
    url: url,
    size: file.size,
    type: file.type
  }

  // 如果是PAG文件，加载libpag
  if (isPagFile(file.name)) {
    try {
      await loadLibPag()
      if (pagPlayerLoaded.value && !pagPlayer.value) {
        // 等待DOM更新后初始化播放器
        setTimeout(() => {
          initPagPlayer()
        }, 100)
      }
    } catch (error) {
      console.error('libpag加载失败:', error)
    }
  }

  return false // 阻止自动上传
}

// 拖拽处理
const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = false
  
  const files = Array.from(e.dataTransfer?.files || [])
  if (files.length > 0) {
    const file = files[0]
    beforeUpload(file)
  }
}

const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = false
}

// 图片加载完成
const onImageLoad = (e: Event) => {
  const img = e.target as HTMLImageElement
  imageDimensions.value = {
    width: img.naturalWidth,
    height: img.naturalHeight
  }
}

// 检查是否是PAG文件
const isPagFile = (fileName: string): boolean => {
  return fileName.toLowerCase().endsWith('.pag')
}

// 加载libpag - 按照官方文档推荐的方式
const loadLibPag = async (): Promise<void> => {
  if (libpag.value) return // 已经加载过了
  
  try {
    // 检查是否已经加载了CDN脚本
    let script = document.querySelector('script[src*="libpag"]') as HTMLScriptElement
    if (!script) {
      script = document.createElement('script')
      // 使用官方推荐的CDN地址
      script.src = 'https://cdn.jsdelivr.net/npm/libpag@latest/lib/libpag.min.js'
      script.crossOrigin = 'anonymous'
      document.head.appendChild(script)
    }
    
    // 等待脚本加载
    await new Promise((resolve, reject) => {
      if (script.onload) {
        resolve(true)
      } else {
        script.onload = () => resolve(true)
        script.onerror = () => reject(new Error('CDN脚本加载失败'))
      }
    })
    
    // 等待一段时间让脚本完全执行
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 按照官方文档的方式：window.libpag.PAGInit()
    // @ts-ignore
    const globalLibpag = (window as any).libpag
    if (!globalLibpag) {
      throw new Error('全局libpag对象未找到')
    }
    
    // 按照官方文档初始化PAG
    if (globalLibpag.PAGInit) {
      // 官方文档：const PAG = await window.libpag.PAGInit();
      const PAG = await globalLibpag.PAGInit()
      
      // 使用初始化后的PAG模块，使用markRaw防止Vue响应式代理
      libpag.value = markRaw(PAG)
    } else {
      throw new Error('libpag对象没有PAGInit方法')
    }
    
    pagPlayerLoaded.value = true
    pagPlayerError.value = false
    
  } catch (error) {
    console.error('CDN版本libpag加载失败:', error)
    pagPlayerError.value = true
    throw new Error(`无法加载libpag，请检查网络连接。错误详情: ${error instanceof Error ? error.message : String(error)}`)
  }
}

// 初始化PAG播放器
const initPagPlayer = async (): Promise<void> => {
  if (!pagCanvas.value || !libpag.value || !currentImage.value) return
  
  // 如果播放器已经初始化，直接返回
  if (pagPlayer.value) {
    return
  }
  
  // 如果播放器已经初始化，先清理再重新初始化
  if (pagPlayer.value) {
    try {
      if (typeof pagPlayer.value.stop === 'function') {
        pagPlayer.value.stop()
      } else if (typeof pagPlayer.value.pause === 'function') {
        pagPlayer.value.pause()
      }
    } catch (error) {
      // 忽略清理错误
    }
    pagPlayer.value = null
    pagPlayerLoaded.value = false
    isPlaying.value = false
  }
  
  try {
    // 获取PAG文件数据
    let pagData: ArrayBuffer
    
    if (currentImage.value.url.startsWith('blob:')) {
      // 本地文件
      const response = await fetch(currentImage.value.url)
      if (!response.ok) {
        throw new Error(`文件加载失败: ${response.status}`)
      }
      pagData = await response.arrayBuffer()
    } else {
      // URL文件
      const response = await fetch(currentImage.value.url, {
        mode: 'cors',
        headers: {
          'Referer': ''
        }
      })
      if (!response.ok) {
        throw new Error(`文件加载失败: ${response.status}`)
      }
      pagData = await response.arrayBuffer()
    }
    
    // 按照官方文档使用PAGFile.load创建PAG文件
    if (!libpag.value.PAGFile || typeof libpag.value.PAGFile.load !== 'function') {
      throw new Error('未找到PAGFile.load方法')
    }
    
    // 使用Promise包装以避免Proxy错误
    const loadPromise = new Promise((resolve, reject) => {
      try {
        const result = libpag.value.PAGFile.load(pagData)
        // 如果返回Promise，等待它完成
        if (result && typeof result.then === 'function') {
          result.then(resolve).catch(reject)
        } else {
          resolve(result)
        }
      } catch (error) {
        reject(error)
      }
    })
    
    // 官方文档：const pagFile = await PAG.PAGFile.load(buffer);
    const pagFile = await loadPromise
    // 使用markRaw防止Vue响应式代理
    const rawPagFile = markRaw(pagFile as any)
    
    if (!pagFile) {
      throw new Error('PAG文件解析失败')
    }
    
    // 设置画布大小
    const width = (rawPagFile as any).width()
    const height = (rawPagFile as any).height()
    
    // 确保Canvas正确初始化
    if (!pagCanvas.value) {
      throw new Error('Canvas元素未找到')
    }
    
    // 设置Canvas尺寸
    pagCanvas.value.width = width
    pagCanvas.value.height = height
    
    // 获取WebGL上下文，PAG播放器需要WebGL，并配置透明背景
    const glOptions = {
      alpha: true,        // 启用透明通道
      premultipliedAlpha: false,  // 禁用预乘alpha
      preserveDrawingBuffer: true, // 保留绘制缓冲区
      antialias: true     // 启用抗锯齿
    }
    
    const gl = pagCanvas.value.getContext('webgl', glOptions) || 
               pagCanvas.value.getContext('webgl2', glOptions) || 
               pagCanvas.value.getContext('experimental-webgl', glOptions)
    
    if (!gl) {
      throw new Error('无法获取Canvas WebGL上下文，浏览器可能不支持WebGL')
    }
    
    // 设置WebGL上下文支持透明背景
    const webgl = gl as WebGLRenderingContext
    webgl.enable(webgl.BLEND)
    webgl.blendFunc(webgl.SRC_ALPHA, webgl.ONE_MINUS_SRC_ALPHA)
    
    imageDimensions.value = { width, height }
    
    // 按照官方文档创建PAGView对象
    if (!libpag.value.PAGView || typeof libpag.value.PAGView.init !== 'function') {
      throw new Error('未找到PAGView.init方法')
    }
    
    // 使用Promise包装以避免Proxy错误，并配置透明背景
    const initPromise = new Promise((resolve, reject) => {
      try {
        // 尝试使用配置选项初始化，支持透明背景
        let result
        if (typeof libpag.value.PAGView.init === 'function') {
          // 检查是否支持配置选项
          try {
            // 尝试使用配置选项初始化
            const initOptions = {
              useCanvas2D: false,  // 使用WebGL以获得更好的透明支持
              useScale: false,     // 禁用缩放
              firstFrame: false,   // 不立即渲染第一帧
              backgroundColor: 'transparent' // 设置透明背景
            }
            result = libpag.value.PAGView.init(rawPagFile, pagCanvas.value, initOptions)
          } catch (configError) {
            // 降级到默认初始化
            result = libpag.value.PAGView.init(rawPagFile, pagCanvas.value)
          }
        } else {
          throw new Error('PAGView.init方法不可用')
        }
        
        // 如果返回Promise，等待它完成
        if (result && typeof result.then === 'function') {
          result.then(resolve).catch(reject)
        } else {
          resolve(result)
        }
      } catch (error) {
        reject(error)
      }
    })
    
    // 官方文档：const pagView = await PAG.PAGView.init(pagFile, canvas);
    const pagView = await initPromise
    // 使用markRaw防止Vue响应式代理
    pagPlayer.value = markRaw(pagView as any)

      if (!pagPlayer.value) {
        throw new Error('PAG播放器创建失败')
      }

    // PAG播放器初始化完成，自动开始播放
    setTimeout(() => {
      playPagAnimation()
    }, 100)
    
  } catch (error) {
    console.error('PAG播放器初始化失败:', error)
    const errorMsg = error instanceof Error ? error.message : '未知错误'
    
    // 根据错误类型提供不同的处理
    if (errorMsg.includes('Make context current fail') || errorMsg.includes('WebGL')) {
      message.error('PAG播放器WebGL上下文初始化失败')
      message.info({
        content: 'WebGL上下文问题。解决方案：1. 刷新页面重试 2. 使用Chrome浏览器 3. 检查浏览器是否支持WebGL 4. 尝试禁用浏览器扩展',
        duration: 10
      })
    } else if (errorMsg.includes('fetch') || errorMsg.includes('网络')) {
      message.error(`PAG文件加载失败: ${errorMsg}`)
      message.info('如果文件来自外部链接，可能是跨域限制导致的。建议下载文件后重新上传。')
    } else {
      message.error(`PAG文件加载失败: ${errorMsg}`)
    }
  }
}

// 播放PAG动画 - 添加兼容性处理和状态管理
const playPagAnimation = async (): Promise<void> => {
  if (!pagPlayer.value) {
    return
  }
  
  try {
    // 检查播放器是否有play方法
    if (typeof pagPlayer.value.play !== 'function') {
      return
    }
    
    // 如果正在播放，先停止再重新播放
    if (isPlaying.value) {
      try {
        if (typeof pagPlayer.value.stop === 'function') {
          pagPlayer.value.stop()
        }
        isPlaying.value = false
        // 等待一小段时间确保停止完成
        await new Promise(resolve => setTimeout(resolve, 100))
      } catch (stopError) {
        // 忽略停止错误
      }
    }
    
    // 尝试播放，使用Promise包装以避免Proxy错误
    const playPromise = new Promise((resolve, reject) => {
      try {
        const result = pagPlayer.value.play()
        // 如果返回Promise，等待它完成
        if (result && typeof result.then === 'function') {
          result.then(resolve).catch(reject)
        } else {
          resolve(result)
        }
      } catch (error) {
        reject(error)
      }
    })
    
    await playPromise
    isPlaying.value = true
    
    // 设置播放完成监听，自动循环播放
    try {
      if (typeof pagPlayer.value.onAnimationEnd === 'function') {
        pagPlayer.value.onAnimationEnd(() => {
          isPlaying.value = false
          // 自动重新播放
          setTimeout(() => {
            playPagAnimation()
          }, 100)
        })
      } else if (typeof pagPlayer.value.addEventListener === 'function') {
        pagPlayer.value.addEventListener('animationEnd', () => {
          isPlaying.value = false
          // 自动重新播放
          setTimeout(() => {
            playPagAnimation()
          }, 100)
        })
      } else {
        // 如果没有事件监听，使用定时器检查播放状态
        setTimeout(() => {
          checkPlaybackStatus()
        }, 1000)
      }
    } catch (eventError) {
      // 使用定时器作为备用方案
      setTimeout(() => {
        checkPlaybackStatus()
      }, 1000)
    }
    
  } catch (error) {
    isPlaying.value = false // 确保状态重置
  }
}

// 检查播放状态
const checkPlaybackStatus = () => {
  if (!pagPlayer.value || !isPlaying.value) return
  
  try {
    // 尝试检查播放状态
    if (typeof pagPlayer.value.isPlaying === 'function') {
      const playing = pagPlayer.value.isPlaying()
      if (!playing) {
        isPlaying.value = false
        // 自动重新播放
        setTimeout(() => {
          playPagAnimation()
        }, 100)
      } else {
        // 继续检查
        setTimeout(() => {
          checkPlaybackStatus()
        }, 500)
      }
    } else if (typeof pagPlayer.value.getProgress === 'function') {
      const progress = pagPlayer.value.getProgress()
      if (progress >= 1.0) {
        isPlaying.value = false
        // 自动重新播放
        setTimeout(() => {
          playPagAnimation()
        }, 100)
      } else {
        // 继续检查
        setTimeout(() => {
          checkPlaybackStatus()
        }, 500)
      }
    } else {
      // 如果没有状态检查方法，使用固定时间后重置状态并重新播放
      setTimeout(() => {
        isPlaying.value = false
        // 自动重新播放
        setTimeout(() => {
          playPagAnimation()
        }, 100)
      }, 5000) // 5秒后重置状态并重新播放
    }
  } catch (error) {
    // 出错时重置状态
    isPlaying.value = false
  }
}

// 暂停PAG动画
const pausePagAnimation = (): void => {
  if (!pagPlayer.value) {
    return
  }
  
  try {
    // 检查是否有pause方法
    if (typeof pagPlayer.value.pause === 'function') {
      pagPlayer.value.pause()
      isPlaying.value = false
    }
  } catch (error) {
    isPlaying.value = false // 即使失败也更新状态
  }
}

// 停止PAG动画
const stopPagAnimation = (): void => {
  if (!pagPlayer.value) {
    return
  }
  
  try {
    // 检查是否有stop方法
    if (typeof pagPlayer.value.stop === 'function') {
      pagPlayer.value.stop()
      isPlaying.value = false
    } else {
      // 如果没有stop方法，尝试pause
      if (typeof pagPlayer.value.pause === 'function') {
        pagPlayer.value.pause()
        isPlaying.value = false
      }
    }
  } catch (error) {
    isPlaying.value = false // 即使失败也更新状态
  }
}

// 重试加载PAG播放器
const retryLoadPagPlayer = async () => {
  pagPlayerError.value = false
  pagPlayerLoaded.value = false
  isPlaying.value = false
  await loadLibPag()
  if (pagPlayerLoaded.value) {
    await initPagPlayer()
  }
}

// 图片加载错误
const onImageError = () => {
  errorMessage.value = '图片加载失败，请检查图片格式或链接'
  message.error('图片加载失败')
}

// 清除图片
const clearImage = () => {
  if (currentImage.value?.url.startsWith('blob:')) {
    URL.revokeObjectURL(currentImage.value.url)
  }
  currentImage.value = null
  imageUrl.value = ''
  fileList.value = []
  errorMessage.value = ''
  imageDimensions.value = { width: 0, height: 0 }
  message.success('已清除图片')
}

// 工具函数
const isValidImageType = (file: File): boolean => {
  const validTypes = [
    'image/jpeg',
    'image/jpg', 
    'image/png',
    'image/gif',
    'image/webp',
    'image/bmp',
    'image/svg+xml',
    'image/avif',
    'image/heic',
    'image/heif'
  ]
  
  const validExtensions = ['.pag']
  
  return validTypes.includes(file.type) || 
         validExtensions.some(ext => file.name.toLowerCase().endsWith(ext))
}

const getImageTypeFromUrl = (url: string): string => {
  const extension = url.split('.').pop()?.toLowerCase()
  const typeMap: Record<string, string> = {
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'png': 'image/png',
    'gif': 'image/gif',
    'webp': 'image/webp',
    'bmp': 'image/bmp',
    'svg': 'image/svg+xml',
    'avif': 'image/avif',
    'heic': 'image/heic',
    'heif': 'image/heif',
    'pag': 'image/pag'
  }
  return typeMap[extension || ''] || '未知格式'
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 加载示例图片
const loadExampleImage = (url: string) => {
  imageUrl.value = url
  loadImageFromUrl()
}

// 获取错误描述
const getErrorDescription = (): string => {
  if (!errorMessage.value) return ''
  
  if (errorMessage.value.includes('跨域')) {
    return '这是跨域访问限制导致的。建议使用支持跨域的图片服务，或直接上传本地图片文件。'
  }
  
  if (errorMessage.value.includes('链接无效')) {
    return '请检查图片链接是否正确，确保链接可以正常访问。'
  }
  
  if (errorMessage.value.includes('网络连接')) {
    return '请检查网络连接是否正常，或稍后重试。'
  }
  
  return '如果问题持续存在，建议直接上传本地图片文件。'
}

// 下载图片
const downloadImage = async () => {
  if (!imageUrl.value) return
  
  try {
    // 尝试通过fetch获取图片数据
    const response = await fetch(imageUrl.value, {
      mode: 'cors',
      headers: {
        'Referer': '', // 清除referer避免防盗链
      }
    })
    
    if (response.ok) {
      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      
      // 创建下载链接
      const link = document.createElement('a')
      link.href = url
      link.download = `image_${Date.now()}.jpg`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 清理URL对象
      URL.revokeObjectURL(url)
      message.success('图片下载已开始，请检查浏览器下载文件夹')
    } else {
      throw new Error('服务器拒绝访问')
    }
  } catch (error) {
    // 如果fetch失败，尝试直接打开链接
    try {
      window.open(imageUrl.value, '_blank')
      message.info('已在新标签页中打开图片，请右键保存图片')
    } catch (openError) {
      message.error('下载失败，请手动复制链接到浏览器中打开并保存图片')
    }
  }
}

// 复制图片链接
const copyImageUrl = async () => {
  if (!imageUrl.value) return
  
  try {
    await navigator.clipboard.writeText(imageUrl.value)
    message.success('图片链接已复制到剪贴板，您可以粘贴到浏览器中打开')
  } catch (error) {
    // 降级方案：使用传统的复制方法
    const textArea = document.createElement('textarea')
    textArea.value = imageUrl.value
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      message.success('图片链接已复制到剪贴板')
    } catch (fallbackError) {
      message.error('复制失败，请手动复制链接')
    }
    document.body.removeChild(textArea)
  }
}

// 下载PAG文件
const downloadPagFile = () => {
  if (!currentImage.value) return
  
  try {
    // 如果是本地文件
    if (currentImage.value.url.startsWith('blob:')) {
      const link = document.createElement('a')
      link.href = currentImage.value.url
      link.download = currentImage.value.name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      message.success('PAG文件下载已开始')
    } else {
      // 如果是URL，尝试下载
      downloadImage()
    }
  } catch (error) {
    message.error('下载失败，请手动保存文件')
  }
}

// 打开PAG信息
const openPagInfo = () => {
  const info = `
PAG (Portable Animated Graphics) 是腾讯开发的动画格式

特点：
• 文件体积小，压缩效率高
• 支持复杂的矢量动画
• 跨平台兼容性好
• 支持实时渲染

使用方式：
• 下载PAG文件后，可以使用腾讯PAG SDK播放
• 支持iOS、Android、Web、Windows、macOS等平台
• 可以集成到各种应用中

更多信息请访问：https://pag.io/
  `
  
  message.info({
    content: info,
    duration: 10,
    style: {
      whiteSpace: 'pre-line',
      maxWidth: '400px'
    }
  })
}


// 切换到上传模式
const switchToUpload = () => {
  inputMethod.value = 'upload'
  errorMessage.value = ''
  message.info('已切换到文件上传模式，请选择本地图片文件')
}
</script>

<style scoped>
.image-processor {
  min-height: 100%;
  background: #f0f2f5;
  padding: 24px;
}

.input-methods {
  margin-bottom: 24px;
}

.url-input-section {
  margin-bottom: 24px;
}

.upload-section {
  margin-bottom: 24px;
}

.upload-content {
  padding: 32px 16px;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-content.drag-over {
  background: #e6f7ff;
}

.upload-icon {
  font-size: 40px;
  color: #1890ff;
  margin-bottom: 12px;
}

.upload-text {
  color: #666;
}

.upload-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin: 0 0 6px 0;
}

.upload-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 11px;
  color: #ff7875;
  margin: 0;
  font-weight: 500;
  padding: 6px 8px;
  background: rgba(255, 120, 117, 0.1);
  border-radius: 4px;
  border: 1px solid rgba(255, 120, 117, 0.2);
}

.upload-note .anticon {
  font-size: 12px;
}

.preview-section {
  margin-top: 24px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.file-info-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #e8e8e8;
  gap: 16px;
}

.file-info-content {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.file-name-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  word-break: break-all;
  line-height: 1.4;
  flex: 1;
  min-width: 0;
}

.file-size {
  font-size: 12px;
  color: #666;
  background: #e8e8e8;
  padding: 2px 8px;
  border-radius: 12px;
  white-space: nowrap;
}

.file-details {
  display: flex;
  align-items: center;
  gap: 16px;
}

.file-format {
  font-size: 12px;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.file-dimensions {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 12px;
}

.file-actions {
  flex-shrink: 0;
  align-self: flex-start;
}

.image-preview {
  padding: 16px;
  text-align: center;
  background: #fafafa;
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.02);
}

.pag-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  text-align: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.pag-icon {
  font-size: 48px;
  color: #6c757d;
  margin-bottom: 16px;
  opacity: 0.8;
}

.pag-info h3 {
  color: #495057;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 600;
}

.pag-info p {
  color: #6c757d;
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.5;
  max-width: 280px;
}

.pag-warning {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #856404;
  font-size: 13px;
  font-weight: 500;
}

.pag-warning .anticon {
  color: #f39c12;
  font-size: 16px;
}

.pag-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.pag-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.pag-loading .pag-icon {
  animation: spin 1s linear infinite;
}

.pag-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
  border-radius: 12px;
  border: 2px dashed #fc8181;
}

.pag-player-container {
  width: 100%;
}

.pag-player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.pag-player-header h3 {
  margin: 0;
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

.pag-controls {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pag-canvas-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 150px;
  background: 
    linear-gradient(45deg, #f0f0f0 25%, transparent 25%), 
    linear-gradient(-45deg, #f0f0f0 25%, transparent 25%), 
    linear-gradient(45deg, transparent 75%, #f0f0f0 75%), 
    linear-gradient(-45deg, transparent 75%, #f0f0f0 75%);
  background-size: 16px 16px;
  background-position: 0 0, 0 8px, 8px -8px, -8px 0px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  margin-bottom: 12px;
}

.pag-canvas {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: block;
  background: transparent;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}


.error-section {
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-content {
    padding: 24px 12px;
  }
  
  .upload-icon {
    font-size: 32px;
  }
  
  .upload-title {
    font-size: 13px;
  }
  
  .upload-hint {
    font-size: 11px;
  }
  
  .pag-player-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .pag-controls {
    width: 100%;
    justify-content: flex-start;
  }
  
  .file-info-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 16px;
  }
  
  .file-actions {
    margin-left: 0;
    align-self: flex-end;
  }
  
  .file-name {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    width: 100%;
  }
  
  .file-name-text {
    width: 100%;
    word-break: break-all;
  }
  
  .file-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}

@media (max-width: 480px) {
  .upload-content {
    padding: 20px 8px;
  }
  
  .upload-icon {
    font-size: 28px;
  }
  
  .upload-title {
    font-size: 12px;
  }
  
  .upload-hint {
    font-size: 10px;
  }
  
  .pag-canvas-container {
    min-height: 120px;
  }
  
  .pag-canvas {
    max-height: 200px;
  }
}
</style>



