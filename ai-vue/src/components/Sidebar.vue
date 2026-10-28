<template>
  <!-- FR-G03 左侧菜单：固定，图标 + 菜单名，当前路由高亮 -->
  <el-menu :default-active="activePath" router class="side-menu">
    <el-menu-item index="/back/dashboard">
      <el-icon><PieChart /></el-icon>
      <span>数据分析</span>
    </el-menu-item>
    <el-menu-item index="/back/articles">
      <el-icon><Document /></el-icon>
      <span>知识文章</span>
    </el-menu-item>
    <el-menu-item index="/back/consultations">
      <el-icon><ChatDotRound /></el-icon>
      <span>咨询记录</span>
    </el-menu-item>
    <el-menu-item index="/back/moods">
      <el-icon><Sunny /></el-icon>
      <span>情绪日志</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { PieChart, Document, ChatDotRound, Sunny } from '@element-plus/icons-vue'

const route = useRoute()
const activePath = computed(() => route.path)
</script>

<style scoped>
.side-menu {
  height: 100%;
  border-right: none;
  background: transparent;
}
</style>
