import { createRouter, createWebHistory } from 'vue-router'
import BackendLayout from '@/components/BackendLayout.vue'
import UserLayout from '@/components/UserLayout.vue'
import { isLogin, isUserLogin } from '@/auth'

// FR-G04：history 模式，后台路由统一前缀 /back/；用户端挂根路径（用户端详设 3.3 [待确认 Q9]）
const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    // 用户端登录/注册页（FR-U02/U03）：独立全屏页，不套 UserLayout
    path: '/user/login',
    name: 'user-login',
    component: () => import('@/views/user/UserAuth.vue'),
    meta: { title: '登录 / 注册' },
  },
  {
    path: '/',
    component: UserLayout,
    children: [
      { path: '', name: 'user-home', component: () => import('@/views/user/Home.vue'), meta: { title: '首页', requiresUserAuth: true } },
      { path: 'chat', name: 'user-chat', component: () => import('@/views/user/Chat.vue'), meta: { title: 'AI咨询', requiresUserAuth: true } },
      { path: 'moods', name: 'user-moods', component: () => import('@/views/user/Moods.vue'), meta: { title: '情绪日志', requiresUserAuth: true } },
      { path: 'articles', name: 'user-articles', component: () => import('@/views/user/Articles.vue'), meta: { title: '知识库', requiresUserAuth: true } },
      { path: 'profile', name: 'user-profile', component: () => import('@/views/user/Profile.vue'), meta: { title: '个人中心', requiresUserAuth: true } },
    ],
  },
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/dashboard.vue'), meta: { title: '数据分析' } },
      { path: 'articles', name: 'articles', component: () => import('@/views/articles.vue'), meta: { title: '知识文章' } },
      { path: 'consultations', name: 'consultations', component: () => import('@/views/consultations.vue'), meta: { title: '咨询记录' } },
      { path: 'moods', name: 'moods', component: () => import('@/views/moods.vue'), meta: { title: '情绪日志' } },
    ],
  },
  // 未匹配路由重定向到用户端首页（用户端详设 3.3）
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// FR-L02：登录态守卫
router.beforeEach((to) => {
  const authed = isLogin()
  // 未登录访问 /back/* → 重定向 /login 并携带 redirect 参数
  if (to.path.startsWith('/back') && !authed) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  // 已登录访问 /login → 重定向后台默认首页
  if (to.path === '/login' && authed) {
    return '/back/dashboard'
  }
  // 用户端统一登录校验（FR-U01：AI 咨询等入口未登录跳转普通用户登录页）
  if (to.matched.some((r) => r.meta.requiresUserAuth) && !isUserLogin()) {
    return { path: '/user/login', query: { redirect: to.fullPath } }
  }
  // 已登录访问用户端登录页 → 回跳来源页
  if (to.path === '/user/login' && isUserLogin()) {
    return to.query.redirect || '/'
  }
})

router.afterEach((to) => {
  const title = to.meta?.title
  // 后台（/back/* 与登录页）标题带「管理后台」，用户端不带
  const isBackend = to.path.startsWith('/back') || to.path === '/login'
  const base = isBackend ? '心理健康AI助手管理后台' : '心理健康AI助手'
  document.title = title ? `${title} · ${base}` : base
})

export default router
