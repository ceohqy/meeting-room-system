```vue
<template>
  <div class="page">

    <!-- 统计 -->
    <div class="approval-stats">
      <div v-for="s in approvalStats" :key="s.label" class="stat-pill">
        <span class="pill-value">{{ s.value }}</span>
        <span class="pill-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">

      <div class="toolbar-left">

        <el-input
          v-model="searchText"
          placeholder="搜索申请..."
          clearable
          style="width:220px"
          @input="handleSearch"
        />

        <el-select
          v-model="filterStatus"
          placeholder="审批状态"
          clearable
          style="width:140px"
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option label="待审批" value="0" />
          <el-option label="已通过" value="1" />
          <el-option label="已拒绝" value="2" />
        </el-select>

      </div>

      <div class="toolbar-right">

        <el-button
          type="success"
          :disabled="!selectedRows.length"
          @click="batchApprove"
        >
          批量通过 ({{ selectedRows.length }})
        </el-button>

        <el-button
          type="danger"
          :disabled="!selectedRows.length"
          @click="batchReject"
        >
          批量拒绝
        </el-button>

      </div>

    </div>

    <!-- 表格 -->
    <div class="table-card">

      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width:100%"
      >
        <el-table-column type="selection" width="40"/>
        <el-table-column prop="roomName" label="会议室" min-width="120">
          <template #default="{row}">
            <div class="room-cell">
              <div class="room-dot" :style="{background: dotColors[row.id % 5]}"></div>
              <span>{{ row.roomName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" min-width="80" align="center"/>
        <el-table-column label="预约时间" min-width="140" align="center">
          <template #default="{row}">
            <div class="time-cell">
              <span class="time-date">{{ row.startTime?.slice(0,10) }}</span>
              <span class="time-range">{{ row.startTime?.slice(11,16) }} ~ {{ row.endTime?.slice(11,16) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="attendees" label="人数" width="60" align="center">
          <template #default="{row}">{{ row.attendees }}人</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="100" show-overflow-tooltip/>
        <el-table-column label="申请时间" min-width="120" align="center">
          <template #default="{row}">{{ row.applyTime?.slice(0,16) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status].type" size="small" :disable-transitions="true">{{ statusMap[row.status].label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{row}">
            <div class="action-btns" v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger"  size="small" @click="openRejectDialog(row)">拒绝</el-button>
            </div>
            <el-button v-else type="primary" size="small" plain @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

    </div>

    <!-- 拒绝弹窗 -->

    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="420px"
    >

      <el-input
        v-model="rejectReason"
        type="textarea"
        rows="4"
        placeholder="请输入拒绝原因"
      />

      <template #footer>

        <el-button @click="rejectDialogVisible=false">
          取消
        </el-button>

        <el-button
          type="danger"
          :loading="actionLoading"
          @click="confirmReject"
        >
          确认拒绝
        </el-button>

      </template>

    </el-dialog>

    <!-- 详情 -->

    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="480px"
    >

      <div v-if="currentRow" class="detail-content">

        <div
          v-for="item in detailFields"
          :key="item.key"
          class="detail-row"
        >

          <span class="detail-label">
            {{ item.label }}
          </span>

          <span class="detail-value">

            <el-tag
              v-if="item.key === 'status'"
              :type="statusMap[currentRow.status].type"
            >
              {{ statusMap[currentRow.status].label }}
            </el-tag>

            <template v-else>
              {{ currentRow[item.key] }}
            </template>

          </span>

        </div>

      </div>

    </el-dialog>

  </div>
</template>

<script setup>

import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approvalApi } from '@/api/approval'

const loading = ref(false)
const actionLoading = ref(false)

const searchText = ref('')
const filterStatus = ref('')

const tableData = ref([])

const selectedRows = ref([])

const rejectDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const rejectReason = ref('')
const currentRow = ref(null)

const pagination = reactive({
  page:1,
  size:10,
  total:0
})

const statusMap = {
  0:{label:'待审批',type:'warning'},
  1:{label:'已通过',type:'success'},
  2:{label:'已拒绝',type:'danger'}
}

const dotColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399'
]

const detailFields = [
  {key:'roomName',label:'会议室'},
  {key:'applicant',label:'申请人'},
  {key:'startTime',label:'开始时间'},
  {key:'endTime',label:'结束时间'},
  {key:'attendees',label:'人数'},
  {key:'purpose',label:'用途'},
  {key:'applyTime',label:'申请时间'},
  {key:'status',label:'审批状态'}
]

const approvalStats = computed(()=>[
  {label:'全部申请',value:pagination.total},
  {label:'待审批',value:tableData.value.filter(r=>r.status===0).length},
  {label:'已通过',value:tableData.value.filter(r=>r.status===1).length},
  {label:'已拒绝',value:tableData.value.filter(r=>r.status===2).length}
])

const fetchData = async ()=>{

  loading.value=true

  try{

    const res = await approvalApi.list()

    let list = res.data || []

    // 搜索过滤
    if (searchText.value) {
      list = list.filter(item =>
        item.roomName?.includes(searchText.value) ||
        item.applicant?.includes(searchText.value)
      )
    }

    // 状态过滤
    if (filterStatus.value !== '') {
      const status = parseInt(filterStatus.value)
      list = list.filter(item => item.status === status)
    }

    tableData.value = list

    pagination.total = list.length

  }catch(e){

    ElMessage.error('获取审批数据失败')

  }finally{

    loading.value=false

  }

}

const handleSearch=()=>{
  fetchData()
}

const handleSelectionChange=(rows)=>{
  selectedRows.value=rows
}

const handleApprove=async(row)=>{

  await ElMessageBox.confirm('确定通过该申请吗？','审批确认')

  try{

    await approvalApi.pass({
      reservationId:row.reservationId,
      approverId:1
    })

    ElMessage.success('审批通过')

    fetchData()

  }catch(e){

    ElMessage.error('审批失败')

  }

}

const openRejectDialog=(row)=>{
  currentRow.value=row
  rejectDialogVisible.value=true
}

const confirmReject=async()=>{

  try{

    await approvalApi.reject({
      reservationId:currentRow.value.reservationId,
      approverId:1,
      remark:rejectReason.value
    })

    ElMessage.success('已拒绝')

    rejectDialogVisible.value=false

    fetchData()

  }catch(e){

    ElMessage.error('操作失败')

  }

}

const batchApprove=async()=>{

  const rows=selectedRows.value.filter(r=>r.status===0)

  for(const r of rows){

    await approvalApi.pass({id:r.id})

  }

  ElMessage.success('批量审批完成')

  fetchData()

}

const batchReject=async()=>{

  const rows=selectedRows.value.filter(r=>r.status===0)

  for(const r of rows){

    await approvalApi.reject({id:r.id})

  }

  ElMessage.success('批量拒绝完成')

  fetchData()

}

const viewDetail=(row)=>{
  currentRow.value=row
  detailDialogVisible.value=true
}

onMounted(()=>{
  fetchData()
})

</script>

<style scoped>

.page{
  display:flex;
  flex-direction:column;
  gap:20px;
}

.approval-stats{
  display:flex;
  gap:12px;
  flex-wrap:wrap;
}

.stat-pill{
  display:flex;
  flex-direction:column;
  align-items:center;
  padding:14px 28px;
  border-radius:var(--radius-sm);
  min-width:110px;
  background:#fff;
  border:1px solid var(--border);
  box-shadow:var(--shadow-card);
  transition: box-shadow var(--transition), border-color var(--transition);
}
.stat-pill:hover {
  box-shadow: var(--shadow-md);
  border-color: #C7C5FF;
}

.pill-value{
  font-size:24px;
  font-weight:800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.pill-label{
  font-size:12px;
  color:var(--text-muted);
  margin-top:2px;
  font-weight:500;
}

.toolbar{
  display:flex;
  justify-content:space-between;
  align-items:center;
  background:#fff;
  padding:14px 20px;
  border-radius:var(--radius);
  border:1px solid var(--border);
  box-shadow:var(--shadow-card);
}

.toolbar-left,
.toolbar-right{
  display:flex;
  gap:10px;
}

.table-card{
  background:#fff;
  border-radius:var(--radius);
  border:1px solid var(--border);
  padding:20px;
  box-shadow:var(--shadow-card);
}

.action-btns{
  display:flex;
  flex-direction:row;
  gap:6px;
  justify-content:center;
}

.room-cell{
  display:flex;
  align-items:center;
  gap:8px;
}

.time-cell{
  display:flex;
  flex-direction:column;
  align-items:center;
  gap:1px;
}

.time-date{
  font-weight:600;
  font-size:13px;
  color: var(--text-primary);
}

.time-range{
  font-size:12px;
  color:var(--text-muted);
}

.room-dot{
  width:7px;
  height:7px;
  border-radius:50%;
  flex-shrink:0;
}

.pagination-wrap{
  display:flex;
  justify-content:flex-end;
  margin-top:20px;
}

.detail-content{
  display:flex;
  flex-direction:column;
  gap:0;
}

.detail-row{
  display:flex;
  align-items:flex-start;
  gap:16px;
  padding:12px 0;
  border-bottom:1px solid var(--border-light);
}

.detail-row:last-child{
  border-bottom:none;
}

.detail-label{
  width:80px;
  font-size:13px;
  color:var(--text-muted);
  font-weight:600;
  flex-shrink:0;
}

.detail-value{
  font-size:14px;
  font-weight:500;
  color: var(--text-primary);
  flex:1;
}

</style>

