<template>
  <div class="home-container">
    <div class="container">
      <div class="text-center">
        <h1 class="main-title">钉子的AI应用</h1>
        <p class="main-subtitle">选择您需要的AI应用开始对话</p>
      </div>
      
      <div class="app-grid">
        <div class="app-card" @click="navigateToEmoConsultant">
          <div class="app-icon">💝</div>
          <h2>AI情感顾问</h2>
          <p>专业的AI情感咨询师，为您提供情感支持和心理建议，帮助您处理情感问题。</p>
          <button class="btn btn-primary">开始咨询</button>
        </div>
        
        <div class="app-card" @click="navigateToJManus">
          <div class="app-icon">🤖</div>
          <h2>AI智能体</h2>
          <p>多功能AI智能助手，可以回答各种问题，协助您完成各种任务。</p>
          <button class="btn btn-success">开始对话</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToEmoConsultant = () => {
  router.push('/emo-consultant')
}

const navigateToJManus = () => {
  router.push('/jmanus-chat')
}
</script>

<style scoped>
.main-title {
  font-size: 48px;
  font-weight: 700;
  color: white;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.main-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
}

.text-center {
  text-align: center;
}
</style>

