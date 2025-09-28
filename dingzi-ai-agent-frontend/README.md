# 钉子AI智能体前端项目

基于Vue3开发的AI智能体前端应用，包含AI情感顾问和AI智能体两个聊天应用。

## 功能特性

- 🏠 **主页**: 应用选择界面，可以切换到不同的AI应用
- 💝 **AI情感顾问**: 专业的AI情感咨询师，提供情感支持和心理建议
- 🤖 **AI智能体**: 多功能AI智能助手，可以回答各种问题
- 💬 **实时聊天**: 基于SSE的实时对话功能
- 📱 **响应式设计**: 适配不同屏幕尺寸

## 技术栈

- Vue 3
- Vue Router 4
- Pinia (状态管理)
- Axios (HTTP请求)
- Vite (构建工具)

## 项目结构

```
src/
├── views/           # 页面组件
│   ├── Home.vue     # 主页
│   ├── EmoConsultant.vue  # AI情感顾问
│   └── JManusChat.vue     # AI智能体
├── services/        # API服务
│   └── api.js       # API配置和SSE连接
├── router/          # 路由配置
│   └── index.js
├── App.vue          # 根组件
├── main.js          # 入口文件
└── style.css        # 全局样式
```

## 安装和运行

1. 安装依赖
```bash
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 构建生产版本
```bash
npm run build
```

## 后端接口

项目需要配合SpringBoot后端使用，后端接口地址：`http://localhost:8100/api`

### 接口说明

- `GET /ai/emo/consultant` - AI情感顾问SSE接口
- `GET /ai/jmanus/chat` - AI智能体SSE接口

## 使用说明

1. 访问主页选择需要的AI应用
2. 在聊天界面输入问题或消息
3. 系统会通过SSE实时返回AI的回复
4. 支持多轮对话，每次进入页面会生成新的会话ID

## 注意事项

- 确保后端服务已启动并运行在8100端口
- 浏览器需要支持EventSource API
- 建议使用现代浏览器以获得最佳体验

