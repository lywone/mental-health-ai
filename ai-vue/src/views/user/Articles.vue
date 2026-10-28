<template>
  <!-- 用户端 4.4 知识库 / FR-K01 列表筛选 + FR-K02 详情（推断设计，仅「已发布」） -->
  <div class="uarticles-page">
    <!-- 分类 chip 筛选 -->
    <div class="chip-bar">
      <span
        v-for="c in categories"
        :key="c"
        class="chip"
        :class="{ active: currentCategory === c }"
        @click="switchCategory(c)"
      >
        {{ c }}
      </span>
    </div>

    <!-- 卡片网格 -->
    <el-row v-loading="loading" :gutter="16">
      <el-col v-for="a in pageData" :key="a.id" :xs="24" :sm="12" :md="8" :lg="8" class="card-col">
        <div class="a-card" @click="openDetail(a)">
          <div class="a-cover" :style="{ background: coverBg(a.category) }">
            <svg viewBox="0 0 36 36" aria-hidden="true">
              <rect x="9" y="13" width="18" height="13" rx="4" fill="rgba(255,255,255,.85)" />
              <circle cx="14.5" cy="19.5" r="1.8" fill="rgba(0,0,0,.25)" />
              <circle cx="21.5" cy="19.5" r="1.8" fill="rgba(0,0,0,.25)" />
            </svg>
          </div>
          <div class="a-body">
            <div class="a-meta-top">
              <span class="a-cat" :style="{ color: coverFg(a.category) }">{{ a.category }}</span>
              <span class="a-views">{{ a.views }} 次浏览</span>
            </div>
            <div class="a-title">{{ a.title }}</div>
            <div class="a-summary">{{ a.summary }}</div>
            <div class="a-time">{{ fmtDate(a.publishTime) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !pageData.length" description="该分类暂无已发布文章" />

    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="pageSize"
      layout="prev, pager, next"
      class="pager"
      @current-change="fetchList"
      v-if="total > pageSize"
    />

    <!-- FR-K02 详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="current?.title" size="480px" class="a-drawer">
      <template v-if="current">
        <div class="d-meta">
          <span class="a-cat" :style="{ color: coverFg(current.category) }">{{ current.category }}</span>
          <span class="d-sub">{{ fmtDateTime(current.publishTime) }} · {{ current.views }} 次浏览</span>
        </div>
        <div v-if="current.cover" class="d-cover"><img :src="current.cover" alt="封面" /></div>
        <div class="d-content">{{ current.content }}</div>
        <div class="d-tip">内容仅供参考，如有需要请寻求专业心理帮助</div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
// FR-K01 分类筛选 + 卡片网格（服务端分页） · FR-K02 详情抽屉（浏览量由后端自增）
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userArticlePage, userArticleDetail } from '@/api'
import { fmtDate, fmtDateTime } from '@/utils/format'

const categories = ['全部', '心理科普', '情绪管理', '睡眠健康', '人际关系']

// 仅展示「已发布」，按发布时间倒序（后端口径）
const currentCategory = ref('全部')
const pageData = ref([])
const total = ref(0)
const loading = ref(true)
const page = ref(1)
const pageSize = 6

async function fetchList() {
  loading.value = true
  try {
    const data = await userArticlePage({
      category: currentCategory.value === '全部' ? '' : currentCategory.value,
      page: page.value,
      pageSize,
    })
    pageData.value = data.list
    total.value = data.total
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}
function switchCategory(c) {
  currentCategory.value = c
  page.value = 1
  fetchList()
}
onMounted(fetchList)

// 封面占位：按分类取柔和底色
const CAT_COLORS = {
  心理科普: ['#e8f0ec', '#67a98c'],
  情绪管理: ['#fdf3e7', '#e6a23c'],
  睡眠健康: ['#f3ecfa', '#8e5cd9'],
  人际关系: ['#ecf3fb', '#5c88d9'],
}
const coverBg = (c) => {
  const pair = CAT_COLORS[c] || ['#eef1f6', '#909399']
  return `linear-gradient(135deg, ${pair[0]}, ${pair[1]}44)`
}
const coverFg = (c) => (CAT_COLORS[c] || ['#', '#909399'])[1]

// ---------- 详情抽屉（详情接口浏览量原子自增，返回后回写列表） ----------
const drawerVisible = ref(false)
const current = ref(null)
async function openDetail(a) {
  try {
    const detail = await userArticleDetail(a.id)
    current.value = detail
    a.views = detail.views // 同步列表中的浏览量
    drawerVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '获取文章失败')
  }
}
</script>

<style scoped>
.uarticles-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px 32px;
}
.chip-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.chip {
  padding: 5px 18px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #dcdfe6;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.15s;
}
.chip:hover {
  color: #409eff;
  border-color: #b3d8ff;
}
.chip.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}
.card-col {
  margin-bottom: 16px;
}
.a-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  height: 100%;
  transition: box-shadow 0.2s, transform 0.2s;
}
.a-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.a-cover {
  height: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.a-cover svg {
  width: 44px;
  opacity: 0.9;
}
.a-body {
  padding: 12px 14px 14px;
}
.a-meta-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.a-cat {
  font-size: 12px;
  font-weight: 600;
}
.a-views {
  font-size: 11.5px;
  color: #c0c4cc;
}
.a-title {
  font-size: 14.5px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.a-summary {
  margin-top: 6px;
  font-size: 12.5px;
  color: #909399;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.a-time {
  margin-top: 8px;
  font-size: 11.5px;
  color: #c0c4cc;
}
.pager {
  margin-top: 6px;
  justify-content: center;
}
/* 详情抽屉 */
.d-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 14px;
}
.d-sub {
  font-size: 12px;
  color: #909399;
}
.d-cover img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 14px;
}
.d-content {
  font-size: 14px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
}
.d-tip {
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
