<template>
  <div class="chat-container">
    <div class="header">
      <h1>💝 AI情感顾问</h1>
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
        placeholder="请输入您的情感问题..."
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
import { createSSEConnection, getEmoConsultantSSE, closeAllConnections } from '../services/api'

const router = useRouter()
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const conversationId = ref('')
const messagesContainer = ref(null)
let eventSource = null
const isConnecting = ref(false) // 添加连接状态标志
let closeTimeoutId = null // 添加关闭定时器ID

// 生成会话ID
const generateConversationId = () => {
  return 'emo_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

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
    // 关闭之前的连接
    if (eventSource) {
      console.log('发送新消息，关闭之前的连接')
      if (eventSource.timeoutId) {
        clearTimeout(eventSource.timeoutId)
      }
      eventSource.close()
      eventSource = null
      // 等待一小段时间确保连接完全关闭
      await new Promise(resolve => setTimeout(resolve, 200))
    }
    
    // 创建SSE连接
    const url = getEmoConsultantSSE(userMessage, conversationId.value)
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
            const lastMessage = messages.value[messages.value.length - 1]
            if (lastMessage && lastMessage.type === 'assistant' && !lastMessage.isTyping) {
              lastMessage.content += cleanData
            }
          }
          handleConnectionClose()
          return
        }
        
        // 检查是否是情感咨询的结束（基于内容特征）
        if (data.includes('希望我的回答对您有帮助') || 
            data.includes('如果您还有其他问题') || 
            data.includes('祝您心情愉快') ||
            data.includes('感谢您的信任')) {
          console.log('检测到情感咨询结束，延迟关闭连接')
          isConversationCompleted = true // 标记对话已完成
          // 清除之前的定时器
          if (closeTimeoutId) {
            clearTimeout(closeTimeoutId)
          }
          // 延迟3秒关闭，确保后端完成所有操作
          closeTimeoutId = setTimeout(() => {
            if (isLoading.value) {
              console.log('情感咨询结束，关闭连接')
              handleConnectionClose()
            }
          }, 3000)
        }
        
        // 处理正常数据
        const lastMessage = messages.value[messages.value.length - 1]
        
        if (lastMessage && lastMessage.type === 'assistant' && !lastMessage.isTyping) {
          // 如果当前数据以句号、问号、感叹号结尾且长度较长，创建新消息
          if (data.match(/[。！？.!?]$/) && data.length > 30) {
            messages.value.push({
              id: Date.now(),
              type: 'assistant',
              content: data
            })
          } else {
            // 添加到最后一个消息
            lastMessage.content += data
          }
        } else {
          // 创建新消息
          messages.value.push({
            id: Date.now(),
            type: 'assistant',
            content: data
          })
        }
        
        scrollToBottom()
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
    }, 30000)
    
    // 保存超时ID以便清理
    eventSource.timeoutId = timeoutId
    
    // 添加数据接收监控，如果长时间没有数据接收，检查是否需要关闭
    let lastDataTime = Date.now()
    let isConversationCompleted = false // 添加对话完成标志
    const dataMonitorId = setInterval(() => {
      if (isLoading.value && eventSource && !isConversationCompleted) {
        const timeSinceLastData = Date.now() - lastDataTime
        // 如果超过10秒没有收到数据，且连接仍然活跃，可能是对话完成
        if (timeSinceLastData > 10000) {
          console.log('长时间未收到数据，可能对话已完成')
          clearInterval(dataMonitorId)
          handleConnectionClose()
        }
      }
    }, 2000) // 每2秒检查一次
    
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
  conversationId.value = generateConversationId()
  
  // 添加欢迎消息
  messages.value.push({
    id: Date.now(),
    type: 'assistant',
    content: '您好！我是您的AI情感顾问，我会倾听您的心声，为您提供专业的情感支持和建议。请告诉我您今天的心情如何，或者有什么情感问题需要我帮助您解决？'
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

