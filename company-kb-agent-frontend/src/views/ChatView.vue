<script setup>
import { ref, watch } from 'vue'
import { askQuestion } from '../api/chat'

const SESSION_STORAGE_KEY = 'kb-agent-session-id'

const asking = ref(false)
const sessionId = ref(window.localStorage.getItem(SESSION_STORAGE_KEY) || `${Date.now()}`)
const question = ref('')
const answer = ref('')
const references = ref([])

watch(sessionId, (value) => {
  window.localStorage.setItem(SESSION_STORAGE_KEY, value)
})

async function submitQuestion() {
  const resolvedSessionId = Number(sessionId.value)
  if (!question.value.trim() || !sessionId.value.trim() || Number.isNaN(resolvedSessionId)) {
    return
  }
  asking.value = true
  try {
    const result = await askQuestion({
      question: question.value.trim(),
      sessionId: resolvedSessionId,
    })
    answer.value = result.answer || ''
    references.value = result.references || []
  } finally {
    asking.value = false
  }
}
</script>

<template>
  <div class="page-grid">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>知识库问答</span>
        </div>
      </template>

      <el-form label-position="top">
        <el-form-item label="会话 ID">
          <el-input
            v-model="sessionId"
            type="number"
            placeholder="可自定义会话 ID，默认自动生成"
          />
        </el-form-item>
        <el-form-item label="你的问题">
          <el-input
            v-model="question"
            type="textarea"
            :rows="5"
            placeholder="例如：订单服务部署失败如何排查？"
          />
        </el-form-item>
        <el-button type="primary" :loading="asking" @click="submitQuestion">
          {{ asking ? '回答生成中...' : '开始提问' }}
        </el-button>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>问答结果</span>
        </div>
      </template>

      <el-empty v-if="!answer" description="提交问题后在这里查看回答" />

      <template v-else>
        <div class="answer-block">{{ answer }}</div>

        <el-divider content-position="left">引用来源</el-divider>

        <el-empty v-if="references.length === 0" description="当前回答没有引用来源" />

        <el-space v-else direction="vertical" fill size="16" style="width: 100%">
          <el-card v-for="(item, index) in references" :key="`${item.documentName}-${item.chunkIndex}-${index}`" shadow="hover">
            <div class="reference-title">[{{ index + 1 }}] {{ item.documentName }} / Chunk {{ item.chunkIndex }}</div>
            <div class="reference-score">相关度：{{ item.score ?? '-' }}</div>
            <div class="reference-content">{{ item.content }}</div>
          </el-card>
        </el-space>
      </template>
    </el-card>
  </div>
</template>
