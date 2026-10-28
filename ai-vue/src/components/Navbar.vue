<template>
  <!-- FR-G02 导航头：左侧品牌 / 中部页面标题 / 右侧用户区 -->
  <div class="navbar">
    <div class="nav-brand">
      <div class="nav-logo">
        <el-icon :size="18" color="#fff"><ChatRound /></el-icon>
      </div>
      <div class="nav-title">
        <div class="nav-title-main">心理健康AI助手</div>
        <div class="nav-title-sub">管理后台</div>
      </div>
    </div>

    <div class="nav-page-title">{{ pageTitle }}</div>

    <el-dropdown class="nav-user" @command="handleCommand">
      <span class="nav-user-inner">
        <el-avatar :size="30" :icon="UserFilled" />
        <span class="nav-username">{{ username }}</span>
        <el-icon class="nav-caret"><CaretBottom /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="logout">
            <el-icon><SwitchButton /></el-icon>退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatRound, UserFilled, CaretBottom, SwitchButton } from '@element-plus/icons-vue'
import { adminLogout } from '@/api'
import { currentUser, clearLogin } from '@/auth'

const route = useRoute()
const router = useRouter()

// 中部页面标题 = 当前路由对应菜单名（见设计文档 3.5 路由表）
const pageTitle = computed(() => route.meta?.title || '')
const username = currentUser()

// FR-G06 退出登录：确认框 → 调用登出接口（token 入黑名单）→ 清除登录态 → 跳转 /login
function handleCommand(cmd) {
  if (cmd !== 'logout') return
  ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await adminLogout()
      } catch {
        // token 已失效也照常清除本地登录态
      }
      clearLogin()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.navbar {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.nav-logo {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-title-main {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.nav-title-sub {
  font-size: 11px;
  color: #909399;
  line-height: 1.2;
}
.nav-page-title {
  margin-left: 40px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.nav-user {
  margin-left: auto;
  cursor: pointer;
  outline: none;
}
.nav-user-inner {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-username {
  font-size: 14px;
  color: #303133;
}
.nav-caret {
  font-size: 12px;
  color: #909399;
}
</style>
