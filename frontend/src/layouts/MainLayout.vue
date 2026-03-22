<template>
  <div class="layout" :class="{ 'sidebar-collapsed': appStore.sidebarCollapsed }">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="32" height="32" rx="10" fill="#4F6EF7"/>
              <path d="M8 10h16M8 16h10M8 22h13" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <transition name="fade">
            <span v-if="!appStore.sidebarCollapsed" class="logo-text">MeetSpace</span>
          </transition>
        </div>
        <button class="toggle-btn" @click="appStore.toggleSidebar">
          <el-icon><Fold v-if="!appStore.sidebarCollapsed" /><Expand v-else /></el-icon>
        </button>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <span class="nav-icon"><el-icon><component :is="item.icon" /></el-icon></span>
          <transition name="fade">
            <span v-if="!appStore.sidebarCollapsed" class="nav-label">{{ item.title }}</span>
          </transition>
          <transition name="fade">
            <span v-if="!appStore.sidebarCollapsed && item.badge" class="nav-badge">{{ item.badge }}</span>
          </transition>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info" v-if="!appStore.sidebarCollapsed">
          <div class="user-avatar">{{ authStore.userName.charAt(0).toUpperCase() }}</div>
          <div class="user-details">
            <div class="user-name">{{ authStore.userName }}</div>
            <div class="user-role">{{ role === 'SUPER_ADMIN' ? '超级管理员' : role === 'ADMIN' ? '租户管理员' : '普通用户' }}</div>
          </div>
        </div>
        <div class="user-avatar small" v-else>{{ authStore.userName.charAt(0).toUpperCase() }}</div>
        <button class="logout-btn" @click="handleLogout" :title="'退出登录'">
          <el-icon><SwitchButton /></el-icon>
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <h1 class="page-title">{{ currentTitle }}</h1>
          <div class="breadcrumb">
            <span>会议室管理</span>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <div class="date-time">
            <span class="date">{{ currentDate }}</span>
            <span class="time">{{ currentTime }}</span>
          </div>
          <div class="notif-wrap" ref="notifWrapRef">
            <el-badge :value="unreadCount > 0 ? unreadCount : ''" :hidden="unreadCount === 0" class="notif-badge">
              <button class="icon-btn" @click="toggleNotif"><el-icon size="18"><Bell /></el-icon></button>
            </el-badge>
            <!-- 通知下拉面板 -->
            <div v-if="notifVisible" class="notif-panel">
              <div class="notif-header">
                <span class="notif-title">通知</span>
                <button class="notif-read-all" @click="handleReadAll" v-if="unreadCount > 0">全部已读</button>
              </div>
              <div class="notif-list">
                <div v-if="notifications.length === 0" class="notif-empty">暂无通知</div>
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  class="notif-item"
                  :class="{ unread: !n.isRead }"
                  @click="openNotif(n)"
                >
                  <div class="notif-dot" :class="n.type?.toLowerCase()"></div>
                  <div class="notif-body">
                    <div class="notif-item-title">{{ n.title }}</div>
                    <div class="notif-item-content">{{ n.content }}</div>
                    <div class="notif-item-time">{{ n.createTime }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 通知详情弹窗 -->
      <el-dialog v-model="notifDetailVisible" width="420px" :show-close="true" destroy-on-close>
        <template #header>
          <div style="display:flex;align-items:center;gap:8px">
            <span class="notif-dot" :class="selectedNotif?.type?.toLowerCase()" style="display:inline-block;width:8px;height:8px;border-radius:50%;flex-shrink:0"></span>
            <span style="font-size:15px;font-weight:600">{{ selectedNotif?.title }}</span>
          </div>
        </template>
        <div style="font-size:14px;color:var(--text-secondary);line-height:1.8">{{ selectedNotif?.content }}</div>
        <div style="font-size:12px;color:var(--text-muted);margin-top:12px">{{ selectedNotif?.createTime }}</div>
      </el-dialog>

      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useAppStore } from '@/store/app'
import { ElMessageBox } from 'element-plus'
import { notificationApi } from '@/api/notification'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const role = computed(() => authStore.user?.role)

