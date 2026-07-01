<template>
  <div>
    <h3 style="margin-bottom: 20px">个人中心</h3>
    <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="80px" style="padding: 20px 30px 10px 0">
      <el-form-item prop="username" label="账号">
        <el-input v-model="data.user.username" autocomplete="off" :disabled="true" />
      </el-form-item>
      <el-form-item prop="name" label="名称">
        <el-input v-model="data.user.name" autocomplete="off" />
      </el-form-item>
      <el-form-item prop="phone" label="电话">
        <el-input v-model="data.user.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item prop="email" label="邮箱">
        <el-input v-model="data.user.email" autocomplete="off" />
      </el-form-item>
      <el-form-item prop="avatar" label="头像">
        <div style="display: flex; align-items: center; gap: 15px">
          <el-upload
              action="http://localhost:8083/file/upload"
              :headers="{ token: data.user.token }"
              :on-success="handleFileSuccess"
              :show-file-list="false"
              accept="image/*"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>
          <el-avatar v-if="data.user.avatar" :size="80" :src="data.user.avatar" />
          <el-avatar v-else :size="80" style="background-color: #409EFF">
            {{ data.user.name?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
      </el-form-item>
    </el-form>
    <div style="text-align: center; margin-top: 30px">
      <el-button type="primary" style="padding: 18px 35px" @click="update">保 存</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/assets/js/request.js";
import { ElMessage } from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem("code_user") || '{}')
})

const handleFileSuccess = (res) => {
  if (res.code === '200') {
    data.user.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '头像上传失败')
  }
}



const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }

  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('图片大小不能超过 50MB！')
    return false
  }

  return true
}

const emit = defineEmits(['updateUser'])
const update = () => {
  let url = data.user.role === 'ADMIN' ? '/admin/update' : '/user/update'
  console.log('发送更新数据:', data.user)
  request.put(url, data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success('更新成功')
      localStorage.setItem("code_user", JSON.stringify(data.user))
      window.dispatchEvent(new Event('storage'))
      emit('updateUser')
    } else {
      ElMessage.error(res.msg || '更新失败')
    }
  }).catch(err => {
    console.error('更新失败:', err)
    ElMessage.error('更新失败，请检查网络')
  })
}
</script>

<style scoped>
h3 {
  color: #333;
  font-size: 18px;
  margin-bottom: 20px;
}
</style>
