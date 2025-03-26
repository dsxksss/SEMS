import { createRouter, createWebHistory } from 'vue-router';
import { authAPI } from '../api/authAPI';

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
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
  
  // 需要认证的路由
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
  } 
  // 仅供游客访问的路由（已登录用户会被重定向到首页）
  else if (to.meta.guest && isAuthenticated) {
    next('/');
  } 
  // 其他情况正常导航
  else {
    next();
  }
});

export default router; 