const allNavItems = [
  { path: '/dashboard', title: '工作台', icon: 'Odometer', roles: ['SUPER_ADMIN', 'ADMIN', 'USER'] },
  { path: '/meeting-room', title: '会议室管理', icon: 'OfficeBuilding', roles: ['SUPER_ADMIN', 'ADMIN', 'USER'] },
  { path: '/reservation', title: '预约管理', icon: 'Calendar', roles: ['SUPER_ADMIN', 'ADMIN', 'USER'] },
  { path: '/equipment', title: '配件管理', icon: 'Monitor', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { path: '/approval', title: '审批管理', icon: 'Stamp', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { path: '/statistics', title: '数据统计', icon: 'TrendCharts', roles: ['SUPER_ADMIN', 'ADMIN'] },
  { path: '/tenant', title: '租户管理', icon: 'School', roles: ['SUPER_ADMIN'] },
  { path: '/user', title: '用户管理', icon: 'User', roles: ['SUPER_ADMIN', 'ADMIN'] },
]

const navItems = computed(() => allNavItems.filter(item => item.roles.includes(role.value)))

const currentTitle = computed(() => {
  const found = navItems.value.find(i => i.path === route.path)
  return found?.title || '工作台'
})

const currentDate = ref('')
const currentTime = ref('')

// 通知
const notifVisible = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const notifWrapRef = ref(null)
const notifDetailVisible = ref(false)
const selectedNotif = ref(null)

const openNotif = async (n) => {
  await handleReadOne(n)
  selectedNotif.value = n
  notifDetailVisible.value = true
}

const fetchNotifications = async () => {
  try {
    const res = await notificationApi.list()
    notifications.value = res.data || []
  } catch {}
}

const fetchUnreadCount = async () => {
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.data ?? 0
  } catch {}
}

const toggleNotif = async () => {
  notifVisible.value = !notifVisible.value
  if (notifVisible.value) {
    await fetchNotifications()
    await fetchUnreadCount()
  }
}

const handleReadAll = async () => {
  await notificationApi.readAll()
  notifications.value.forEach(n => n.isRead = true)
  unreadCount.value = 0
}

const handleReadOne = async (n) => {
  if (!n.isRead) {
    await notificationApi.readOne(n.id)
    n.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const handleClickOutside = (e) => {
  if (notifWrapRef.value && !notifWrapRef.value.contains(e.target)) {
    notifVisible.value = false
  }
}

let notifTimer
let timer

const updateTime = () => {
  currentDate.value = dayjs().format('MM月DD日 dddd')
  currentTime.value = dayjs().format('HH:mm:ss')
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  fetchUnreadCount()
  notifTimer = setInterval(fetchUnreadCount, 30000)
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  clearInterval(timer)
  clearInterval(notifTimer)
  document.removeEventListener('click', handleClickOutside)
})

const handleLogout = async () => {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning'
  })
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  background: var(--bg);
  overflow: hidden;
}

/* ── Sidebar ─────────────────────────────────────────────── */
.sidebar {
  width: 240px;
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-slow);
  flex-shrink: 0;
  z-index: 10;
}
.layout.sidebar-collapsed .sidebar { width: 68px; }

.sidebar-header {
  padding: 18px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-light);
  min-height: 64px;
}
.logo { display: flex; align-items: center; gap: 10px; overflow: hidden; }
.logo-icon { width: 30px; height: 30px; flex-shrink: 0; }
.logo-icon svg { width: 100%; height: 100%; }
.logo-text {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 16px;
  color: var(--text-primary);
  white-space: nowrap;
  letter-spacing: -0.02em;
}

.toggle-btn {
  background: none; border: none; cursor: pointer;
  color: var(--text-muted);
  display: flex; align-items: center; justify-content: center;
  width: 28px; height: 28px;
  border-radius: var(--radius-xs);
  transition: all var(--transition);
  flex-shrink: 0;
}
.toggle-btn:hover { background: var(--bg); color: var(--primary); }

