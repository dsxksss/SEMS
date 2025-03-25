import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/events',
    name: 'Events',
    component: () => import('@/views/Events.vue'),
    meta: { title: '赛事活动' }
  },
  {
    path: '/events/:id',
    name: 'EventDetail',
    component: () => import('@/views/EventDetail.vue'),
    meta: { title: '赛事详情' }
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('@/views/Announcements.vue'),
    meta: { title: '公告信息' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人资料', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: { name: 'AdminDashboard' }
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理控制台' }
      },
      {
        path: 'events',
        name: 'AdminEvents',
        component: () => import('@/views/admin/Events.vue'),
        meta: { title: '赛事管理' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/Announcements.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },
  {
    path: '/test-api',
    name: 'TestApi',
    component: () => import('@/views/TestApi.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 体育赛事管理系统` : '体育赛事管理系统'
  
  const userStore = useUserStore()
  
  // 权限校验
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但未登录，重定向到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    // 需要管理员权限但不是管理员，重定向到首页
    next({ name: 'Home' })
  } else if (to.meta.guest && userStore.isLoggedIn) {
    // 游客专属页面但已登录，重定向到首页
    next({ name: 'Home' })
  } else {
    // 允许访问
    next()
  }
})

export default router 