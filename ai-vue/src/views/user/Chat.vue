<template>
  <!-- 用户端 4.2 AI咨询 / FR-C01~C05：左栏三卡 + 右聊天区 -->
  <div class="chat-page">
    <!-- 左栏 -->
    <aside class="left-col">
      <!-- FR-C01 助手卡 -->
      <div class="side-card assist-card">
        <div class="assist-avatar">
          <svg viewBox="0 0 36 36" aria-hidden="true">
            <rect x="9" y="13" width="18" height="13" rx="4" fill="#fff" />
            <circle cx="14.5" cy="19.5" r="1.8" fill="#f08c3e" />
            <circle cx="21.5" cy="19.5" r="1.8" fill="#f08c3e" />
            <line x1="18" y1="13" x2="18" y2="8" stroke="#fff" stroke-width="2.4" />
            <circle cx="18" cy="7" r="2.2" fill="#fff" />
          </svg>
        </div>
        <div class="assist-info">
          <div class="assist-name">{{ ASSISTANT.name }}</div>
          <div class="assist-status"><span class="dot-online" />在线服务中</div>
        </div>
      </div>

      <!-- FR-C01 情绪花园（联动今日情绪，[待确认 Q6]） -->
      <div class="side-card garden-card">
        <div class="card-title">情绪花园</div>
        <template v-if="garden">
          <div class="garden-main">
            <div class="garden-ring" :style="{ background: garden.color }">
              <span class="garden-state">{{ garden.state }}</span>
              <span class="garden-value">{{ garden.value }}</span>
            </div>
            <div class="garden-feel">
              今天感觉 <b>{{ garden.state === '不错' ? '很不错' : garden.state }}</b>
            </div>
          </div>
          <div class="garden-tip"><span class="heart">♥</span> 给你的小建议 · {{ garden.tip }}</div>
        </template>
        <template v-else>
          <div class="garden-empty">
            <p>还没有记录今天的心情</p>
            <el-button link type="primary" @click="$router.push('/moods')">去记录 →</el-button>
          </div>
        </template>
      </div>

      <!-- FR-C02 会话历史 -->
      <div class="side-card history-card">
        <div class="card-title">会话历史</div>
        <template v-if="mySessions.length">
          <div
            v-for="s in mySessions"
            :key="s.id"
            class="session-item"
            :class="{ current: currentSession?.id === s.id }"
            @click="loadSession(s)"
          >
            <div class="session-title">{{ s.title }}</div>
            <div class="session-meta">{{ fmtRelative(s.startTime) }} · {{ s.messageCount }} 条消息</div>
          </div>
        </template>
        <el-empty v-else description="暂无会话记录" :image-size="60" />
      </div>
    </aside>

    <!-- 右聊天区 -->
    <section class="chat-area">
      <div class="chat-head">
        <div class="chat-head-icon">♥</div>
        <div class="chat-head-info">
          <div class="chat-head-name">{{ ASSISTANT.name }}</div>
          <div class="chat-head-sub">{{ ASSISTANT.subtitle }}</div>
        </div>
        <el-tooltip content="新建会话" placement="bottom">
          <button class="btn-new" @click="newSession">＋</button>
        </el-tooltip>
      </div>

      <div ref="msgBoxRef" class="msg-box">
        <div v-for="(m, i) in displayMsgs" :key="i" class="msg-row" :class="m.from">
          <div v-if="m.from === 'ai'" class="msg-avatar">
            <svg viewBox="0 0 36 36" aria-hidden="true">
              <rect x="9" y="13" width="18" height="13" rx="4" fill="#fff" />
              <circle cx="14.5" cy="19.5" r="1.8" fill="#f08c3e" />
              <circle cx="21.5" cy="19.5" r="1.8" fill="#f08c3e" />
            </svg>
          </div>
          <div class="msg-body">
            <div class="bubble">{{ m.text }}</div>
            <div class="msg-time">{{ fmtRelative(m.time) }}</div>
          </div>
        </div>
        <!-- 交互与异常：AI 回复等待中「正在输入…」占位 -->
        <div v-if="waiting" class="msg-row ai">
          <div class="msg-avatar">
            <svg viewBox="0 0 36 36" aria-hidden="true">
              <rect x="9" y="13" width="18" height="13" rx="4" fill="#fff" />
              <circle cx="14.5" cy="19.5" r="1.8" fill="#f08c3e" />
              <circle cx="21.5" cy="19.5" r="1.8" fill="#f08c3e" />
            </svg>
          </div>
          <div class="msg-body">
            <div class="bubble typing"><i /><i /><i /></div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="2"
          resize="none"
          maxlength="500"
          show-word-limit
          placeholder="请输入您想分享的内容..."
          class="chat-input"
          @keydown.enter.exact.prevent="send"
        />
        <button class="btn-send" :disabled="!draft.trim() || waiting" @click="send">➤</button>
      </div>
      <div class="input-foot">
        <span>按 Enter 发送，Shift+Enter 换行</span>
        <span class="foot-privacy">内容属敏感数据，请勿外传</span>
      </div>
    </section>
  </div>
