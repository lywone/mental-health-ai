// 统一请求封装（对接 ai-serve 后端，契约见 docs/后端接口设计文档-v1.0.md）
// 响应结构 {code, message, data}：code===0 取 data，其余抛错；
// 401/403 由 Spring Security 输出 JSON（1002/1003/1004），同样按业务码处理。

import { clearLogin, clearUserLogin, getAdminToken, getUserToken } from '@/auth'

// 请求重写为 Promise， rejection: Error 对象（含 code/message）

async function rawRequest(path, { method = 'GET', body = null, scope = 'admin', token = null, anonymous = false } = {}) {
  const headers = {}
  const bearer = anonymous ? null : token || (scope === 'user' ? getUserToken() : getAdminToken())
  if (bearer) headers.Authorization = `Bearer ${bearer}`
  let payload
  if (body instanceof FormData) {
    payload = body // 浏览器自动设置 multipart 边界
  } else if (body != null) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }
  const resp = await fetch(path, { method, headers, body: payload })

  let json
  try {
    json = await resp.json()
  } catch {
    throw Object.assign(new Error(`服务异常（HTTP ${resp.status}）`), { code: resp.status })
  }
  if (resp.status === 401 || resp.status === 403) {
    const err = Object.assign(new Error(json?.message || '未登录或无权限'), { code: json?.code ?? resp.status })
    err.auth = resp.status === 401
    throw err
  }
  if (json.code !== 0) {
    throw Object.assign(new Error(json.message || '请求失败'), { code: json.code })
  }
  return json.data
}

// 用户端 401（token 缺失/过期）：清除登录态并跳转用户端登录页（FR-U01，不再静默 dev-login）
async function requestAsUser(path, options) {
  try {
    return await rawRequest(path, { ...options, scope: 'user' })
  } catch (e) {
    if (e.auth && !path.startsWith('/api/auth/user/')) {
      clearUserLogin()
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      window.location.href = `/user/login?redirect=${redirect}`
    }
    throw e
  }
}

export function request(path, options = {}) {
  if (options.scope === 'user') return requestAsUser(path, options)
  return rawRequest(path, { ...options, scope: 'admin' }).catch((e) => {
    // 管理端登录态失效：清除本地登录并跳转登录页
    if (e.auth && !path.startsWith('/api/auth/admin/login')) {
      clearLogin()
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      window.location.href = `/login?redirect=${redirect}`
    }
    throw e
  })
}

export const get = (path, options) => request(path, { ...options, method: 'GET' })
export const post = (path, body, options) => request(path, { ...options, method: 'POST', body })
export const put = (path, body, options) => request(path, { ...options, method: 'PUT', body })
export const del = (path, options) => request(path, { ...options, method: 'DELETE' })
export const upload = (path, formData, options) => request(path, { ...options, method: 'POST', body: formData })
