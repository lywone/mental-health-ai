// 登录态管理（对接 ai-serve JWT 鉴权）
// 后台与用户端各自持有 token，统一持久化在 localStorage。

const AUTH_KEY = 'back_auth'
const USER_AUTH_KEY = 'user_auth'

function read(key) {
  try {
    return JSON.parse(localStorage.getItem(key)) || null
  } catch {
    return null
  }
}

// ---------- 管理后台登录态 ----------
// 存储：{ token, tokenType, expiresIn, profile: { id, username, nickname }, loginAt }

export function isLogin() {
  return !!read(AUTH_KEY)?.token
}

/** login: POST /api/auth/admin/login 的 data（{token, tokenType, expiresIn, profile}） */
export function setLogin(login) {
  localStorage.setItem(
    AUTH_KEY,
    JSON.stringify({
      token: login.token,
      tokenType: login.tokenType || 'Bearer',
      expiresIn: login.expiresIn,
      profile: login.profile,
      loginAt: Date.now(),
    }),
  )
}

export function clearLogin() {
  localStorage.removeItem(AUTH_KEY)
}

export function currentUser() {
  return read(AUTH_KEY)?.profile?.nickname || read(AUTH_KEY)?.profile?.username || 'admin'
}

export function getAdminToken() {
  return read(AUTH_KEY)?.token || null
}

// ---------- 用户端登录态 ----------
// 存储：{ token, tokenType, expiresIn, profile: { id, nickname }, loginAt }

export function isUserLogin() {
  return !!read(USER_AUTH_KEY)?.token
}

/** login: POST /api/auth/user/dev-login 的 data（{token, tokenType, expiresIn, profile}） */
export function setUserLogin(login) {
  localStorage.setItem(
    USER_AUTH_KEY,
    JSON.stringify({
      token: login.token,
      tokenType: login.tokenType || 'Bearer',
      expiresIn: login.expiresIn,
      profile: login.profile,
      loginAt: Date.now(),
    }),
  )
}

export function clearUserLogin() {
  localStorage.removeItem(USER_AUTH_KEY)
}

export function currentUserInfo() {
  return read(USER_AUTH_KEY)?.profile || null
}

export function getUserToken() {
  return read(USER_AUTH_KEY)?.token || null
}

/** 个人中心保存后同步本地用户信息（昵称/头像即时刷新） */
export function updateUserProfile(patch) {
  const auth = read(USER_AUTH_KEY)
  if (!auth) return
  auth.profile = { ...(auth.profile || {}), ...patch }
  localStorage.setItem(USER_AUTH_KEY, JSON.stringify(auth))
}
