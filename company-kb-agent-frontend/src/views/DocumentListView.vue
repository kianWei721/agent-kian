<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDocuments, parseDocument } from '../api/document'

const loading = ref(false)
const reparseLoadingId = ref(null)
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
    ElMessage.success('已触发解析')
    await fetchDocuments()
  } finally {
    reparseLoadingId.value = null
  }
}

function formatFileSize(size) {
  if (!size && size !== 0) {
    return '-'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function statusType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'PARSING') return 'warning'
  return 'info'
}

onMounted(fetchDocuments)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header between">
        <span>文档列表</span>
        <el-button @click="fetchDocuments" :loading="loading">刷新</el-button>
      </div>
    </template>

    <el-table :data="documents" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="fileName" label="文件名" min-width="220" />
      <el-table-column prop="fileType" label="类型" width="100" />
      <el-table-column label="大小" width="120">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="解析状态" width="120">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.parseStatus)">
            {{ scope.row.parseStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
      <el-table-column label="错误信息" min-width="220">
        <template #default="scope">
          {{ scope.row.errorMessage || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            link
            :loading="reparseLoadingId === scope.row.id"
            @click="handleParse(scope.row.id)"
          >
            重新解析
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
