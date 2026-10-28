// ai-serve 接口封装（与后端 Controller 一一对应）

import { del, get, post, put, upload } from './http'

// ---------- 认证 ----------
export const adminLogin = (username, password) => post('/api/auth/admin/login', { username, password })
export const adminLogout = () => post('/api/auth/admin/logout', null, { scope: 'admin' })
export const adminMe = () => get('/api/auth/admin/me', { scope: 'admin' })
export const userDevLogin = (userId = null) => post('/api/auth/user/dev-login', { userId }, { scope: 'user' })

// ---------- 用户端认证：验证码注册/登录（FR-U02/U03，匿名接口） ----------
export const userSmsSend = (phone, scene) =>
  post('/api/auth/user/sms/send', { phone, scene }, { scope: 'user', anonymous: true })
export const userRegister = (phone, code, nickname = '') =>
  post('/api/auth/user/register', { phone, code, nickname }, { scope: 'user', anonymous: true })
export const userLogin = (phone, code) =>
  post('/api/auth/user/login', { phone, code }, { scope: 'user', anonymous: true })

// ---------- 数据分析（管理端） ----------
export const getDashboardStats = () => get('/api/admin/dashboard/stats', { scope: 'admin' })
export const getDashboardTrends = (days = 30) =>
  get(`/api/admin/dashboard/trends?days=${days}`, { scope: 'admin' })

// ---------- 文章（管理端） ----------
export const adminArticlePage = (params) => get(`/api/admin/articles?${qs(params)}`, { scope: 'admin' })
export const adminArticleDetail = (id) => get(`/api/admin/articles/${id}`, { scope: 'admin' })
export const adminArticleCreate = (data) => post('/api/admin/articles', data, { scope: 'admin' })
export const adminArticleUpdate = (id, data) => put(`/api/admin/articles/${id}`, data, { scope: 'admin' })
export const adminArticleDelete = (id) => del(`/api/admin/articles/${id}`, { scope: 'admin' })
export const adminArticleStatus = (id, action) =>
  put(`/api/admin/articles/${id}/status`, { action }, { scope: 'admin' })
export const uploadImage = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return upload('/api/admin/upload/image', fd, { scope: 'admin' })
}

// ---------- 咨询记录（管理端） ----------
export const adminConsultPage = (params) => get(`/api/admin/consultations?${qs(params)}`, { scope: 'admin' })
export const adminConsultDetail = (id) => get(`/api/admin/consultations/${id}`, { scope: 'admin' })

// ---------- 情绪日志（管理端） ----------
export const adminMoodPage = (params) => get(`/api/admin/moods?${qs(params)}`, { scope: 'admin' })
export const adminMoodDetail = (id) => get(`/api/admin/moods/${id}`, { scope: 'admin' })

// ---------- AI 咨询（用户端） ----------
export const mySessions = () => get('/api/user/consultations', { scope: 'user' })
export const mySessionDetail = (id) => get(`/api/user/consultations/${id}`, { scope: 'user' })
export const sendChatMessage = (sessionId, content) =>
  post('/api/user/chat/messages', { sessionId, content }, { scope: 'user' })
export const endSession = (id) => post(`/api/user/consultations/${id}/end`, null, { scope: 'user' })
export const scoreSession = (id, score) =>
  put(`/api/user/consultations/${id}/score`, { score }, { scope: 'user' })

// ---------- 情绪日志（用户端） ----------
export const emotionGarden = () => get('/api/user/emotion-garden', { scope: 'user' })
export const createMood = (data) => post('/api/user/moods', data, { scope: 'user' })
export const myMoodPage = (page, pageSize) =>
  get(`/api/user/moods?page=${page}&pageSize=${pageSize}`, { scope: 'user' })

// ---------- 知识库（用户端） ----------
export const userArticlePage = (params) => get(`/api/user/articles?${qs(params)}`, { scope: 'user' })
export const userArticleDetail = (id) => get(`/api/user/articles/${id}`, { scope: 'user' })

// ---------- 个人中心（用户端） ----------
export const myProfile = () => get('/api/user/profile', { scope: 'user' })
export const updateProfile = (data) => put('/api/user/profile', data, { scope: 'user' })
export const uploadAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return upload('/api/user/upload/avatar', fd, { scope: 'user' })
}

// ---------- 公共字典（任意登录） ----------
export const getDicts = (type) =>
  get(`/api/common/dicts${type ? `?type=${encodeURIComponent(type)}` : ''}`, { scope: 'user' })

function qs(params) {
  const sp = new URLSearchParams()
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== null && v !== undefined && v !== '') sp.append(k, v)
  })
  return sp.toString()
}
