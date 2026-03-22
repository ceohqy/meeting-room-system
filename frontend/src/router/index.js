import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'meeting-room',
        name: 'MeetingRoom',
        component: () => import('@/views/meetingRoom/MeetingRoomView.vue'),
        meta: { title: '会议室管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'reservation',
        name: 'Reservation',
        component: () => import('@/views/reservation/ReservationView.vue'),
        meta: { title: '预约管理', icon: 'Calendar' }
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('@/views/equipment/EquipmentView.vue'),
        meta: { title: '配件管理', icon: 'Monitor' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/views/approval/ApprovalView.vue'),
        meta: { title: '审批管理', icon: 'Stamp' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计', icon: 'TrendCharts' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserManagementView.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'tenant',
        name: 'Tenant',
        component: () => import('@/views/tenant/TenantManagementView.vue'),
        meta: { title: '租户管理', icon: 'OfficeBuilding' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  document.title = `${to.meta.title || '会议室系统'} - 会议室管理`
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
