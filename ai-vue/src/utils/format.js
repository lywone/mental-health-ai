// 通用格式化与规则函数（对应设计文档共享规则 R3 / R6）

export function pad2(n) {
  return String(n).padStart(2, '0')
}

// R3：日期时间格式 YYYY-MM-DD HH:mm
export function fmtDateTime(d) {
  if (!d) return '—'
  const t = d instanceof Date ? d : new Date(d)
  return `${t.getFullYear()}-${pad2(t.getMonth() + 1)}-${pad2(t.getDate())} ${pad2(t.getHours())}:${pad2(t.getMinutes())}`
}

// R3：纯日期格式 YYYY-MM-DD
export function fmtDate(d) {
  if (!d) return '—'
  const t = d instanceof Date ? d : new Date(d)
  return `${t.getFullYear()}-${pad2(t.getMonth() + 1)}-${pad2(t.getDate())}`
}

// R3：图表 X 轴用 MM-DD
export function fmtMD(d) {
  const t = d instanceof Date ? d : new Date(d)
  return `${pad2(t.getMonth() + 1)}-${pad2(t.getDate())}`
}

// R6：评分分档 low(0-3 红) / mid(4-6 橙) / high(7-10 绿)
export function scoreLevel(score) {
  if (score == null) return null
  return score <= 3 ? 'low' : score <= 6 ? 'mid' : 'high'
}

export function scoreColor(score) {
  return { low: '#F56C6C', mid: '#E6A23C', high: '#67C23A' }[scoreLevel(score)] || '#909399'
}

export function scoreTagType(score) {
  return { low: 'danger', mid: 'warning', high: 'success' }[scoreLevel(score)] || 'info'
}

// 用户端 R2：会话消息相对时间（刚刚 / N 分钟前 / 今天 HH:mm / 日期）
export function fmtRelative(d) {
  const t = d instanceof Date ? d : new Date(d)
  const now = new Date()
  const diff = Math.floor((now - t) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  const sameDay =
    t.getFullYear() === now.getFullYear() && t.getMonth() === now.getMonth() && t.getDate() === now.getDate()
  if (sameDay) return `今天 ${pad2(t.getHours())}:${pad2(t.getMinutes())}`
  return `${pad2(t.getMonth() + 1)}-${pad2(t.getDate())} ${pad2(t.getHours())}:${pad2(t.getMinutes())}`
}
