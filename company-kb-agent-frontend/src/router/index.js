import { createRouter, createWebHistory } from 'vue-router'
import DocumentUploadView from '../views/DocumentUploadView.vue'
import DocumentListView from '../views/DocumentListView.vue'
import ChatView from '../views/ChatView.vue'

const routes = [
  {
    path: '/',
    redirect: '/chat',
  },
  {
    path: '/documents/upload',
    name: 'document-upload',
    component: DocumentUploadView,
    meta: { title: '文档上传' },
  },
  {
    path: '/documents',
    name: 'document-list',
    component: DocumentListView,
    meta: { title: '文档列表' },
  },
  {
    path: '/chat',
    name: 'kb-chat',
    component: ChatView,
    meta: { title: '知识库问答' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
