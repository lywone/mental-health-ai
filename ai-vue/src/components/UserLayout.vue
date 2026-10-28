<template>
  <!-- 用户端 FR-N01~N03 布局骨架：导航头固定顶部 / 内容区独立滚动（el-container） -->
  <el-container class="ulayout">
    <el-header class="uheader">
      <div class="uheader-inner">
        <router-link to="/" class="brand">
          <svg class="brand-logo" viewBox="0 0 36 36" aria-hidden="true">
            <rect x="1" y="1" width="34" height="34" rx="10" fill="#f08c3e" />
            <rect x="9" y="13" width="18" height="13" rx="4" fill="#fff" />
            <circle cx="14.5" cy="19.5" r="1.8" fill="#f08c3e" />
            <circle cx="21.5" cy="19.5" r="1.8" fill="#f08c3e" />
            <line x1="18" y1="13" x2="18" y2="8" stroke="#fff" stroke-width="2.4" />
            <circle cx="18" cy="7" r="2.2" fill="#fff" />
          </svg>
          <span class="brand-name">心理健康AI助手</span>
        </router-link>

        <nav class="nav">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav-item" active-class="active">
            {{ item.label }}
          </router-link>
        </nav>

        <div class="user-area">
          <template v-if="user">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-chip">
                <img v-if="user.avatar" :src="user.avatar" class="user-avatar" alt="头像" />
                <span v-else class="user-avatar user-avatar-text">{{ (user.nickname || '用')[0] }}</span>
                <span class="user-name">{{ user.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <router-link v-else to="/user/login" class="login-link">请登录</router-link>
        </div>
      </div>
    </el-header>
    <el-main class="umain">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
// 用户端导航头（用户端详设 3.2 / 图 1）· 登录态由路由守卫与用户端登录页建立（FR-U01）
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearUserLogin, currentUserInfo } from '@/auth'

const route = useRoute()
const router = useRouter()

// FR-N01 四个导航入口（3.3 路由表）
const navItems = [
  { path: '/', label: '首页' },
  { path: '/chat', label: 'AI咨询' },
  { path: '/moods', label: '情绪日志' },
  { path: '/articles', label: '知识库' },
]

// 登录信息随路由变化重新读取（个人中心保存后昵称/头像即时刷新）
const user = computed(() => {
  route.fullPath
  return currentUserInfo()
})

function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'logout') handleLogout()
}

// FR-N03 退出登录：确认框 → 清除用户端登录态 → 去登录页
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      clearUserLogin()
      ElMessage.success('已退出登录')
      router.push('/user/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.ulayout {
  height: 100vh;
}
.uheader {
  height: 64px;
  flex: none;
  padding: 0;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  z-index: 10;
}
.uheader-inner {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 32px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.brand-logo {
  width: 36px;
  height: 36px;
}
.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}
.nav {
  margin-left: auto;
  display: flex;
  gap: 8px;
}
.nav-item {
  position: relative;
  padding: 6px 14px;
  font-size: 14px;
  color: #303133;
  text-decoration: none;
  border-radius: 6px;
  transition: color 0.15s, background 0.15s;
}
.nav-item:hover {
  color: #409eff;
}
.nav-item.active {
  color: #409eff;
  font-weight: 600;
}
/* FR-N02 当前页高亮下划线 */
.nav-item.active::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: -2px;
  height: 2px;
  border-radius: 1px;
  background: #409eff;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}
.user-avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(135deg, #f8b26a, #f08c3e);
}
.user-name {
  font-size: 13px;
  color: #606266;
}
.login-link {
  font-size: 13px;
  color: #f08c3e;
  text-decoration: none;
}
.login-link:hover {
  color: #e07f30;
}
/* FR-G05 对应：滚动条仅出现在内容区右缘 */
.umain {
  overflow-y: auto;
  padding: 0;
  background: #f5f7fa;
}
</style>
