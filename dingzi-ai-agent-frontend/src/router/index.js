import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import EmoConsultant from '../views/EmoConsultant.vue'
import JManusChat from '../views/JManusChat.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/emo-consultant',
    name: 'EmoConsultant',
    component: EmoConsultant
  },
  {
    path: '/jmanus-chat',
    name: 'JManusChat',
    component: JManusChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

