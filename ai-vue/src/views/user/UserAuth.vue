<template>
  <!-- 用户端登录/注册页（FR-U02 验证码登录 / FR-U03 注册）：独立全屏页，不套 UserLayout -->
  <div class="uauth-page">
    <div class="auth-card">
      <div class="brand">
        <svg class="brand-logo" viewBox="0 0 36 36" aria-hidden="true">
          <rect x="1" y="1" width="34" height="34" rx="10" fill="#f08c3e" />
          <rect x="9" y="13" width="18" height="13" rx="4" fill="#fff" />
          <circle cx="14.5" cy="19.5" r="1.8" fill="#f08c3e" />
          <circle cx="21.5" cy="19.5" r="1.8" fill="#f08c3e" />
          <line x1="18" y1="13" x2="18" y2="8" stroke="#fff" stroke-width="2.4" />
          <circle cx="18" cy="7" r="2.2" fill="#fff" />
        </svg>
        <div class="brand-name">心理健康AI助手</div>
        <div class="brand-sub">登录后开启你的 AI 心理咨询</div>
      </div>

      <div class="tabs">
        <span class="tab" :class="{ active: tab === 'login' }" @click="switchTab('login')">登录</span>
        <span class="tab" :class="{ active: tab === 'register' }" @click="switchTab('register')">注册</span>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleSubmit">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" maxlength="11" clearable />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="6 位验证码" :prefix-icon="Lock" maxlength="6" />
            <el-button class="code-btn" :disabled="countdown > 0" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="tab === 'register'" prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" :prefix-icon="User" maxlength="20" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
            {{ tab === 'login' ? '登 录' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="devCode" class="dev-tip">开发模式验证码：{{ devCode }}</div>
      <div class="foot-tip"><router-link to="/login">管理员登录 →</router-link></div>
    </div>
  </div>
</template>

<script setup>
// FR-U02 手机号+验证码登录 · FR-U03 注册（昵称选填，注册即登录），登录后回跳 redirect
import { computed, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, User } from '@element-plus/icons-vue'
import { userLogin, userRegister, userSmsSend } from '@/api'
import { setUserLogin } from '@/auth'

const PHONE_RE = /^1[3-9]\d{9}$/

const route = useRoute()
const router = useRouter()

const tab = ref('login')
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
const devCode = ref('')
let timer = null

const form = reactive({ phone: '', code: '', nickname: '' })
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: PHONE_RE, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' },
  ],
}

const scene = computed(() => (tab.value === 'login' ? 'LOGIN' : 'REGISTER'))

function switchTab(t) {
  tab.value = t
  devCode.value = ''
  formRef.value?.clearValidate()
}

function startCountdown(seconds) {
  countdown.value = seconds
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}
onUnmounted(() => clearInterval(timer))

async function handleSendCode() {
  // 前置手机号校验，避免无效请求
  if (!PHONE_RE.test(form.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    const res = await userSmsSend(form.phone, scene.value)
    startCountdown(res.intervalSeconds || 60)
    devCode.value = res.devCode || ''
    ElMessage.success('验证码已发送')
  } catch (e) {
    ElMessage.error(e.message || '验证码发送失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const login =
      tab.value === 'login'
        ? await userLogin(form.phone, form.code)
        : await userRegister(form.phone, form.code, form.nickname)
    setUserLogin(login)
    ElMessage.success(tab.value === 'login' ? '登录成功' : '注册成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 与管理端 Login.vue 同风格：全屏浅灰底 + 居中白卡 */
.uauth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef1f6;
}
.auth-card {
  width: 380px;
  padding: 32px 32px 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}
.brand {
  text-align: center;
  margin-bottom: 16px;
}
.brand-logo {
  width: 44px;
  height: 44px;
  margin-bottom: 10px;
}
.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
}
.brand-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.tabs {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-bottom: 18px;
}
.tab {
  padding: 4px 2px;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.15s;
}
.tab.active {
  color: #f08c3e;
  font-weight: 600;
  border-bottom-color: #f08c3e;
}
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-btn {
  flex: none;
  width: 118px;
}
.submit-btn {
  width: 100%;
  letter-spacing: 8px;
  background: #f08c3e;
  border-color: #f08c3e;
}
.submit-btn:hover {
  background: #e07f30;
  border-color: #e07f30;
}
.dev-tip {
  text-align: center;
  font-size: 12px;
  color: #e6a23c;
  margin-bottom: 6px;
}
.foot-tip {
  text-align: center;
  font-size: 12px;
}
.foot-tip a {
  color: #909399;
  text-decoration: none;
}
.foot-tip a:hover {
  color: #f08c3e;
}
</style>
