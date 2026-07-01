<template>
  <div>
    <div class="header">
      <div style="display: flex; align-items: center">
        <img style="height: 40px;margin-left: 10px" src="../assets/img/logo.png" alt="">
        <span class="system-title">信息管理系统</span>
      </div>
      <div style="flex: 1"></div>

      <div style="display: flex; align-items: center">
        <el-dropdown>
          <div class="user-info">
            <img v-if="data.user?.avatar" class="avatar" :src="data.user?.avatar" alt="">
            <img v-else class="avatar" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="">
            <span class="user-name">{{data.user?.name}}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="main-container">
      <div class="sidebar">
        <el-menu router class="menu"
                 default-active="/manager/home">
          <el-menu-item index="/manager/home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-sub-menu index="1">
            <template #title>
              <el-icon><location /></el-icon>
              <span>人员信息管理</span>
            </template>
            <el-menu-item v-if="data.user.role === 'ADMIN'" index="/manager/admin">管理员信息</el-menu-item>
            <el-menu-item index="/manager/user">普通用户信息</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="3" v-if="data.user.role === 'USER'">
            <template #title>
              <el-icon><EditPen /></el-icon>
              <span>提交申请</span>
            </template>
            <el-menu-item index="/manager/leaveRequest">请假申请</el-menu-item>
          </el-sub-menu>
          <el-menu-item v-if="data.user.role === 'ADMIN'" index="/manager/leaveRequest">
            <el-icon><Tickets /></el-icon>
            <span>请假申请</span>
          </el-menu-item>
          <el-menu-item index="/manager/announcement">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="content">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<script setup>
import router from "@/router/index.js"
import {reactive} from "vue";
import { House, Location, User, DataAnalysis, EditPen, Tickets, Bell } from "@element-plus/icons-vue";

const logout = () => {
  localStorage.removeItem("code_user");
  router.push("/");
}

const getUserInfo = () => {
  return JSON.parse(localStorage.getItem("code_user")) || {}
}

const data = reactive({
  user: getUserInfo()
})

window.addEventListener('storage', () => {
  data.user = getUserInfo()
})
</script>

<style scoped>.header {
  height: 60px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  position: relative;
  z-index: 1000;
}

.system-title {
  font-size: 22px;
  padding-left: 12px;
  color: white;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.02);
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-name {
  font-size: 14px;
  color: white;
  margin-left: 8px;
  font-weight: 500;
}

.main-container {
  display: flex;
  min-height: calc(100vh - 60px);
}

.sidebar {
  width: 220px;
  background: white;
  border-right: none;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.05);
  z-index: 999;
}

.menu {
  min-height: calc(100vh - 60px);
  border-right: none;
}

.menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  transition: all 0.3s ease;
}

.menu .el-menu-item:hover {
  background-color: #ecf5ff;
}

.menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #ecf5ff 0%, transparent 100%);
  color: #409EFF;
  border-right: 3px solid #409EFF;
}

.content {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
}
</style>
