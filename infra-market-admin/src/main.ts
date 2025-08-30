import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import './styles/index.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)

// 初始化认证状态 - 移除这里的调用，改为在路由守卫中处理
// const { useAuthStore } = await import('@/stores/auth')
// const authStore = useAuthStore()
// await authStore.initializeAuth()

app.mount('#app')
