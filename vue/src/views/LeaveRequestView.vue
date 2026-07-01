<template>
  <div>
    <div class="card search-card">
      <el-input clearable @clear="load" style="width: 260px; margin-right: 8px" v-model="data.title" placeholder="标题" :prefix-icon="Search"></el-input>
      <el-button type="primary" @click="load">查 询</el-button>
      <el-button @click="reset">重 置</el-button>
    </div>

    <div class="card action-card" v-if="data.user.role === 'USER'">
      <el-button type="primary" @click="handleAdd">新 增</el-button>
    </div>

    <div class="card table-card">
      <!-- USER角色显示提交表单 -->
      <el-table :data="data.tableData" style="width: 100%"
                :header-cell-style="{ fontWeight: '600', backgroundColor: '#f5f7fa' }">
        <el-table-column prop="title" label="请假标题" />
        <el-table-column prop="description" label="请假说明" show-overflow-tooltip />
        <el-table-column prop="requesterName" label="请假人" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewerName" label="审核人" width="100" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审核说明" show-overflow-tooltip />
        <el-table-column prop="reviewTime" label="审批时间" width="160">
          <template #default="scope">
            {{ scope.row.reviewTime ? formatDateTime(scope.row.reviewTime) : '' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button v-if="canEdit(scope.row)" type="primary" icon="Edit" circle size="small" @click="handleEdit(scope.row)"></el-button>
            <el-button v-if="canDelete(scope.row)" type="danger" icon="Delete" circle size="small" @click="del(scope.row.id)"></el-button>
            <el-button v-if="canReview(scope.row)" type="primary" size="small" @click="handleReview(scope.row)">审 核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card pagination-card">
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


    <!-- 新增/编辑弹窗：用于填写或修改请假申请信息 -->
    <el-dialog title="请假申请" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding: 20px 30px 10px 0">
        <!-- 请假标题输入框 -->
        <el-form-item prop="title" label="请假标题">
          <el-input v-model="data.form.title" placeholder="请输入请假标题" autocomplete="off" />
        </el-form-item>
        <!-- 请假说明文本域 -->
        <el-form-item prop="description" label="请假说明">
          <el-input v-model="data.form.description" type="textarea" :rows="4" placeholder="请输入请假说明" autocomplete="off" />
        </el-form-item>
        <!-- 审批人选择器：支持远程搜索管理员列表 -->
        <el-form-item prop="reviewerName" label="审批人">
          <el-select
              v-model="data.form.reviewerName"
              filterable
              remote
              reserve-keyword
              placeholder="请输入关键词搜索"
              :remote-method="searchAdmins"
              :loading="data.adminLoading"
              @change="handleAdminSelect"
              style="width: 100%">
            <el-option
                v-for="item in data.adminList"
                :key="item.id"
                :label="item.name"
                :value="item.name">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核弹窗：管理员用于审批请假申请 -->
    <el-dialog title="请假申请" v-model="data.reviewVisible" width="40%" destroy-on-close>
      <el-form ref="reviewFormRef" :model="data.reviewForm" :rules="data.reviewRules" label-width="80px" style="padding: 20px 30px 10px 0">
        <!-- 审核状态单选组：待审核选项禁用，只能选择通过或拒绝 -->
        <el-form-item prop="status" label="审核状态">
          <el-radio-group v-model="data.reviewForm.status">
            <el-radio value="待审核" disabled>待审核</el-radio>
            <el-radio value="已审核">审核通过</el-radio>
            <el-radio value="审核拒绝">审核拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 审核说明文本域：仅在拒绝时显示且为必填项 -->
        <el-form-item prop="reviewComment" label="审核说明" v-if="data.reviewForm.status === '审核拒绝'">
          <el-input v-model="data.reviewForm.reviewComment" type="textarea" :rows="4" placeholder="请输入拒绝说明" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.reviewVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitReview">保 存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 请假申请管理页面组件
 * 功能说明：
 *   - 普通用户视角：可提交、查看、编辑、删除自己的请假申请
 *   - 管理员视角：可查看分配给自己的申请并进行审核操作
 * 权限控制：
 *   - 新增按钮仅普通用户可见
 *   - 编辑/删除按钮仅在待审核状态且是自己的申请时显示
 *   - 审核按钮仅管理员可见且待审核状态时可用
 */
import { reactive, ref, onMounted } from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/assets/js/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

/** 表单引用对象，用于表单验证 */
const formRef = ref()
/** 审核表单引用对象 */
const reviewFormRef = ref()

/** 页面响应式数据对象，存储所有状态和数据 */
const data = reactive({
  /** 当前登录用户信息（从localStorage读取） */
  user: JSON.parse(localStorage.getItem("code_user")) || {},
  /** 搜索条件：标题关键字 */
  title: null,
  /** 分页参数：当前页码 */
  pageNum: 1,
  /** 分页参数：每页显示数量 */
  pageSize: 5,
  /** 总记录数（用于分页组件） */
  total: 0,
  /** 表格数据列表 */
  tableData: [],
  /** 控制新增/编辑弹窗的显示状态 */
  formVisible: false,
  /** 控制审核弹窗的显示状态 */
  reviewVisible: false,
  /** 表单数据对象（新增或编辑时使用） */
  form: {
    requesterId: null,
    requesterName: '',
    reviewerId: null,
    reviewerName: ''
  },
  /** 审核表单数据对象 */
  reviewForm: {},
  /** 管理员搜索结果列表（用于审批人选择） */
  adminList: [],
  /** 管理员搜索加载状态 */
  adminLoading: false,
  /** 表单验证规则 */
  rules: {
    title: [
      { required: true, message: '请填写请假标题', trigger: 'blur' }
    ],
    description: [
      { required: true, message: '请填写请假说明', trigger: 'blur' }
    ]
  },
  /** 审核表单验证规则 */
  reviewRules: {
    status: [
      { required: true, message: '请选择审核状态', trigger: 'change' }
    ],
    reviewComment: [
      { required: true, message: '审核拒绝时必须填写说明', trigger: 'blur' }
    ]
  }
})

/** 组件挂载完成后自动加载数据 */
onMounted(() => {
  load()
})

/**
 * 格式化日期时间为易读格式
 * 将数据库返回的时间戳转换为 "YYYY-MM-DD HH:mm:ss" 格式
 *
 * @param {string|Date} dateTime - 需要格式化的时间值
 * @returns {string} 格式化后的时间字符串，空值返回空字符串
 */
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

/**
 * 根据审核状态获取对应的标签类型（用于颜色区分）
 * 待审核-警告色(橙色)、已审核-成功色(绿色)、审核拒绝-危险色(红色)
 *
 * @param {string} status - 审核状态值
 * @returns {string} Element Plus Tag组件的type属性值
 */
const getStatusType = (status) => {
  switch (status) {
    case '待审核': return 'warning'
    case '已审核': return 'success'
    case '审核拒绝': return 'danger'
    default: return 'info'
  }
}

/**
 * 判断当前行数据是否允许编辑
 * 条件：状态为"待审核" 且 当前用户是普通用户 且 是自己提交的申请
 *
 * @param {Object} row - 当前行数据对象
 * @returns {boolean} 是否显示编辑按钮
 */
const canEdit = (row) => {
  return row.status === '待审核' && data.user.role === 'USER' && row.requesterId === data.user.id
}

/**
 * 判断当前行数据是否允许删除
 * 条件：状态为"待审核" 且 当前用户是普通用户 且 是自己提交的申请
 *
 * @param {Object} row - 当前行数据对象
 * @returns {boolean} 是否显示删除按钮
 */
const canDelete = (row) => {
  return row.status === '待审核' && data.user.role === 'USER' && row.requesterId === data.user.id
}

/**
 * 判断当前行数据是否允许审核
 * 条件：状态为"待审核" 且 当前用户是管理员 且 申请分配给自己审核
 *
 * @param {Object} row - 当前行数据对象
 * @returns {boolean} 是否显示审核按钮
 */
const canReview = (row) => {
  return row.status === '待审核' && data.user.role === 'ADMIN' && row.reviewerId === data.user.id
}

/**
 * 加载请假申请列表数据
 * 根据当前用户角色动态构建查询条件：
 *   - 普通用户：只查询自己提交的申请（requesterId）
 *   - 管理员：只查询分配给自己审核的申请（reviewerId）
 */
const load = () => {
  // 构建基础查询参数
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    title: data.title
  }

  // 根据角色添加不同的筛选条件
  if (data.user.role === 'USER') {
    // 普通用户：查询自己提交的所有申请
    params.requesterId = data.user.id
  } else if (data.user.role === 'ADMIN') {
    // 管理员：查询需要自己审核的申请
    params.reviewerId = data.user.id
  }

  // 发送请求获取分页数据
  request.get('/leave/selectPage', { params }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data.list
      data.total = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

/**
 * 重置搜索条件并重新加载数据
 * 清空标题输入框并刷新列表
 */
const reset = () => {
  data.title = null
  load()
}

/**
 * 打开新增请假申请弹窗
 * 初始化表单数据，预填请求人信息（从当前登录用户获取）
 */
const handleAdd = () => {
  data.formVisible = true
  data.form = {
    // 自动填入当前用户的ID和姓名作为请求人
    requesterId: data.user.id,
    requesterName: data.user.name,
    reviewerId: null,
    reviewerName: ''
  }
}

/**
 * 远程搜索管理员列表（用于选择审批人）
 * 调用getAdminList接口按姓名模糊查询，结果用于下拉选择
 *
 * @param {string} query - 用户输入的搜索关键词
 */
const searchAdmins = (query) => {
  if (query !== '') {
    data.adminLoading = true  // 显示加载状态
    // 调用获取管理员列表接口（无权限限制，所有登录用户可用）
    request.get('/admin/getAdminList', {
      params: {
        name: query
      }
    }).then(res => {
      data.adminLoading = false  // 隐藏加载状态
      if (res.code === '200') {
        data.adminList = res.data  // 更新下拉选项列表
      } else {
        data.adminList = []
        ElMessage.warning(res.msg || '获取管理员列表失败')
      }
    }).catch(err => {
      data.adminLoading = false
      data.adminList = []
      console.error('搜索管理员失败:', err)
      ElMessage.error('搜索管理员失败，请稍后重试')
    })
  } else {
    data.adminList = []  // 清空搜索结果
  }
}

/**
 * 处理审批人选择事件
 * 从选中的管理员对象中提取ID和姓名，保存到表单中
 *
 * @param {string} name - 选中的管理员姓名
 */
const handleAdminSelect = (name) => {
  // 在搜索结果中查找对应的管理员对象
  const selected = data.adminList.find(item => item.name === name)
  if (selected) {
    // 同时保存ID和姓名（ID用于后端关联，姓名用于展示）
    data.form.reviewerId = selected.id
    data.form.reviewerName = selected.name
  }
}

/**
 * 执行新增操作
 * 向后端发送POST请求创建新的请假申请
 */
const add = () => {
  request.post("/leave/add", data.form).then(res => {
    if (res.code === "200") {
      data.formVisible = false  // 关闭弹窗
      ElMessage.success(res.msg)  // 提示成功
      load()  // 刷新列表
    } else {
      ElMessage.error(res.msg)  // 提示错误
    }
  })
}

/**
 * 执行更新操作
 * 先进行表单验证，通过后发送更新请求
 */
const update = () => {
  formRef.value.validate((valid) => {
    if (valid) {  // 表单验证通过
      request.post('/leave/update', data.form).then(res => {
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

/**
 * 统一的保存方法
 * 根据表单中是否存在id判断执行新增还是更新操作
 */
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      data.form.id ? update() : add()  // 有id则更新，无id则新增
    }
  })
}

/**
 * 打开编辑弹窗并加载数据
 * 使用深拷贝避免直接修改原数据影响表格显示
 *
 * @param {Object} row - 当前行数据对象
 */
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))  // 深度拷贝数据
  data.formVisible = true
}

/**
 * 删除请假申请
 * 弹出确认对话框，确认后调用删除接口
 *
 * @param {number} id - 要删除的申请ID
 */
const del = (id) => {
  ElMessageBox.confirm('删除后无法恢复，您确认删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.post('/leave/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()  // 刷新列表
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})  // 取消删除时不做任何操作
}

/**
 * 打开审核弹窗
 * 初始化审核表单数据，默认选中"已审核"状态
 *
 * @param {Object} row - 当前要审核的申请数据
 */
const handleReview = (row) => {
  data.reviewVisible = true
  data.reviewForm = {
    id: row.id,
    status: '已审核',  // 默认选中"审核通过"
    reviewComment: ''  // 清空审核说明
  }
}

/**
 * 提交审核结果
 * 校验必填项后发送审核请求到后端
 * 特别处理：审核拒绝时必须填写说明
 */
const submitReview = () => {
  // 前端二次校验：拒绝时必须填写说明
  if (data.reviewForm.status === '审核拒绝' && (!data.reviewForm.reviewComment || !data.reviewForm.reviewComment.trim())) {
    ElMessage.warning('审核拒绝时必须填写说明')
    return
  }
  // 发送审核请求
  request.post('/leave/review', data.reviewForm).then(res => {
    if (res.code === '200') {
      data.reviewVisible = false  // 关闭弹窗
      ElMessage.success('审核完成')
      load()  // 刷新列表以反映最新状态
    } else {
      ElMessage.error(res.msg)
    }
  })
}


</script>


<style scoped>.search-card {
  margin-bottom: 16px;
}

.action-card {
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

.el-tag {
  border-radius: 4px;
  padding: 4px 8px;
}

.el-button--circle {
  padding: 8px;
}

.el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  background: white;
}

.el-dialog__body {
  padding: 24px;
}

@media (max-width: 768px) {
  .el-table-column--selection,
  .el-table__expand-icon {
    display: none;
  }
}
</style>
