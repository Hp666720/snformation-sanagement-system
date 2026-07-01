<template>
  <div>
    <div class="card" style="margin-bottom: 5px" v-if="data.user.role === 'ADMIN'">
      <el-button type="primary" @click="handleAdd">新 增</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" style="width: 100%"
                :header-cell-style="{ color: '#333', backgroundColor: '#eaf4ff' }">
        <el-table-column prop="title" label="公告标题" />
        <el-table-column prop="content" label="公告内容" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看详情</el-button>
            <el-button v-if="data.user.role === 'ADMIN'" type="success" size="small" @click="handleEdit(scope.row)">编 辑</el-button>
            <el-button v-if="data.user.role === 'ADMIN'" :type="scope.row.status === '已发布' ? 'warning' : 'primary'" size="small" @click="toggleStatus(scope.row)">
              {{ scope.row.status === '已发布' ? '撤 回' : '发 布' }}
            </el-button>
            <el-button v-if="data.user.role === 'ADMIN'" type="danger" size="small" @click="del(scope.row.id)">删 除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="发布公告" v-model="data.formVisible" width="50%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding: 20px 30px 10px 0">
        <el-form-item prop="title" label="标题">
          <el-input v-model="data.form.title" placeholder="请输入标题" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="content" label="正文内容">
          <el-input v-model="data.form.content" type="textarea" :rows="8" placeholder="请输入正文内容" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog title="公告详情" v-model="data.viewVisible" width="50%">
      <div style="padding: 0 20px;">
        <h2 style="text-align: center; margin-bottom: 20px;">{{ data.viewData.title }}</h2>
        <div style="color: #666; margin-bottom: 15px; font-size: 14px;">
          <span>发布人：{{ data.viewData.publisherName }}</span>
          <span style="margin-left: 30px;">发布时间：{{ formatDateTime(data.viewData.publishTime) }}</span>
        </div>
        <el-divider></el-divider>
        <div style="line-height: 1.8; min-height: 200px;">{{ data.viewData.content || '暂无内容' }}</div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.viewVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import request from "@/assets/js/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem("code_user")) || {},
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  viewVisible: false,
  viewData: {},
  form: {
    publisherId: null,
    publisherName: '',
    status: '已发布'
  },
  rules: {
    title: [
      { required: true, message: '请填写公告标题', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请填写正文内容', trigger: 'blur' }
    ]
  }
})

onMounted(() => {
  load()
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const load = () => {
  request.get('/announcement/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data.list
      data.total = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleAdd = () => {
  if (data.user.role !== 'ADMIN') {
    ElMessage.warning('只有管理员可以发布公告')
    return
  }
  data.formVisible = true
  data.form = {
    publisherId: data.user.id,
    publisherName: data.user.name,
    status: '已发布'
  }
}

const add = () => {
  request.post("/announcement/add", data.form).then(res => {
    if (res.code === "200") {
      data.formVisible = false
      ElMessage.success(res.msg)
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/announcement/update', data.form).then(res => {
        if (res.code === '200') {
          data.formVisible = false
          ElMessage.success('修改成功')
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

const handleView = (row) => {
  data.viewData = row
  data.viewVisible = true
}

const toggleStatus = (row) => {
  const newStatus = row.status === '已发布' ? '已撤回' : '已发布'
  const action = newStatus === '已发布' ? '发布' : '撤回'

  ElMessageBox.confirm(`确认${action}该公告吗？`, `${action}确认`, { type: 'warning' }).then(() => {
    request.post('/announcement/update', {
      id: row.id,
      status: newStatus
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success(`${action}成功`)
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const del = (id) => {
  ElMessageBox.confirm('删除后无法恢复，您确认删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.post('/announcement/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}
</script>
