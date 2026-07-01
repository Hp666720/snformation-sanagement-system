<template>
  <div>
    <div class="card search-card">
      <el-input clearable @clear="load" style="width: 260px; margin-right: 8px" v-model="data.username" placeholder="请输入用户名查询"></el-input>
      <el-input clearable @clear="load" style="width: 260px; margin-right: 8px" v-model="data.name" placeholder="请输入姓名查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <div class="card button-group">
      <el-button type="primary" @click="handleAdd">新 增</el-button>
      <el-button type="danger" @click="deleteBatch">批量删除</el-button>
      <el-button type="success" @click="handleImportClick">批量导入</el-button>
      <el-button type="info" @click="exportAdmin">批量导出</el-button>
    </div>

    <div class="card table-card">
      <el-table :data="data.tableData" @selection-change="handleSelect" style="width: 100%"
                :header-cell-style="{ fontWeight: '600', backgroundColor: '#f5f7fa' }">
        <el-table-column type="selection" width="55" />
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-image v-if="scope.row.avatar"
                      :src="scope.row.avatar"
                      :preview-src-list="[scope.row.avatar]"
                      :preview-teleported="true"                      style="width: 40px; height: 40px; border-radius: 50%; cursor: pointer;"
                      fit="cover" />
            <el-avatar v-else :size="40" style="background-color: #409EFF;">
              {{ scope.row.name?.charAt(0) || 'A' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="password" label="密码" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" circle @click="handleEdit(scope.row)">修改</el-button>
            <el-button type="danger" circle @click="del(scope.row.id)">删除</el-button>
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

    <el-dialog v-model="data.formVisible" title="新增管理人员" width="500">
      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <el-form-item label="账号：" prop="username">
          <el-input
              v-model="data.form.username"
              autocomplete="off"
              @blur="checkAdminUsernameUnique"
          />
        </el-form-item>
        <el-form-item label="姓名：" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码：" prop="password">
          <el-input v-model="data.form.password" placeholder="默认123456" autocomplete="off" />
        </el-form-item>
        <el-form-item label="电话：" prop="phone">
          <el-input v-model="data.form.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮箱：" prop="email">
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
import request from "@/assets/js/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

// 自定义管理员用户名验证器（异步）- 必须在data之前定义
const validateAdminUsername = async (rule, value, callback) => {
  if (!value) {
    data.adminUsernameError = ''
    callback(new Error('请输入用户名'))
    return
  }

  if (data.form.id) {
    data.adminUsernameError = ''
    callback()
    return
  }

  try {
    const res = await request.get('/admin/checkUsername', {
      params: { username: value }
    })

    if (res.code === '200' && res.data.exists) {
      data.adminUsernameError = '该账号已被占用，请更换新的账号'
      callback(new Error('该账号已被占用，请更换新的账号'))
    } else {
      data.adminUsernameError = ''
      callback()
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
    data.adminUsernameError = ''
    callback()
  }
}

const checkAdminUsernameUnique = () => {
  if (data.form.username && !data.form.id) {
    formRef.value.validateField('username')
  }
}

const data = reactive({
  username:null,
  name: null,
  pageSize:5,
  pageNum:1,
  total:4,
  tableData:[],
  formVisible:false,
  form:{},
  adminUsernameError: '',
  rows:[],
  ids:[],
  role:"ADMIN",
  rules:{
    username: [
      {required:true, message:'请输入用户名', trigger:'blur'},
      { validator: validateAdminUsername, trigger: 'blur' }
    ],
    name: [
      {required:true, message:'请输入姓名', trigger:'blur'}
    ],
    password: [
      {required:true, message:'请输入密码', trigger:'blur'}
    ],
    phone: [
      {required:true, message:'请输入电话', trigger:'blur'},
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    email: [
      {required:true, message:'请输入用邮箱', trigger:'blur'}
    ]
  }
})

onMounted(() => {
  load()
})

const handleSelect = (rows)=>{
  data.rows = rows
  data.ids = rows.map(item=>item.id)
}

const importFileRef = ref()

const handleImportClick = () => {
  importFileRef.value.click()
}

const handleFileImport = (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  request.post('/admin/importAdmin', formData, {
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

const exportAdmin = () =>{
  const user = JSON.parse(localStorage.getItem("code_user") || '{}')
  const token = user.token || ''
  const idsStr = data.ids && data.ids.length > 0 ? data.ids.join(',') : ''

  let url = `http://localhost:8083/admin/exportAdmin?token=${encodeURIComponent(token)}&username=${data.username || ''}&name=${data.name || ''}&ids=${idsStr}`
  window.open(url)
}

const load = ()=>{
  request.get('/admin/selectAdminPage',{params:{
      pageNum:data.pageNum,
      pageSize:data.pageSize,
      username:data.username,
      name:data.name
    }}).then(res=>{
    if(res.code === '200'){
      data.tableData = res.data.list
      data.total = res.data.total
    }else {
      ElMessage.error(res.msg)
    }
  })
}

const deleteBatch = ()=>{
  if(data.ids.length === 0){
    ElMessage.warning('请选择要删除的数据')
    return
  }
  ElMessageBox.confirm('确定要删除吗？删除后数据永久消失','提示',{type:'warning'}).then(()=>{
    request.delete('/admin/deleteBatch/'+data.ids.join(',')).then(res=>{
      if(res.code === '200'){
        ElMessage.success(res.message)
        load()
      }else {
        ElMessage.error(res.message)
      }
    })
  })
}

const del = (id) =>{
  ElMessageBox.confirm('确定要删除吗？删除后数据永久消失', '提示', {type: 'warning'}).then(() => {
    request.delete('/admin/delete/' + id).then(res=>{
      if(res.code === '200'){
        ElMessage.success(res.message)
        load()
      }else {
        ElMessage.error(res.message)
      }
    })
  })
}

const handleAdd = () =>{
  data.formVisible = true
  data.form = {
    role: 'ADMIN',
    password: '123456'
  }
  data.adminUsernameError = ''
}

const handleEdit = (row) =>{
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const handleFileSuccess = (res) =>{
  data.form.avatar = res.data
}

const formRef = ref()

const add = () =>{
  formRef.value.validate((valid)=>{
    if (valid){
      request.post('/admin/add', data.form).then(res=>{
        if (res.code === '200'){
          ElMessage.success(res.message || res.msg || '添加成功')
          data.formVisible = false
          load()
        }else {
          ElMessage.error(res.message || res.msg || '添加失败')
        }
      }).catch(err => {
        console.error('添加请求失败:', err)
        let errorMsg = '添加失败，请稍后重试'
        if (err.response && err.response.data) {
          errorMsg = err.response.data.msg || err.response.data.message || errorMsg
        }
        ElMessage.error(errorMsg)
      })
    }
  })
}

const update = () =>{
  formRef.value.validate((valid)=>{
    if (valid){
      request.post('/admin/update', data.form).then(res=>{
        if (res.code === '200'){
          ElMessage.success(res.message)
          data.formVisible = false
          load()
        }else {
          ElMessage.error(res.message)
        }
      })
    }
  })
}

const save = () =>{
  data.form.id ? update() : add()
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