</template>

<script setup>
// FR-C01 助手卡/情绪花园 · FR-C02 会话历史 · FR-C03 新建会话 · FR-C04 发送与 AI 回复 · FR-C05 输入约束
// 全部对接 ai-serve：情绪花园 / 会话历史 / 会话详情 / 发送消息（无会话自动新建）
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { emotionGarden, mySessions as fetchMySessions, mySessionDetail, sendChatMessage } from '@/api'
import { fmtRelative } from '@/utils/format'

// Q5 [待确认]：助手命名与欢迎语按设计稿原样呈现
const ASSISTANT = {
  name: '宁澄AI助手',
  subtitle: '您的贴AI心理健康助手',
  welcome:
    '您好！我是小暖，您的AI心理健康助手。很高兴陪伴您，为您提供温暖的心理支持。请告诉我，今天您感觉怎么样？有什么想要分享的吗？',
}

// ---------- FR-C01 情绪花园（今日最新一条；后端无记录返回 null） ----------
const garden = ref(null)
onMounted(async () => {
  loadSessions()
  try {
    garden.value = (await emotionGarden()) || null
  } catch {
    garden.value = null
  }
})

// ---------- FR-C02 会话历史（仅当前用户，后端按 start_time 倒序） ----------
const mySessions = ref([])
async function loadSessions() {
  try {
    mySessions.value = await fetchMySessions()
  } catch {
    mySessions.value = []
  }
}

// ---------- 聊天区显示模型 ----------
const WELCOME_MSG = { from: 'ai', text: ASSISTANT.welcome, time: new Date() }
const displayMsgs = ref([{ ...WELCOME_MSG }])
const currentSession = ref(null) // { id, title?, status? } 或 null（新会话）
const waiting = ref(false)
const draft = ref('')
const msgBoxRef = ref(null)

function scrollToBottom() {
  nextTick(() => {
    const box = msgBoxRef.value
    if (box) box.scrollTop = box.scrollHeight
  })
}

// 点击历史会话：拉取完整消息到聊天区（[待确认 Q7]）
async function loadSession(s) {
  if (waiting.value || currentSession.value?.id === s.id) return
  try {
    const detail = await mySessionDetail(s.id)
    currentSession.value = s
    displayMsgs.value = (detail.messages || []).map((m) => ({ from: m.from, text: m.text, time: m.time }))
    scrollToBottom()
  } catch (e) {
    ElMessage.error(e.message || '加载会话失败')
  }
}

// FR-C03 新建会话：清空聊天区为欢迎语，发送首条消息时由后端自动创建会话
function newSession() {
  if (waiting.value) return
  currentSession.value = null
  displayMsgs.value = [{ ...WELCOME_MSG, time: new Date() }]
  draft.value = ''
}

