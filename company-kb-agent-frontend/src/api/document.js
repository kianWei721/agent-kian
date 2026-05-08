import request from '../utils/request'

export function uploadDocument(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function listDocuments() {
  return request.get('/documents')
}

export function parseDocument(id) {
  return request.post(`/documents/${id}/parse`)
}

export function deleteDocument(id) {
  return request.delete(`/documents/${id}`)
}
