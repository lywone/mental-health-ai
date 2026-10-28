<template>
  <!-- 用户端 4.1 首页 / FR-H01~H03：绿色渐变 Hero + 双按钮 + 机器人圆卡 -->
  <div class="hero">
    <div class="hero-inner">
      <div class="hero-text">
        <h1 class="title-white">一次温暖的对话</h1>
        <h1 class="title-yellow">化孤独为慰藉</h1>
        <p class="desc">每个深夜，每个焦虑的时刻，我们都在这里。不必独自承受，让心与心的连接温暖您的每一天</p>
        <div class="hero-actions">
          <el-button class="btn-solid" size="large" @click="go('/chat')">开始倾听，获得陪伴</el-button>
          <el-button class="btn-ghost" size="large" @click="go('/moods')">记录心情，释放情感</el-button>
        </div>
      </div>
      <div class="hero-figure">
        <div class="robot-circle">
          <svg viewBox="0 0 96 80" aria-hidden="true">
            <line x1="48" y1="10" x2="48" y2="2" stroke="#fff" stroke-width="4" />
            <circle cx="48" cy="4" r="5" fill="#fff" />
            <rect x="16" y="12" width="64" height="52" rx="14" fill="#fff" />
            <circle cx="35" cy="38" r="6" fill="#2f6d5f" />
            <circle cx="61" cy="38" r="6" fill="#2f6d5f" />
            <rect x="38" y="50" width="20" height="4" rx="2" fill="#cfe3d9" />
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
// FR-H02 / FR-H03 [推断]：双按钮分别跳转 AI咨询 / 情绪日志
const go = (path) => router.push(path)
</script>

<style scoped>
.hero {
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #2f6d5f 0%, #5d9480 55%, #8bb8a5 100%);
  display: flex;
  align-items: center;
}
.hero-inner {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 60px 24px;
  display: flex;
  align-items: center;
  gap: 40px;
}
.hero-text {
  flex: 1;
}
.title-white {
  font-size: 44px;
  line-height: 1.3;
  color: #fff;
  letter-spacing: 2px;
}
.title-yellow {
  font-size: 44px;
  line-height: 1.3;
  color: #f5c518;
  letter-spacing: 2px;
}
.desc {
  margin-top: 18px;
  max-width: 520px;
  font-size: 15px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.85);
}
.hero-actions {
  margin-top: 36px;
  display: flex;
  gap: 16px;
}
.btn-solid {
  background: #fff;
  border-color: #fff;
  color: #2f6d5f;
  font-weight: 600;
  padding: 20px 28px;
}
.btn-solid:hover {
  background: #eef7f2;
  border-color: #eef7f2;
  color: #2f6d5f;
}
.btn-ghost {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.8);
  color: #fff;
  padding: 20px 28px;
}
.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: #fff;
  color: #fff;
}
/* 右侧大圆形玻璃质感机器人卡 */
.hero-figure {
  flex: none;
}
.robot-circle {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
}
.robot-circle svg {
  width: 150px;
}
</style>
