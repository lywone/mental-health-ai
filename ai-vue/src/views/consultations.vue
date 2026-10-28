<template>
  <!-- 4.3 咨询记录 /back/consultations -->
  <div class="consultations-page">
    <!-- 筛选栏 FR-C01 -->
    <div class="panel">
      <el-form inline class="filter-form" @submit.prevent>
        <el-form-item label="用户">
          <el-input v-model="filterDraft.user" placeholder="用户昵称 / ID" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="filterDraft.dateRange"
            type="daterange"
            range-separator="—"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterDraft.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="进行中" value="进行中" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 会话列表 -->
    <div class="panel mt16">
      <el-table :data="pageData" v-loading="loading" stripe>
        <el-table-column prop="id" label="会话编号" width="100" />
        <el-table-column label="用户" width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <span>{{ row.userNickname }}</span>
              <span class="user-id">#{{ row.userId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ fmtDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="时长" width="90">
          <template #default="{ row }">{{ row.durationMin != null ? `${row.durationMin}分钟` : '—' }}</template>
        </el-table-column>
        <el-table-column prop="messageCount" label="消息数" width="80" />
        <el-table-column label="情绪评分" width="90">
          <template #default="{ row }">
            <span v-if="row.score != null" class="score" :style="{ color: scoreColor(row.score) }">{{ row.score }}</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '进行中' ? 'primary' : 'info'" disable-transitions>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="view(row)">查看</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无数据" /></template>
      </el-table>

      <!-- R4 分页（服务端分页） -->
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pager"
        @current-change="fetchList"
        @size-change="() => { page = 1; fetchList() }"
      />
    </div>

    <!-- 详情抽屉 FR-C02（图 6） -->
    <el-drawer v-model="drawerVisible" title="咨询详情" size="460px">
      <template v-if="current">
        <div class="detail-meta">
          {{ current.userNickname }} · {{ fmtDateTime(current.startTime) }} ·
          时长 {{ current.durationMin != null ? `${current.durationMin}分钟` : '—' }} ·
          消息 {{ current.messageCount }} 条 ·
          情绪评分 <span :style="{ color: current.score != null ? scoreColor(current.score) : '#909399' }">{{ current.score ?? '—' }}</span>
        </div>
        <div v-loading="detailLoading" class="chat">
          <div
            v-for="(msg, i) in current.messages"
            :key="i"
            class="chat-row"
            :class="msg.from === 'user' ? 'chat-user' : 'chat-ai'"
          >
            <div class="bubble" :class="msg.from === 'user' ? 'bubble-user' : 'bubble-ai'">{{ msg.text }}</div>
            <div class="chat-time">{{ fmtDateTime(msg.time) }}</div>
          </div>
        </div>
        <div class="detail-tip">仅管理员可查看 · 内容属敏感数据，请勿外传</div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
// FR-C01 列表与筛选 / FR-C02 详情查看（仅查看，Q6 敏感操作边界；服务端分页 + 筛选）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminConsultPage, adminConsultDetail } from '@/api'
import { fmtDateTime, scoreColor } from '@/utils/format'

// ---------- 列表（服务端分页 + 筛选） ----------
const pageData = ref([])
const total = ref(0)
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)

const filterDraft = reactive({ user: '', dateRange: null, status: null })
const filter = reactive({ user: '', dateRange: null, status: null })

async function fetchList() {
  loading.value = true
  try {
    const [start, end] = filter.dateRange || []
    const data = await adminConsultPage({
      user: filter.user,
      startDate: start || '',
      endDate: end || '',
      status: filter.status,
      page: page.value,
      pageSize: pageSize.value,
    })
    pageData.value = data.list
    total.value = data.total
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  Object.assign(filter, filterDraft)
  page.value = 1
  fetchList()
}
function resetFilter() {
  filterDraft.user = ''
  filterDraft.dateRange = null
  filterDraft.status = null
  Object.assign(filter, filterDraft)
  page.value = 1
  fetchList()
}
onMounted(fetchList)

// ---------- 详情抽屉（消息为敏感数据，打开时单独拉取） ----------
const drawerVisible = ref(false)
const current = ref(null)
const detailLoading = ref(false)
async function view(row) {
  current.value = { ...row, messages: [] }
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const detail = await adminConsultDetail(row.id)
    current.value = detail
  } catch (e) {
    ElMessage.error(e.message || '获取详情失败')
    drawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}
.panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
}
.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
.user-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}
.user-id {
  font-size: 12px;
  color: #909399;
}
.score {
  font-weight: 700;
}
/* 抽屉对话气泡 */
.detail-meta {
  font-size: 12px;
  color: #909399;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.chat {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.chat-row {
  max-width: 78%;
}
.chat-user {
  align-self: flex-start;
}
.chat-ai {
  align-self: flex-end;
  text-align: right;
}
.bubble {
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.6;
  text-align: left;
  word-break: break-all;
}
.bubble-user {
  background: #f4f4f5;
  color: #303133;
  border-top-left-radius: 2px;
}
.bubble-ai {
  background: #ecf5ff;
  color: #3a5f81;
  border-top-right-radius: 2px;
}
.chat-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}
.detail-tip {
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
