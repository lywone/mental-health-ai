<template>
  <!-- FR-G01 整体布局骨架：导航头固定顶部 / 左侧菜单固定 / 右侧内容区独立滚动 -->
  <el-container class="layout">
    <el-header class="layout-header">
      <Navbar />
    </el-header>
    <el-container class="layout-body">
      <el-aside width="200px" class="layout-aside">
        <Sidebar />
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Navbar from './Navbar.vue'
import Sidebar from './Sidebar.vue'
</script>

<style scoped>
.layout {
  height: 100vh;
}
.layout-header {
  height: 60px;
  flex: none;
  padding: 0;
  background: #fff;
}
.layout-body {
  min-height: 0;
}
.layout-aside {
  flex: none;
  background: #fbfcfd;
  border-right: 1px solid #e4e7ed;
}
/* FR-G05 滚动条仅出现在内容区右缘 */
.layout-main {
  overflow-y: auto;
  background: #f5f7fa;
  padding: 16px 20px;
}
</style>
