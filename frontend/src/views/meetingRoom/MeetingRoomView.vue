<template>
  <div class="page">
    <!-- Toolbar -->
    <div class="toolbar animate-in">
      <div class="toolbar-left">
        <el-input v-model="searchText" placeholder="搜索会议室..." prefix-icon="Search" clearable style="width:240px" @input="handleSearch" />
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width:130px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="可用" value="1" />
          <el-option label="停用" value="0" />
        </el-select>
      </div>
      <el-button v-if="isAdmin" type="primary" icon="Plus" @click="openAdd">新增会议室</el-button>
    </div>

    <!-- Table -->
    <div class="table-card animate-in delay-2">
      <el-table :data="tableData" v-loading="loading" row-class-name="table-row">
<el-table-column prop="name" label="会议室名称" min-width="160">
          <template #default="{row}">
            <div class="room-name-cell">
              <div class="room-avatar" :style="{background: roomColors[row.id % 5]}">{{ row.name?.charAt(0) }}</div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数" width="110">
          <template #default="{row}"><span>{{ row.capacity }} 人</span></template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column prop="equipment" label="设备配置" min-width="180">
          <template #default="{row}">
            <div class="tags-wrap">
              <el-tag v-for="eq in (row.equipment || '').split(',').filter(Boolean).slice(0,3)" :key="eq" size="small" type="info">{{ eq }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status == 1 ? 'success' : 'danger'" size="small">{{ row.status == 1 ? '可用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <template v-if="isAdmin">
              <el-button text type="primary" size="small" icon="Edit" @click="openEdit(row)">编辑</el-button>
              <el-button text type="danger" size="small" icon="Delete" @click="handleDelete(row)">删除</el-button>
            </template>
            <span v-else class="text-muted" style="font-size:12px;color:#94A3B8">只读</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会议室' : '新增会议室'" width="560px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="会议室名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" :max="500" style="width:100%" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="如：3楼A区" />
        </el-form-item>
        <el-form-item label="设备配置">
          <el-input v-model="formData.equipment" placeholder="多个设备用逗号分隔，如：投影仪,白板" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ isEdit ? '保存修改' : '立即创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roomApi } from '@/api/room'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const isAdmin = computed(() => ['ADMIN', 'SUPER_ADMIN'].includes(authStore.user?.role))

const loading = ref(false)
const submitLoading = ref(false)
const searchText = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const roomColors = ['rgba(79,110,247,0.12)', 'rgba(16,185,129,0.12)', 'rgba(249,115,22,0.12)', 'rgba(245,158,11,0.12)', 'rgba(239,68,68,0.12)']

const formData = reactive({ id: null, name: '', capacity: 10, location: '', equipment: '', status: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
}

const mockData = () => {
  tableData.value = [
    { id: 1, name: '创新会议室A', capacity: 20, location: '3楼A区', equipment: '投影仪,视频会议,白板', status: 1 },
    { id: 2, name: '技术会议室B', capacity: 12, location: '2楼B区', equipment: '电视,白板', status: 1 },
    { id: 3, name: '大型报告厅', capacity: 100, location: '1楼', equipment: '投影仪,视频会议,麦克风', status: 1 },
    { id: 4, name: '小型洽谈室', capacity: 6, location: '4楼C区', equipment: '电视', status: 0 },
  ]
  pagination.total = 4
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await roomApi.list({ page: pagination.page, size: pagination.size, name: searchText.value, status: filterStatus.value })
    tableData.value = res.data?.list || res.data?.records || []
    pagination.total = res.data?.total || 0
    if (!tableData.value.length) mockData()
  } catch { mockData() } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchData() }

const openAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', capacity: 10, location: '', equipment: '', status: 1, remark: '' })
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
        await roomApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await roomApi.add(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch {} finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除会议室 "${row.name}" 吗？`, '警告', { type: 'warning' })
  try {
    await roomApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

.toolbar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 14px 20px;
  border-radius: var(--radius); border: 1px solid var(--border);
  box-shadow: var(--shadow-card);
}
.toolbar-left { display: flex; gap: 10px; }

.table-card {
  background: #fff; border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 20px; box-shadow: var(--shadow-card);
}
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }

.room-name-cell { display: flex; align-items: center; gap: 10px; }
.room-avatar {
  width: 32px; height: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 13px; color: var(--text-secondary);
}
.tags-wrap { display: flex; flex-wrap: wrap; gap: 4px; }
</style>
