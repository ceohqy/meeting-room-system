<template>
  <div class="page">
    <div class="toolbar animate-in">
      <div class="toolbar-left">
        <el-input v-model="searchText" placeholder="搜索配件..." prefix-icon="Search" clearable style="width:220px" @input="handleSearch" />
        <el-select v-model="filterType" placeholder="类型筛选" clearable style="width:130px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="投影仪" value="projector" />
          <el-option label="电视" value="tv" />
          <el-option label="白板" value="whiteboard" />
          <el-option label="视频会议" value="video" />
        </el-select>
      </div>
      <el-button type="primary" icon="Plus" @click="openAdd">新增配件</el-button>
    </div>

    <!-- Equipment cards grid -->
    <div class="equipment-grid animate-in delay-2">
      <div v-for="(item, i) in tableData" :key="item.id" class="equipment-card hover-card animate-in" :class="`delay-${(i%5)+1}`">
        <div class="eq-header">
          <div class="eq-icon" :style="{background: typeConfig[item.type]?.bg || 'rgba(79,110,247,0.1)'}">
            <el-icon :size="24" :style="{color: typeConfig[item.type]?.color || '#4F6EF7'}">
              <component :is="typeConfig[item.type]?.icon || 'Monitor'" />
            </el-icon>
          </div>
          <el-tag :type="item.status == 1 ? 'success' : 'danger'" size="small">{{ item.status == 1 ? '正常' : '故障' }}</el-tag>
        </div>
        <h3 class="eq-name">{{ item.name }}</h3>
        <p class="eq-room">📍 {{ item.roomName || '未分配' }}</p>
        <p class="eq-type">{{ typeConfig[item.type]?.label || item.type }}</p>
        <div class="eq-footer">
          <span class="eq-sn">SN: {{ item.serialNo || 'N/A' }}</span>
          <div class="eq-actions">
            <el-button text type="primary" size="small" icon="Edit" @click="openEdit(item)">编辑</el-button>
            <el-button text type="danger" size="small" icon="Delete" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Fallback table if no cards -->
    <div v-if="tableData.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无配件数据，点击新增配件" />
    </div>

    <!-- Pagination -->
    <div class="pagination-wrap animate-in delay-3">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :total="pagination.total" layout="total, prev, pager, next" @change="fetchData" />
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑配件' : '新增配件'" width="520px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="配件名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入配件名称" />
        </el-form-item>
        <el-form-item label="配件类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width:100%">
            <el-option label="投影仪" value="projector" />
            <el-option label="电视" value="tv" />
            <el-option label="白板" value="whiteboard" />
            <el-option label="视频会议设备" value="video" />
            <el-option label="麦克风" value="mic" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属会议室">
          <el-select v-model="formData.roomId" placeholder="请选择会议室" style="width:100%" clearable>
            <el-option v-for="room in rooms" :key="room.id" :label="room.name" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="序列号">
          <el-input v-model="formData.serialNo" placeholder="请输入设备序列号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">故障</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { equipmentApi } from '@/api/equipment'
import { roomApi } from '@/api/room'

const loading = ref(false)
const submitLoading = ref(false)
const searchText = ref('')
const filterType = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const rooms = ref([])
const pagination = reactive({ page: 1, size: 12, total: 0 })

const typeConfig = {
  projector: { label: '投影仪', icon: 'Monitor', bg: 'rgba(79,110,247,0.1)', color: '#4F6EF7' },
  tv: { label: '电视', icon: 'Monitor', bg: 'rgba(16,185,129,0.1)', color: '#10B981' },
  whiteboard: { label: '白板', icon: 'Edit', bg: 'rgba(249,115,22,0.1)', color: '#F97316' },
  video: { label: '视频会议', icon: 'VideoCamera', bg: 'rgba(245,158,11,0.1)', color: '#F59E0B' },
  mic: { label: '麦克风', icon: 'Microphone', bg: 'rgba(139,92,246,0.1)', color: '#8B5CF6' },
  other: { label: '其他', icon: 'Box', bg: 'rgba(100,116,139,0.1)', color: '#64748B' },
}

const formData = reactive({ id: null, name: '', type: '', roomId: '', serialNo: '', status: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入配件名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择配件类型', trigger: 'change' }],
}

const mockData = () => {
  tableData.value = [
    { id: 1, name: '激光投影仪-A01', type: 'projector', roomName: '创新会议室A', serialNo: 'PJ2024001', status: 1 },
    { id: 2, name: '55寸智能电视', type: 'tv', roomName: '技术会议室B', serialNo: 'TV2024002', status: 1 },
    { id: 3, name: '磁性白板', type: 'whiteboard', roomName: '创新会议室A', serialNo: 'WB2024003', status: 1 },
    { id: 4, name: '视频会议系统', type: 'video', roomName: '大型报告厅', serialNo: 'VC2024004', status: 1 },
    { id: 5, name: '无线麦克风套装', type: 'mic', roomName: '大型报告厅', serialNo: 'MC2024005', status: 0 },
    { id: 6, name: '4K投影仪-B02', type: 'projector', roomName: '小型洽谈室', serialNo: 'PJ2024006', status: 1 },
  ]
  pagination.total = 6
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await equipmentApi.list({ page: pagination.page, size: pagination.size, name: searchText.value, type: filterType.value })
    tableData.value = res.data?.list || res.data?.records || []
    pagination.total = res.data?.total || 0
    if (!tableData.value.length) mockData()
  } catch { mockData() } finally { loading.value = false }
}

const fetchRooms = async () => {
  try {
    const res = await roomApi.list({ size: 100 })
    rooms.value = res.data?.list || res.data?.records || []
  } catch { rooms.value = [{ id: 1, name: '创新会议室A' }, { id: 2, name: '技术会议室B' }] }
}

const handleSearch = () => { pagination.page = 1; fetchData() }

const openAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', type: '', roomId: '', serialNo: '', status: 1, remark: '' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await equipmentApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await equipmentApi.add(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await fetchData()
    } catch {} finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除配件 "${row.name}" 吗？`, '警告', { type: 'warning' })
  try {
    await equipmentApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(() => { fetchData(); fetchRooms() })
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; background: var(--bg-card); padding: 16px 20px; border-radius: var(--radius); box-shadow: var(--shadow-card); }
.toolbar-left { display: flex; gap: 12px; }
.equipment-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.equipment-card {
  background: var(--bg-card); border-radius: var(--radius); padding: 20px;
  box-shadow: var(--shadow-card); cursor: default;
}
.eq-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
.eq-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.eq-name { font-family: var(--font-display); font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 6px; }
.eq-room { font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
.eq-type { font-size: 12px; color: var(--text-muted); margin-bottom: 14px; }
.eq-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-light); padding-top: 12px; }
.eq-sn { font-size: 11px; color: var(--text-muted); font-family: monospace; }
.eq-actions { display: flex; gap: 4px; }
.pagination-wrap { display: flex; justify-content: flex-end; }
</style>
