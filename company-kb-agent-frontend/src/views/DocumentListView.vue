<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDocuments, parseDocument, deleteDocument } from '../api/document'

const loading = ref(false)
const reparseLoadingId = ref(null)
const deleteLoadingId = ref(null)
const documents = ref([])

async function fetchDocuments() {
  loading.value = true
  try {
    documents.value = await listDocuments()
  } finally {
    loading.value = false
  }
}

async function handleParse(id) {
  reparseLoadingId.value = id
  try {
    await parseDocument(id)
    ElMessage.success('已触发重新解析')
    await fetchDocuments()
  } finally {
    reparseLoadingId.value = null
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档「${row.fileName}」吗？该操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  deleteLoadingId.value = row.id
  try {
    await deleteDocument(row.id)
    ElMessage.success('已删除')
    await fetchDocuments()
  } finally {
    deleteLoadingId.value = null
  }
}

function formatFileSize(size) {
  if (!size && size !== 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function statusType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'PARSING') return 'warning'
  if (status === 'PENDING') return ''
  return 'info'
}

function statusLabel(status) {
  const map = { SUCCESS: '已完成', FAILED: '失败', PARSING: '解析中', PENDING: '待解析' }
  return map[status] || status
}

onMounted(fetchDocuments)
</script>

<template>
  <div class="doc-page">
    <div class="doc-page-header">
      <h2>文档管理</h2>
      <p>查看所有已上传的知识库文档及其解析状态</p>
    </div>

    <div class="doc-card">
      <div class="list-header">
        <h3>文档列表</h3>
        <el-button :loading="loading" @click="fetchDocuments">刷新</el-button>
      </div>

      <el-table :data="documents" v-loading="loading" border stripe>
        <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="90" />
        <el-table-column label="大小" width="110">
          <template #default="scope">{{ formatFileSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="解析状态" width="110">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.parseStatus)" size="small">
              {{ statusLabel(scope.row.parseStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              :loading="reparseLoadingId === scope.row.id"
              @click="handleParse(scope.row.id)"
            >
              重新解析
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              :loading="deleteLoadingId === scope.row.id"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
