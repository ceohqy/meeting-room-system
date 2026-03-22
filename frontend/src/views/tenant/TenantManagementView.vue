<template>
  <div class="tenant-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>租户管理</span>
          <el-button type="primary" @click="showAddDialog">新增租户</el-button>
        </div>
      </template>

      <el-table :data="tenants" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="租户名称" />
        <el-table-column prop="code" label="租户编码" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deleteTenant(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑租户' : '新增租户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="租户编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tenants = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  code: ''
})

const rules = {
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入租户编码', trigger: 'blur' }]
}

onMounted(() => {
  fetchTenants()
})

const fetchTenants = async () => {
  try {
    const res = await request.get('/tenant/list')
    tenants.value = res.data
  } catch (e) {
    ElMessage.error('获取租户列表失败')
  }
}

const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(form, { id: null, name: '', code: '' })
  formRef.value?.clearValidate()
}

const showEditDialog = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(form, { id: row.id, name: row.name, code: row.code })
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await request.put(`/tenant/${form.id}`, form)
        ElMessage.success('更新成功')
      } else {
        await request.post('/tenant/add', form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchTenants()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  })
}

const deleteTenant = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该租户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/tenant/${id}`)
    ElMessage.success('删除成功')
    fetchTenants()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.tenant-management {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
