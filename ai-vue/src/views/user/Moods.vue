<template>
  <!-- 用户端 4.3 情绪日志 / FR-M01 记录心情 + FR-M02 我的记录（推断设计） -->
  <div class="umoods-page">
    <!-- FR-M01 记录今天的心情 -->
    <div class="panel">
      <div class="panel-title">记录今天的心情</div>
      <div class="score-row">
        <span class="field-label">情绪评分</span>
        <div class="score-dots">
          <span
            v-for="n in 10"
            :key="n"
            class="dot"
            :style="{ background: n <= (form.score || 0) ? scoreColor(n) : '#f0f2f5' }"
            @click="form.score = n"
          />
        </div>
        <span class="score-readout" v-if="form.score">已选 {{ form.score }} 分 · {{ scoreLevel(form.score) === 'low' ? '状态不佳' : scoreLevel(form.score) === 'mid' ? '一般' : '不错' }}</span>
        <span class="score-readout placeholder" v-else>请选择今日评分（1-10）</span>
      </div>
      <div class="tag-row">
        <span class="field-label">情绪标签</span>
        <div class="tag-list">
          <span
            v-for="t in MOOD_TAGS"
            :key="t"
            class="tag-chip"
            :class="{ active: form.tags.includes(t) }"
            @click="toggleTag(t)"
          >
            {{ t }}
          </span>
        </div>
        <span class="field-hint">至多选 2 个</span>
      </div>
      <div class="content-row">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="2"
          resize="none"
          maxlength="200"
          show-word-limit
          placeholder="写点什么记录此刻的心情…（可选）"
        />
        <el-button type="primary" class="btn-save" :disabled="!form.score" @click="save">保存记录</el-button>
      </div>
    </div>

    <!-- FR-M02 我的情绪记录 -->
    <div class="panel mt16">
      <div class="panel-title">我的情绪记录</div>
      <el-table :data="myList" v-loading="loading" stripe>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.time) }}</template>
        </el-table-column>
        <el-table-column label="评分" width="90">
          <template #default="{ row }">
            <span class="score-badge" :style="{ background: scoreColor(row.score) }">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="t in row.tags" :key="t" size="small" class="mood-tag" disable-transitions>{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="320">
          <template #default="{ row }">{{ row.content || '—' }}</template>
        </el-table-column>
        <template #empty><el-empty description="还没有情绪记录，先记录第一条吧" /></template>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="fetchList"
        @size-change="() => { page = 1; fetchList() }"
      />
    </div>
  </div>
</template>

<script setup>
// FR-M01 记录心情（评分必填/标签至多2个/文字选填≤200 R6）· FR-M02 我的记录（服务端分页，仅本人）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createMood, myMoodPage } from '@/api'
import { fmtDateTime, scoreColor, scoreLevel } from '@/utils/format'


const MOOD_TAGS = ['开心', '平静', '焦虑', '低落', '愤怒', '疲惫'] // 与后端白名单一致

// ---------- 记录表单 ----------
const form = reactive({ score: 0, tags: [], content: '' })
function toggleTag(t) {
  const i = form.tags.indexOf(t)
  if (i >= 0) form.tags.splice(i, 1)
  else if (form.tags.length < 2) form.tags.push(t)
}

async function save() {
  if (!form.score) return
  try {
    const record = await createMood({ score: form.score, tags: [...form.tags], content: form.content.trim() })
    page.value = 1
    myList.value.unshift(record)
    total.value += 1
    form.score = 0
    form.tags = []
    form.content = ''
    ElMessage.success('记录成功，愿今天的心情被好好安放')
  } catch (e) {
    ElMessage.error(e.message || '记录失败')
  }
}

// ---------- 我的记录列表（服务端分页，时间倒序 R4） ----------
const myList = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)

async function fetchList() {
  loading.value = true
  try {
    const data = await myMoodPage(page.value, pageSize.value)
    myList.value = data.list
    total.value = data.total
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}
onMounted(fetchList)
</script>

<style scoped>
.umoods-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px 32px;
}
.mt16 {
  margin-top: 16px;
}
.panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 18px 20px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 14px;
}
.score-row,
.tag-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.field-label {
  font-size: 13px;
  color: #606266;
  width: 62px;
  flex: none;
}
.field-hint {
  font-size: 12px;
  color: #c0c4cc;
}
/* FR-M01 评分圆点（R1 分档配色） */
.score-dots {
  display: flex;
  gap: 10px;
}
.dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.12s;
}
.dot:hover {
  transform: scale(1.15);
}
.score-readout {
  font-size: 13px;
  color: #606266;
}
.score-readout.placeholder {
  color: #c0c4cc;
}
/* 标签 chip */
.tag-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tag-chip {
  padding: 4px 14px;
  border-radius: 12px;
  border: 1px solid #dcdfe6;
  font-size: 12.5px;
  color: #606266;
  cursor: pointer;
  transition: all 0.15s;
}
.tag-chip:hover {
  color: #409eff;
  border-color: #b3d8ff;
}
.tag-chip.active {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}
.content-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
}
.btn-save {
  flex: none;
}
/* R1 评分徽标 */
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
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
