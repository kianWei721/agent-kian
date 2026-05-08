<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '企业知识库 Agent')
</script>

<template>
  <el-container class="app-shell">
    <el-aside width="220px" class="app-aside">
      <div class="brand">企业知识库 Agent</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/documents/upload">文档上传</el-menu-item>
        <el-menu-item index="/documents">文档列表</el-menu-item>
        <el-menu-item index="/chat">知识库问答</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <div>
          <h2>{{ pageTitle }}</h2>
          <p>简洁可用的 RAG 管理前端</p>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
