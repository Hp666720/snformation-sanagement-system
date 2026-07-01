<template>
  <div>
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon :size="30"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.leaveTotal }}</div>
              <div class="stat-label">请假申请总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon :size="30"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.leavePending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon :size="30"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.leaveApproved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon :size="30"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.leaveRejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="font-size: 16px; font-weight: bold;">请假状态分布</div>
          </template>
          <div ref="pieChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="font-size: 16px; font-weight: bold;">月度请假趋势</div>
          </template>
          <div ref="barChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告区域 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 16px; font-weight: bold;">📢 最新公告</span>
          <el-button type="primary" link @click="$router.push('/manager/announcement')">查看更多</el-button>
        </div>
      </template>
      <div v-if="announcements.length > 0">
        <div v-for="(item, index) in announcements" :key="item.id"
             class="announcement-item"
             @click="viewAnnouncement(item)">
          <div class="announcement-title">
            <el-tag v-if="index === 0" type="danger" size="small" style="margin-right: 8px;">最新</el-tag>
            {{ item.title }}
          </div>
          <div class="announcement-meta">
            <span>{{ item.publisherName }}</span>
            <span style="margin-left: 15px;">{{ formatDateTime(item.publishTime) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无公告"></el-empty>
    </el-card>

    <!-- 公告详情弹窗 -->
    <el-dialog title="公告详情" v-model="dialogVisible" width="50%">
      <div style="padding: 0 20px;">
        <h2 style="text-align: center; margin-bottom: 20px;">{{ currentAnnouncement.title }}</h2>
        <div style="color: #666; margin-bottom: 15px; font-size: 14px;">
          <span>发布人：{{ currentAnnouncement.publisherName }}</span>
          <span style="margin-left: 30px;">发布时间：{{ formatDateTime(currentAnnouncement.publishTime) }}</span>
        </div>
        <el-divider></el-divider>
        <div style="line-height: 1.8; min-height: 200px;">{{ currentAnnouncement.content || '暂无内容' }}</div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick, onBeforeUnmount } from "vue";
import { Document, Clock, CircleCheck, CircleClose } from "@element-plus/icons-vue";
import * as echarts from 'echarts';
import request from "@/assets/js/request.js";

const announcements = ref([])
const dialogVisible = ref(false)
const currentAnnouncement = ref({})

const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

const stats = reactive({
  leaveTotal: 0,
  leavePending: 0,
  leaveApproved: 0,
  leaveRejected: 0
})

onMounted(() => {
  loadAnnouncements()
  loadStatistics()
})

onBeforeUnmount(() => {
  if (pieChart) {
    pieChart.dispose()
  }
  if (barChart) {
    barChart.dispose()
  }
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const loadAnnouncements = () => {
  request.get('/announcement/latest', { params: { limit: 5 } }).then(res => {
    if (res.code === '200') {
      announcements.value = res.data || []
    }
  })
}

const loadStatistics = () => {
  const user = JSON.parse(localStorage.getItem("code_user")) || {}
  const params = {}

  // 所有用户（包括管理员和普通用户）都查看所有请假数据
  // if (user.role === 'USER') {
  //   params.requesterId = user.id
  // }

  request.get('/leave/selectPage', { params: { pageNum: 1, pageSize: 1000, ...params } }).then(res => {
    if (res.code === '200') {
      const list = res.data.list || []
      stats.leaveTotal = list.length
      stats.leavePending = list.filter(item => item.status === '待审核').length
      stats.leaveApproved = list.filter(item => item.status === '已审核').length
      stats.leaveRejected = list.filter(item => item.status === '审核拒绝').length

      // 加载月度统计数据
      loadMonthlyStats(params.requesterId)

      nextTick(() => {
        initPieChart()
      })
    }
  })
}

const loadMonthlyStats = (requesterId) => {
  const params = {}
  // 不再根据requesterId过滤，所有用户都查看全部数据
  // if (requesterId) {
  //   params.requesterId = requesterId
  // }

  request.get('/leave/getMonthlyStats', { params }).then(res => {
    if (res.code === '200') {
      const monthlyData = res.data || []
      nextTick(() => {
        initBarChart(monthlyData)
      })
    }
  })
}
const initPieChart = () => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '请假状态',
        type: 'pie',
        radius: '60%',
        data: [
          { value: stats.leavePending, name: '待审核' },
          { value: stats.leaveApproved, name: '已通过' },
          { value: stats.leaveRejected, name: '已拒绝' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  pieChart.setOption(option)
}

const initBarChart = (monthlyData) => {
  if (!barChartRef.value) return

  barChart = echarts.init(barChartRef.value)

  // 从真实数据中提取月份和数量
  const months = monthlyData.map(item => item.month)
  const counts = monthlyData.map(item => item.count)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '请假次数',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  barChart.setOption(option)
}

const viewAnnouncement = (item) => {
  currentAnnouncement.value = item
  dialogVisible.value = true
}
</script>

<style scoped>.stat-card {
  border-radius: 12px;
  background: white;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 15px 0;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.05);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 6px;
  font-weight: 500;
}

.el-card__header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.el-card__body {
  padding: 20px;
}

.announcement-item {
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin-bottom: 8px;
}

.announcement-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.announcement-item:hover {
  background-color: #f5f7fa;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.announcement-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-weight: 500;
}

.announcement-meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .stat-card {
    margin-bottom: 16px;
  }

  .stat-number {
    font-size: 24px;
  }
}
</style>
