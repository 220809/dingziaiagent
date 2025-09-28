<template>
  <div class="chat-container">
    <div class="header">
      <h1>🤖 AI智能体</h1>
      <button class="btn btn-secondary" @click="goHome">返回首页</button>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="message in messages" :key="message.id" :class="['message', message.type]">
        <div class="message-content">
          <div v-if="message.type === 'assistant' && message.isTyping" class="typing-indicator">
            <span>AI正在思考</span>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
          </div>
          <div v-else v-html="formatMessage(message.content)"></div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input
        v-model="inputMessage"
        @keyup.enter="sendMessage"
        placeholder="请输入您的问题..."
        :disabled="isLoading"
      />
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
        <span v-if="isLoading" class="loading"></span>
        <span v-else>发送</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { createSSEConnection, getJManusChatSSE, closeAllConnections } from '../services/api'

const router = useRouter()
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
let eventSource = null
const isConnecting = ref(false) // 添加连接状态标志
let closeTimeoutId = null // 添加关闭定时器ID

// 格式化消息内容
const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value || isConnecting.value) return
  
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    type: 'user',
    content: userMessage
  })
  
  scrollToBottom()
  
  // 添加AI思考状态
  const thinkingMessage = {
    id: Date.now() + 1,
    type: 'assistant',
    content: '',
    isTyping: true
  }
  messages.value.push(thinkingMessage)
  
  isLoading.value = true
  isConnecting.value = true
  
  try {
    // 关闭之前的连接（只有在发送新消息时才关闭）
    if (eventSource) {
      console.log('发送新消息，关闭之前的连接')
      // 确保连接被正确关闭
      if (eventSource.timeoutId) {
        clearTimeout(eventSource.timeoutId)
      }
      eventSource.close()
      eventSource = null
      // 等待一小段时间确保连接完全关闭
      await new Promise(resolve => setTimeout(resolve, 200))
    }
    
    // 创建SSE连接
    const url = getJManusChatSSE(userMessage)
    eventSource = createSSEConnection(
      url,
      (data) => {
        // 移除思考状态
        const thinkingIndex = messages.value.findIndex(msg => msg.isTyping)
        if (thinkingIndex !== -1) {
          messages.value.splice(thinkingIndex, 1)
        }
        
        // 检查是否是结束标记
        if (data === '[DONE]' || data === 'data: [DONE]' || data.trim() === '') {
          console.log('SSE流正常结束')
          handleConnectionClose()
          return
        }
        
        // 检查是否包含结束标记
        if (data.includes('[DONE]') || data.includes('data: [DONE]')) {
          console.log('SSE流包含结束标记')
          // 移除结束标记后显示内容
          const cleanData = data.replace(/\[DONE\]|data: \[DONE\]/g, '').trim()
          if (cleanData) {
            messages.value.push({
              id: Date.now(),
              type: 'assistant',
              content: cleanData
            })
          }
          handleConnectionClose()
          return
        }
        
        // 智能分割AI智能体的输出内容
        const shouldCreateNewMessage = (data) => {
          // 1. 新步骤开始
          if (data.match(/^步骤\d+[:：]/)) return true
          
          // 2. 工具调用相关
          if (data.includes('工具:') || data.includes('调用成功') || data.includes('调用结果')) return true
          
          // 3. 任务完成相关
          if (data.includes('执行完成') || data.includes('任务完成') || data.includes('总结')) return true
          
          // 4. 检查是否是新的段落（以句号、问号、感叹号结尾且长度较长）
          if (data.match(/[。！？.!?]$/) && data.length > 30) return true
          
          // 5. 检查是否是JSON数据（工具调用结果通常是JSON格式）
          if (data.trim().startsWith('{') || data.trim().startsWith('[')) return true
          
          return false
        }
        
        if (shouldCreateNewMessage(data)) {
          // 创建新的消息气泡
          messages.value.push({
            id: Date.now(),
            type: 'assistant',
            content: data
          })
        } else {
          // 添加到最后一个消息或创建新消息
          const lastMessage = messages.value[messages.value.length - 1]
          
          if (lastMessage && lastMessage.type === 'assistant' && !lastMessage.isTyping) {
            lastMessage.content += data
          } else {
            messages.value.push({
              id: Date.now(),
              type: 'assistant',
              content: data
            })
          }
        }
        
        scrollToBottom()
        
        // 检查是否是明确的结束标记
        if (data === '[DONE]' || data === 'data: [DONE]' || data.trim() === '') {
          console.log('收到明确的结束标记，关闭连接')
          handleConnectionClose()
          return
        }
        
        // 检查是否包含结束标记
        if (data.includes('[DONE]') || data.includes('data: [DONE]')) {
          console.log('数据包含结束标记，关闭连接')
          // 移除结束标记后显示内容
          const cleanData = data.replace(/\[DONE\]|data: \[DONE\]/g, '').trim()
          if (cleanData) {
            messages.value.push({
              id: Date.now(),
              type: 'assistant',
              content: cleanData
            })
          }
          handleConnectionClose()
          return
        }
        
        // 检查是否是任务完成（但给后端更多时间发送完整结果）
        if (data.includes('任务完成') || data.includes('执行完成') || data.includes('terminate')) {
          console.log('检测到任务完成，延迟关闭连接')
          isTaskCompleted = true // 标记任务已完成
          // 清除之前的定时器
          if (closeTimeoutId) {
            clearTimeout(closeTimeoutId)
          }
          // 重置数据监控时间，避免误判
          if (eventSource && eventSource.dataMonitorId) {
            lastDataTime = Date.now()
          }
          // 延迟5秒关闭，确保后端完成所有操作
          closeTimeoutId = setTimeout(() => {
            if (isLoading.value) {
              console.log('任务完成，关闭连接')
              handleConnectionClose()
            }
          }, 5000)
        }
        
        // 检查是否是智能体对话的结束（基于内容特征）
        if (data.includes('希望我的回答对您有帮助') || 
            data.includes('如果您还有其他问题') || 
            data.includes('还有什么我可以帮助您的吗') ||
            data.includes('任务已全部完成')) {
          console.log('检测到智能体对话结束，延迟关闭连接')
          isTaskCompleted = true // 标记任务已完成
          // 清除之前的定时器
          if (closeTimeoutId) {
            clearTimeout(closeTimeoutId)
          }
          // 延迟5秒关闭，确保后端完成所有操作
          closeTimeoutId = setTimeout(() => {
            if (isLoading.value) {
              console.log('智能体对话结束，关闭连接')
              handleConnectionClose()
            }
          }, 5000)
        }
      },
      (error) => {
        console.error('SSE错误:', error)
        handleConnectionError()
      },
      () => {
        console.log('SSE连接已建立')
        isConnecting.value = false
      }
    )
    
    // 监听SSE连接关闭事件
    eventSource.addEventListener('close', () => {
      console.log('SSE连接已关闭')
      handleConnectionClose()
    })
    
    // 设置超时
    const timeoutId = setTimeout(() => {
      if (isLoading.value) {
        console.log('SSE连接超时')
        handleConnectionError()
      }
    }, 30000) // 30秒超时
    
    // 保存超时ID以便清理
    eventSource.timeoutId = timeoutId
    
    // 添加数据接收监控，如果长时间没有数据接收，检查是否需要关闭
    let lastDataTime = Date.now()
    let isTaskCompleted = false // 添加任务完成标志
    const dataMonitorId = setInterval(() => {
      if (isLoading.value && eventSource && !isTaskCompleted) {
        const timeSinceLastData = Date.now() - lastDataTime
        // 如果超过15秒没有收到数据，且连接仍然活跃，可能是任务完成
        if (timeSinceLastData > 15000) {
          console.log('长时间未收到数据，可能任务已完成')
          clearInterval(dataMonitorId)
          handleConnectionClose()
        }
      }
    }, 3000) // 每3秒检查一次
    
    // 保存监控ID
    eventSource.dataMonitorId = dataMonitorId
    
    // 监听数据接收，更新最后接收时间
    const originalOnMessage = eventSource.onmessage
    eventSource.onmessage = (event) => {
      lastDataTime = Date.now()
      originalOnMessage.call(eventSource, event)
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    handleConnectionError()
  }
}

