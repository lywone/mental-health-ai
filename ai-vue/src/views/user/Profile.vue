<template>
  <!-- 用户端个人中心（FR-U05）：基本信息配置，仅本人可改 -->
  <div class="uprofile-page">
    <div v-loading="loading" class="profile-card">
      <div class="card-title">个人中心</div>

      <div class="avatar-row">
        <el-upload
          :show-file-list="false"
          accept="image/jpeg,image/png,image/webp"
          :before-upload="beforeAvatar"
          :http-request="doUpload"
        >
          <div class="avatar-wrap">
            <img v-if="form.avatar" :src="form.avatar" alt="头像" />
            <span v-else class="avatar-placeholder">{{ avatarText }}</span>
            <span class="avatar-mask">更换头像</span>
          </div>
        </el-upload>
        <div class="avatar-tip">支持 jpg/png/webp，不超过 5MB</div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="72px" class="profile-form">
        <el-form-item label="手机号">
          <el-input :model-value="form.phone" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="20" show-word-limit placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择生日（选填）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="介绍一下自己（选填）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
// FR-U05 用户基本信息配置：昵称/头像/性别/生日/简介；手机号只读（脱敏展示）
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myProfile, updateProfile, uploadAvatar } from '@/api'
import { updateUserProfile } from '@/auth'

const loading = ref(true)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ phone: '', nickname: '', avatar: '', gender: 0, birthday: '', bio: '' })

const avatarText = computed(() => (form.nickname || '用').slice(0, 1))

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

onMounted(async () => {
  try {
    const p = await myProfile()
    Object.assign(form, {
      phone: p.phone || '',
      nickname: p.nickname || '',
      avatar: p.avatar || '',
      gender: p.gender ?? 0,
      birthday: p.birthday || '',
      bio: p.bio || '',
    })
  } catch (e) {
    ElMessage.error(e.message || '加载个人信息失败')
  } finally {
    loading.value = false
  }
})

function beforeAvatar(file) {
  const okType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  if (!okType) {
    ElMessage.error('仅支持 jpg/png/webp 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片不能超过 5MB')
    return false
  }
  return true
}

async function doUpload({ file }) {
  try {
    const res = await uploadAvatar(file)
    form.avatar = res.url
    ElMessage.success('头像已上传，点击保存修改生效')
  } catch (e) {
    ElMessage.error(e.message || '头像上传失败')
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const p = await updateProfile({
      nickname: form.nickname,
      avatar: form.avatar || null,
      gender: form.gender,
      birthday: form.birthday || null,
      bio: form.bio,
    })
    // 同步本地登录态，UserLayout 顶部昵称/头像即时刷新
    updateUserProfile({ nickname: p.nickname, avatar: p.avatar })
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.uprofile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
.profile-card {
  max-width: 560px;
  margin: 0 auto;
  padding: 28px 32px 32px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}
.avatar-tip {
  font-size: 12px;
  color: #c0c4cc;
}
.avatar-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  background: #f5f7fa;
}
.avatar-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  background: linear-gradient(135deg, #f8b26a, #f08c3e);
}
.avatar-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 3px 0;
  font-size: 11px;
  color: #fff;
  text-align: center;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.15s;
}
.avatar-wrap:hover .avatar-mask {
  opacity: 1;
}
.profile-form {
  max-width: 400px;
}
</style>
