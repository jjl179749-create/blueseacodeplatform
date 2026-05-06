import axios from 'axios'

const request = axios.create({
  baseURL: '',
  timeout: 15000,
})

// 请求拦截器 - 自动带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器 - 统一处理 401 自动刷新 Token
request.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      const refreshToken = localStorage.getItem('refreshToken')

      if (refreshToken) {
        try {
          const res = await axios.post(
            '/api/portal/auth/refresh',
            null,
            { headers: { 'Refresh-Token': refreshToken } },
          )
          const data = res.data
          if (data.code === 200) {
            const newToken = data.data.accessToken
            localStorage.setItem('accessToken', newToken)
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return request(originalRequest)
          }
        } catch {
          // 刷新失败，清空登录态
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
        }
      } else {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default request