/* Nav */
.sidebar-nav {
  flex: 1; padding: 10px 8px;
  display: flex; flex-direction: column; gap: 2px;
  overflow-y: auto;
}
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 10px;
  border-radius: var(--radius-xs);
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px; font-weight: 500;
  transition: all var(--transition);
  white-space: nowrap; overflow: hidden;
  position: relative;
}
.nav-item:hover {
  background: var(--bg);
  color: var(--text-primary);
}
.nav-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 600;
}
.nav-item.active .nav-icon { color: var(--primary); }
.nav-icon {
  display: flex; align-items: center; justify-content: center;
  width: 20px; height: 20px; flex-shrink: 0; font-size: 15px;
}
.nav-label { flex: 1; }
.nav-badge {
  margin-left: auto;
  background: var(--danger);
  color: white; font-size: 10px; font-weight: 700;
  padding: 1px 6px; border-radius: 99px; flex-shrink: 0;
}

/* Footer */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--border-light);
  display: flex; align-items: center; gap: 8px;
}
.user-info { display: flex; align-items: center; gap: 9px; flex: 1; overflow: hidden; }
.user-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), #818CF8);
  color: white; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 13px; flex-shrink: 0;
}
.user-avatar.small { width: 30px; height: 30px; font-size: 12px; }
.user-details { overflow: hidden; }
.user-name {
  font-size: 13px; font-weight: 600; color: var(--text-primary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.user-role { font-size: 11px; color: var(--text-muted); }
.logout-btn {
  background: none; border: none; cursor: pointer;
  color: var(--text-muted);
  display: flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; border-radius: var(--radius-xs);
  transition: all var(--transition); flex-shrink: 0;
}
.logout-btn:hover { background: var(--danger-bg); color: var(--danger); }

/* ── Main content ─────────────────────────────────────────── */
.main-content {
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; min-width: 0;
}

/* Topbar */
.topbar {
  padding: 0 28px;
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  display: flex; align-items: center; justify-content: space-between;
  flex-shrink: 0;
}
.page-title {
  font-family: var(--font-display);
  font-size: 17px; font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}
.breadcrumb {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--text-muted); margin-top: 1px;
}
.breadcrumb .el-icon { font-size: 10px; }
.breadcrumb .current { color: var(--primary); font-weight: 500; }

.topbar-right { display: flex; align-items: center; gap: 14px; }
.date-time { text-align: right; }
.date { display: block; font-size: 12px; color: var(--text-secondary); font-weight: 500; }
.time { display: block; font-size: 11px; color: var(--text-muted); font-variant-numeric: tabular-nums; }

.icon-btn {
  background: #fff;
  border: 1px solid var(--border);
  cursor: pointer; color: var(--text-secondary);
  display: flex; align-items: center; justify-content: center;
  width: 34px; height: 34px; border-radius: var(--radius-xs);
  transition: all var(--transition);
  box-shadow: var(--shadow-xs);
}
.icon-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99,91,255,0.1);
}

/* Notification panel */
.notif-wrap { position: relative; }

.notif-panel {
  position: absolute; right: 0; top: calc(100% + 8px);
  width: 340px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  z-index: 999;
}
.notif-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px 12px;
  border-bottom: 1px solid var(--border-light);
}
.notif-title { font-size: 14px; font-weight: 700; color: var(--text-primary); }
.notif-read-all {
  background: none; border: none; cursor: pointer;
  font-size: 12px; color: var(--primary); font-weight: 600; padding: 0;
}
.notif-read-all:hover { opacity: 0.75; }

.notif-list { max-height: 360px; overflow-y: auto; }
.notif-empty {
  padding: 36px 0; text-align: center;
  font-size: 13px; color: var(--text-muted);
}

.notif-item {
  display: flex; gap: 10px; padding: 12px 16px;
  cursor: pointer; transition: background var(--transition);
  border-bottom: 1px solid var(--border-light);
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: #F8FAFC; }
.notif-item.unread { background: #FAFAFE; }

.notif-dot {
  width: 8px; height: 8px; border-radius: 50%;
  flex-shrink: 0; margin-top: 5px;
}
.notif-dot.approved { background: var(--success); }
.notif-dot.rejected { background: var(--danger); }
.notif-dot.pending  { background: var(--warning); }

.notif-body { flex: 1; overflow: hidden; }
.notif-item-title { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.notif-item-content {
  font-size: 12px; color: var(--text-secondary);
  margin-top: 2px; line-height: 1.5;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.notif-item-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }

/* Page content */
.page-content { flex: 1; overflow-y: auto; padding: 28px 32px; }
</style>
