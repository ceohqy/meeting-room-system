<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showAddDialog">新增用户</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="租户管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="users" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger">超级管理员</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning">租户管理员</el-tag>
            <el-tag v-else type="success">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="所属租户" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deleteUser(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="租户管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属租户" prop="tenantId" v-if="isSuperAdmin">
          <el-select v-model="form.tenantId" style="width:100%">
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id" />
          </el-select>
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

const users = ref([])
const tenants = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const isSuperAdmin = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  username: '',
  role: ''
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: 'USER',
  tenantId: null
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  tenantId: [{ required: isSuperAdmin.value, message: '请选择所属租户', trigger: 'change' }]
}

onMounted(async () => {
  await checkRole()
  fetchUsers()
  if (isSuperAdmin.value) {
    fetchTenants()
  }
})

const checkRole = async () => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  isSuperAdmin.value = user?.role === 'SUPER_ADMIN'
}

const fetchUsers = async () => {
  try {
    const res = await request.get('/user/list', { params: searchForm })
    users.value = res.data
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  }
}

const fetchTenants = async () => {
  try {
    const res = await request.get('/tenant/list')
    tenants.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.role = ''
  fetchUsers()
}

const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    realName: '',
    role: 'USER',
    tenantId: null
  })
  formRef.value?.clearValidate()
}

const showEditDialog = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    role: row.role,
    tenantId: row.tenantId
  })
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await request.put(`/user/${form.id}`, form)
        ElMessage.success('更新成功')
      } else {
        await request.post('/user/add', form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchUsers()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  })
}

const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/user/${id}`)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.user-management { display: flex; flex-direction: column; gap: 20px; }

.page-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 14px 20px;
  border-radius: var(--radius); border: 1px solid var(--border);
  box-shadow: var(--shadow-card); flex-wrap: wrap; gap: 12px;
}
.toolbar-left { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }

.table-card {
  background: #fff; border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 20px; box-shadow: var(--shadow-card);
}
</style>
