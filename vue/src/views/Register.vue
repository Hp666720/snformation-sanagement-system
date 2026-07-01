<template>
  <div class="bg">
    <div class="register-container">
      <div class="register-header">
        <img src="../assets/img/logo.png" alt="logo" class="logo">
        <h2>信息管理系统</h2>
      </div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" class="register-form">
        <div class="register-title">欢迎注册</div>
        <el-form-item prop="username">
          <el-input
              size="large"
              v-model="data.form.username"
              autocomplete="off"
              prefix-icon="User"
              placeholder="请输入账号"
              @blur="checkUsernameUnique"
          />
        </el-form-item>
        <el-form-item prop="name">
          <el-input size="large" v-model="data.form.name" autocomplete="off" prefix-icon="User" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="large" show-password v-model="data.form.password" autocomplete="off" prefix-icon="Lock" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input size="large" show-password v-model="data.form.confirmPassword" autocomplete="off" prefix-icon="Lock" placeholder="请再次确认密码" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input size="large" v-model="data.form.phone" autocomplete="off" prefix-icon="Phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input size="large" v-model="data.form.email" autocomplete="off" prefix-icon="Message" placeholder="请输入邮箱" />
        </el-form-item>
        <div class="register-btn-wrapper">
          <el-button class="register-btn" size="large" type="primary" @click="register">注 册</el-button>
        </div>
        <div class="login-link">
          已有账号？请 <a href="/login">登录</a>
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

const validatePass = (rule, value, callback) => {
  // value 表示用户输入的确认密码
  if (value !== data.form.password) {
    callback(new Error("两次输入的密码不匹配！"))
  } else {
    callback()
  }
}

// 自定义用户名验证器（异步）
const validateUsername = async (rule, value, callback) => {
  if (!value) {
    data.usernameError = ''
    callback(new Error('请输入账号'))
    return
  }

  if (value.length < 3) {
    data.usernameError = ''
    callback(new Error('账号最少3位'))
    return
  }

  // 调用后端接口检查用户名是否已存在
  try {
    const res = await request.get('/user/checkUsername', {
      params: { username: value }
    })

    if (res.code === '200' && res.data.exists) {
      data.usernameError = '该账号已被占用，请更换新的账号'
      callback(new Error('该账号已被占用，请更换新的账号'))
    } else {
      data.usernameError = ''
      callback()
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
    data.usernameError = ''
    callback()
  }
}


// 手动触发用户名唯一性检查
const checkUsernameUnique = () => {
  if (data.form.username) {
    formRef.value.validateField('username')
  }
}


const formRef = ref()
const data = reactive({
  form: {
    role: 'USER'
  },
  usernameError: '', // 存储用户名重复错误信息
  rules: {
    username: [
      { validator: validateUsername, trigger: 'blur' },
    ],
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次确认密码', trigger: 'blur' },
      { validator: validatePass, trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ]
  }
})

const register = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/user/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功')
          router.push('/')
        } else {
          ElMessage.error(res.message)
        }
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
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
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

.register-container {
  width: 480px;
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

.register-header {
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

.register-header h2 {
  margin: 0;
  font-size: 28px;
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.register-form {
  margin-top: 20px;
}

.register-title {
  margin-bottom: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 24px;
  color: #333;
}

.register-btn-wrapper {
  margin-bottom: 20px;
}

.register-btn {
  width: 100%;
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s ease;
}

.register-btn:hover {
  background: linear-gradient(135deg, #233140 0%, #2980b9 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(44, 62, 80, 0.4);
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #2c3e50;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.login-link a:hover {
  color: #3498db;
  text-decoration: underline;
}

.el-input__wrapper {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-input__wrapper:hover {
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.15);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.25);
}

@media (max-width: 768px) {
  .register-container {
    width: 90%;
    padding: 30px 20px;
  }

  .register-header h2 {
    font-size: 24px;
  }

  .register-title {
    font-size: 20px;
  }
}
</style>