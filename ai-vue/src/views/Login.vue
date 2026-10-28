<template>
  <!-- 4.5 登录页：不套用后台布局，全屏浅灰底、居中白色卡片 -->
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">
          <el-icon :size="22" color="#fff"><ChatRound /></el-icon>
        </div>
        <div class="brand-name">心理健康AI助手</div>
        <div class="brand-sub">管理后台</div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">预置账号：admin / 123456</div>
    </div>
  </div>
</template>

<script setup>
// FR-L01 登录
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatRound, User, Lock } from '@element-plus/icons-vue'
import { adminLogin } from '@/api'
import { setLogin } from '@/auth'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  // 非空校验：失败字段级红字提示，不提交
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const login = await adminLogin(form.username, form.password)
    setLogin(login)
    ElMessage.success('登录成功')
    // 跳转到 redirect 指定页面（无参数默认 /back/dashboard）
    router.push(route.query.redirect || '/back/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef1f6;
}
.login-card {
  width: 360px;
  padding: 36px 32px 24px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}
.brand {
  text-align: center;
  margin-bottom: 22px;
}
.brand-logo {
  width: 44px;
  height: 44px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
}
.brand-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.login-btn {
  width: 100%;
  letter-spacing: 8px;
}
.login-tip {
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>
