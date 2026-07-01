<template>
  <div class="bg">
    <div class="login-container">
      <div class="login-header">
        <img src="../assets/img/logo.png" alt="logo" class="logo">
        <h2>信息管理系统</h2>
      </div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" class="login-form">
        <div class="login-title">欢迎登录</div>
        <el-form-item prop="username">
          <el-input size="large" v-model="data.form.username" autocomplete="off" prefix-icon="User" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="large" show-password v-model="data.form.password" autocomplete="off" prefix-icon="Lock" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item prop="role">
          <el-select size="large" style="width: 100%" v-model="data.form.role">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="普通用户" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <div class="login-btn-wrapper">
          <el-button class="login-btn" size="large" type="primary" @click="login">登 录</el-button>
        </div>
        <div class="register-link">
          还没有账号？请 <a href="/register">注册</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/assets/js/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const formRef = ref()
const data = reactive({
  form: { role: 'ADMIN' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 3, message: '账号最少3位', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
  }
})

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const url = data.form.role === 'ADMIN' ? '/admin/login' : '/user/login'
      request.post(url, data.form).then(res => {
        if (res.code === '200') {
          console.log("登录成功返回数据：", res.data)
          localStorage.setItem("code_user", JSON.stringify(res.data))
          router.push('/manager/home')
        } else {
          ElMessage.error(res.message)
        }
      }).catch(err => {
        console.error("登录请求失败：", err)
        ElMessage.error("登录失败，请检查网络或账号密码")
      })
    }
  })
}
</script>

<style scoped>
.bg {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("@/assets/img/dlbj.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0.1;
}

.login-container {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.logo {
  width: 50px;
  height: 50px;
  margin-right: 15px;
}

.login-header h2 {
  margin: 0;
  font-size: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.login-form {
  margin-top: 20px;
}

.login-title {
  margin-bottom: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 24px;
  color: #333;
}

.login-btn-wrapper {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.register-link a:hover {
  color: #764ba2;
  text-decoration: underline;
}

.el-input__wrapper {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-input__wrapper:hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.el-select .el-input__wrapper {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
