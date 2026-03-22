<template>
  <div class="login-page">
    <div class="bg-orb orb1"></div>
    <div class="bg-orb orb2"></div>
    <div class="login-container animate-in">
      <div class="login-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="14" fill="#4F6EF7"/>
            <path d="M10 13h20M10 20h14M10 27h17" stroke="white" stroke-width="3" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="brand-name">MeetSpace</h1>
        <p class="brand-sub">会议室预约管理系统</p>
      </div>
      <div class="login-card animate-in delay-2">
        <h2 class="login-title">{{ isRegister ? '注册账号' : '欢迎回来' }}</h2>
        <p class="login-desc">{{ isRegister ? '创建您的普通用户账号' : '登录您的账号以继续' }}</p>
        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <div class="input-wrap">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input v-model.trim="form.username" placeholder="请输入账号" size="large" clearable />
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <div class="input-wrap">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input v-model.trim="form.password" type="password" placeholder="请输入密码" show-password size="large" clearable @keyup.enter="isRegister ? handleRegister : handleLogin" />
            </div>
          </el-form-item>
          <el-form-item v-if="isRegister" prop="confirmPassword">
            <div class="input-wrap">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password size="large" @keyup.enter="handleRegister" />
            </div>
          </el-form-item>
          <el-form-item v-if="isRegister" prop="tenantId">
            <div class="input-wrap">
              <el-icon class="input-icon"><OfficeBuilding /></el-icon>
              <el-select v-model="form.tenantId" placeholder="请选择所属公司" size="large" style="width:100%">
                <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id" />
              </el-select>
            </div>
          </el-form-item>
          <div class="form-options" v-if="!isRegister">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-link">忘记密码?</a>
          </div>
          <el-button native-type="button" type="primary" size="large" class="login-btn" :loading="loading" @click="isRegister ? handleRegister() : handleLogin()">
            {{ loading ? (isRegister ? '注册中...' : '登录中...') : (isRegister ? '注 册' : '登 录') }}
          </el-button>
        </el-form>
        <div class="switch-mode">
          <span>{{ isRegister ? '已有账号？' : '还没有账号？' }}</span>
          <a @click="toggleMode">{{ isRegister ? '立即登录' : '立即注册' }}</a>
        </div>
        <div class="login-hint" v-if="!isRegister">测试账号: admin / 123456</div>
      </div>
    </div>
    <div class="login-footer animate-in delay-4">© 2025 MeetSpace · 会议室管理平台</div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const rememberMe = ref(false)
const isRegister = ref(false)
const loading = ref(false)
const tenants = ref([])
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  tenantId: null
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  tenantId: [{ required: true, message: '请选择所属公司', trigger: 'change' }]
}

const toggleMode = async () => {
  isRegister.value = !isRegister.value
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.tenantId = null
  formRef.value?.clearValidate()

  // 切换到注册模式时获取租户列表
  if (isRegister.value) {
    try {
      const res = await fetch('http://localhost:8080/tenant/list')
      const data = await res.json()
      tenants.value = data.data || []
    } catch (e) {
      console.error('获取租户列表失败', e)
    }
  }
}

const handleLogin = async () => {
  console.log('点击登录，表单数据:', form)
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    console.log('表单验证结果:', valid)
    if (!valid) return
    try {
      loading.value = true
      await authStore.login(form)
      ElMessage.success('登录成功，欢迎回来！')
      router.push('/dashboard')
    } catch {
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      loading.value = true
      await authApi.register({ username: form.username, password: form.password, tenantId: form.tenantId })
      ElMessage.success('注册成功！请登录')
      isRegister.value = false
      form.password = ''
      form.confirmPassword = ''
      form.tenantId = null
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  background: var(--bg);
  position: relative; overflow: hidden;
}

/* Subtle background orbs — light version */
.bg-orb { position: absolute; border-radius: 50%; filter: blur(100px); pointer-events: none; }
.orb1 {
  width: 700px; height: 700px;
  background: radial-gradient(circle, rgba(99,91,255,0.08) 0%, transparent 70%);
  top: -250px; right: -200px;
  animation: float 12s ease-in-out infinite;
}
.orb2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(0,180,216,0.07) 0%, transparent 70%);
  bottom: -180px; left: -120px;
  animation: float 15s ease-in-out infinite reverse;
}

@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-20px)} }

.login-container {
  display: flex; flex-direction: column; align-items: center;
  gap: 28px; position: relative; z-index: 1;
}

/* Brand */
.login-brand { text-align: center; }
.brand-icon { width: 56px; height: 56px; margin: 0 auto 14px; }
.brand-icon svg { width: 100%; height: 100%; }
.brand-name {
  font-family: var(--font-display); font-size: 28px; font-weight: 800;
  color: var(--text-primary); letter-spacing: -0.03em;
}
.brand-sub { font-size: 14px; color: var(--text-muted); margin-top: 5px; }

/* Card */
.login-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 40px 44px;
  width: 420px;
  box-shadow: var(--shadow-lg);
  position: relative; z-index: 10;
}

.login-title {
  font-family: var(--font-display); font-size: 22px; font-weight: 800;
  color: var(--text-primary); letter-spacing: -0.02em;
}
.login-desc { font-size: 14px; color: var(--text-muted); margin: 6px 0 28px; }

.login-form { display: flex; flex-direction: column; gap: 4px; }
.input-wrap { position: relative; width: 100%; }
.input-icon {
  position: absolute; left: 13px; top: 50%; transform: translateY(-50%);
  color: var(--text-muted); z-index: 2; font-size: 15px; pointer-events: none;
}
.login-form :deep(.el-input__wrapper) {
  padding-left: 38px !important;
  border-radius: 9px !important;
  background: #fff !important;
  box-shadow: 0 0 0 1px var(--border) !important;
  transition: box-shadow var(--transition) !important;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #CBD5E1 !important;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(99,91,255,0.2), 0 0 0 1px var(--primary) !important;
}
.login-form :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  cursor: text !important;
}
.login-form :deep(.el-input__inner::placeholder) { color: var(--text-muted) !important; }
.login-form :deep(.el-select .el-select__wrapper) {
  background: #fff !important;
  box-shadow: 0 0 0 1px var(--border) !important;
  border-radius: 9px !important;
  color: var(--text-primary) !important;
}

.form-options {
  display: flex; justify-content: space-between; align-items: center;
  margin: 6px 0 20px;
}
.forgot-link { font-size: 13px; color: var(--primary); text-decoration: none; font-weight: 500; }
.forgot-link:hover { text-decoration: underline; }

.login-btn {
  width: 100% !important;
  height: 46px !important;
  border-radius: 10px !important;
  font-size: 15px !important;
  font-weight: 700 !important;
  letter-spacing: 0.05em !important;
  background: var(--primary) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(99,91,255,0.35) !important;
  transition: all var(--transition) !important;
}
.login-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 8px 24px rgba(99,91,255,0.45) !important;
  background: var(--primary-light) !important;
}
.login-btn:active { transform: translateY(0) !important; }

.login-hint {
  text-align: center; margin-top: 18px; font-size: 12px;
  color: var(--text-muted);
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 7px; padding: 7px 12px;
}

.switch-mode { text-align: center; margin-top: 18px; font-size: 13px; color: var(--text-muted); }
.switch-mode a {
  color: var(--primary); font-weight: 600; cursor: pointer;
  margin-left: 4px; text-decoration: none;
}
.switch-mode a:hover { text-decoration: underline; }

.login-footer { position: fixed; bottom: 20px; font-size: 12px; color: var(--text-muted); }
</style>
