<template>
  <footer class="bg-gray-900 text-white">
    <!-- 主要内容 -->
    <div class="container mx-auto px-4 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- Logo和简介 -->
        <div class="col-span-1 md:col-span-2">
          <div class="flex items-center space-x-2 mb-4">
            <div class="flex items-center justify-center w-10 h-10 rounded-lg bg-gradient-to-r from-primary-600 to-primary-500 text-white">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <span class="text-xl font-bold">体育赛事管理</span>
          </div>
          <p class="text-gray-400 mb-6 max-w-md">
            提供全面的体育赛事管理解决方案，从赛事策划、报名管理到成绩统计，让体育赛事组织变得简单高效。
          </p>
          <div class="flex space-x-4">
            <a href="#" class="text-gray-400 hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                <path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z" />
              </svg>
            </a>
            <a href="#" class="text-gray-400 hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                <path d="M8.29 20.251c7.547 0 11.675-6.253 11.675-11.675 0-.178 0-.355-.012-.53A8.348 8.348 0 0022 5.92a8.19 8.19 0 01-2.357.646 4.118 4.118 0 001.804-2.27 8.224 8.224 0 01-2.605.996 4.107 4.107 0 00-6.993 3.743 11.65 11.65 0 01-8.457-4.287 4.106 4.106 0 001.27 5.477A4.072 4.072 0 012.8 9.713v.052a4.105 4.105 0 003.292 4.022 4.095 4.095 0 01-1.853.07 4.108 4.108 0 003.834 2.85A8.233 8.233 0 012 18.407a11.616 11.616 0 006.29 1.84" />
              </svg>
            </a>
            <a href="#" class="text-gray-400 hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0C5.373 0 0 5.373 0 12c0 5.302 3.438 9.8 8.205 11.387.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61-.546-1.387-1.333-1.756-1.333-1.756-1.09-.745.083-.73.083-.73 1.205.085 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.468-2.38 1.235-3.22-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.3 1.23A11.51 11.51 0 0112 5.803c1.02.005 2.047.138 3.006.404 2.29-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.768.84 1.235 1.91 1.235 3.22 0 4.61-2.807 5.625-5.48 5.92.43.372.824 1.102.824 2.222 0 1.604-.015 2.897-.015 3.292 0 .32.216.694.825.577C20.565 21.795 24 17.298 24 12c0-6.627-5.373-12-12-12z" />
              </svg>
            </a>
          </div>
        </div>
        
        <!-- 快速链接 -->
        <div>
          <h3 class="text-lg font-bold mb-4">快速链接</h3>
          <ul class="space-y-2">
            <li>
              <router-link to="/" class="text-gray-400 hover:text-white transition-colors">首页</router-link>
            </li>
            <li>
              <router-link to="/events" class="text-gray-400 hover:text-white transition-colors">赛事列表</router-link>
            </li>
            <li>
              <router-link to="/announcements" class="text-gray-400 hover:text-white transition-colors">公告信息</router-link>
            </li>
            <li>
              <router-link to="/about" class="text-gray-400 hover:text-white transition-colors">关于我们</router-link>
            </li>
          </ul>
        </div>
        
        <!-- 联系我们 -->
        <div>
          <h3 class="text-lg font-bold mb-4">联系我们</h3>
          <ul class="space-y-2">
            <li class="flex items-start">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-gray-400 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span class="text-gray-400">北京市海淀区中关村大街1号</span>
            </li>
            <li class="flex items-start">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-gray-400 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              <span class="text-gray-400">contact@sems.com</span>
            </li>
            <li class="flex items-start">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-gray-400 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
              <span class="text-gray-400">010-12345678</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 版权信息 -->
    <div class="border-t border-gray-800 py-6">
      <div class="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center">
        <p class="text-gray-400 text-sm">© {{ new Date().getFullYear() }} 体育赛事管理系统. 保留所有权利.</p>
        <div class="flex space-x-4 mt-4 md:mt-0">
          <a href="#" class="text-gray-400 hover:text-white text-sm transition-colors">隐私政策</a>
          <a href="#" class="text-gray-400 hover:text-white text-sm transition-colors">服务条款</a>
          <a href="#" class="text-gray-400 hover:text-white text-sm transition-colors">帮助中心</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 无需特定逻辑
</script> 