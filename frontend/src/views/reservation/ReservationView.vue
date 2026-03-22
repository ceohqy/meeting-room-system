console.log("ReservationView加载了")
<template>
  <div class="page">
    <div class="toolbar animate-in">
      <div class="toolbar-left">
        <el-input v-model="searchText" placeholder="搜索预约..." prefix-icon="Search" clearable style="width:220px" @input="handleSearch" />
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width:120px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="待审批" value="0" />
          <el-option label="已通过" value="1" />
          <el-option label="已拒绝" value="2" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width:240px" @change="handleSearch" />
      </div>
      <el-button type="primary" icon="Plus" @click="openAddDialog">新建预约</el-button>
    </div>

    <div class="table-card animate-in delay-2">
    <el-table :data="tableData" v-loading="loading" style="width:100%">
      <el-table-column prop="roomName" label="会议室" min-width="100" />
      <el-table-column prop="applicant" label="预约人" min-width="80" />
      <el-table-column label="预约时间" min-width="180">
        <template #default="{row}">
          <span style="white-space:nowrap">
            {{ row.startTime.slice(0,10) }}
            {{ row.startTime.slice(11,16) }} ~ {{ row.endTime.slice(11,16) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="purpose" label="会议用途" min-width="120" show-overflow-tooltip />
      <el-table-column prop="attendees" label="人数" width="70">
        <template #default="{row}">
          {{ row.attendees }} 人
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="80">
        <template #default="{row}">
          <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{row}">
          <div class="action-btns">
            <el-button text type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" text type="danger" size="small" @click="handleCancel(row)">取消</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" layout="total, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- New Reservation Dialog -->
    <el-dialog v-model="dialogVisible" title="新建预约" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="选择会议室" prop="roomId">
          <el-select v-model="formData.roomId" placeholder="请选择会议室" style="width:100%">
            <el-option v-for="room in rooms" :key="room.id" :label="`${room.name} (${room.capacity}人)`" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约人" prop="applicant">
          <el-input v-model="formData.applicant" placeholder="请输入预约人姓名" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="formData.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="formData.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="参与人数" prop="attendees">
          <el-input-number v-model="formData.attendees" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="会议用途" prop="purpose">
          <el-input v-model="formData.purpose" type="textarea" :rows="3" placeholder="请描述会议用途" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reservationApi } from '@/api/reservation'
import { roomApi } from '@/api/room'

const loading = ref(false)
const submitLoading = ref(false)
const searchText = ref('')
const filterStatus = ref('')
const dateRange = ref(null)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const rooms = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const currentUser = ref(JSON.parse(localStorage.getItem('user') || 'null'))

const formData = reactive({ roomId: '', applicant: '', startTime: '', endTime: '', attendees: 1, purpose: '' })
const rules = {
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入预约人', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入会议用途', trigger: 'blur' }],
}

const statusMap = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已取消', type: 'info' },
}



const fetchRooms = async () => {
  try {
    const res = await roomApi.list({ status: 1, size: 100 })
    rooms.value = res.data?.list || res.data?.records || [
      { id: 1, name: '创新会议室A', capacity: 20 },
      { id: 2, name: '技术会议室B', capacity: 12 },
      { id: 3, name: '大型报告厅', capacity: 100 },
    ]
  } catch {
    rooms.value = [
      { id: 1, name: '创新会议室A', capacity: 20 },
      { id: 2, name: '技术会议室B', capacity: 12 },
    ]
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await reservationApi.list()
    let list = res.data || []

    // 搜索过滤
    if (searchText.value) {
      list = list.filter(item =>
        item.applicant?.includes(searchText.value) ||
        item.purpose?.includes(searchText.value)
      )
    }

    // 状态过滤
    if (filterStatus.value !== '') {
      const statusMap = { '0': 'PENDING', '1': 'APPROVED', '2': 'REJECTED', '3': 'CANCELLED' }
      const targetStatus = statusMap[filterStatus.value]
      list = list.filter(item => item.status === targetStatus)
    }

    // 时间范围过滤
    if (dateRange.value && dateRange.value.length === 2) {
      const [start, end] = dateRange.value
      list = list.filter(item => {
        const itemDate = new Date(item.reservationDate)
        return itemDate >= start && itemDate <= end
      })
    }

    tableData.value = list.map(item => {
      const room = rooms.value.find(r => r.id === item.roomId)
      let statusNum = 0
      if (item.status === 'PENDING') statusNum = 0
      else if (item.status === 'APPROVED') statusNum = 1
      else if (item.status === 'REJECTED') statusNum = 2
      else if (item.status === 'CANCELLED') statusNum = 3

      return {
        id: item.id,
        roomName: item.roomName || (room ? room.name : "未知会议室"),
        applicant: item.applicant || "用户" + item.userId,
        startTime: item.reservationDate + " " + item.startTime,
        endTime: item.reservationDate + " " + item.endTime,
        purpose: item.purpose || "会议讨论",
        attendees: item.attendees || 0,
        status: statusNum
      }
    })

    pagination.total = tableData.value.length
  } catch (e) {
    console.error("获取预约失败", e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }

const openAddDialog = () => {
  Object.assign(formData, { roomId: '', applicant: '', startTime: '', endTime: '', attendees: 1, purpose: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true

    try {

      const start = new Date(formData.startTime)
      const end = new Date(formData.endTime)

     const data = {
  userId: currentUser.value?.id,
  roomId: formData.roomId,
  applicant: formData.applicant,
  purpose: formData.purpose,
  attendees: formData.attendees,

  reservationDate: start.toISOString().slice(0,10),
  startTime: start.toTimeString().slice(0,8),
  endTime: end.toTimeString().slice(0,8),

  status: 0
}

      await reservationApi.add(data)

      ElMessage.success('预约申请已提交，等待审批')

      dialogVisible.value = false
      fetchData()

    } catch (e) {
      ElMessage.error("预约失败")
    } finally {
      submitLoading.value = false
    }
  })
}
const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
  try {
    await reservationApi.cancel(row.id)
    ElMessage.success('已取消预约')
    fetchData()
  } catch {}
}

const viewDetail = (row) => {
  ElMessageBox.alert(`
    <div style="line-height: 2;">
      <p><strong>预约ID：</strong>${row.id}</p>
      <p><strong>会议室：</strong>${row.roomName}</p>
      <p><strong>预约人：</strong>${row.applicant}</p>
      <p><strong>开始时间：</strong>${row.startTime}</p>
      <p><strong>结束时间：</strong>${row.endTime}</p>
      <p><strong>参与人数：</strong>${row.attendees} 人</p>
      <p><strong>会议用途：</strong>${row.purpose}</p>
      <p><strong>状态：</strong>${statusMap[row.status]?.label || '未知'}</p>
    </div>
  `, '预约详情', { dangerouslyUseHTMLString: true })
}

onMounted(async () => {

  await fetchRooms()

  fetchData()

})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.toolbar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 14px 20px;
  border-radius: var(--radius); border: 1px solid var(--border);
  box-shadow: var(--shadow-card); flex-wrap: wrap; gap: 12px;
}
.toolbar-left { display: flex; gap: 10px; flex-wrap: wrap; }
.table-card {
  background: #fff; border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 20px; box-shadow: var(--shadow-card);
}
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
.action-btns { display: flex; gap: 6px; }
</style>
