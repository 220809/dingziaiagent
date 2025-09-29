import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production'
    ? '/api' // 生产环境使用相对路径，适用于前后端部署在同一域名下
    : 'http://localhost:8123/api' // 开发环境指向本地后端服务


// 全局连接管理
let activeConnections = new Map()

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// SSE连接函数
export const createSSEConnection = (url, onMessage, onError, onOpen) => {
  // 检查是否已有相同URL的活跃连接
  if (activeConnections.has(url)) {
    console.log(`关闭已存在的连接: ${url}`)
    const existingConnection = activeConnections.get(url)
    existingConnection.close()
    activeConnections.delete(url)
  }
  
  const eventSource = new EventSource(url)
  let isConnected = false
  let hasReceivedData = false
  let isManuallyClosed = false
  let lastDataTime = Date.now()
  let connectionId = Date.now() // 添加连接ID用于调试
  let errorCount = 0 // 添加错误计数
  
  console.log(`创建SSE连接 #${connectionId}: ${url}`)
  
  // 将连接添加到活跃连接映射
  activeConnections.set(url, eventSource)
  
  eventSource.onmessage = (event) => {
    hasReceivedData = true
    lastDataTime = Date.now()
    errorCount = 0 // 重置错误计数
    console.log(`SSE连接 #${connectionId} 收到消息:`, event.data.substring(0, 50) + '...')
    onMessage(event.data)
  }
  
  eventSource.onerror = (error) => {
    errorCount++
    console.log(`SSE连接 #${connectionId} 状态变化:`, eventSource.readyState, '手动关闭:', isManuallyClosed, '已接收数据:', hasReceivedData, '错误次数:', errorCount)
    
    // 如果手动关闭，不触发错误
    if (isManuallyClosed) {
      console.log(`SSE连接 #${connectionId} 手动关闭，不触发错误处理`)
      return
    }
    
    // 如果错误次数过多，强制关闭连接
    if (errorCount > 3) {
      console.log(`SSE连接 #${connectionId} 错误次数过多，强制关闭`)
      eventSource.close()
      onError(error)
      return
    }
    
    // 只有在连接失败时才触发错误处理
    if (eventSource.readyState === EventSource.CLOSED) {
      console.log(`SSE连接 #${connectionId} 已关闭`)
      // 如果已经接收到数据，说明是正常结束，不需要显示错误
      if (hasReceivedData) {
        console.log(`SSE连接 #${connectionId} 正常结束，不显示错误`)
        return
      }
      // 只有在真正连接失败时才显示错误
      console.log(`SSE连接 #${connectionId} 异常关闭，触发错误处理`)
      onError(error)
    } else if (eventSource.readyState === EventSource.CONNECTING) {
      console.log(`SSE连接 #${connectionId} 重连中...`)
      // 如果正在重连且已经接收过数据，说明可能是正常结束
      if (hasReceivedData) {
        console.log(`SSE连接 #${connectionId} 已接收数据，停止重连`)
        eventSource.close()
        return
      }
    }
  }
  
  eventSource.onopen = () => {
    console.log(`SSE连接 #${connectionId} 已建立`)
    isConnected = true
    errorCount = 0 // 重置错误计数
    onOpen && onOpen()
  }
  
  // 添加自定义关闭方法
  const originalClose = eventSource.close.bind(eventSource)
  eventSource.close = () => {
    console.log(`手动关闭SSE连接 #${connectionId}`)
    isManuallyClosed = true
    isConnected = false
    // 从活跃连接中移除
    activeConnections.delete(url)
    originalClose()
  }
  
  // 添加获取最后数据时间的方法
  eventSource.getLastDataTime = () => lastDataTime
  eventSource.connectionId = connectionId
  eventSource.errorCount = () => errorCount
  
  return eventSource
}

// 情感顾问SSE接口
export const getEmoConsultantSSE = (message, conversationId) => {
  const url = `${API_BASE_URL}/ai/emo/consultant?message=${encodeURIComponent(message)}&conversationId=${conversationId}`
  return url
}

// JManus聊天SSE接口
export const getJManusChatSSE = (message) => {
  const url = `${API_BASE_URL}/ai/jmanus/chat?message=${encodeURIComponent(message)}`
  return url
}

// 清理所有活跃连接
export const closeAllConnections = () => {
  console.log(`清理所有活跃连接，共 ${activeConnections.size} 个`)
  for (const [url, connection] of activeConnections) {
    console.log(`关闭连接: ${url}`)
    connection.close()
  }
  activeConnections.clear()
}

// 获取活跃连接数量
export const getActiveConnectionsCount = () => {
  return activeConnections.size
}

export default api