// 处理连接错误
const handleConnectionError = () => {
  isLoading.value = false
  isConnecting.value = false
  
  // 关闭连接
  if (eventSource) {
    if (eventSource.timeoutId) {
      clearTimeout(eventSource.timeoutId)
    }
    if (eventSource.dataMonitorId) {
      clearInterval(eventSource.dataMonitorId)
    }
    eventSource.close()
    eventSource = null
  }
  
  // 移除思考状态
  const thinkingIndex = messages.value.findIndex(msg => msg.isTyping)
  if (thinkingIndex !== -1) {
    messages.value.splice(thinkingIndex, 1)
  }
  
  // 添加错误消息
  messages.value.push({
    id: Date.now(),
    type: 'assistant',
    content: '抱歉，连接出现问题，请稍后重试。'
  })
}

// 处理连接关闭
const handleConnectionClose = () => {
  isLoading.value = false
  isConnecting.value = false
  
  // 清除关闭定时器
  if (closeTimeoutId) {
    clearTimeout(closeTimeoutId)
    closeTimeoutId = null
  }
  
  // 关闭连接
  if (eventSource) {
    if (eventSource.timeoutId) {
      clearTimeout(eventSource.timeoutId)
    }
    if (eventSource.dataMonitorId) {
      clearInterval(eventSource.dataMonitorId)
    }
    eventSource.close()
    eventSource = null
  }
  
  // 移除思考状态
  const thinkingIndex = messages.value.findIndex(msg => msg.isTyping)
  if (thinkingIndex !== -1) {
    messages.value.splice(thinkingIndex, 1)
  }
}

// 返回首页
const goHome = () => {
  if (eventSource) {
    eventSource.close()
  }
  router.push('/')
}

// 组件挂载时初始化
onMounted(() => {
  // 添加欢迎消息
  messages.value.push({
    id: Date.now(),
    type: 'assistant',
    content: '您好！我是AI智能体，我可以回答各种问题，协助您完成各种任务。请告诉我您需要什么帮助？'
  })
})

// 组件卸载时清理
onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
  // 清理所有活跃连接
  closeAllConnections()
})
</script>

