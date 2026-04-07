import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

http.interceptors.request.use((config) => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    // blob / arraybuffer 时 body 为二进制，没有 .data，不能走统一 R 包装解析
    if (res.config.responseType === 'blob' || res.config.responseType === 'arraybuffer') {
      return body
    }
    if (body && typeof body.code === 'number' && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body?.data
  },
  (err) => {
    const status = err.response?.status
    const data = err.response?.data
    if (status === 401) {
      const store = useUserStore()
      store.clear()
      router.push({ name: 'Login' })
    }
    const msg = data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default http