// FR-C04 发送：用户气泡立即显示 → 调后端（AI 回复落库后返回）；期间禁止重复发送
async function send() {
  const text = draft.value.trim()
  if (!text || waiting.value) return
  draft.value = ''
  waiting.value = true
  const optimistic = { from: 'user', text, time: new Date() }
  displayMsgs.value.push(optimistic)
  scrollToBottom()

  try {
    const res = await sendChatMessage(currentSession.value?.id ?? null, text)
    // res: { sessionId, userMessage, reply }
    if (!currentSession.value) {
      currentSession.value = { id: res.sessionId, status: '进行中' }
    }
    displayMsgs.value.push({ from: res.reply.from, text: res.reply.text, time: res.reply.time })
    loadSessions() // 刷新左侧会话历史（新会话/消息数变化）
  } catch (e) {
    displayMsgs.value = displayMsgs.value.filter((m) => m !== optimistic)
    ElMessage.error(e.message || '发送失败，请稍后重试')
  } finally {
    waiting.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  height: calc(100vh - 64px);
  display: flex;
  gap: 16px;
}
/* ---------- 左栏 ---------- */
.left-col {
  width: 300px;
  flex: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-bottom: 16px;
}
.side-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px;
}
.card-title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.assist-card {
  background: linear-gradient(135deg, #fdf3e7, #fdeede);
  display: flex;
  align-items: center;
  gap: 12px;
}
.assist-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #f08c3e;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.assist-avatar svg {
  width: 28px;
}
.assist-name {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}
.assist-status {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}
.dot-online {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #67c23a;
}
.garden-card {
  background: linear-gradient(160deg, #fdf6ee, #fdefe4);
}
.garden-main {
  text-align: center;
}
.garden-ring {
  width: 88px;
  height: 88px;
  margin: 2px auto 8px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: inset 0 0 0 6px rgba(255, 255, 255, 0.35);
}
.garden-state {
  font-size: 12px;
  opacity: 0.92;
}
.garden-value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.1;
}
.garden-feel {
  font-size: 13px;
  color: #909399;
}
.garden-feel b {
  color: #303133;
  font-size: 14px;
}
.garden-tip {
  margin-top: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}
.heart {
  color: #e0695f;
}
.garden-empty {
  text-align: center;
  padding: 8px 0;
}
.garden-empty p {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.history-card {
  flex: 1;
  min-height: 140px;
}
.session-item {
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.session-item:hover {
  background: #f5f7fa;
}
.session-item.current {
  background: #ecf5ff;
}
.session-title {
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.session-meta {
  margin-top: 2px;
  font-size: 11.5px;
  color: #c0c4cc;
}
/* ---------- 右聊天区 ---------- */
.chat-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fbf8f3;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  overflow: hidden;
}
.chat-head {
  flex: none;
  height: 64px;
  padding: 0 16px;
  background: linear-gradient(90deg, #f2a54a, #e8823c);
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}
.chat-head-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #e0695f;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.chat-head-name {
  font-size: 15px;
  font-weight: 700;
}
.chat-head-sub {
  margin-top: 1px;
  font-size: 11px;
  opacity: 0.9;
}
.btn-new {
  margin-left: auto;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.85);
  color: #e8823c;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-new:hover {
  background: #fff;
}
.msg-box {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 20px 8px;
}
.msg-row {
  display: flex;
  margin-bottom: 14px;
}
.msg-row.user {
  justify-content: flex-end;
}
.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #f08c3e;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  margin-right: 10px;
}
.msg-avatar svg {
  width: 22px;
}
.msg-body {
  max-width: 68%;
}
.bubble {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
}
.msg-row.ai .bubble {
  background: #fff;
  color: #303133;
  border-top-left-radius: 2px;
}
.msg-row.user .bubble {
  background: #f08c3e;
  color: #fff;
  border-top-right-radius: 2px;
}
.msg-time {
  margin-top: 4px;
  font-size: 11px;
  color: #c0c4cc;
}
.msg-row.ai .msg-time {
  text-align: left;
}
.msg-row.user .msg-time {
  text-align: right;
}
/* 「正在输入…」动态点 */
.typing {
  display: flex;
  gap: 4px;
  align-items: center;
  min-height: 20px;
}
.typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #dcd4c8;
  animation: blink 1.2s infinite ease-in-out;
}
.typing i:nth-child(2) {
  animation-delay: 0.2s;
}
.typing i:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes blink {
  0%,
  60%,
  100% {
    opacity: 0.35;
  }
  30% {
    opacity: 1;
  }
}
.input-area {
  flex: none;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 16px 0;
  background: #fff;
}
.chat-input {
  flex: 1;
}
.btn-send {
  flex: none;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: #f08c3e;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn-send:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.input-foot {
  flex: none;
  display: flex;
  justify-content: space-between;
  padding: 6px 18px 10px;
  font-size: 11px;
  color: #c0c4cc;
  background: #fff;
}
</style>
