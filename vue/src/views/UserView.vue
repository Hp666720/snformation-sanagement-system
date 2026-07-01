<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input clearable @clear="load" style="width: 260px; margin-right: 5px" v-model="data.username" placeholder="请输入用户名查询"></el-input>
      <el-input clearable @clear="load" style="width: 260px; margin-right: 5px" v-model="data.name" placeholder="请输入名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-button v-if="data.user.role === 'ADMIN'" type="primary" @click="handleAdd">新 增</el-button>
      <el-button v-if="data.user.role === 'ADMIN'" type="danger" @click="deleteBatch">批量删除</el-button>
      <el-button v-if="data.user.role === 'ADMIN'" type="success" @click="handleImportClick">批量导入</el-button>
      <el-button v-if="data.user.role === 'ADMIN'" type="info" @click="exportUser">批量导出</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" @selection-change="handleSelectionChange" style="width: 100%"
                :header-cell-style="{ color: '#333', backgroundColor: '#eaf4ff' }">
        <el-table-column v-if="data.user.role === 'ADMIN'" type="selection" width="55" />
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-image v-if="scope.row.avatar"
                      :src="scope.row.avatar"
                      :preview-src-list="[scope.row.avatar]"
                      :preview-teleported="true"
                      style="width: 40px; height: 40px; border-radius: 50%; cursor: pointer;"
                      fit="cover" />
            <el-avatar v-else :size="40" style="background-color: #409EFF;">
              {{ scope.row.name?.charAt(0) || 'U' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="password" label="密码" show-password />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button v-if="canEdit(scope.row)" type="primary" circle @click="handleEdit(scope.row)">修改</el-button>
            <el-button v-if="data.user.role === 'ADMIN'" type="danger" circle @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[5, 10, 20]"
          :total="data.total"
          @current-change="load"
          @size-change="load"
      />
    </div>

    <el-dialog v-model="data.formVisible" title="新增用户" width="500">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding: 20px 30px 10px 0">
        <el-form-item prop="username" label="账号">
          <el-input
              v-model="data.form.username"
              autocomplete="off"
              :disabled="!!data.form.id"
              @blur="checkUserUsernameUnique"
          />
        </el-form-item>
        <el-form-item prop="name" label="名称">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="data.form.password" placeholder="默认123456" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="phone" label="电话">
          <el-input v-model="data.form.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="data.form.email" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="avatar" label="头像">
          <el-upload
              action="http://localhost:8083/file/upload"
              :headers="{token: data.user?.token || ''}"
              :on-success="handleFileSuccess"
              list-type="picture"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </template>
    </el-dialog>
    <input
        type="file"
        ref="importFileRef"
        @change="handleFileImport"
        accept=".xlsx,.xls"
        style="display: none;"
    />
  </div>
</template>

<script setup>
import {reactive, ref, onMounted} from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/assets/js/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

// 自定义用户用户名验证器（异步）- 必须在data之前定义
const validateUserUsername = async (rule, value, callback) => {
  if (!value) {
    data.userUsernameError = ''
    callback(new Error('请填写账号'))
    return
  }

  if (data.form.id) {
    data.userUsernameError = ''
    callback()
    return
  }

  try {
    const res = await request.get('/user/checkUsername', {
      params: { username: value }
    })

    if (res.code === '200' && res.data.exists) {
      data.userUsernameError = '该账号已被占用，请更换新的账号'
      callback(new Error('该账号已被占用，请更换新的账号'))
    } else {
      data.userUsernameError = ''
      callback()
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
    data.userUsernameError = ''
    callback()
  }
}

const checkUserUsernameUnique = () => {
  if (data.form.username && !data.form.id) {
    formRef.value.validateField('username')
  }
}

const formRef = ref()

// 导入文件引用
const importFileRef = ref()

// 点击批量导入按钮
const handleImportClick = () => {
  importFileRef.value.click()
}

// 处理文件导入
const handleFileImport = (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  request.post('/user/importUser', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success(res.message)
      load()
    } else {
      ElMessage.error(res.message)
    }
  }).catch(err => {
    ElMessage.error('导入失败，请检查文件格式')
    console.error('导入错误:', err)
  })

  event.target.value = ''
}

// 导出用户数据
const exportUser = () => {
  let url = `http://localhost:8083/user/exportUser?username=${data.username === null ? '' : data.username}&name=${data.name === null ? '' : data.name}&ids=${data.ids}`
  window.open(url)
}

const data = reactive({
  user: JSON.parse(localStorage.getItem("code_user")) || {},
  username: null,
  name: null,
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  ids: [],
  role: "USER",
  rules: {
    username: [
      { required: true, message: '请填写账号', trigger: 'blur' },
      { validator: validateUserUsername, trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请填写名称', trigger: 'blur' }
    ],
    password: [
      {required:true, message:'请输入密码', trigger:'blur'}
    ],
    phone: [
      { required: true, message: '请填写电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请填写邮箱', trigger: 'blur' }
    ]
  },
  rows: [],
  userUsernameError: ''
})

onMounted(() => {
  load()
})


const canEdit = (row) => {
  if (data.user.role === 'ADMIN') {
    return true // ADMIN可以编辑所有用户
  }
  return row.id === data.user.id // USER只能编辑自己
}

const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    username: data.username,
    name: data.name,
  }

  // USER角色只能查看自己的数据
  if (data.user.role === 'USER') {
    params.id = data.user.id
  }

  request.get('/user/selectPage', { params }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data.list
      data.total = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const reset = () => {
  data.username = null
  data.name = null
  load()
}

const handleAdd = () => {
  if (data.user.role !== 'ADMIN') {
    ElMessage.warning('您没有权限执行此操作')
    return
  }
  data.formVisible = true
  data.form = {
    password: '123456'
  }
  data.userUsernameError = ''
}

const add = () => {
  request.post("/user/addUser", data.form).then(res => {
    if (res.code === "200") {
      data.formVisible = false
      ElMessage.success(res.message || res.msg || '添加成功')
      load()
    } else {
      ElMessage.error(res.message || res.msg || '添加失败')
    }
  })
}

const update = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/user/update', data.form).then(res => {
        if (res.code === '200') {
          data.formVisible = false
          ElMessage.success('修改成功')

          if (data.form.id === data.user.id) {
            Object.assign(data.user, data.form)
            localStorage.setItem("code_user", JSON.stringify(data.user))
            window.dispatchEvent(new Event('storage'))
          }

          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const del = (id) => {
  if (data.user.role !== 'ADMIN') {
    ElMessage.warning('您没有权限执行此操作')
    return
  }

  ElMessageBox.confirm('删除后无法恢复，您确认删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.post('/user/deleteUserById/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleSelectionChange = (rows) => {
  data.rows = rows
  data.ids = rows.map(item => item.id)
}

const deleteBatch = () => {
  if (data.user.role !== 'ADMIN') {
    ElMessage.warning('您没有权限执行此操作')
    return
  }

  if (data.ids.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }

  ElMessageBox.confirm('删除后无法恢复，您确认删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.post('/user/deleteBatch', data.ids).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleFileSuccess = (res) => {
  data.form.avatar = res.data
}
</script>

<style scoped>.search-card {
  margin-bottom: 16px;
}

.button-group {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

.pagination-card {
  padding-top: 0;
}

.el-table th {
  font-weight: 600;
  background-color: #f5f7fa !important;
}

.el-button--primary,
.el-button--danger,
.el-button--success,
.el-button--info {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.el-button--circle {
  padding: 8px 12px;
}

.el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  background: white;
}

.el-dialog__body {
  padding: 24px;
}

.el-form-item__label {
  font-weight: 500;
  color: #606266;
}

@media (max-width: 768px) {
  .button-group {
    flex-wrap: wrap;
  }

  .el-button {
    margin-bottom: 8px;
  }
}
</style>

