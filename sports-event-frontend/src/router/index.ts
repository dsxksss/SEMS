import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { authAPI } from '../api';

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'events',
        name: 'Events',
        component: () => import('../views/Events.vue'),
        meta: { title: '赛事列表' }
      },
      {
        path: 'events/:id',
        name: 'EventDetail',
        component: () => import('../views/EventDetail.vue'),
        props: true,
        meta: { title: '赛事详情' }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('../views/Announcements.vue'),
        meta: { title: '公告列表' }
      },
      {
        path: 'announcements/:id',
        name: 'AnnouncementDetail',
        component: () => import('../views/AnnouncementDetail.vue'),
        props: true,
        meta: { title: '公告详情' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('../views/MyRegistrations.vue'),
        meta: { title: '我的报名', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '管理控制台' }
      },
      {
        path: 'events',
        name: 'AdminEvents',
        component: () => import('../views/admin/Events.vue'),
        meta: { title: '赛事管理' }
      },
      {
        path: 'events/create',
        name: 'AdminCreateEvent',
        component: () => import('../views/admin/EventForm.vue'),
        meta: { title: '创建赛事' }
      },
      {
        path: 'events/:id/edit',
        name: 'AdminEditEvent',
        component: () => import('../views/admin/EventForm.vue'),
        props: true,
        meta: { title: '编辑赛事' }
      },
      {
        path: 'events/:id/registrations',
        name: 'AdminEventRegistrations',
        component: () => import('../views/admin/EventRegistrations.vue'),
        props: true,
        meta: { title: '报名管理' }
      },
      {
        path: 'events/:id/results',
        name: 'AdminEventResults',
        component: () => import('../views/admin/EventResults.vue'),
        props: true,
        meta: { title: '成绩管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('../views/admin/Categories.vue'),
        meta: { title: '类别管理' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('../views/admin/Announcements.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'announcements/create',
        name: 'AdminCreateAnnouncement',
        component: () => import('../views/admin/AnnouncementForm.vue'),
        meta: { title: '创建公告' }
      },
      {
        path: 'announcements/:id/edit',
        name: 'AdminEditAnnouncement',
        component: () => import('../views/admin/AnnouncementForm.vue'),
        props: true,
        meta: { title: '编辑公告' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '体育赛事管理系统'} - SEMS`;

  // 检查认证状态
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);
  const isAuthenticated = authAPI.checkAuthStatus();
  
  // 未认证但需要认证的路由，跳转到登录页
  if (requiresAuth && !isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } });
    return;
  }
  
  // 需要管理员权限，但不是管理员的情况下，跳转到首页
  if (requiresAdmin) {
    // 懒加载加载用户状态
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      if (!user.roles.includes('ROLE_ADMIN')) {
        next({ name: 'Home' });
        return;
      }
    } else {
      next({ name: 'Home' });
      return;
    }
  }
  
  next();
});

export default router; 