<template>
  <!-- 4.2 知识文章 /back/articles -->
  <div class="articles-page">
    <!-- 筛选栏 FR-A01 -->
    <div class="panel">
      <el-form inline class="filter-form" @submit.prevent>
        <el-form-item label="标题">
          <el-input v-model="filterDraft.keyword" placeholder="关键词搜索（标题）" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filterDraft.category" placeholder="全部分类" clearable style="width: 140px">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterDraft.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option v-for="s in STATUSES" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
        <el-form-item class="filter-right">
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增文章</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表 -->
    <div class="panel mt16">
      <el-table :data="pageData" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" disable-transitions>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="90" />
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ fmtDateTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === '已发布'" link type="primary" @click="togglePublish(row)">下架</el-button>
            <el-button v-else link type="primary" @click="togglePublish(row)">发布</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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

    <!-- 新增 / 编辑弹窗 FR-A02（图 4） -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑文章' : '新增文章'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="不超过 50 字" />
        </el-form-item>
        <el-form-item label="文章分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 200px">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
            class="cover-upload"
            list-type="picture-card"
            :auto-upload="false"
            :limit="1"
            :file-list="coverList"
            :on-change="onCoverChange"
            :on-remove="() => (coverList = [])"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="100" show-word-limit placeholder="不填时展示正文前 50 字" />
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入正文（纯文本，Q5 待确认编辑形式）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// FR-A01 列表与筛选 / FR-A02 新增编辑 / FR-A03 发布下架（R5）/ FR-A04 删除（服务端分页）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  adminArticlePage,
  adminArticleDetail,
  adminArticleCreate,
  adminArticleUpdate,
  adminArticleDelete,
  adminArticleStatus,
  uploadImage,
} from '@/api'
import { fmtDateTime } from '@/utils/format'

const CATEGORIES = ['心理科普', '情绪管理', '睡眠健康', '人际关系']
const STATUSES = ['草稿', '已发布', '已下架']

// ---------- 列表（服务端分页 + 筛选） ----------
const pageData = ref([])
const total = ref(0)
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)

const filterDraft = reactive({ keyword: '', category: null, status: null })
const filter = reactive({ keyword: '', category: null, status: null })

async function fetchList() {
  loading.value = true
  try {
    const data = await adminArticlePage({
      keyword: filter.keyword,
      category: filter.category,
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
  filterDraft.keyword = ''
  filterDraft.category = null
  filterDraft.status = null
  Object.assign(filter, filterDraft)
  page.value = 1
  fetchList()
}
onMounted(fetchList)

function statusType(status) {
  return { 草稿: 'warning', 已发布: 'success', 已下架: 'info' }[status] || 'info'
}

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const coverList = ref([])
const coverUploading = ref(false)
const form = reactive({ title: '', category: null, cover: '', summary: '', content: '' })
const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 50, message: '标题不超过 50 字', trigger: 'blur' },
  ],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }],
}

async function openDialog(row) {
  isEdit.value = !!row
  editingId.value = row?.id ?? null
  if (row) {
    // 列表不含正文，编辑前拉取详情
    try {
      const detail = await adminArticleDetail(row.id)
      Object.assign(form, {
        title: detail.title,
        category: detail.category,
        cover: detail.cover || '',
        summary: detail.summary || '',
        content: detail.content || '',
      })
    } catch (e) {
      ElMessage.error(e.message || '获取详情失败')
      return
    }
  } else {
    Object.assign(form, { title: '', category: null, cover: '', summary: '', content: '' })
  }
  coverList.value = form.cover ? [{ name: 'cover', url: form.cover }] : []
  dialogVisible.value = true
}

async function onCoverChange(file) {
  if (coverUploading.value) return
  coverUploading.value = true
  try {
    const data = await uploadImage(file.raw)
    form.cover = data.url
    coverList.value = [{ name: file.name, url: form.cover }]
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
    coverList.value = []
  } finally {
    coverUploading.value = false
  }
}

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = {
    title: form.title,
    category: form.category,
    cover: form.cover || '',
    summary: form.summary || '',
    content: form.content,
  }
  try {
    if (isEdit.value) {
      await adminArticleUpdate(editingId.value, payload)
      ElMessage.success('保存成功')
    } else {
      await adminArticleCreate(payload) // 后端固定新增为草稿
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

// ---------- FR-A03 发布 / 下架（R5 状态流转） ----------
async function togglePublish(row) {
  const action = row.status === '已发布' ? 'offline' : 'publish'
  try {
    await adminArticleStatus(row.id, action)
    ElMessage.success(action === 'offline' ? `《${row.title}》已下架` : `《${row.title}》已发布`)
    fetchList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

// ---------- FR-A04 删除（二次确认） ----------
function remove(row) {
  ElMessageBox.confirm(`确认删除文章《${row.title}》吗？删除后不可恢复。`, '删除文章', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await adminArticleDelete(row.id)
        ElMessage.success('删除成功')
        fetchList()
      } catch (e) {
        ElMessage.error(e.message || '删除失败')
      }
    })
    .catch(() => {})
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
.filter-right {
  margin-left: auto;
}
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
.cover-upload :deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
}
.cover-upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
}
</style>
