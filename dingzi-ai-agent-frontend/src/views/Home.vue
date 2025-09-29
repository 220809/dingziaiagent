<template>
  <div class="home-container">
    <div class="container">
      <div class="text-center">
        <h1 class="main-title">Dingzi AI Agent</h1>
        <p class="main-subtitle">选择您的AI应用开始编程</p>
        <div class="version-info">
          <span class="version-badge">v1.0.0</span>
          <span class="status-indicator">●</span>
          <span class="status-text">在线</span>
        </div>
      </div>
      
      <div class="app-grid">
        <div class="app-card" @click="navigateToEmoConsultant">
          <div class="app-icon">💝</div>
          <h2>AI情感顾问</h2>
          <p>专业的AI情感咨询师，为您提供情感支持和心理建议，帮助您处理情感问题。</p>
          <div class="tech-stack">
            <span class="tech-tag">Vue.js</span>
            <span class="tech-tag">SSE</span>
            <span class="tech-tag">AI</span>
          </div>
          <button class="btn btn-primary">开始咨询</button>
        </div>
        
        <div class="app-card" @click="navigateToJManus">
          <div class="app-icon">🤖</div>
          <h2>AI智能体</h2>
          <p>多功能AI智能助手，可以回答各种问题，协助您完成各种任务，提供智能自动化服务。</p>
          <div class="tech-stack">
            <span class="tech-tag">Vue.js</span>
            <span class="tech-tag">SSE</span>
            <span class="tech-tag">Agent</span>
          </div>
          <button class="btn btn-success">开始对话</button>
        </div>
      </div>
      
      <footer class="footer">
        <div class="footer-content">
          <div class="footer-left">
            <p>&copy; 2025 Dingzi AI Agent. Built with ❤️ for developers.</p>
          </div>
          <div class="footer-right">
            <a href="https://github.com/220809/dingziaiagent" target="_blank" class="github-link">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
              <span>GitHub</span>
            </a>
          </div>
        </div>
      </footer>
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
  color: #c9d1d9;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  font-family: 'JetBrains Mono', monospace;
}

.main-subtitle {
  font-size: 20px;
  color: #8b949e;
  margin-bottom: 20px;
}

.text-center {
  text-align: center;
}

.version-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 40px;
}

.version-badge {
  background: #238636;
  color: #ffffff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  font-family: 'JetBrains Mono', monospace;
}

.status-indicator {
  color: #2ea043;
  font-size: 12px;
  animation: pulse 2s infinite;
}

.status-text {
  color: #8b949e;
  font-size: 14px;
  font-family: 'JetBrains Mono', monospace;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.tech-stack {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tech-tag {
  background: #21262d;
  color: #58a6ff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'JetBrains Mono', monospace;
  border: 1px solid #30363d;
}

.footer {
  margin-top: 60px;
  padding-top: 40px;
  border-top: 1px solid #30363d;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.footer-left p {
  color: #8b949e;
  font-size: 14px;
  margin: 0;
}

.footer-right {
  display: flex;
  align-items: center;
}

.github-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #c9d1d9;
  text-decoration: none;
  padding: 8px 16px;
  border: 1px solid #30363d;
  border-radius: 6px;
  background: #21262d;
  transition: all 0.3s ease;
  font-size: 14px;
  font-family: 'JetBrains Mono', monospace;
}

.github-link:hover {
  background: #30363d;
  border-color: #1f6feb;
  color: #ffffff;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
  }
  
  .main-title {
    font-size: 36px;
  }
  
  .main-subtitle {
    font-size: 18px;
  }
}
</style>

