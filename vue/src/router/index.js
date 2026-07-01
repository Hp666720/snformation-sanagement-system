import { createRouter, createWebHistory } from 'vue-router'



const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', redirect: '/login'},   // 默认重定向到登录页
    {path: '/login', component: () => import('../views/Login.vue'),},
    {path: '/about', name: 'about', component: () => import('../views/AboutView.vue'),},
    {path: '/notFount', name: 'notFount', component: () => import('../views/404.vue'),},
    {path: '/manager', component: () => import('../views/Manager.vue'),
      children:[
        {path: 'home', component: () => import('../views/HomeView.vue'),},
        {path: 'admin', component: () => import('../views/AdminView.vue'),},
        {path: 'user', component: () => import('../views/UserView.vue'),},
        {path: 'person', component: () => import('../views/Person.vue'),},
        {path: 'leaveRequest', component: () => import('../views/LeaveRequestView.vue')},
        {path: 'announcement', component: () => import('../views/AnnouncementView.vue'), name: 'announcement'}
      ]
    },
    {path: '/register', component: () => import('../views/Register.vue'),},
    {path: '/:catchAll(.*)', redirect: '/notFount'},  // 404路由放到最后
  ],
})






export default router
