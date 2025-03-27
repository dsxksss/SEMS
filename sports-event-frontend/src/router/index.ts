import { createRouter, createWebHistory } from 'vue-router';
import { authAPI } from '../api/authAPI';
import { useAuthStore } from '../stores/auth';
import AdminLayout from '../components/AdminLayout.vue';
import UserLayout from '../views/user/UserLayout.vue';

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    redirect: '/user/dashboard',
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { guest: true }
  },
  // 普通用户路由
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/dashboard',
    meta: { 
      requiresAuth: true 
    },
    children: [
      {
        path: 'dashboard',
        name: 'UserDashboard',
        component: () => import('../views/user/Dashboard.vue'),
        meta: { 
          title: '用户首页',
          requiresAuth: true
        }
      },
      {
        path: 'events',
        name: 'UserEventList',
        component: () => import('../views/user/EventList.vue'),
        meta: { 
          title: '赛事列表',
          requiresAuth: true
        }
      },
      {
        path: 'events/:id',
        name: 'UserEventDetail',
        component: () => import('../views/user/EventDetail.vue'),
        meta: { 
          title: '赛事详情',
          requiresAuth: true
        }
      },
      {
        path: 'registrations',
        name: 'UserRegistrations',
        component: () => import('../views/user/RegistrationList.vue'),
        meta: { 
          title: '我的报名',
          requiresAuth: true
        }
      },
      {
        path: 'announcements',
        name: 'UserAnnouncements',
        component: () => import('../views/user/AnnouncementList.vue'),
        meta: { 
          title: '公告列表',
          requiresAuth: true
        }
      }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'Admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { 
      requiresAuth: true, 
      requiresAdmin: true 
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { 
          title: '系统首页',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/Profile.vue'),
        meta: { 
          title: '个人中心',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users/list',
        name: 'UserList',
        component: () => import('../views/admin/users/UserList.vue'),
        meta: { 
          title: '用户列表',
          parent: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users/roles',
        name: 'RoleManagement',
        component: () => import('../views/admin/users/RoleManagement.vue'),
        meta: { 
          title: '角色管理',
          parent: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'events/categories',
        name: 'EventCategories',
        component: () => import('../views/admin/events/CategoryList.vue'),
        meta: { 
          title: '赛事分类',
          parent: '赛事项目管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'events/list',
        name: 'EventList',
        component: () => import('../views/admin/events/EventList.vue'),
        meta: { 
          title: '赛事列表',
          parent: '赛事项目管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'events/edit/:id',
        name: 'EditEvent',
        component: () => import('../views/admin/events/EditEvent.vue'),
        meta: { 
          title: '编辑赛事',
          parent: '赛事项目管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'events/:id/participants',
        name: 'EventParticipants',
        component: () => import('../views/admin/events/EventParticipants.vue'),
        meta: { 
          title: '赛事参与者管理',
          parent: '赛事项目管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'registrations',
        name: 'RegistrationManagement',
        component: () => import('../views/admin/registrations/RegistrationList.vue'),
        meta: { 
          title: '运动员报名管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'results',
        name: 'ResultManagement',
        component: () => import('../views/admin/results/ResultList.vue'),
        meta: { 
          title: '赛事结果管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('../views/admin/reports/ReportCenter.vue'),
        meta: { 
          title: '报表汇总打印',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('../views/admin/announcements/AnnouncementList.vue'),
        meta: { 
          title: '公告管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
];

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局路由守卫
router.beforeEach((to, _from, next) => {
  const isAuthenticated = authAPI.checkAuthStatus();
  const authStore = useAuthStore();
  
  // 需要认证的路由
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
  } 
  // 需要管理员权限的路由
  else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next('/user/dashboard');
  }
  // 仅供游客访问的路由（已登录用户会被重定向到首页）
  else if (to.meta.guest && isAuthenticated) {
    // 根据角色重定向到相应的首页
    if (authStore.isAdmin) {
      next('/admin/dashboard');
    } else {
      next('/user/dashboard');
    }
  } 
  // 其他情况正常导航
  else {
    next();
  }
});

export default router; 