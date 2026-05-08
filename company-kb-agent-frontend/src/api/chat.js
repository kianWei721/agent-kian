import request from '../utils/request'

export function askQuestion(payload) {
  return request.post('/chat/ask', payload)
}
