<script setup>
import { ref, nextTick, onMounted } from 'vue'
import MarkdownIt from 'markdown-it'
import { askQuestion } from '../api/chat'

const md = new MarkdownIt({ breaks: true, linkify: true })

const SESSION_KEY = 'kb-agent-session-id'

const asking = ref(false)
const question = ref('')
const messages = ref([])
const messagesEl = ref(null)
const textareaEl = ref(null)

let sessionId = window.localStorage.getItem(SESSION_KEY)
if (!sessionId) {
  sessionId = String(Date.now())
  window.localStorage.setItem(SESSION_KEY, sessionId)
}

function renderMarkdown(text) {
  return md.render(text || '')
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

async function submit() {
  const text = question.value.trim()
  if (!text || asking.value) return

  messages.value.push({ role: 'user', content: text })
  question.value = ''
  autoResize()
  await scrollToBottom()

  asking.value = true
  try {
    const result = await askQuestion({
      question: text,
      sessionId: Number(sessionId),
    })
    messages.value.push({
      role: 'ai',
      content: result.answer || '',
      references: result.references || [],
    })
  } finally {
    asking.value = false
    await scrollToBottom()
  }
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    submit()
  }
}

function autoResize() {
  if (!textareaEl.value) return
  textareaEl.value.style.height = 'auto'
  textareaEl.value.style.height = Math.min(textareaEl.value.scrollHeight, 200) + 'px'
}

// per-reference collapsed state
const collapsedRefs = ref({})

function toggleRef(msgIdx, refIdx) {
  const key = `${msgIdx}-${refIdx}`
  collapsedRefs.value[key] = !collapsedRefs.value[key]
}

function isRefOpen(msgIdx, refIdx) {
  return !!collapsedRefs.value[`${msgIdx}-${refIdx}`]
}

onMounted(() => {
  if (textareaEl.value) textareaEl.value.focus()
})
</script>

<template>
  <div class="chat-page">
    <!-- Header -->
    <div class="chat-header">
      <h2>知识库问答</h2>
      <p>基于公司内部文档的 RAG 问答助手</p>
    </div>

    <!-- Messages -->
    <div ref="messagesEl" class="chat-messages">
      <!-- Empty state -->
      <div v-if="messages.length === 0" class="chat-empty">
        <div class="chat-empty-icon">🧠</div>
        <div class="chat-empty-title">企业知识库助手</div>
        <div class="chat-empty-subtitle">输入问题，我将从公司内部文档中检索相关内容并生成回答</div>
      </div>

      <!-- Message list -->
      <template v-for="(msg, msgIdx) in messages" :key="msgIdx">
        <!-- User message -->
        <div v-if="msg.role === 'user'" class="message-row user">
          <div class="message-avatar user">你</div>
          <div class="message-body">
            <div class="user-bubble">{{ msg.content }}</div>
          </div>
        </div>

        <!-- AI message -->
        <div v-else class="message-row">
          <div class="message-avatar ai">🤖</div>
          <div class="message-body">
            <div class="ai-card">
              <div class="markdown-body" v-html="renderMarkdown(msg.content)" />

              <!-- References -->
              <div v-if="msg.references && msg.references.length > 0" class="references-section">
                <div class="references-label">引用来源 ({{ msg.references.length }})</div>
                <div class="reference-cards">
                  <div
                    v-for="(ref, refIdx) in msg.references"
                    :key="`${ref.documentName}-${ref.chunkIndex}-${refIdx}`"
                    class="reference-card"
                  >
                    <div class="reference-card-header" @click="toggleRef(msgIdx, refIdx)">
                      <div class="reference-card-title">
                        <span class="reference-badge">{{ refIdx + 1 }}</span>
                        {{ ref.documentName }}
                        <span class="reference-chunk">· Chunk {{ ref.chunkIndex }}</span>
                      </div>
                      <span class="reference-toggle" :class="{ open: isRefOpen(msgIdx, refIdx) }">▼</span>
                    </div>
                    <div v-if="isRefOpen(msgIdx, refIdx)" class="reference-card-content">
                      {{ ref.content }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Thinking indicator -->
      <div v-if="asking" class="thinking-row">
        <div class="message-avatar ai">🤖</div>
        <div class="thinking-bubble">
          <div class="dot-pulse">
            <span /><span /><span />
          </div>
          正在检索知识库并生成回答...
        </div>
      </div>
    </div>

    <!-- Input area -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <textarea
          ref="textareaEl"
          v-model="question"
          class="chat-textarea"
          placeholder="输入问题，按 Enter 发送，Shift + Enter 换行"
          rows="1"
          :disabled="asking"
          @keydown="onKeydown"
          @input="autoResize"
        />
        <button class="send-btn" :disabled="asking || !question.trim()" @click="submit">
          ➤
        </button>
      </div>
      <div class="input-hint">Enter 发送 · Shift + Enter 换行</div>
    </div>
  </div>
</template>
