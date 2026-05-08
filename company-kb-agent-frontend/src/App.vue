<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

function newChat() {
  router.push('/chat')
}
</script>

<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">🧠</span>
        <span class="brand-text">企业知识库 Agent</span>
      </div>

      <div class="sidebar-actions">
        <button class="new-chat-btn" @click="newChat">
          <span>＋</span> 新建对话
        </button>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/chat" class="nav-item" :class="{ active: activeMenu === '/chat' }">
          <span class="nav-icon">💬</span> 知识库问答
        </router-link>
        <router-link to="/documents/upload" class="nav-item" :class="{ active: activeMenu === '/documents/upload' }">
          <span class="nav-icon">📤</span> 文档上传
        </router-link>
        <router-link to="/documents" class="nav-item" :class="{ active: activeMenu === '/documents' }">
          <span class="nav-icon">📄</span> 文档管理
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-footer-label">近期会话</div>
        <div class="recent-session">今天的对话</div>
        <div class="recent-session">昨天的对话</div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>
