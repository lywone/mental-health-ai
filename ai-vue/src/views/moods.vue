<template>
  <!-- 4.4 情绪日志 /back/moods -->
  <div class="moods-page">
    <!-- 筛选栏 FR-M01 -->
    <div class="panel">
      <el-form inline class="filter-form" @submit.prevent>
        <el-form-item label="用户">
          <el-input v-model="filterDraft.user" placeholder="用户昵称 / ID" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="记录日期">
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
        <el-form-item label="评分区间">
          <el-select v-model="filterDraft.scoreRange" placeholder="全部评分" clearable style="width: 140px">
            <el-option label="低（0-3）" value="low" />
            <el-option label="中（4-6）" value="mid" />
            <el-option label="高（7-10）" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志列表 -->
    <div class="panel mt16">
      <el-table :data="pageData" v-loading="loading" stripe>
        <el-table-column label="用户" width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <span>{{ row.userNickname }}</span>
              <span class="user-id">#{{ row.userId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="记录时间" width="160">
          <template #default="{ row }">{{ fmtDateTime(row.time) }}</template>
        </el-table-column>
        <el-table-column label="情绪评分" width="100">
          <template #default="{ row }">
            <span class="score-badge" :style="{ background: scoreColor(row.score) }">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="情绪标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="t in row.tags" :key="t" size="small" class="mood-tag" disable-transitions>{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容摘要" min-width="240">
          <template #default="{ row }">{{ row.content.slice(0, 30) }}</template>
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

    <!-- 详情抽屉 FR-M02（复用图 6 样式） -->
    <el-drawer v-model="drawerVisible" title="情绪日志详情" size="420px">
      <template v-if="current">
        <div class="detail-meta">
          {{ current.userNickname }}（#{{ current.userId }}） · {{ fmtDateTime(current.time) }}
        </div>
        <div class="detail-tags">
          <el-tag v-for="t in current.tags" :key="t" disable-transitions>{{ t }}</el-tag>
        </div>
        <div class="detail-content">{{ current.content }}</div>
        <div class="detail-score">
          情绪评分 <span class="score-badge" :style="{ background: scoreColor(current.score) }">{{ current.score }}</span>
          <span class="detail-score-range">/ 10</span>
        </div>
        <div class="detail-tip">仅管理员可查看 · 内容属敏感数据，请勿外传</div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
// FR-M01 列表与筛选 / FR-M02 详情查看（仅查看，Q6 敏感操作边界；服务端分页 + 筛选）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminMoodPage, adminMoodDetail } from '@/api'
import { fmtDateTime, scoreColor } from '@/utils/format'

// ---------- 列表（服务端分页 + 筛选） ----------
const pageData = ref([])
const total = ref(0)
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)

const filterDraft = reactive({ user: '', dateRange: null, scoreRange: null })
const filter = reactive({ user: '', dateRange: null, scoreRange: null })

async function fetchList() {
  loading.value = true
  try {
    const [start, end] = filter.dateRange || []
    const data = await adminMoodPage({
      user: filter.user,
      startDate: start || '',
      endDate: end || '',
      scoreRange: filter.scoreRange,
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
  filterDraft.scoreRange = null
  Object.assign(filter, filterDraft)
  page.value = 1
  fetchList()
}
onMounted(fetchList)

// ---------- 详情抽屉（敏感数据，打开时单独拉取） ----------
const drawerVisible = ref(false)
const current = ref(null)
async function view(row) {
  try {
    current.value = await adminMoodDetail(row.id)
    drawerVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '获取详情失败')
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
/* R6 评分徽标：0-3 红 / 4-6 橙 / 7-10 绿 */
.score-badge {
  display: inline-block;
  min-width: 26px;
  padding: 2px 8px;
  border-radius: 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}
.mood-tag {
  margin-right: 6px;
}
/* 抽屉详情 */
.detail-meta {
  font-size: 12px;
  color: #909399;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.detail-tags {
  margin-bottom: 12px;
}
.detail-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 14px;
  white-space: pre-wrap;
}
.detail-score {
  margin-top: 16px;
  font-size: 13px;
  color: #606266;
}
.detail-score-range {
  color: #909399;
  margin-left: 4px;
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
