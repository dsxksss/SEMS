import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/events',
    name: 'Events',
    component: () => import('@/views/Events.vue')
  },
  {
    path: '/events/:id',
    name: 'EventDetail',
    component: () => import('@/views/EventDetail.vue')
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('@/views/Announcements.vue')
  },
  {
    // 用户中心
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    // 运动员报名
    path: '/athlete-registration',
    name: 'AthleteRegistration',
    component: () => import('@/views/AthleteRegistration.vue'),
    meta: { requiresAuth: true, roles: ['ATHLETE'] }
  },
  {
    // 观众报名
    path: '/audience-registration',
    name: 'AudienceRegistration',
    component: () => import('@/views/AudienceRegistration.vue'),
    meta: { requiresAuth: true, roles: ['USER'] }
  },
  {
    // 管理员路由组
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue')
      },
      {
        path: 'events',
        name: 'EventManagement',
        component: () => import('@/views/admin/EventManagement.vue')
      },
      {
        path: 'registrations',
        name: 'RegistrationManagement',
        component: () => import('@/views/admin/RegistrationManagement.vue')
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/admin/Reports.vue')
      },
      {
        path: 'results',
        name: 'ResultManagement',
        component: () => import('@/views/admin/ResultManagement.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const userRole = userStore.role

  // 需要登录但未登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 角色限制
  if (to.meta.roles && isLoggedIn && !to.meta.roles.includes(userRole)) {
    next({ name: 'Home' })
    return
  }

  // 已登录用户不应访问登录/注册页
  if (to.meta.guest && isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router 